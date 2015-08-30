package com.nagopy.android.easyprefs.sample.prefs;

import android.content.Context;
import android.os.Build;

import com.nagopy.android.easyprefs.EasyPref;
import com.nagopy.android.easyprefs.SingleSelectionItem;
import com.nagopy.android.easyprefs.annotations.EasyPrefSingleSelection;

@EasyPrefSingleSelection(titleStr = "test", defValue = "", nullable = true, target = SingleSelectionSample.TargetEnum.class)
public interface SingleSelectionSample extends EasyPref<SingleSelectionSample.TargetEnum> {

    static enum TargetEnum implements SingleSelectionItem {
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
        }
    }
}
