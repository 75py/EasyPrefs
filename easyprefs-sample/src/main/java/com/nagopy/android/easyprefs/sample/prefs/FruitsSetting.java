package com.nagopy.android.easyprefs.sample.prefs;

import com.nagopy.android.easyprefs.EasyPref;
import com.nagopy.android.easyprefs.annotations.EasyPrefListMulti;

import java.util.List;

@EasyPrefListMulti(target = Fruits.class, titleStr = "Fruits", defValue = "")
public interface FruitsSetting extends EasyPref<List<Fruits>> {
}
