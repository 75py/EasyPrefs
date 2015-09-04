package com.nagopy.android.easyprefs.sample.prefs;

import com.nagopy.android.easyprefs.BoolPref;
import com.nagopy.android.easyprefs.annotations.EasyPrefBoolean;

@EasyPrefBoolean(titleStr = "Update automatically", defValueBool = true)
public interface PrefAutomaticallyUpdate extends BoolPref {
}
