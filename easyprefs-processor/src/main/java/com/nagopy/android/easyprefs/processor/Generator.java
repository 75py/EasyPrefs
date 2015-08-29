package com.nagopy.android.easyprefs.processor;


import com.nagopy.android.easyprefs.annotations.EasyPrefBoolean;
import com.nagopy.android.easyprefs.annotations.EasyPrefMultiSelection;
import com.nagopy.android.easyprefs.annotations.EasyPrefSingleSelection;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public abstract class Generator {

    protected ProcessingEnvironment processingEnv;

    public Generator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public abstract void generateProviderClass();

    public abstract void generateNewPreferenceMethod();

    public static Generator getInstance(TypeElement annotationTypeElement, ProcessingEnvironment processingEnv, Element element) {
        Name qualifiedName = annotationTypeElement.getQualifiedName();
        if (qualifiedName.contentEquals(EasyPrefBoolean.class.getCanonicalName())) {
            return new BooleanGenerator(processingEnv, element);
        } else if (qualifiedName.contentEquals(EasyPrefSingleSelection.class.getCanonicalName())) {
            return new SingleSelectionGenerator(processingEnv, element);
        } else if (qualifiedName.contentEquals(EasyPrefMultiSelection.class.getCanonicalName())) {
            return new MultiSelectionGenerator(processingEnv, element);
        }

        throw new IllegalArgumentException("Unknown:" + annotationTypeElement.getSimpleName());
    }
}
