package com.nagopy.android.easyprefs.processor;


import com.nagopy.android.easyprefs.annotations.EasyPrefBoolean;
import com.nagopy.android.easyprefs.annotations.EasyPrefMultiSelection;
import com.nagopy.android.easyprefs.annotations.EasyPrefSingleSelection;
import com.squareup.javapoet.MethodSpec;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public abstract class Generator {

    protected ProcessingEnvironment processingEnv;

    public Generator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public Set<String> validate() {
        return Collections.emptySet();
    }

    public abstract void generateProviderClass();

    public abstract void generateNewPreferenceMethod();

    public abstract Optional<MethodSpec> generateDaggerProviderMethod();

    public static Generator getInstance(Class<? extends Annotation> annotationClass, ProcessingEnvironment processingEnv, Element element) {
        if (annotationClass == EasyPrefBoolean.class) {
            return new BooleanGenerator(processingEnv, element);
        } else if (annotationClass == EasyPrefSingleSelection.class) {
            return new SingleSelectionGenerator(processingEnv, element);
        } else if (annotationClass == EasyPrefMultiSelection.class) {
            return new MultiSelectionGenerator(processingEnv, element);
        }

        throw new IllegalArgumentException("Unknown:" + annotationClass.getSimpleName());
    }
}
