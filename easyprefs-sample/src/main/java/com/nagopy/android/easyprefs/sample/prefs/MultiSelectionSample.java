package com.nagopy.android.easyprefs.sample.prefs;

import android.content.Context;
import android.os.Build;

import com.nagopy.android.easyprefs.EasyPref;
import com.nagopy.android.easyprefs.MultiSelectionItem;
import com.nagopy.android.easyprefs.annotations.EasyPrefMultiSelection;

import java.util.List;

@EasyPrefMultiSelection(titleStr = "title", defValue = "", nullable = true, target = MultiSelectionSample.TargetEnum.class)
public interface MultiSelectionSample extends EasyPref<List<MultiSelectionSample.TargetEnum>> {

    static enum TargetEnum implements MultiSelectionItem {
        TEST {
            @Override
            public String getTitle(Context context) {
                return "title1";
            }

            @Override
            public String getSummary(Context context) {
                return "summary1";
            }

            @Override
            public int minSdkVersion() {
                return Build.VERSION_CODES.KITKAT;
            }

            @Override
            public int maxSdkVersion() {
                return Integer.MAX_VALUE;
            }
        }, TEST2 {
            @Override
            public String getTitle(Context context) {
                return "title2";
            }

            @Override
            public String getSummary(Context context) {
                return "summary";
            }

            @Override
            public int minSdkVersion() {
                return 0;
            }

            @Override
            public int maxSdkVersion() {
                return Integer.MAX_VALUE;
            }
        }
    }
}
