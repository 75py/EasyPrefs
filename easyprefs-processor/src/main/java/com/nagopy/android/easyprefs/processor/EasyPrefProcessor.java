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
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
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
@SupportedAnnotationTypes("com.nagopy.android.easyprefs.annotations.EasyPrefBoolean")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EasyPrefProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        debugLog("--------------------------------------------------------------------------------");

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EasyPrefBoolean.class);
        for (Element element : elements) {
            Builder builder = new Builder(element);
            builder.buildProviderClass();
            builder.buildPrefrenceClass();
        }


        debugLog("--------------------------------------------------------------------------------");
        return true;
    }

    private class Builder {
        final String fullClassName;
        final String simpleClassName;
        final String packageName;
        final String getClassName;
        final String key;
        final EasyPrefBoolean easyPrefBoolean;

        Builder(Element element) {
            fullClassName = element.asType().toString();
            simpleClassName = element.getSimpleName().toString();
            packageName = fullClassName.substring(0, fullClassName.length() - simpleClassName.length() - 1);
            getClassName = "Gen" + simpleClassName;
            easyPrefBoolean = element.getAnnotation(EasyPrefBoolean.class);
            key = fullClassName;
        }

        void buildProviderClass() {
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Application.class, "application")
                    .addStatement("sp = $T.getDefaultSharedPreferences(application)", PreferenceManager.class)
                    .addStatement("key = $S", key);
            if (easyPrefBoolean.inject()) {
                constructor.addAnnotation(ClassName.get("javax.inject", "Inject"));
            }
            if (easyPrefBoolean.defValue() != 0) {
                constructor.addStatement("defValue = application.getResources().getBoolean($L)", easyPrefBoolean.defValue());
            } else {
                constructor.addStatement("defValue = $L", easyPrefBoolean.defValueBool());
            }

            MethodSpec.Builder getValue = MethodSpec.methodBuilder("getValue")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(Boolean.class)
                    .addStatement("return sp.getBoolean(key, defValue)");

            MethodSpec.Builder getDefaultValue = MethodSpec.methodBuilder("getDefaultValue")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(Boolean.class)
                    .addStatement("return defValue");

            MethodSpec.Builder updateValue = MethodSpec.methodBuilder("updateValue")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Boolean.class, "newValue")
                    .returns(boolean.class)
                    .addStatement("return sp.edit().putBoolean(key, newValue).commit()");

            MethodSpec.Builder clearValue = MethodSpec.methodBuilder("clearValue")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(boolean.class)
                    .addStatement("return sp.edit().remove(key).commit()");

            TypeSpec providerType = TypeSpec.classBuilder(getClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(packageName, simpleClassName))
                    .addField(SharedPreferences.class, "sp", Modifier.PRIVATE, Modifier.FINAL)
                    .addField(boolean.class, "defValue", Modifier.PRIVATE, Modifier.FINAL)
                    .addField(String.class, "key", Modifier.PRIVATE, Modifier.FINAL)
                    .addMethod(constructor.build())
                    .addMethod(getValue.build())
                    .addMethod(getDefaultValue.build())
                    .addMethod(updateValue.build())
                    .addMethod(clearValue.build())
                    .build();
            JavaFile providerFile = JavaFile.builder(packageName, providerType)
                    .build();
            try {
                providerFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }

        void buildPrefrenceClass() {
            MethodSpec constructor1 = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Context.class, "context")
                    .addStatement("super(context)")
                    .addStatement("initialize()")
                    .build();
            MethodSpec constructor2 = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Context.class, "context")
                    .addParameter(AttributeSet.class, "attrs")
                    .addStatement("super(context, attrs)")
                    .addStatement("initialize()")
                    .build();
            MethodSpec constructor3 = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Context.class, "context")
                    .addParameter(AttributeSet.class, "attrs")
                    .addParameter(int.class, "defStyleAttr")
                    .addStatement("super(context, attrs, defStyleAttr)")
                    .addStatement("initialize()")
                    .build();
            MethodSpec constructor4 = MethodSpec.constructorBuilder()
                    .addAnnotation(
                            AnnotationSpec.builder(TargetApi.class)
                                    .addMember("value", "$T.VERSION_CODES.LOLLIPOP", Build.class)
                                    .build()
                    ).addModifiers(Modifier.PUBLIC)
                    .addParameter(Context.class, "context")
                    .addParameter(AttributeSet.class, "attrs")
                    .addParameter(int.class, "defStyleAttr")
                    .addParameter(int.class, "defStyleRes")
                    .addStatement("super(context, attrs, defStyleAttr, defStyleRes)")
                    .addStatement("initialize()")
                    .build();

            MethodSpec.Builder initialize = MethodSpec.methodBuilder("initialize")
                    .addModifiers(Modifier.PRIVATE)
                    .addStatement("setKey($S)", key);
            if (easyPrefBoolean.title() > 0) {
                initialize.addStatement("setTitle($L)", easyPrefBoolean.title());
            } else if (easyPrefBoolean.titleStr() != null && easyPrefBoolean.titleStr().length() > 0) {
                initialize.addStatement("setTitle($S)", easyPrefBoolean.titleStr());
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "title or titleStr is required.");
                throw new RuntimeException("title or titleStr is required.");
            }

            if (easyPrefBoolean.summary() > 0) {
                initialize.addStatement("setSummary($L)", easyPrefBoolean.summary());
            } else if (easyPrefBoolean.summaryStr() != null && easyPrefBoolean.summaryStr().length() > 0) {
                initialize.addStatement("setSummary($S)", easyPrefBoolean.summaryStr());
            }

            TypeSpec prefType = TypeSpec.classBuilder(simpleClassName + "Preference")
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(CheckBoxPreference.class)
                    .addMethod(constructor1)
                    .addMethod(constructor2)
                    .addMethod(constructor3)
                    .addMethod(constructor4)
                    .addMethod(initialize.build())
                    .build();

            JavaFile prefFile = JavaFile.builder(packageName, prefType)
                    .build();
            try {
                prefFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
    }

    private void debugLog(String msg) {
        System.out.println(msg);
    }

}
