package com.nagopy.android.easyprefs.sample.prefs;

import android.content.Context;

import com.nagopy.android.easyprefs.MultiSelectionItem;

public enum Fruits implements MultiSelectionItem {

    APPLE {
        @Override
        public String getTitle(Context context) {
            return "Apple";
        }
    }, ORANGE {
        @Override
        public String getTitle(Context context) {
            return "Orange";
        }
    }, CHOCOLATE {
        @Override
        public String getTitle(Context context) {
            return "Chocolate";
        }

        @Override
        public String getSummary(Context context) {
            return "It's not a fruit.";
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
}

