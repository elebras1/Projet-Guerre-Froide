package com.populaire.projetguerrefroide.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.populaire.projetguerrefroide.configuration.Settings;
import games.rednblack.miniaudio.MAAudioBuffer;
import games.rednblack.miniaudio.MAGroup;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;

import java.util.List;

public final class AudioContext implements Disposable {
    private static final String CLICK_SOUND = "sound/click.wav";
    private static final String ERROR_SOUND = "sound/error.wav";
    private static final int CHANNELS = 2;
    private static final short NO_SPATIALIZATION = MASound.Flags.MA_SOUND_FLAG_NO_SPATIALIZATION;

    private final MiniAudio miniAudio;
    private final MAGroup effectsGroup;
    private final MAGroup musicGroup;
    private final MASound click;
    private final MASound error;
    private final MusicPlayer music;

    public AudioContext(Settings settings, List<String> songs) {
        this.miniAudio = new MiniAudio();
        this.effectsGroup = this.miniAudio.createGroup(NO_SPATIALIZATION, null);
        this.musicGroup = this.miniAudio.createGroup(NO_SPATIALIZATION, null);
        this.click = this.loadEffect(CLICK_SOUND);
        this.error = this.loadEffect(ERROR_SOUND);
        this.music = new MusicPlayer(this.miniAudio, this.musicGroup, songs);
        this.applyVolumes(settings);
    }

    public void playClick() {
        this.restart(this.click);
    }

    public void playError() {
        this.restart(this.error);
    }

    public void playMenuMusic() {
        this.music.playMenu();
    }

    public void playGameMusic() {
        this.music.playGame();
    }

    public void applyVolumes(Settings settings) {
        this.miniAudio.setMasterVolume(gain(settings.getMasterVolume()));
        this.effectsGroup.setVolume(gain(settings.getEffectsVolume()));
        this.musicGroup.setVolume(gain(settings.getMusicVolume()));
    }

    public void pause() {
        this.miniAudio.stopEngine();
    }

    public void resume() {
        this.miniAudio.startEngine();
    }

    private MASound loadEffect(String path) {
        MAAudioBuffer buffer = this.miniAudio.decodeBytes(Gdx.files.internal(path).readBytes(), CHANNELS);
        MASound sound = this.miniAudio.createSound(buffer, NO_SPATIALIZATION, this.effectsGroup);
        sound.setLinkedAudioBuffer(buffer);
        return sound;
    }

    private void restart(MASound sound) {
        sound.seekTo(0f);
        sound.play();
    }

    private static float gain(short percent) {
        float volume = percent / 100f;
        return volume * volume;
    }

    @Override
    public void dispose() {
        this.music.dispose();
        this.click.dispose();
        this.error.dispose();
        this.musicGroup.dispose();
        this.effectsGroup.dispose();
        this.miniAudio.dispose();
    }
}
