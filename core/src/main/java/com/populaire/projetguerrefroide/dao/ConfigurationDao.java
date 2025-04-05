package com.populaire.projetguerrefroide.dao;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.configuration.Bookmark;
import com.populaire.projetguerrefroide.configuration.Settings;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConfigurationDao {
    private final ObjectMapper mapper;
    private final String commonPath = "common/";
    private final String settingsPath = "settings/";
    private final String bookmarkJsonFile = this.commonPath + "bookmark.json";
    private final String settingsJsonFile = this.settingsPath + "settings.json";

    public ConfigurationDao() {
        this.mapper = new ObjectMapper();
    }

    private JsonNode openJson(String fileName) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(fileName);
        return this.mapper.readTree(fileHandle.readString());
    }

    private JsonNode openJsonSettings(String fileName) throws IOException {
        Gdx.files.local(settingsPath).mkdirs();
        FileHandle fileHandle = Gdx.files.local(fileName);

        if (!fileHandle.exists()) {
            fileHandle.writeString("{}", false);
        }

        return this.mapper.readTree(fileHandle.readString());
    }

    public Bookmark loadBookmark() {
        Bookmark bookmark = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            JsonNode rootNode = this.openJson(this.bookmarkJsonFile);
            JsonNode bookmarkNode = rootNode.get("bookmark");
            String iconNameFile = bookmarkNode.get("icon").asText();
            String nameId = bookmarkNode.get("name").asText();
            String descriptionId = bookmarkNode.get("desc").asText();
            Date date = null;
            try {
                date = dateFormat.parse(bookmarkNode.get("date").asText());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            List<String> countriesId = new ObjectList<>();
            JsonNode countriesNode = bookmarkNode.get("country");
            if (countriesNode != null && countriesNode.isArray()) {
                countriesNode.forEach(countryId -> countriesId.add(countryId.asText()));
            }

            bookmark = new Bookmark(iconNameFile, nameId, descriptionId, date, countriesId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookmark;
    }

    public Settings loadSettings() {
        Settings settings;

        try {
            JsonNode settingsNode = this.openJsonSettings(this.settingsJsonFile);
            String language = settingsNode.get("language").asText();
            short masterVolume = (short) settingsNode.get("masterVolume").asInt();
            short musicVolume = (short) settingsNode.get("musicVolume").asInt();
            short effectsVolume = (short) settingsNode.get("effectsVolume").asInt();
            boolean vsync = settingsNode.get("vsync").asBoolean();
            short capFrameRate = (short) settingsNode.get("capFrameRate").asInt();
            boolean fullscreen = settingsNode.get("fullscreen").asBoolean();
            boolean debugMode = settingsNode.get("debugMode").asBoolean();
            settings = new Settings(language, masterVolume, musicVolume, effectsVolume, vsync, capFrameRate, fullscreen, debugMode);
        } catch(IOException | NullPointerException e) {
            settings = new Settings();
            this.saveSettings(settings);
        }

        return settings;
    }

    public void saveSettings(Settings settings) {
        try {
            this.mapper.writeValue(Gdx.files.local(this.settingsJsonFile).file(), settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
