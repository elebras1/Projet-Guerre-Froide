package com.populaire.projetguerrefroide.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.ObjectList;
import games.rednblack.miniaudio.MAGroup;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class MusicPlayer implements Disposable {
    private static final String DIRECTORY = "music";
    private static final String MENU_TRACK = "ww3.mp3";
    private static final short STREAM = MASound.Flags.MA_SOUND_FLAG_STREAM | MASound.Flags.MA_SOUND_FLAG_NO_SPATIALIZATION;

    private final MiniAudio miniAudio;
    private final MAGroup group;
    private final Path directory;
    private final MASound menu;
    private final ObjectList<String> playlist;
    private final Runnable nextTrack = this::nextTrack;
    private MASound track;
    private int position;

    MusicPlayer(MiniAudio miniAudio, MAGroup group, List<String> songs) {
        this.miniAudio = miniAudio;
        this.group = group;
        this.directory = musicDirectory();
        this.menu = this.open(MENU_TRACK);
        this.track = this.menu;
        this.playlist = new ObjectList<>(songs);
        this.playlist.remove(MENU_TRACK);
        this.position = this.playlist.size();
        miniAudio.setEndListener(sound -> Gdx.app.postRunnable(this.nextTrack));
    }

    void playMenu() {
        this.releaseTrack();
        this.menu.loop();
    }

    void playGame() {
        this.menu.stop();
        this.releaseTrack();
        this.track = this.open(this.nextFile());
        this.track.play();
    }

    private void nextTrack() {
        if (this.track.isEnd()) {
            this.playGame();
        }
    }

    private String nextFile() {
        if (this.position == this.playlist.size()) {
            this.playlist.shuffle(MathUtils.random);
            this.position = 0;
        }
        return this.playlist.get(this.position++);
    }

    private void releaseTrack() {
        if (this.track != this.menu) {
            this.track.dispose();
            this.track = this.menu;
        }
    }

    private MASound open(String file) {
        return this.miniAudio.createSound(this.directory.resolve(file).toString(), STREAM, this.group);
    }

    private static Path musicDirectory() {
        try {
            Path application = Path.of(MusicPlayer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path packaged = application.resolveSibling(DIRECTORY);
            return Files.isDirectory(packaged) ? packaged : Path.of(DIRECTORY);
        } catch (URISyntaxException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public void dispose() {
        this.releaseTrack();
        this.menu.dispose();
    }
}
