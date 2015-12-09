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

import javax.lang.model.element.Modifier;

import io.leao.codecolors.plugin.res.CodeColorsConfiguration;
import io.leao.codecolors.plugin.res.Resource;

public class SourceGeneratorHandler {
    public static void generateSource(Set<CodeColorsConfiguration> configurations, Set<Resource> resources,
                                      String packageName, String applicationId, File outputDir) {
        /*
         * Setup configurations.
         */

        CodeBlock.Builder configurationArrayInitializer = CodeBlock.builder()
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
                    .add("new $T($Lf, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, $L, " +
                                    "$L, $L)", CodeColorsConfiguration.class, configuration.fontScale, configuration.mcc,
                            configuration.mnc, localeLanguage, localeCountry, localeVariant,
                            configuration.userSetLocale, configuration.touchscreen, configuration.keyboard,
                            configuration.keyboardHidden, configuration.hardKeyboardHidden, configuration.navigation,
                            configuration.navigationHidden, configuration.orientation, configuration.screenLayout,
                            configuration.uiMode, configuration.screenWidthDp, configuration.screenHeightDp,
                            configuration.smallestScreenWidthDp, configuration.densityDpi, configuration.sdkVersion,
                            configuration.minorVersion)
                    .build();
            configurationArrayInitializer.add(configurationBlock);

            if (configurationsIterator.hasNext()) {
                configurationArrayInitializer.add(",\n");
            } else {
                configurationArrayInitializer.add("\n");
                break; // THAT optimization.
            }
        }

        FieldSpec configurationArrayField = FieldSpec.builder(CodeColorsConfiguration[].class, "sConfigurations")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(configurationArrayInitializer.unindent().add("}").build())
                .build();

        /*
         * Setup resources and their dependencies.
         */

        Map<Resource, Map<CodeColorsConfiguration, Integer>> resourceConfigurationDependenciesIndexes =
                new HashMap<>(resources.size());

        List<FieldSpec> resourceDependenciesFields = new ArrayList<>();

        for (Resource resource : resources) {
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

                // Fill indexes of dependencies for a specific configuration.
                configurationDependenciesIndexes.put(configuration, configurationDependenciesIndex++);

                resourceDependenciesInitializer.add("new $T($T.asList(new $T[]{",
                        ParameterizedTypeName.get(HashSet.class, Integer.class),
                        Arrays.class,
                        Integer.class);

                Iterator<Resource> dependenciesIterator = configurationDependencies.get(configuration).iterator();
                while (dependenciesIterator.hasNext()) {
                    Resource dependency = dependenciesIterator.next();
                    resourceDependenciesInitializer.add(createResourceCodeBlock(packageName, dependency));
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
                    break; // THAT optimization.
                }
            }

            resourceDependenciesFields.add(FieldSpec.builder(
                    ArrayTypeName.of(ParameterizedTypeName.get(HashSet.class, Integer.class)),
                    createResourceVariableName(resource))
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer(resourceDependenciesInitializer.unindent().add("}").build())
                    .build());
        }

        /*
         * Configurations with resource and dependencies.
         */

        // HashMap<Integer, Set<Integer>>.
        ParameterizedTypeName resourceDependenciesType = ParameterizedTypeName.get(
                ClassName.get(HashMap.class),
                ClassName.get(Integer.class),
                ParameterizedTypeName.get(Set.class, Integer.class));

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
                        .indent()
                        .add(createResourceCodeBlock(packageName, resource))
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

        TypeSpec.Builder codeColorResourcesClass = TypeSpec.classBuilder("CodeColorResources")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(configurationArrayField)
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

    private static CodeBlock createResourceCodeBlock(String packageName, Resource resource) {
        switch (resource.getType()) {
            case DRAWABLE:
                return CodeBlock.builder()
                        .add("$T$L", ClassName.get(packageName, "R"), ".drawable." + resource.getName())
                        .build();
            case COLOR:
                return CodeBlock.builder()
                        .add("$T$L", ClassName.get(packageName, "R"), ".color." + resource.getName())
                        .build();
            case ATTR:
                return CodeBlock.builder()
                        .add("$T$L", ClassName.get(packageName, "R"), ".attr." + resource.getName())
                        .build();
            case ANDROID_ATTR:
                return CodeBlock.builder()
                        .add("$L", "android.R.attr." + resource.getName())
                        .build();
        }
        // Just cause.
        throw new IllegalStateException("Resource type not supported: " + resource.getType());
    }

    private static String createResourceVariableName(Resource resource) {
        switch (resource.getType()) {
            case DRAWABLE:
                return "Drawable_" + resource.getName();
            case COLOR:
                return "Color_" + resource.getName();
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
