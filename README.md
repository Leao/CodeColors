CodeColors
==========

<img src="./sample_in_action.gif" width="292px">

Gradle plugin and Android library working together to create color resources that can be changed dynamically in Java code.

It allows to programmatically create color themes, without the need to previously define them in xml.

DISCLAIMER: **CodeColors is not 100% safe**. It makes use of reflection to override colors and drawables, and to intercept views inflated from xml. Use it with discretion, as it can have issues with other libraries, and is not guaranteed to work with every device.

Workflow
--------

The library works by injecting mutable colors, commonly referred as “code-colors”, to `Resources`. The code-colors are subclasses of `ColorStateLists` that implement the `CodeColor` interface. There are two `CodeColor` classes — `CcColorStateList` and `CcColorStateListList`:

1. `CcColorStateList` are the “true” code-colors. Their states and values can be changed programmatically, and they are the base of the code-color workflow. 
2. `CcColorStateListLists` are `ColorStateLists` that support `CcColorStateLists` as values for their states, instead of a simple `int` value. They can't be directly updated.

Components that use code-colors must be invalidated when the colors are updated. For that purpose, `CodeColor` classes support a flexible callback system.

Callback adapters can be created to manage view attributes (like `android:textColor`) or other use cases.

The plugin generates dependencies between drawables and code-colors. Every drawable that contains attributes or code-colors references is marked as dependent.

Drawable creation is intercepted by the library, and dependent drawables are wrapped in `CcDrawableWrappers`, which automatically manage all the needed callbacks to invalidate the drawable.

Usage
-----

Steps to use CodeColors:

1. On the application module:
  1. apply the plugin;
  2. declare the library as a dependency.
2. Start CodeColors in the module's `Application` class.
3. Create code-color resources and use them as any other Android color.
4. Make sure to use CodeColor activities and look into `CodeColors` class for general use.

### 1. Plugin and library

Include the Gradle plugin on the **project's** `build.gradle` file.

```groovy
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath 'io.leao:codecolors-plugin:0.3.0'
    }
}
```

Apply the plugin on the **module's** `build.gradle` file.

```groovy
apply plugin: 'com.android.application'
apply plugin: 'io.leao.codecolors.plugin'

//[...]
```

Declare the library as dependency on the **module's** `build.gradle` file.

```groovy
dependencies {
    compile 'io.leao:codecolors:0.3.0'
}
```

If you need AppCompat support in your application, use codecolors-appcompat (instead of the above).

```groovy
dependencies {
    compile 'io.leao:codecolors-appcompat:0.3.0'
}
```

### 2. Application

In your application class make sure to start CodeColors.
```java
import android.app.Application;

import io.leao.codecolors.CodeColors;

public class CodeColorsSample extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CodeColors.start(this, new CodeColors.Callback() {
            @Override
            public void onCodeColorsStarted() {
                // Add custom callback adapters (optional).
                CodeColors.addAttrCallbackAdapter(new CcStatusBarColorAnchorCallbackAdapter());
                CodeColors.addViewDefStyleAdapter(new CcCoordinatorLayoutDefStyleAdapter());
            }

            @Override
            public void onCodeColorsFailed(Exception e) {
                // Log exceptions in your tracker.
            }
        });
    }
}
```

### 3. Code-colors resources

To create code-colors resources, create a `codecolors.xml` file inside your application's `res` directory. Add your code-colors and their default values to that file. 

You can create multiple `codecolors.xml` files inside different configuration directories, to provide different default values depending on the configuration.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="cc_color_primary">#3F51B5</color>
    <color name="cc_color_accent">@color/cc_color_accent_default</color>
</resources>
```

If you need a different name for your code-colors resource file (like `codecolors_res.xml`), you can configure it in your **module's** `build.gradle` file:

```groovy
codecolors {
    resFileName "codecolors_res"
}
```

Attributes are **not** valid default values for code-colors.

```xml
<resources>
    <!-- Invalid default values. -->
    <color name="cc_color_primary">?attr/foo</color>
    <color name="cc_color_accent">?attr/bar</color>
