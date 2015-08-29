package com.nagopy.android.easyprefs.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface EasyPrefMultiSelection {

    Class<? extends Enum> target();

    boolean inject() default true;

    int title() default 0;

    String titleStr() default "";

    String defValue() default "";

    boolean nullable() default true;

}
