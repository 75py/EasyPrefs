package com.nagopy.android.easyprefs.sample;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nagopy.android.easyprefs.sample.prefs.CategorySetting;
import com.nagopy.android.easyprefs.sample.prefs.FruitsSetting;
import com.nagopy.android.easyprefs.sample.prefs.ShowToastSetting;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    final Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    ShowToastSetting provideShowToast(Application application){
        // The class name must be written in full path if you use Dagger2.
        return new com.nagopy.android.easyprefs.sample.prefs.GenShowToastSetting(application);
    }
    @Provides
    @Singleton
    CategorySetting provideCategoryList(Application application){
        return new com.nagopy.android.easyprefs.sample.prefs.GenCategorySetting(application);
    }
    @Provides
    @Singleton
    FruitsSetting provideFruitsSetting(Application application){
        return new com.nagopy.android.easyprefs.sample.prefs.GenFruitsSetting(application);
    }

}
