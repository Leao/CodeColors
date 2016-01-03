package io.leao.codecolors.plugin.source;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Iterator;

import javax.lang.model.element.Modifier;

import io.leao.codecolors.plugin.res.CcConfiguration;

public class GeneratorUtils {
    public static final String CONFIGURATIONS_FIELD_NAME = "sConfigurations";

    public static FieldSpec getConfigurationsField(Iterable<CcConfiguration> configurations) {
        return getConfigurationsField(configurations, null);
    }

    public static FieldSpec getConfigurationsField(Iterable<CcConfiguration> configurations,
                                                   Callback<CcConfiguration> callback) {
        return getConfigurationsField(configurations, callback, CONFIGURATIONS_FIELD_NAME);
    }

    public static FieldSpec getConfigurationsField(Iterable<CcConfiguration> configurations,
                                                   Callback<CcConfiguration> callback, String fieldName) {
        CodeBlock.Builder configurationsInitializer = CodeBlock.builder()
                .add("new $T[]{\n", CcConfiguration.class)
                .indent();

        int index = 0;
        Iterator<CcConfiguration> configurationsIterator = configurations.iterator();
        while (configurationsIterator.hasNext()) {
            CcConfiguration configuration = configurationsIterator.next();

            if (callback != null) {
                callback.onNext(configuration, index++);
            }

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
                            CcConfiguration.class, configuration.sdkVersion, configuration.fontScale,
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

        return FieldSpec.builder(CcConfiguration[].class, fieldName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(configurationsInitializer.unindent().add("}").build())
                .build();
    }

    public static void addGeneratedTimeJavaDoc(TypeSpec.Builder classBuilder) {
        long time = System.currentTimeMillis();
        int seconds = (int) (time / 1000) % 60;
        int minutes = (int) ((time / (1000 * 60)) % 60);
        int hours = (int) ((time / (1000 * 60 * 60)) % 24);

        classBuilder.addJavadoc("Generated at $L:$L:$L.\n", String.format("%02d", hours),
                String.format("%02d", minutes), String.format("%02d", seconds));
    }

    public interface Callback<T> {
        void onNext(T object, int index);
    }
}
