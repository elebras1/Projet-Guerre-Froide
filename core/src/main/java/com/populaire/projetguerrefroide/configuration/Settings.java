package com.populaire.projetguerrefroide.configuration;

import com.badlogic.gdx.Gdx;
import com.dslplatform.json.CompiledJson;

@CompiledJson
public class Settings implements Cloneable{
    private String language;
    private short masterVolume;
    private short musicVolume;
    private short effectsVolume;
    private boolean vsync;
    private short capFrameRate;
    private boolean fullscreen;
    private boolean debugMode;

    public Settings(String language, short masterVolume, short musicVolume, short effectsVolume, boolean vsync, short capFrameRate, boolean fullscreen, boolean debugMode) {
        this.language = language;
        this.masterVolume = masterVolume;
        this.musicVolume = musicVolume;
        this.effectsVolume = effectsVolume;
        this.vsync = vsync;
        this.capFrameRate = capFrameRate;
        this.fullscreen = fullscreen;
        this.debugMode = debugMode;
    }

    public Settings() {
        this("ENGLISH", (short) 100, (short) 100, (short) 100, true, (short) 144, true, false);
    }

    public void applyGraphicsSettings() {
        Gdx.graphics.setVSync(this.vsync);
        Gdx.graphics.setForegroundFPS(this.capFrameRate);
        if(this.fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height - 50);
        }
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public short getMasterVolume() {
        return this.masterVolume;
    }

    public void setMasterVolume(short masterVolume) {
        this.masterVolume = masterVolume;
    }

    public short getMusicVolume() {
        return this.musicVolume;
    }

    public void setMusicVolume(short musicVolume) {
        this.musicVolume = musicVolume;
    }

    public short getEffectsVolume() {
        return this.effectsVolume;
    }

    public void setEffectsVolume(short effectsVolume) {
        this.effectsVolume = effectsVolume;
    }

    public boolean isVsync() {
        return this.vsync;
    }

    public void setVsync(boolean vsync) {
        this.vsync = vsync;
    }

    public short getCapFrameRate() {
        return this.capFrameRate;
    }

    public void setCapFrameRate(short capFrameRate) {
        this.capFrameRate = capFrameRate;
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "language='" + this.language + '\'' +
                ", masterVolume=" + this.masterVolume +
                ", musicVolume=" + this.musicVolume +
                ", effectsVolume=" + this.effectsVolume +
                ", vsync=" + this.vsync +
                ", capFrameRate=" + this.capFrameRate +
                ", fullscreen=" + this.fullscreen +
                ", debugMode=" + this.debugMode +
                '}';
    }

    @Override
    public Settings clone() {
        try {
            return (Settings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
