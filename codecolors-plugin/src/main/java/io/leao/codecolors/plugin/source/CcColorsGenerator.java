package io.leao.codecolors.plugin.source;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Modifier;

import io.leao.codecolors.plugin.CcConst;
import io.leao.codecolors.plugin.res.CcConfiguration;
import io.leao.codecolors.plugin.res.ColorUtils;

public class CcColorsGenerator {
    private static final String COLOR_ID_BASE = ".color.%s";

    public static void generateColors(Map<String, Set<CcConfiguration>> colorConfigurations, String packageName,
                                      String applicationId, File outputDir) {

        /*
         * Collect all configurations and their indexes.
         */

        TreeSet<CcConfiguration> allConfigurations = new TreeSet<>();

        for (Set<CcConfiguration> configurations : colorConfigurations.values()) {
            allConfigurations.addAll(configurations);
        }

        Map<CcConfiguration, Integer> allConfigurationsIndexes = new HashMap<>();
        FieldSpec allConfigurationsField = GeneratorUtils.generateConfigurationsField(
                allConfigurations,
                new GeneratorUtils.Callback<CcConfiguration>() {
                    @Override
                    public void onNext(CcConfiguration object, int index) {
                        allConfigurationsIndexes.put(object, index);
                    }
                }
        );

        /*
         * Setup colors and their configurations and values.
         */

        ClassName rClassName = ClassName.get(packageName, "R");

        CodeBlock.Builder putColorConfigurationsBuilder = CodeBlock.builder();

        CodeBlock.Builder putColorValueBuilder = CodeBlock.builder().indent();

        for (String color : colorConfigurations.keySet()) {
            // Add configurations to TreeSet.
            putColorConfigurationsBuilder
                    .add("put(\n")
                    .indent()
                    .add("$T$L,\n", rClassName, String.format(COLOR_ID_BASE, color))
                    .add("new $T() {{\n", ParameterizedTypeName.get(TreeSet.class, CcConfiguration.class))
                    .indent();
            Set<CcConfiguration> configurations = colorConfigurations.get(color);
            for (CcConfiguration configuration : configurations) {
                putColorConfigurationsBuilder.add(
                        "add($L[$L]);\n",
                        GeneratorUtils.CONFIGURATIONS_FIELD_NAME,
                        allConfigurationsIndexes.get(configuration));
            }
            putColorConfigurationsBuilder
                    .unindent()
                    .add("}});\n")
                    .unindent();

            putColorValueBuilder
                    .add("put($T$L, $T$L);\n",
                            rClassName,
                            String.format(COLOR_ID_BASE, color),
                            rClassName,
                            String.format(COLOR_ID_BASE, ColorUtils.getDefaultValue(color)));
        }

        ParameterizedTypeName colorConfigurationsType = ParameterizedTypeName.get(
                ClassName.get(HashMap.class),
                ClassName.get(Integer.class),
                ParameterizedTypeName.get(Set.class, CcConfiguration.class));
        FieldSpec colorConfigurationsField =
                FieldSpec.builder(colorConfigurationsType, CcConst.COLOR_CONFIGURATIONS_FIELD_NAME)
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(
                                "new $T() {{\n$L}}",
                                colorConfigurationsType,
                                putColorConfigurationsBuilder.build())
                        .build();

        ParameterizedTypeName colorValueType = ParameterizedTypeName.get(HashMap.class, Integer.class, Integer.class);
        FieldSpec colorValueField =
                FieldSpec.builder(colorValueType, CcConst.COLOR_VALUE_FIELD_NAME)
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(
                                "new $T() {{\n$L}}",
                                colorValueType,
                                putColorValueBuilder.unindent().build())
                        .build();

        TypeSpec.Builder codeColorResourcesClass = TypeSpec.classBuilder(CcConst.COLORS_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(allConfigurationsField)
                .addField(colorConfigurationsField)
                .addField(colorValueField);

        GeneratorUtils.addGeneratedTimeJavaDoc(codeColorResourcesClass);

        JavaFile javaFile = JavaFile.builder(applicationId, codeColorResourcesClass.build())
                .build();

        try {
            javaFile.writeTo(outputDir);
        } catch (IOException e) {
            System.out.println("Error generating sources: " + e.toString());
        }
    }
}
