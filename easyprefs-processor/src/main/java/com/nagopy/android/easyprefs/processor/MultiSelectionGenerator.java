package com.nagopy.android.easyprefs.processor;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.nagopy.android.easyprefs.annotations.EasyPrefMultiSelection;
import com.nagopy.android.easyprefs.preference.AbstractMultiSelectPreference;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;

public class MultiSelectionGenerator extends Generator {

    final Element element;
    final String fullClassName;
    final String simpleClassName;
    final String packageName;
    final String getClassName;
    final String key;
    final EasyPrefMultiSelection annotation;
    private String targetClassName;

    public MultiSelectionGenerator(ProcessingEnvironment processingEnv, Element element) {
        super(processingEnv);
        this.element = element;
        fullClassName = element.asType().toString();
        simpleClassName = element.getSimpleName().toString();
        packageName = fullClassName.substring(0, fullClassName.length() - simpleClassName.length() - 1);
        getClassName = "Gen" + simpleClassName;
        annotation = element.getAnnotation(EasyPrefMultiSelection.class);
        key = fullClassName;

        try {
            targetClassName = annotation.target().getName();
        } catch (MirroredTypeException mte) {
            targetClassName = mte.getTypeMirror().toString();
        }
    }

    @Override
    public void generateProviderClass() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Application.class, "application")
                .addStatement("sp = $T.getDefaultSharedPreferences(application)", PreferenceManager.class)
                .addStatement("key = $S", key);
        if (annotation.inject()) {
            constructor.addAnnotation(ClassName.get("javax.inject", "Inject"));
        }

        constructor.addStatement("defValue = str2Enum($S)", annotation.defValue());

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(List.class), TypeVariableName.get(targetClassName));
        MethodSpec str2Enum = MethodSpec.methodBuilder("str2Enum")
                .addModifiers(Modifier.PRIVATE)
                .returns(parameterizedTypeName)
                .addParameter(String.class, "strValue")
                .beginControlFlow("if (strValue == null || strValue.isEmpty())")
                .addStatement("return $T.emptyList()", Collections.class)
                .endControlFlow()
                .addStatement("return strArray2Enum(strValue.split($S))", ",")
                .build();
        MethodSpec strArray2Enum = MethodSpec.methodBuilder("strArray2Enum")
                .addModifiers(Modifier.PRIVATE)
                .returns(parameterizedTypeName)
                .addParameter(String[].class, "strValues")
                .beginControlFlow("if (strValues == null || strValues.length == 0)")
                .addStatement("return $T.emptyList()", Collections.class)
                .endControlFlow()
                .addStatement("$T result = new $T<>(strValues.length)", List.class, ArrayList.class)
                .beginControlFlow("for (String s : strValues)")
                .addStatement("result.add($L.valueOf(s))", targetClassName)
                .endControlFlow()
                .addStatement("return result")
                .build();

        MethodSpec toStrArray = MethodSpec.methodBuilder("toStringSet")
                .addModifiers(Modifier.PRIVATE)
                .returns(ParameterizedTypeName.get(Set.class, String.class))
                .addParameter(parameterizedTypeName, "values")
                .beginControlFlow("if (values == null || values.isEmpty())")
                .addStatement("return $T.emptySet()", Collections.class)
                .endControlFlow()
                .addStatement("$T<String> result = new $T()", Set.class, LinkedHashSet.class)
                .beginControlFlow("for ($L value : values)", targetClassName)
                .addStatement("result.add(value.name())")
                .endControlFlow()
                .addStatement("return result")
                .build();

        MethodSpec.Builder getValue = MethodSpec.methodBuilder("getValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(List.class)
                .returns(parameterizedTypeName)
                .addCode(CodeBlock.builder()
                        .addStatement("String value = sp.getString(key, null)")
                        .addStatement("return str2Enum(value)")
//                            .beginControlFlow("if (value == null || value.isEmpty())")
//                            .addStatement("return defValue")
//                            .endControlFlow()
//                            .addStatement("$T resultList = new $T<>()", List.class, ArrayList.class)
//                            .beginControlFlow("for (String v : value.split($S))", ",")
//                            .addStatement("resultList.add($L.valueOf(v))", targetClassName)
//                            .endControlFlow()
//                            .addStatement("return resultList")
                        .build());

        MethodSpec.Builder getDefaultValue = MethodSpec.methodBuilder("getDefaultValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(parameterizedTypeName)
                .addStatement("return defValue");

        MethodSpec.Builder updateValue = MethodSpec.methodBuilder("updateValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterizedTypeName, "newValue")
                .returns(boolean.class)
                .addStatement("return sp.edit().putStringSet(key, toStringSet(newValue)).commit()");

        MethodSpec.Builder clearValue = MethodSpec.methodBuilder("clearValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addStatement("return sp.edit().remove(key).commit()");

        TypeSpec providerType = TypeSpec.classBuilder(getClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(packageName, simpleClassName))
                .addField(SharedPreferences.class, "sp", Modifier.PRIVATE, Modifier.FINAL)
                .addField(FieldSpec.builder(
                        parameterizedTypeName, "defValue", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addField(String.class, "key", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(constructor.build())
                .addMethod(getValue.build())
                .addMethod(getDefaultValue.build())
                .addMethod(updateValue.build())
                .addMethod(clearValue.build())
                .addMethod(str2Enum)
                .addMethod(strArray2Enum)
                .addMethod(toStrArray)
                .build();
        JavaFile providerFile = JavaFile.builder(packageName, providerType)
                .build();
        try {
            providerFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    @Override
    public void generateNewPreferenceMethod() {
        MethodSpec constructor1 = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Context.class, "context")
                .addStatement("super(context)")
                .addStatement("initialize(context)")
                .build();
        MethodSpec constructor2 = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Context.class, "context")
                .addParameter(AttributeSet.class, "attrs")
                .addStatement("super(context, attrs)")
                .addStatement("initialize(context)")
                .build();
        MethodSpec constructor3 = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Context.class, "context")
                .addParameter(AttributeSet.class, "attrs")
                .addParameter(int.class, "defStyleAttr")
                .addStatement("super(context, attrs, defStyleAttr)")
                .addStatement("initialize(context)")
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
                .addStatement("initialize(context)")
                .build();

        MethodSpec.Builder initialize = MethodSpec.methodBuilder("initialize")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Context.class, "context")
                .addStatement("setKey($S)", key);
        if (annotation.defValue().length() > 0) {
            initialize.addStatement("setDefaultValue($S)", annotation.defValue());
        }

        if (annotation.title() > 0) {
            initialize.addStatement("setTitle($L)", annotation.title());
        } else if (annotation.titleStr() != null && annotation.titleStr().length() > 0) {
            initialize.addStatement("setTitle($S)", annotation.titleStr());
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "title or titleStr is required.");
            throw new RuntimeException("title or titleStr is required.");
        }


        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(List.class),
                TypeVariableName.get(targetClassName));
        MethodSpec getEntries = MethodSpec.methodBuilder("getEntries")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(parameterizedTypeName)
                .addStatement("$T entries = new $T<>()", List.class, ArrayList.class)
                .beginControlFlow("for ($L value : $L.values())", targetClassName, targetClassName)
                .beginControlFlow("if (value.minSdkVersion() <= $T.SDK_INT )", Build.VERSION.class)
                .addStatement("entries.add(value)")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return entries")
                .build();

        MethodSpec.Builder getDefaultValue = MethodSpec.methodBuilder("getDefaultValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(String.class);
        if (annotation.defValue().isEmpty()) {
            if (!annotation.nullable()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "defValue is required if it's not nullable");
            }
            getDefaultValue.addStatement("return null");
        } else {
            getDefaultValue.addStatement("return $L.$L.name()", targetClassName, annotation.defValue());
        }

        MethodSpec nullable = MethodSpec.methodBuilder("nullable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(boolean.class)
                .addStatement("return $L", annotation.nullable())
                .build();

        TypeSpec prefType = TypeSpec.classBuilder(simpleClassName + "Preference")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(AbstractMultiSelectPreference.class)
                        , TypeVariableName.get(targetClassName)))
                .addMethod(constructor1)
                .addMethod(constructor2)
                .addMethod(constructor3)
                .addMethod(constructor4)
                .addMethod(initialize.build())
                .addMethod(getEntries)
                .addMethod(getDefaultValue.build())
                .addMethod(nullable)
                .build();

        JavaFile prefFile = JavaFile.builder(packageName, prefType)
                .build();
        try {
            prefFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    @Override
    public Optional<MethodSpec> generateDaggerProviderMethod() {
        return annotation.inject() ?
                Optional.of(MethodSpec.methodBuilder("provide" + simpleClassName)
                        .addAnnotation(ClassName.get("dagger", "Provides"))
                        .addAnnotation(ClassName.get("javax.inject", "Singleton"))
                        .addParameter(Application.class, "application")
                        .returns(TypeVariableName.get(element.asType()))
                        .addStatement("return new $T(application)",
                                ClassName.get(packageName, getClassName))
                        .build())
                :
                Optional.<MethodSpec>empty();
    }
}
