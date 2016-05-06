package io.leao.codecolors.plugin.source;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Modifier;

import io.leao.codecolors.plugin.CcConst;
import io.leao.codecolors.plugin.res.CcConfiguration;
import io.leao.codecolors.plugin.res.Resource;

public class DependenciesGenerator {
    private static final String RESOURCE_ID_BASE = ".%s.%s";
    private static final String ANDROID_RESOURCE_ID_PUBLIC_BASE = "android.R.%s.%s";
    private static final String ANDROID_RESOURCE_ID_PRIVATE_BASE = "android_R_%s_%s";

    public static void generateDependencies(Set<Resource> resources, String packageName, String applicationId,
                                            File outputDir) {
        /*
         * Setup resources and collect all different configurations.
         */

        Set<CcConfiguration> configurations = new TreeSet<>();

        List<FieldSpec> privateResourcesFields = new ArrayList<>();

        Iterator<Resource> resourcesIterator = resources.iterator();
        while (resourcesIterator.hasNext()) {
            Resource resource = resourcesIterator.next();

            // If the resource doesn't have code colors, remove it and continue.
            if (!resource.hasCodeColors()) {
                resourcesIterator.remove();
                continue;
            }

            // Prune dependency resources that don't have code colors.
            resource.pruneDependencies();

            // Initialize private resource ids.
            if (!resource.isPublic()) {
                privateResourcesFields.add(
                        FieldSpec.builder(
                                String.class,
                                getAndroidResourceId(resource),
                                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                .initializer("\"android:$L/$L\"", resource.getType().getName(), resource.getName())
                                .build());
            }

            // If the resource doesn't have dependencies, continue.
            if (!resource.hasDependencies()) {
                continue;
            }

            Map<CcConfiguration, Set<Resource>> configurationDependencies =
                    resource.getConfigurationDependencies();

            for (CcConfiguration configuration : configurationDependencies.keySet()) {
                // Collect configurations.
                configurations.add(configuration);
            }
        }

        /*
         * Setup configurations.
         */

        Map<CcConfiguration, Integer> configurationIndexes = new HashMap<>();
        FieldSpec configurationsField =
                GeneratorUtils.generateConfigurationsField(
                        configurations, new GeneratorUtils.Callback<CcConfiguration>() {
                            @Override
                            public void onNext(CcConfiguration object, int index) {
                                configurationIndexes.put(object, index);
                            }
                        });

        /*
         * Resources with configurations and dependencies.
         */


        // Object[][]
        TypeName configurationDependenciesType = TypeName.get(Object[][].class);

        // HashMap<Object, Object[][]>
        ParameterizedTypeName configurationResourceDependenciesType = ParameterizedTypeName.get(
                ClassName.get(HashMap.class), ClassName.get(Object.class), configurationDependenciesType);

        // Put resource dependencies code block.
        CodeBlock.Builder putResourceConfigurationDependenciesBuilder = CodeBlock.builder().indent();

        for (Resource resource : resources) {
            // If the resource doesn't have dependencies, continue.
            if (!resource.hasDependencies()) {
                continue;
            }

            Map<CcConfiguration, Set<Resource>> configurationDependencies =
                    resource.getConfigurationDependencies();

            CodeBlock.Builder configurationsBuilder = CodeBlock.builder().indent().add("{");
            CodeBlock.Builder dependenciesBuilder = CodeBlock.builder().indent();

            Iterator<CcConfiguration> configurationsIterator = configurationDependencies.keySet().iterator();
            while (configurationsIterator.hasNext()) {
                CcConfiguration configuration = configurationsIterator.next();
                int configurationIndex = configurationIndexes.get(configuration);
                configurationsBuilder.add("$L", configurationIndex);


                dependenciesBuilder.add("{");
                Set<Resource> dependencies = configurationDependencies.get(configuration);
                Iterator<Resource> dependenciesIterator = dependencies.iterator();
                while (dependenciesIterator.hasNext()) {
                    Resource dependency = dependenciesIterator.next();
                    addResourceId(dependenciesBuilder, dependency, packageName);
                    if (dependenciesIterator.hasNext()) {
                        dependenciesBuilder.add(", ");
                    } else {
                        dependenciesBuilder.add("}");
                    }
                }

                if (configurationsIterator.hasNext()) {
                    configurationsBuilder.add(", ");
                    dependenciesBuilder.add(",\n");
                } else {
                    configurationsBuilder.add("},").unindent();
                }
            }

            putResourceConfigurationDependenciesBuilder
                    .add("put(\n")
                    .indent();
            addResourceId(putResourceConfigurationDependenciesBuilder, resource, packageName);
            putResourceConfigurationDependenciesBuilder
                    .add(",\n")
                    .add("new $T{\n$L\n$L\n}\n", configurationDependenciesType, configurationsBuilder.build(),
                            dependenciesBuilder.unindent().build())
                    .unindent()
                    .add(");\n");
        }

        FieldSpec dependenciesField =
                FieldSpec.builder(configurationResourceDependenciesType, CcConst.DEPENDENCIES_FIELD_NAME)
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T() {{\n$L}}", configurationResourceDependenciesType,
                                putResourceConfigurationDependenciesBuilder.unindent().build())
                        .build();

        TypeSpec.Builder codeColorResourcesClass = TypeSpec.classBuilder(CcConst.DEPENDENCIES_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(privateResourcesFields)
                .addField(configurationsField)
                .addField(dependenciesField);

        GeneratorUtils.addSuppressWarningsAnnotations(codeColorResourcesClass);

        GeneratorUtils.addGeneratedTimeJavaDoc(codeColorResourcesClass);

        JavaFile javaFile = JavaFile.builder(applicationId, codeColorResourcesClass.build())
                .build();

        try {
            javaFile.writeTo(outputDir);
        } catch (IOException e) {
            System.out.println("Error generating sources: " + e.toString());
        }
    }

    private static void addResourceId(CodeBlock.Builder codeBlockBuilder, Resource resource, String packageName) {
        switch (resource.getType()) {
            case DRAWABLE:
            case COLOR:
            case ATTR:
                codeBlockBuilder.add(
                        "$T$L",
                        ClassName.get(packageName, "R"),
                        String.format(RESOURCE_ID_BASE, resource.getType().getName(), resource.getName()));
                return;
            case ANDROID_DRAWABLE:
            case ANDROID_COLOR:
            case ANDROID_ATTR:
                codeBlockBuilder.add(getAndroidResourceId(resource));
                return;
        }
        throw new IllegalStateException("Resource type not supported: " + resource.getType());
    }

    private static String getAndroidResourceId(Resource resource) {
        String nameBase = resource.isPublic() ? ANDROID_RESOURCE_ID_PUBLIC_BASE : ANDROID_RESOURCE_ID_PRIVATE_BASE;
        return String.format(nameBase, resource.getType().getName(), resource.getName());
    }
}
