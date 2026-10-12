package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dao.ConfigurationDao;
import com.populaire.projetguerrefroide.dao.json.BookmarkFileJson;
import com.populaire.projetguerrefroide.dao.json.BookmarkJson;
import com.populaire.projetguerrefroide.pojo.Bookmark;
import com.populaire.projetguerrefroide.configuration.Settings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ConfigurationDaoImpl implements ConfigurationDao {
    private final DslJson<Object> dslJson;
    private final String commonPath = "common/";
    private final String settingsPath = "settings/";
    private final String bookmarkJsonFile = this.commonPath + "bookmark.json";
    private final String settingsJsonFile = this.settingsPath + "settings.json";

    public ConfigurationDaoImpl() {
        this.dslJson = new DslJson<>();
    }

    private <T> T readJson(FileHandle fileHandle, Class<T> type) throws IOException {
        byte[] bytes = fileHandle.readBytes();
        return this.dslJson.deserialize(type, bytes, bytes.length);
    }

    private FileHandle getSettingsFileHandle(String filePath) {
        Gdx.files.local(this.settingsPath).mkdirs();
        return Gdx.files.local(filePath);
    }

    @Override
    public Bookmark loadBookmark() {
        Bookmark bookmark = null;
        try {
            BookmarkJson bookmarkJson = this.readJson(Gdx.files.internal(this.bookmarkJsonFile), BookmarkFileJson.class).bookmark();
            LocalDate date = LocalDate.parse(bookmarkJson.date());

            List<String> countriesId = new ObjectList<>();
            if (bookmarkJson.country() != null) {
                countriesId.addAll(bookmarkJson.country());
            }

            bookmark = new Bookmark(bookmarkJson.icon(), bookmarkJson.name(), bookmarkJson.desc(), date, countriesId);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return bookmark;
    }

    @Override
    public Settings loadSettings() {
        Settings settings;

        try {
            FileHandle fileHandle = this.getSettingsFileHandle(this.settingsJsonFile);
            if (fileHandle.exists() && fileHandle.length() > 0) {
                settings = this.readJson(fileHandle, Settings.class);
            } else {
                settings = new Settings();
                this.saveSettings(settings);
            }
        } catch(IOException ioException) {
            ioException.printStackTrace();
            settings = new Settings();
        } catch (Exception exception) {
            exception.printStackTrace();
            settings = new Settings();
        }

        return settings;
    }

    @Override
    public void saveSettings(Settings settings) {
        try {
            JsonWriter writer = this.dslJson.newWriter();
            this.dslJson.serialize(writer, settings);
            try (FileOutputStream fileOutputStream = new FileOutputStream(Gdx.files.local(this.settingsJsonFile).file())) {
                writer.toStream(fileOutputStream);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
