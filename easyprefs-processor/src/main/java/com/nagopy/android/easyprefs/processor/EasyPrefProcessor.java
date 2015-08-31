package com.nagopy.android.easyprefs.processor;

import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

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
        DaggerModuleGenerator daggerModuleGenerator = new DaggerModuleGenerator(processingEnv);
        annotations.forEach(annotation -> {
            roundEnv.getElementsAnnotatedWith(annotation).forEach(element -> {
                Generator generator = Generator.getInstance(annotation, processingEnv, element);
                if (generator.validate()) {
                    generator.generateProviderClass();
                    generator.generateNewPreferenceMethod();
                    daggerModuleGenerator.addModule(generator);
                }
            });
        });
        daggerModuleGenerator.generate();
        return true;
    }

}
