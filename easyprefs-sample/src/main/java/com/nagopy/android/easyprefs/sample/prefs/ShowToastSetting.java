package com.nagopy.android.easyprefs.sample.prefs;

import com.nagopy.android.easyprefs.BoolPref;
import com.nagopy.android.easyprefs.annotations.EasyPrefBoolean;

@EasyPrefBoolean(titleStr = "トーストを表示する", summaryStr = "何かさまりー", defValueBool = false)
public interface ShowToastSetting extends BoolPref {
}
