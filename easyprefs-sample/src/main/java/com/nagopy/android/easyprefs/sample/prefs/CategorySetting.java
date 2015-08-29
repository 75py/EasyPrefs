package com.nagopy.android.easyprefs.sample.prefs;

import com.nagopy.android.easyprefs.EasyPref;
import com.nagopy.android.easyprefs.annotations.EasyPrefSingleSelection;

@EasyPrefSingleSelection(target = Category.class, titleStr = "title", defValue = "ALL")
public interface CategorySetting extends EasyPref<Category> {
}
