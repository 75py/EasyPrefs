package com.nagopy.android.easyprefs.sample;

import android.app.Application;

public class SampleApp  extends Application{

    public static ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }
}
