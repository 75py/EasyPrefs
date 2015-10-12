# EasyPrefs

EasyPrefs is a wrapper of SharedPreferences.

## Sample

```java
// Your code
@EasyPrefBoolean(titleStr = "Update automatically", defValueBool = true)
public interface PrefAutomaticallyUpdate extends BoolPref {
}

// Usage
PrefAutomaticallyUpdate provider = new GenPrefAutomaticallyUpdate(application);
boolean currentValue = provider.getValue(); // GETTER
provider.updateValue(true); // SETTER
```

## Installation

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
    }
}

ext {
    SDK_DIR = getSdkDir()
    TARGET_SDK_VERSION = 22
}

def getSdkDir() {
    Properties properties = new Properties()
    properties.load(project.rootProject.file('local.properties').newDataInputStream())
    def sdkDir = properties.getProperty('sdk.dir', null)

    return sdkDir
}


apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'com.nagopy.android:easyprefs:0.2.0'
    apt 'com.nagopy.android:easyprefs-processor:0.2.0'
    apt files("${SDK_DIR}/platforms/android-${TARGET_SDK_VERSION}/android.jar")
}
```