package com.nagopy.android.easyprefs.sample.prefs;

import com.nagopy.android.easyprefs.EasyPref;
import com.nagopy.android.easyprefs.annotations.EasyPrefMultiSelection;

import java.util.List;

@EasyPrefMultiSelection(target = Fruits.class, titleStr = "Fruits", defValue = "")
public interface FruitsSetting extends EasyPref<List<Fruits>> {
}
