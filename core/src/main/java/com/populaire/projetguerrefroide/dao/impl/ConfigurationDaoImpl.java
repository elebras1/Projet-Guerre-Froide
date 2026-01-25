package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonMapper;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonValue;
import com.populaire.projetguerrefroide.dao.ConfigurationDao;
import com.populaire.projetguerrefroide.pojo.Bookmark;
import com.populaire.projetguerrefroide.configuration.Settings;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ConfigurationDaoImpl implements ConfigurationDao {
    private final JsonMapper parser;
    private final String commonPath = "common/";
    private final String settingsPath = "settings/";
    private final String bookmarkJsonFile = this.commonPath + "bookmark.json";
    private final String settingsJsonFile = this.settingsPath + "settings.json";

    public ConfigurationDaoImpl() {
        this.parser = new JsonMapper();
    }

    private JsonValue parseJsonFile(String filePath) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(filePath);
        return this.parser.parse(fileHandle.readBytes());
    }

    private FileHandle getSettingsFileHandle(String filePath) {
        Gdx.files.local(this.settingsPath).mkdirs();
        return Gdx.files.local(filePath);
    }

    @Override
    public Bookmark loadBookmark() {
        Bookmark bookmark = null;
        try {
            JsonValue bookmarkValues = this.parseJsonFile(this.bookmarkJsonFile);
            JsonValue bookmarkValue = bookmarkValues.get("bookmark");
            String iconNameFile = bookmarkValue.get("icon").asString();
            String nameId = bookmarkValue.get("name").asString();
            String descriptionId = bookmarkValue.get("desc").asString();
            LocalDate date = LocalDate.parse(bookmarkValue.get("date").asString());

            List<String> countriesId = new ObjectList<>();
            JsonValue countriesNode = bookmarkValue.get("country");
            if (countriesNode != null) {
                for(JsonValue countryId : countriesNode.array()) {
                    countriesId.add(countryId.asString());
                }
            }

            bookmark = new Bookmark(iconNameFile, nameId, descriptionId, date, countriesId);
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
                byte[] buffer = fileHandle.readBytes();
                settings = this.parser.readValue(buffer, Settings.class);
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
            this.parser.writeValue(settings, Gdx.files.local(this.settingsJsonFile).file());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
