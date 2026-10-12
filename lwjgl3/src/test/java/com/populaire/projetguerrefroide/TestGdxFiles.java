package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

public final class TestGdxFiles {
    private TestGdxFiles() {
    }

    public static void install() {
        GdxNativesLoader.load();

        Gdx.files = new Files() {
            @Override
            public FileHandle getFileHandle(String path, FileType type) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle classpath(String path) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle internal(String path) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle external(String path) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle absolute(String path) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle local(String path) {
                return new FileHandle(path);
            }

            @Override
            public String getExternalStoragePath() {
                return System.getProperty("user.home") + File.separator;
            }

            @Override
            public boolean isExternalStorageAvailable() {
                return true;
            }

            @Override
            public String getLocalStoragePath() {
                return new File("").getAbsolutePath() + File.separator;
            }

            @Override
            public boolean isLocalStorageAvailable() {
                return true;
            }
        };

        if (!Gdx.files.internal("common/bookmark.json").exists()) {
            fail("Assets not found. Run the test through Gradle (`./gradlew :lwjgl3:test`) "
                + "so the working directory points to the 'assets' folder.");
        }
    }
}
