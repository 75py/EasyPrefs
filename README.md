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

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'com.nagopy.android:easyprefs-api:0.0.1'
    apt 'com.nagopy.android:easyprefs-compiler:0.0.1'
}
```