package com.nagopy.android.easyprefs.sample;

import com.nagopy.android.easyprefs.EasyPrefModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class
        , EasyPrefModule.class})
public interface ApplicationComponent {
    void inject(SamplePrefActivity samplePrefActivity);
}
