package com.nagopy.android.easyprefs.processor;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.google.auto.service.AutoService;
import com.nagopy.android.easyprefs.annotations.EasyPrefListSingle;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
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
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.nagopy.android.easyprefs.annotations.EasyPrefListSingle")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class PrefListProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        debugLog("--------------------------------------------------------------------------------");

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EasyPrefListSingle.class);
        for (Element element : elements) {
            Builder builder = new Builder(element);
            builder.buildProviderClass();
            builder.buildPreferenceClass();
        }


        debugLog("--------------------------------------------------------------------------------");
        return true;
    }

    private class Builder {
        final Element element;
        final String fullClassName;
        final String simpleClassName;
        final String packageName;
        final String getClassName;
        final String key;
        final EasyPrefListSingle annotation;
        private String targetClassName;

        Builder(Element element) {
            this.element = element;
            fullClassName = element.asType().toString();
            simpleClassName = element.getSimpleName().toString();
            packageName = fullClassName.substring(0, fullClassName.length() - simpleClassName.length() - 1);
            getClassName = "Gen" + simpleClassName;
            annotation = element.getAnnotation(EasyPrefListSingle.class);
            key = fullClassName;

            try {
                targetClassName = annotation.target().getName();
            } catch (MirroredTypeException mte) {
                targetClassName = mte.getTypeMirror().toString();
            }
        }

        void buildProviderClass() {
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
                    .build();
            JavaFile providerFile = JavaFile.builder(packageName, providerType)
                    .build();
            try {
                providerFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }

        void buildPreferenceClass() {
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
                    .addStatement("setKey($S)", key)
                    .addStatement("$L[] values = $L.values()", targetClassName, targetClassName)
                    .addStatement("$T<String, String> entries = new $T<>()", Map.class, LinkedHashMap.class)
                    .beginControlFlow("for ($L val : values)", targetClassName)
                    .addStatement("entries.put(val.name(), val.getTitle(context))")
                    .endControlFlow()
                    .addStatement("setEntryValues(entries.keySet().toArray(new String[entries.size()]))")
                    .addStatement("setEntries(entries.values().toArray(new String[entries.size()]))");

            if (annotation.title() > 0) {
                initialize.addStatement("setTitle($L)", annotation.title());
            } else if (annotation.titleStr() != null && annotation.titleStr().length() > 0) {
                initialize.addStatement("setTitle($S)", annotation.titleStr());
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "title or titleStr is required.");
                throw new RuntimeException("title or titleStr is required.");
            }

            if (annotation.defValue().length() > 0) {
                initialize.addStatement("setDefaultValue($S)", annotation.defValue());
            }

            TypeSpec prefType = TypeSpec.classBuilder(simpleClassName + "Preference")
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ListPreference.class)
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
//        System.out.println(msg);
    }

}
