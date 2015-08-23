package com.nagopy.android.easyprefs;

// TODO Javadoc

public interface EasyPref<T> {

    T getDefaultValue();

    T getValue();

    boolean updateValue(T newValue);

    boolean clearValue();
}
