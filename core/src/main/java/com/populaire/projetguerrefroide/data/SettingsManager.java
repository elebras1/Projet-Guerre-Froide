package com.populaire.projetguerrefroide.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.populaire.projetguerrefroide.service.Settings;

import java.io.IOException;

public class SettingsManager {
    private final ObjectMapper mapper;
    private final String settingsPath = "settings/";
    private final String settingsJsonFile = this.settingsPath + "settings.json";

    public SettingsManager() {
        this.mapper = new ObjectMapper();
        System.out.println(Gdx.files.local(this.settingsJsonFile).file().getAbsolutePath());
    }

    private JsonNode openJson(String fileName) throws IOException {
        Gdx.files.local(settingsPath).mkdirs();
        FileHandle fileHandle = Gdx.files.local(fileName);

        if (!fileHandle.exists()) {
            fileHandle.writeString("{}", false);
        }

        return this.mapper.readTree(fileHandle.readString());
    }

    public Settings loadSettings() {
        Settings settings;

        try {
            JsonNode settingsNode = this.openJson(this.settingsJsonFile);
            String language = settingsNode.get("language").asText();
            short musicVolume = (short) settingsNode.get("musicVolume").asInt();
            short effectsVolume = (short) settingsNode.get("effectsVolume").asInt();
            boolean vsync = settingsNode.get("vsync").asBoolean();
            short capFrameRate = (short) settingsNode.get("capFrameRate").asInt();
            boolean fullscreen = settingsNode.get("fullscreen").asBoolean();
            boolean debugMode = settingsNode.get("debugMode").asBoolean();
            settings = new Settings(language, musicVolume, effectsVolume, vsync, capFrameRate, fullscreen, debugMode);
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
