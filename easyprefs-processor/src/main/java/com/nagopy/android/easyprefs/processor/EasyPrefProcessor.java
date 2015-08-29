package com.nagopy.android.easyprefs.processor;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.google.auto.service.AutoService;
import com.nagopy.android.easyprefs.annotations.EasyPrefBoolean;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.nagopy.android.easyprefs.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EasyPrefProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        debugLog("--------------------------------------------------------------------------------");

        annotations.forEach(annotation -> {
            roundEnv.getElementsAnnotatedWith(annotation).forEach(element -> {
                Generator generator = Generator.getInstance(annotation, processingEnv, element);
                generator.generateProviderClass();
                generator.generateNewPreferenceMethod();
            });
        });

        debugLog("--------------------------------------------------------------------------------");
        return true;
    }


    private void debugLog(String msg) {
        System.out.println(msg);
    }
}
