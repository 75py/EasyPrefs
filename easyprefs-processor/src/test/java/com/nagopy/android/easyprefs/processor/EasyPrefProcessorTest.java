package com.nagopy.android.easyprefs.processor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;
import javax.tools.JavaFileObject;

import dagger.Module;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

@Module
@RunWith(JUnit4.class)
public class EasyPrefProcessorTest {

    @Inject
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void bool() throws Throwable {
        testProvider("BoolPrefSample");
        testProvider("BoolPrefSample2");
        testBoolPreference("BoolPrefSample");
        testBoolPreference("BoolPrefSample2");
    }

    @Test
    public void boolコンパイル時チェック_タイトル未指定() throws Throwable {
        JavaFileObject src = getJavaFileObject("BoolPrefSample", "validate/bool/Title.txt");
        assert_().about(javaSource())
                .that(src)
                .processedWith(new EasyPrefProcessor())
                .failsToCompile()
                .withErrorContaining("title or titleStr is required.");
    }

    @Test
    public void singleSelection() throws Throwable {
        testProvider("SingleSelectionSample");
        testProvider("SingleSelectionSample2");
        testSingleSelectionPreference("SingleSelectionSample");
        testSingleSelectionPreference("SingleSelectionSample2");
    }

    @Test
    public void singleSelectionコンパイル時チェック_タイトル未指定() throws Throwable {
        JavaFileObject src = getJavaFileObject("SingleSelectionSample", "validate/singleselection/Title.txt");
        assert_().about(javaSource())
                .that(src)
                .processedWith(new EasyPrefProcessor())
                .failsToCompile()
                .withErrorContaining("title or titleStr is required.");
    }

    private void testProvider(String testCaseName) throws IOException, URISyntaxException {
        JavaFileObject src = getJavaFileObject(testCaseName, testCaseName + ".txt");
        JavaFileObject expected = getJavaFileObject("Gen" + testCaseName, testCaseName + "Provider.txt");
        compare(src, expected);
    }

    private void testBoolPreference(String testCaseName) throws IOException, URISyntaxException {
        JavaFileObject src = getJavaFileObject(testCaseName, testCaseName + ".txt");
        JavaFileObject expected = getJavaFileObject(testCaseName + "_CheckBoxPreference", testCaseName + "Preference.txt");
        compare(src, expected);
    }

    private void testSingleSelectionPreference(String testCaseName) throws IOException, URISyntaxException {
        JavaFileObject src = getJavaFileObject(testCaseName, testCaseName + ".txt");
        JavaFileObject expected = getJavaFileObject(testCaseName + "_SingleSelectionPreference", testCaseName + "Preference.txt");
        compare(src, expected);
    }


    private void compare(JavaFileObject src, JavaFileObject expected) {
        assert_().about(javaSource())
                .that(src)
                .processedWith(new EasyPrefProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    private JavaFileObject getJavaFileObject(String className, String fileName) throws IOException, URISyntaxException {
        return JavaFileObjects.forSourceLines(className, getResourceString("/" + fileName));
    }

    private List<String> getResourceString(String path) throws URISyntaxException, IOException {
        return Files.readAllLines(Paths.get(getClass().getResource(path).toURI()));
    }

}