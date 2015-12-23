package io.leao.codecolors.plugin.source;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Modifier;

import io.leao.codecolors.plugin.res.CodeColorsConfiguration;
import io.leao.codecolors.plugin.res.Resource;

public class CodeColorsDependenciesGenerator {
    private static final String SOURCE_CLASS_NAME = "CodeColorsDependencies";

    private static final String RESOURCE_ID_BASE = ".%s.%s";
    private static final String ANDROID_RESOURCE_ID_PUBLIC_BASE = "android.R.%s.%s";
    private static final String ANDROID_RESOURCE_ID_PRIVATE_BASE = "android_R_%s_%s";

    public static void generateDependencies(Set<Resource> resources, String packageName, String applicationId,
                                            File outputDir) {
        /*
         * Setup resources and collect all different configurations.
         */

        Set<CodeColorsConfiguration> configurations = new TreeSet<>();

        List<FieldSpec> privateResourcesFields = new ArrayList<>();

        Map<Resource, Map<CodeColorsConfiguration, Integer>> resourceConfigurationDependenciesIndexes =
                new HashMap<>(resources.size());

        List<FieldSpec> resourceDependenciesFields = new ArrayList<>();

        for (Resource resource : resources) {
            // Initialize private resource ids.
            if (!resource.isPublic() && resource.hasDependents()) {
                privateResourcesFields.add(
                        FieldSpec.builder(
                                String.class,
                                getAndroidResourceId(resource),
                                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                .initializer("\"android:$L/$L\"", resource.getType().getName(), resource.getName())
                                .build());
            }

            // Skip everything else if resource doesn't have dependencies.
            if (!resource.hasDependencies()) {
                continue;
            }

            Map<CodeColorsConfiguration, Set<Resource>> configurationDependencies =
                    resource.getConfigurationDependencies();

            Map<CodeColorsConfiguration, Integer> configurationDependenciesIndexes =
                    new HashMap<>(configurationDependencies.keySet().size());
            resourceConfigurationDependenciesIndexes.put(resource, configurationDependenciesIndexes);

            CodeBlock.Builder resourceDependenciesInitializer = CodeBlock.builder()
                    .add("new $T[]{\n", HashSet.class)
                    .indent();

            int configurationDependenciesIndex = 0;
            Iterator<CodeColorsConfiguration> configurationDependenciesIterator =
                    configurationDependencies.keySet().iterator();
            while (configurationDependenciesIterator.hasNext()) {
                CodeColorsConfiguration configuration = configurationDependenciesIterator.next();

                // Collect configurations.
                configurations.add(configuration);

                // Fill indexes of dependencies for a specific configuration.
                configurationDependenciesIndexes.put(configuration, configurationDependenciesIndex++);

                resourceDependenciesInitializer.add("new $T($T.asList(new $T[]{",
                        ParameterizedTypeName.get(HashSet.class, Object.class),
                        Arrays.class,
                        Object.class);

                Iterator<Resource> dependenciesIterator = configurationDependencies.get(configuration).iterator();
                while (dependenciesIterator.hasNext()) {
                    Resource dependency = dependenciesIterator.next();
                    addResourceId(resourceDependenciesInitializer, dependency, packageName);
                    if (dependenciesIterator.hasNext()) {
                        resourceDependenciesInitializer.add(", ");
                    } else {
                        break;
                    }
                }

                if (configurationDependenciesIterator.hasNext()) {
                    resourceDependenciesInitializer.add("})),\n");
                } else {
                    resourceDependenciesInitializer.add("}))\n");
                    break;
                }
            }

            resourceDependenciesFields.add(
                    FieldSpec.builder(
                            ArrayTypeName.of(ParameterizedTypeName.get(HashSet.class, Object.class)),
                            createResourceVariableName(resource),
                            Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .initializer(resourceDependenciesInitializer.unindent().add("}").build())
                            .build());
        }

        /*
         * Setup configurations.
         */

        CodeBlock.Builder configurationsInitializer = CodeBlock.builder()
                .add("new $T[]{\n", CodeColorsConfiguration.class)
                .indent();

        Iterator<CodeColorsConfiguration> configurationsIterator = configurations.iterator();
        while (configurationsIterator.hasNext()) {
            CodeColorsConfiguration configuration = configurationsIterator.next();

            // Put configuration resource dependencies code block.
            CodeBlock.Builder putConfigurationResourceDependenciesBuilder = CodeBlock.builder();
            putConfigurationResourceDependenciesBuilder.indent();

            // Configuration initialization code block.
            String localeLanguage, localeCountry, localeVariant;
            if (configuration.locale != null) {
                localeLanguage = configuration.locale.getLanguage();
                localeCountry = configuration.locale.getCountry();
                localeVariant = configuration.locale.getVariant();
            } else {
                localeLanguage = localeCountry = localeVariant = null;
            }

            CodeBlock configurationBlock = CodeBlock.builder()
                    .add("new $T($L, $Lf, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L)",
                            CodeColorsConfiguration.class, configuration.sdkVersion, configuration.fontScale,
                            configuration.mcc, configuration.mnc, localeLanguage, localeCountry, localeVariant,
                            configuration.userSetLocale, configuration.touchscreen, configuration.keyboard,
                            configuration.keyboardHidden, configuration.hardKeyboardHidden, configuration.navigation,
                            configuration.navigationHidden, configuration.orientation, configuration.screenLayout,
                            configuration.uiMode, configuration.screenWidthDp, configuration.screenHeightDp,
                            configuration.smallestScreenWidthDp, configuration.densityDpi)
                    .build();
            configurationsInitializer.add(configurationBlock);

            if (configurationsIterator.hasNext()) {
                configurationsInitializer.add(",\n");
            } else {
                configurationsInitializer.add("\n");
                break;
            }
        }

        FieldSpec configurationsField = FieldSpec.builder(CodeColorsConfiguration[].class, "sConfigurations")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(configurationsInitializer.unindent().add("}").build())
                .build();

        /*
         * Configurations with resource and dependencies.
         */

        // HashMap<Integer, Set<Integer>>.
        ParameterizedTypeName resourceDependenciesType = ParameterizedTypeName.get(
                ClassName.get(HashMap.class),
                ClassName.get(Object.class),
                ParameterizedTypeName.get(Set.class, Object.class));

        // HashMap<CodeColorsConfiguration, HashMap<Integer, Set<Integer>>>.
        ParameterizedTypeName configurationResourceDependenciesType = ParameterizedTypeName.get(
                ClassName.get(HashMap.class),
                ClassName.get(CodeColorsConfiguration.class),
                resourceDependenciesType);

        // Put configuration resource dependencies code block.
        CodeBlock.Builder putConfigurationResourceDependenciesBuilder = CodeBlock.builder();
        putConfigurationResourceDependenciesBuilder.indent();

        int configurationIndex = 0;
        for (CodeColorsConfiguration configuration : configurations) {

            CodeBlock.Builder putResourceDependenciesBuilder = CodeBlock.builder().indent();

            for (Resource resource : resources) {
                if (!resource.hasDependencies()) {
                    continue;
                }

                Map<CodeColorsConfiguration, Set<Resource>> configurationDependencies = resource.getConfigurationDependencies();

                int configurationDependenciesIndex;
                if (configurationDependencies.containsKey(configuration)) {
                    configurationDependenciesIndex =
                            resourceConfigurationDependenciesIndexes.get(resource).get(configuration);
                } else {
                    configurationDependenciesIndex = 0;
                }

                putResourceDependenciesBuilder
                        .add("put(\n")
                        .indent();
                addResourceId(putResourceDependenciesBuilder, resource, packageName);
                putResourceDependenciesBuilder
                        .add(",\n")
                        .add("$L[$L]", createResourceVariableName(resource), configurationDependenciesIndex)
                        .unindent()
                        .add(");\n");
            }
            putResourceDependenciesBuilder.unindent();

            putConfigurationResourceDependenciesBuilder
                    .add("put(\n")
                    .indent()
                    .add("sConfigurations[$L],\n", configurationIndex++)
                    .add("new $T() {{\n$L}}\n", resourceDependenciesType, putResourceDependenciesBuilder.build())
                    .unindent()
                    .add(");\n");
        }
        putConfigurationResourceDependenciesBuilder.unindent();

        FieldSpec dependenciesField = FieldSpec.builder(configurationResourceDependenciesType, "sDependencies")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T() {{\n$L}}", configurationResourceDependenciesType,
                        putConfigurationResourceDependenciesBuilder.build())
                .build();


        TypeSpec.Builder codeColorResourcesClass = TypeSpec.classBuilder(SOURCE_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(privateResourcesFields)
                .addField(configurationsField)
                .addFields(resourceDependenciesFields)
                .addField(dependenciesField);
        addGeneratedTimeJavaDoc(codeColorResourcesClass);

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
        // Just cause.
        throw new IllegalStateException("Resource type not supported: " + resource.getType());
    }

    private static String getAndroidResourceId(Resource resource) {
        String nameBase = resource.isPublic() ? ANDROID_RESOURCE_ID_PUBLIC_BASE : ANDROID_RESOURCE_ID_PRIVATE_BASE;
        return String.format(nameBase, resource.getType().getName(), resource.getName());
    }

    private static String createResourceVariableName(Resource resource) {
        switch (resource.getType()) {
            case DRAWABLE:
                return "Drawable_" + resource.getName();
            case ANDROID_DRAWABLE:
                return "AndroidDrawable_" + resource.getName();
            case COLOR:
                return "Color_" + resource.getName();
            case ANDROID_COLOR:
                return "AndroidColor_" + resource.getName();
            case ATTR:
                return "Attr_" + resource.getName();
            case ANDROID_ATTR:
                return "AndroidAttr_" + resource.getName();
        }
        // Just cause.
        throw new IllegalStateException("Resource type not supported: " + resource.getType());
    }

    private static void addGeneratedTimeJavaDoc(TypeSpec.Builder classBuilder) {
        long time = System.currentTimeMillis();
        int seconds = (int) (time / 1000) % 60;
        int minutes = (int) ((time / (1000 * 60)) % 60);
        int hours = (int) ((time / (1000 * 60 * 60)) % 24);

        classBuilder.addJavadoc("Generated at $L:$L:$L.\n", String.format("%02d", hours),
                String.format("%02d", minutes), String.format("%02d", seconds));

    }
}