</resources>
```

### 4. General use

Make sure your activities extend `CcActivity`, and make use of the `CodeColors` class.

```java
public class MainActivity extends CcActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set primary code-color to red.
        CodeColors.set(R.color.cc__color_primary).setColor(Color.RED).submit();
        
        // Restore primary code-color to xml default value.
        CodeColors.set(R.color.cc__color_primary).setColor(null).submit();
        
        // Animate accent code-color to blue.
        CodeColors.animate(R.color.cc__color_accent).setColor(Color.BLUE).start();
    }
}
```

Don't forget to extend `CcAppCompatActivity` instead, if you need support for AppCompat components.

```java
public class MainActivity extends CcAppCompatActivity {
    //[...]
}
```

#### 4.1. Editors

Editors allow to update the code-colors values. There are 4 types of editors:

1. `CcEditorSet`, to update a single code-color.
2. `CcEditorAnimate`, to update a single code-color with animation.
3. `CcMultiEditorSet`, to update multiple code-colors at once.
4. `CcMultiEditorAnimate`, to update multiple code-colors at once with animation.

Note: using `CcMultiEditorSet` is not necessarily faster than using multiple `CcEditorSets`. It depends on the distribution of the code-colors among the UI components. However, when slower, the difference should be negligible. 

`CcEditorSet` and `CcEditorAnimate` can be accessed directly on the `CcColorStateLists` by calling:

1. `CcColorStateList.set()`, for the `CcEditorSet`.
2. `CcColorStateList.animate()`, for the `CcEditorAnimate`.

`CcMultiEditorSet` and `CcMultiEditorAnimate` can be accessed by calling:

1. `CodeColors.setMultiple()`, for the `CcMultiEditorSet`.
2. `CodeColors.animateMultiple()`, for the `CcEditorAnimate`.

##### 4.1.1 Examples

Three ways of updating the value of a single code-color, without animation. The result is the same on all cases.

```java
public static void setPrimaryColor(int color) {
    CodeColors.set(R.color.cc__color_primary).setColor(color).submit();
}

public static void setPrimaryColor2(int color) {
    CodeColors.getColor(R.color.cc__color_primary).set().setColor(color).submit();
}

public static void setPrimaryColor3(int color) {
    CcColorStateList accentColor = CodeColors.getColor(R.color.cc__color_primary);
    CcEditorSet editor = accentColor.set();
    editor.setColor(color);
    editor.submit();
}
```

Two ways of updating the value of multiple code-colors and animating the result. The result is the same on both cases.

```java
public static void animateColors(int primary, int accent) {
    CodeColors.animateMultiple()
              .setColor(R.color.cc__color_primary, primary)
              .setColor(R.color.cc__color_accent, accent)
              .start();
}

public static void animateColors2(int primary, int accent) {
    CcMultiEditorAnimate editor = CodeColors.animateMultiple();
    editor.setColor(R.color.cc__color_primary, primary);
    editor.setColor(R.color.cc__color_accent, accent);
    editor.start();
}
```

#### 4.2. Adapters

Adapters allow to automatically add callbacks to views when they are inflated. There are 2 types of callback adapters:

1. `CcAttrCallbackAdapters`, to create callbacks for a given xml attribute.
2. `CcColorCallbackAdapter`, to create callbacks for a given `CodeColor` in a view.

There is also a third adapter type that is used to support the two previous adapters:

1. `CcDefStyleAdapter`, to define the `defStyleAttr` and `defStyleRes` for a given view class.

Be sure to check the adapters in the sample app, as well as the built-in adapters in the libraries, to further explore its use. 

Some examples are: `CcStatusBarBackgroundAttrCallbackAdapter` and `CcCoordinatorLayoutDefStyleAdapter` (in the sample app), and `CcTextColorsColorCallbackAdapter` (one of built-in adapters). 

Other details
-------------

1. AppCompat support is for version `23.4.0`.
2. There is some overhead when loading drawables and colors that are dependent on code-colors. Some preloaded drawables cannot be reused and some attributes have to be read twice.
3. `Source folders generated at incorrect location` warning is expected and unharmful.
4. Code-colors' default values are created as Android color resources, and named as: `<code-color name>__default` (note the double '\_'). Take that into account when naming your resources, to avoid conflicts.
5. Android colors are not mutable. In that sense, some Android components make use of integer colors (which use the default color of `ColorStateLists`), instead of using the `ColorStateLists` directly. That makes it impossible to update some components' color without custom code, which varies from case to case. AppCompat library tinting mechanism is one example of that. However, `codecolors-appcompat` library should be able handle the majority of those cases (work in progress).
6. When testing, make sure to actually update the colors, because some components will probably require custom callbacks to invalidate their appearance.
7. Don't forget to take a look at the sample app for further implementation details.
 
Development
-----------

To publish a new release increment the `VERSION` and update the `VERSION_NAME` in `gradle.properties` file.

Then, run the following Gradle commands to publish and upload the plugin and both libraries:

```bash
gradle clean
#optional: gradle :codecolors-plugin:generateAndroidSdkDependencies
gradle :codecolors-plugin:publish
gradle assembleRelease
gradle bintrayUpload
```

License
-------

    Copyright 2015 João Martins Costa

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
