package com.nagopy.android.easyprefs.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface EasyPrefBoolean {

    boolean inject() default true;

    int title() default 0;

    String titleStr() default "";

    int summary() default 0;

    String summaryStr() default "";

    int defValue() default 0;

    boolean defValueBool() default false;
}
