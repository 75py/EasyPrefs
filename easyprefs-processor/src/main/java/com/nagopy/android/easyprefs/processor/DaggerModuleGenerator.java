package com.nagopy.android.easyprefs.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

public class DaggerModuleGenerator {
    ProcessingEnvironment processingEnv;
    List<MethodSpec> methods;

    public DaggerModuleGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        methods = new ArrayList<>();
    }

    public DaggerModuleGenerator addModule(Generator generator) {
        generator.generateDaggerProviderMethod().ifPresent(methods::add);
        return this;
    }

    public void generate() {
        if (methods.isEmpty()) {
            return;
        }

        TypeSpec moduleClass = TypeSpec.classBuilder("EasyPrefModule")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ClassName.get("dagger", "Module"))
                .addMethods(methods)
                .build();

        JavaFile providerFile = JavaFile.builder("com.nagopy.android.easyprefs", moduleClass)
                .build();
        try {
            providerFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

}
