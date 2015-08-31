package com.nagopy.android.easyprefs.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

@RunWith(JUnit4.class)
public class EasyPrefProcessorSingleSelectionTest {

    final TestHelper testHelper = new TestHelper(
            "SingleSelectionSample",
            "/singleselection/",
            "GenSingleSelectionSample",
            "SingleSelectionSample_SingleSelectionPreference");

    @Test
    public void よくある形() throws Throwable {
        testHelper.testProvider("リソース指定あり");
        testHelper.testProvider("リソース指定なし");
        testHelper.testPreference("リソース指定あり");
        testHelper.testPreference("リソース指定なし");
    }

    @Test
    public void コンパイル時チェック_タイトル未指定() throws Throwable {
        JavaFileObject src = testHelper.getJavaFileObject("タイトルなし.txt");
        assert_().about(javaSource())
                .that(src)
                .processedWith(new EasyPrefProcessor())
                .failsToCompile()
                .withErrorContaining("title or titleStr is required");
    }

    @Test
    public void コンパイル時チェック_Interface未実装() throws Throwable {
        JavaFileObject src = testHelper.getJavaFileObject("Interface未実装.txt");
        assert_().about(javaSource())
                .that(src)
                .processedWith(new EasyPrefProcessor())
                .failsToCompile()
                .withErrorContaining("must implements SingleSelectionItem");
    }

}