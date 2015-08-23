package com.nagopy.android.easyprefs.processor;

import com.google.testing.compile.JavaFileObjects;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class TestHelper {

    /**
     * テキストファイルで用意するやつのクラス名
     */
    protected final String testClassName;

    /**
     * test/resources以降のパス
     */
    protected final String resourcesRoot;

    /**
     * 自動生成されるProviderクラス名
     */
    protected final String genProviderName;

    /**
     * 自動生成されるPreferenceクラス名
     */
    protected final String genPreferenceName;

    public TestHelper(String testClassName, String resourcesRoot, String genProviderName, String genPreferenceName) {
        this.testClassName = testClassName;
        this.resourcesRoot = resourcesRoot;
        this.genProviderName = genProviderName;
        this.genPreferenceName = genPreferenceName;
    }

    public void testProvider(String testCaseName) throws IOException, URISyntaxException {
        JavaFileObject src = getJavaFileObject(testCaseName + ".txt");
        JavaFileObject expected = getJavaFileObject(genProviderName, testCaseName + "Provider.txt");
        compare(src, expected);
    }

    public void testPreference(String testCaseName) throws IOException, URISyntaxException {
        JavaFileObject src = getJavaFileObject(testCaseName + ".txt");
        JavaFileObject expected = getJavaFileObject(genPreferenceName, testCaseName + "Preference.txt");
        compare(src, expected);
    }

    public void compare(JavaFileObject src, JavaFileObject expected) {
        assert_().about(javaSource())
                .that(src)
                .processedWith(new EasyPrefProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }

    public JavaFileObject getJavaFileObject(String fileName) throws IOException, URISyntaxException {
        return getJavaFileObject(testClassName, fileName);
    }

    public JavaFileObject getJavaFileObject(String className, String fileName) throws IOException, URISyntaxException {
        return JavaFileObjects.forSourceLines(className, getResourceString(resourcesRoot + fileName));
    }

    public List<String> getResourceString(String path) throws URISyntaxException, IOException {
        return Files.readAllLines(Paths.get(getClass().getResource(path).toURI()));
    }

}