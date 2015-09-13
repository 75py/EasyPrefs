package com.nagopy.android.easyprefs.processor;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.nagopy.android.easyprefs.SingleSelectionItem;
import com.nagopy.android.easyprefs.annotations.EasyPrefSingleSelection;
import com.nagopy.android.easyprefs.preference.AbstractSingleSelectionPreference;
import com.nagopy.android.easyprefs.processor.util.ProcessorUtil;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;

public class SingleSelectionGenerator extends Generator {
    final Element element;
    final String fullClassName;
    final String simpleClassName;
    final String packageName;
    final String getClassName;
    final String key;
    final EasyPrefSingleSelection annotation;

    private String targetClassName;
    Optional<Element> targetClassElement;

    public SingleSelectionGenerator(ProcessingEnvironment processingEnv, Element element) {
        super(processingEnv);
        this.element = element;
        fullClassName = element.asType().toString();

        simpleClassName = element.getSimpleName().toString();
        packageName = fullClassName.substring(0, fullClassName.length() - simpleClassName.length() - 1);
        getClassName = "Gen" + simpleClassName;
        annotation = element.getAnnotation(EasyPrefSingleSelection.class);
        key = fullClassName;

        try {
            targetClassName = annotation.target().getName();
            targetClassElement = Optional.empty();
        } catch (MirroredTypeException mte) {
            targetClassName = mte.getTypeMirror().toString();
            targetClassElement = ProcessorUtil.toElement(mte.getTypeMirror());
        }
    }

    @Override
    public Set<String> validate() {
        Set<String> errors = new LinkedHashSet<>();

        if (annotation.title() == 0 &&
                (annotation.titleStr() == null || annotation.titleStr().isEmpty())) {
            errors.add("title or titleStr is required.");
        }

        targetClassElement.ifPresent(e -> {
            if (!ProcessorUtil.isImplements(e, SingleSelectionItem.class.getName())) {
                errors.add(e.getSimpleName() + " must implements SingleSelectionItem");
            }
        });

        if (!annotation.nullable() && annotation.defValue().isEmpty()) {
            errors.add("defValue is required if it's not nullable");
        }

        for (String error : errors) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error);
        }
        return errors;
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

        if (annotation.defValue() != null && annotation.defValue().length() > 0) {
            constructor.addStatement("defValue = $L.valueOf($S)", targetClassName, annotation.defValue());
        } else {
            constructor.addStatement("defValue = null");
        }

        MethodSpec.Builder getValue = MethodSpec.methodBuilder("getValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeVariableName.get(targetClassName))
                .addCode(CodeBlock.builder()
                        .addStatement("String value = sp.getString(key, null)")
                        .beginControlFlow("if (value == null || value.isEmpty())")
                        .addStatement("return defValue")
                        .endControlFlow()
                        .addStatement("return $L.valueOf(value)", targetClassName)
                        .build());

        MethodSpec.Builder getDefaultValue = MethodSpec.methodBuilder("getDefaultValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeVariableName.get(targetClassName))
                .addStatement("return defValue");

        MethodSpec.Builder updateValue = MethodSpec.methodBuilder("updateValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeVariableName.get(targetClassName), "newValue")
                .returns(boolean.class)
                .addStatement("return sp.edit().putString(key, newValue.name()).commit()");

        MethodSpec.Builder clearValue = MethodSpec.methodBuilder("clearValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addStatement("return sp.edit().remove(key).commit()");

        MethodSpec.Builder getKey = MethodSpec.methodBuilder("getKey")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return key");

        TypeSpec providerType = TypeSpec.classBuilder(getClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(packageName, simpleClassName))
                .addField(SharedPreferences.class, "sp", Modifier.PRIVATE, Modifier.FINAL)
                .addField(FieldSpec.builder(
                        TypeVariableName.get(targetClassName), "defValue", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addField(String.class, "key", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(constructor.build())
                .addMethod(getValue.build())
                .addMethod(getDefaultValue.build())
                .addMethod(updateValue.build())
                .addMethod(clearValue.build())
                .addMethod(getKey.build())
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
                .addAnnotation(
                        AnnotationSpec.builder(TargetApi.class)
                                .addMember("value", "$T.VERSION_CODES.LOLLIPOP", Build.class)
                                .build()
                ).addModifiers(Modifier.PUBLIC)
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

        if (annotation.title() > 0) {
            initialize.addStatement("setTitle($L)", annotation.title());
        } else if (annotation.titleStr() != null && annotation.titleStr().length() > 0) {
            initialize.addStatement("setTitle($S)", annotation.titleStr());
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "title or titleStr is required.");
            return;
        }

        initialize.addStatement("setDefaultValue($S)", annotation.defValue());

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(List.class),
                TypeVariableName.get(targetClassName));
        MethodSpec getEntries = MethodSpec.methodBuilder("getEntries")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(parameterizedTypeName)
                .addStatement("$T entries = new $T<>()", List.class, ArrayList.class)
                .beginControlFlow("for ($L value : $L.values())", targetClassName, targetClassName)
                .beginControlFlow("if (value.minSdkVersion() <= $T.SDK_INT && $T.SDK_INT <= value.maxSdkVersion() )", Build.VERSION.class, Build.VERSION.class)
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

        TypeSpec prefType = TypeSpec.classBuilder(simpleClassName + "_SingleSelectionPreference")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(AbstractSingleSelectionPreference.class)
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
