package com.nagopy.android.easyprefs.sample.prefs;

import android.content.Context;

import com.nagopy.android.easyprefs.SingleSelectionItem;

public enum Category implements SingleSelectionItem {

    ALL {
        @Override
        public String getTitle(Context context) {
            return "ぜんぶ！";
        }
    }, RUNNING {
        @Override
        public String getTitle(Context context) {
            return "CHOTTO!";
        }
    };

    @Override
    public String getSummary(Context context) {
        return null;
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

