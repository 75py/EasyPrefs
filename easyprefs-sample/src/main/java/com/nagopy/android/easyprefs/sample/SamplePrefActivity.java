package com.nagopy.android.easyprefs.sample;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Toast;

import com.nagopy.android.easyprefs.sample.prefs.CategorySetting;
import com.nagopy.android.easyprefs.sample.prefs.FruitsSetting;
import com.nagopy.android.easyprefs.sample.prefs.ShowToastSetting;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SamplePrefActivity extends PreferenceActivity {

    @Inject
    ShowToastSetting showToastSetting;

    @Inject
    CategorySetting categorySetting;

    @Inject
    FruitsSetting fruitsSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SampleApp.applicationComponent.inject(this);

        setContentView(R.layout.activity_sample_pref);
        addPreferencesFromResource(R.xml.prefs);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_show_toast)
    public void onClickShowToastBtn(View view) {
        Toast.makeText(getApplicationContext(), showToastSetting.getValue() ? "Show toast!" : "silent", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_category)
    public void onClickCategoryBtn(View view) {
        Toast.makeText(getApplicationContext(), categorySetting.getValue().name(), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_fruits)
    public void onClickMultiCategoryBtn(View view) {
        Toast.makeText(getApplicationContext(), fruitsSetting.getValue().toString(), Toast.LENGTH_SHORT).show();
    }
}
