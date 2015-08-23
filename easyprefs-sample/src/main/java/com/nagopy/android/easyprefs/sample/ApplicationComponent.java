package com.nagopy.android.easyprefs.sample;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(SamplePrefActivity samplePrefActivity);
}
