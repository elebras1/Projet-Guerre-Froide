package com.populaire.projetguerrefroide.ui.view.settings;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class SettingsAudioTab extends UiWindow {
    private final Image audioBackground;
    private final Label mastervolumeLabel;
    private final Slider mastervolumeSlider;
    private final Label effectvolumeLabel;
    private final Slider effectvolumeSlider;
    private final Label musicvolumeLabel;
    private final Slider musicvolumeSlider;
    private Settings settings;

    public SettingsAudioTab(UiBuilder ui) {
        super(ui, 370f, 598f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.audioBackground = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.FRONTEND_SETTINGS_AUDIO_BG, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.mastervolumeLabel = ui.label(this, "MASTER_VOLUME", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 31f, 103f, 306f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.mastervolumeSlider = ui.slider(this, 33f, 131f, 305f, 12f, UiAnchor.UPPER_LEFT, 0f, 100f, 1f, 20f);
        this.effectvolumeLabel = ui.label(this, "EFFECT_VOLUME", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 31f, 164f, 306f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.effectvolumeSlider = ui.slider(this, 33f, 192f, 305f, 12f, UiAnchor.UPPER_LEFT, 0f, 100f, 1f, 20f);
        this.musicvolumeLabel = ui.label(this, "MUSIC_VOLUME", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 31f, 225f, 306f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.musicvolumeSlider = ui.slider(this, 33f, 253f, 305f, 12f, UiAnchor.UPPER_LEFT, 0f, 100f, 1f, 20f);
        ui.onChange(this.mastervolumeSlider, () -> this.settings.setMasterVolume((short) this.mastervolumeSlider.getValue()));
        ui.onChange(this.effectvolumeSlider, () -> this.settings.setEffectsVolume((short) this.effectvolumeSlider.getValue()));
        ui.onChange(this.musicvolumeSlider, () -> this.settings.setMusicVolume((short) this.musicvolumeSlider.getValue()));
    }

    public void bind(Settings settings) {
        this.settings = settings;
        this.mastervolumeSlider.setValue(settings.getMasterVolume());
        this.effectvolumeSlider.setValue(settings.getEffectsVolume());
        this.musicvolumeSlider.setValue(settings.getMusicVolume());
    }
}
