package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonMapper;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonValue;
import com.populaire.projetguerrefroide.dao.ConfigurationDao;
import com.populaire.projetguerrefroide.entity.Bookmark;
import com.populaire.projetguerrefroide.configuration.Settings;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Iterator;
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

    private JsonValue parseJsonFileSettings(String filePath) throws IOException {
        Gdx.files.local(this.settingsPath).mkdirs();
        FileHandle fileHandle = Gdx.files.local(filePath);

        if (!fileHandle.exists()) {
            fileHandle.writeString("{}", false);
        }

        return this.parser.parse(fileHandle.readBytes());
    }

    @Override
    public Bookmark loadBookmark() {
        Bookmark bookmark = null;
        try {
            JsonValue bookmarkValues = this.parseJsonFile(this.bookmarkJsonFile);
            JsonValue bookmarValue = bookmarkValues.get("bookmark");
            String iconNameFile = bookmarValue.get("icon").asString();
            String nameId = bookmarValue.get("name").asString();
            String descriptionId = bookmarValue.get("desc").asString();
            LocalDate date = LocalDate.parse(bookmarValue.get("date").asString());

            List<String> countriesId = new ObjectList<>();
            JsonValue countriesNode = bookmarValue.get("country");
            if (countriesNode != null && countriesNode.isArray()) {
                Iterator<JsonValue> iterator = countriesNode.arrayIterator();
                while (iterator.hasNext()) {
                    JsonValue countryId = iterator.next();
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
            JsonValue settingsValues = this.parseJsonFileSettings(this.settingsJsonFile);
            String language = settingsValues.get("language").asString();
            short masterVolume = (short) settingsValues.get("masterVolume").asLong();
            short musicVolume = (short) settingsValues.get("musicVolume").asLong();
            short effectsVolume = (short) settingsValues.get("effectsVolume").asLong();
            boolean vsync = settingsValues.get("vsync").asBoolean();
            short capFrameRate = (short) settingsValues.get("capFrameRate").asLong();
            boolean fullscreen = settingsValues.get("fullscreen").asBoolean();
            boolean debugMode = settingsValues.get("debugMode").asBoolean();
            settings = new Settings(language, masterVolume, musicVolume, effectsVolume, vsync, capFrameRate, fullscreen, debugMode);
        } catch(IOException | NullPointerException e) {
            settings = new Settings();
            this.saveSettings(settings);
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
