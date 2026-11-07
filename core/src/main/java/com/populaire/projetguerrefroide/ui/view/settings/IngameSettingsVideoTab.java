package com.populaire.projetguerrefroide.ui.view.settings;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuIgSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class IngameSettingsVideoTab extends UiWindow {
    private final Image videoBackground;
    private final Label framerateLabel;
    private final Label framerateValue;
    private final Button decreaseFramerateButton;
    private final Button increaseFramerateButton;
    private final Label refreshrateLabel;
    private final Label refreshrateValue;
    private final Button decreaseRefreshrateButton;
    private final Button increaseRefreshrateButton;
    private final Label multisampleLabel;
    private final Label multisampleValue;
    private final Button decreaseMultisampleButton;
    private final Button increaseMultisampleButton;
    private final Label gammaLabel;
    private final Slider gammaSlider;
    private final Label sliderLabel;
    private final Slider counterSlider;
    private final Label counterScaleLabel;
    private final Slider counterScaleSlider;
    private final Button vsyncCheckbox;
    private final Label vsyncLabel;
    private final Button fullscreenCheckbox;
    private final Label fullscreenLabel;
    private final Button unitNameCheckbox;
    private final Label unitNameLabel;
    private final Button treesCheckbox;
    private final Label treesLabel;
    private final Button waterCheckbox;
    private final Label waterLabel;
    private Settings settings;

    public IngameSettingsVideoTab(UiBuilder ui) {
        super(ui, 370f, 598f);
        Skin ingameSkin = ui.skin(Skins.UI_MAINMENU_IG);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        this.videoBackground = ui.image(this, ingameSkin, UiMainmenuIgSkin.Drawables.INGAME_SETTINGS_VIDEO_NAKED, -19f, 0f, UiAnchor.UPPER_LEFT);
        this.framerateLabel = ui.label(this, "FRAMERATE", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 61f, 108f, 127f, 15f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.framerateValue = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_18_YELLOW, 187f, 107f, 106f, 15f, Align.top, UiAnchor.UPPER_LEFT);
        this.decreaseFramerateButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_LEFT_BTN, 179f, 99f, UiAnchor.UPPER_LEFT);
        this.increaseFramerateButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_RIGHT_BTN, 287f, 99f, UiAnchor.UPPER_LEFT);
        this.refreshrateLabel = ui.label(this, "REFRESH_RATE", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 61f, 140f, 127f, 15f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.refreshrateValue = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_18_YELLOW, 187f, 139f, 106f, 15f, Align.top, UiAnchor.UPPER_LEFT);
        this.decreaseRefreshrateButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_LEFT_BTN, 179f, 131f, UiAnchor.UPPER_LEFT);
        this.increaseRefreshrateButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_RIGHT_BTN, 287f, 131f, UiAnchor.UPPER_LEFT);
        this.multisampleLabel = ui.label(this, "SM_MULTISAMPLE", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 61f, 172f, 127f, 15f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.multisampleValue = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_18_YELLOW, 187f, 171f, 106f, 15f, Align.top, UiAnchor.UPPER_LEFT);
        this.decreaseMultisampleButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_LEFT_BTN, 179f, 163f, UiAnchor.UPPER_LEFT);
        this.increaseMultisampleButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_RIGHT_BTN, 287f, 163f, UiAnchor.UPPER_LEFT);
        this.gammaLabel = ui.label(this, "SM_GAMMA", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 31f, 211f, 306f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.gammaSlider = ui.slider(this, 33f, 239f, 305f, 12f, UiAnchor.UPPER_LEFT, 0f, 100f, 1f, 20f);
        this.sliderLabel = ui.label(this, "SM_COUNTER_SLIDER", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 31f, 272f, 306f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.counterSlider = ui.slider(this, 33f, 300f, 305f, 12f, UiAnchor.UPPER_LEFT, 0f, 100f, 1f, 20f);
        this.counterScaleLabel = ui.label(this, "SM_COUNTER_SCALE", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 31f, 333f, 306f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.counterScaleSlider = ui.slider(this, 33f, 361f, 305f, 12f, UiAnchor.UPPER_LEFT, 0f, 2f, 0.1f, 1f);
        this.vsyncCheckbox = ui.button(this, uiSkin, UiSkin.Styles.Button.CHECKBUTTON_TGL, 283f, 399f, UiAnchor.UPPER_LEFT);
        this.vsyncLabel = ui.label(this, "VSync", FontsSkin.Styles.Label.JOCKEY_14_DARK, 55f, 402f, 170f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.fullscreenCheckbox = ui.button(this, uiSkin, UiSkin.Styles.Button.CHECKBUTTON_TGL, 283f, 431f, UiAnchor.UPPER_LEFT);
        this.fullscreenLabel = ui.label(this, "FULLSCREEN", FontsSkin.Styles.Label.JOCKEY_14_DARK, 55f, 434f, 170f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.unitNameCheckbox = ui.button(this, uiSkin, UiSkin.Styles.Button.CHECKBUTTON_TGL, 283f, 463f, UiAnchor.UPPER_LEFT);
        this.unitNameLabel = ui.label(this, "SM_UNIT_NAMES", FontsSkin.Styles.Label.JOCKEY_14_DARK, 55f, 466f, 170f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.treesCheckbox = ui.button(this, uiSkin, UiSkin.Styles.Button.CHECKBUTTON_TGL, 153f, 495f, UiAnchor.UPPER_LEFT);
        this.treesLabel = ui.label(this, "SM_TREES", FontsSkin.Styles.Label.JOCKEY_14_DARK, 55f, 498f, 170f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.waterCheckbox = ui.button(this, uiSkin, UiSkin.Styles.Button.CHECKBUTTON_TGL, 283f, 495f, UiAnchor.UPPER_LEFT);
        this.waterLabel = ui.label(this, "SM_WATER", FontsSkin.Styles.Label.JOCKEY_14_DARK, 187f, 498f, 170f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.refreshrateLabel.setVisible(false);
        this.refreshrateValue.setVisible(false);
        this.decreaseRefreshrateButton.setVisible(false);
        this.increaseRefreshrateButton.setVisible(false);
        this.multisampleLabel.setVisible(false);
        this.multisampleValue.setVisible(false);
        this.decreaseMultisampleButton.setVisible(false);
        this.increaseMultisampleButton.setVisible(false);
        this.gammaLabel.setVisible(false);
        this.gammaSlider.setVisible(false);
        this.sliderLabel.setVisible(false);
        this.counterSlider.setVisible(false);
        this.counterScaleLabel.setVisible(false);
        this.counterScaleSlider.setVisible(false);
        this.unitNameCheckbox.setVisible(false);
        this.unitNameLabel.setVisible(false);
        this.treesCheckbox.setVisible(false);
        this.treesLabel.setVisible(false);
        this.waterCheckbox.setVisible(false);
        this.waterLabel.setVisible(false);
        ui.onClick(this.decreaseFramerateButton, () -> this.stepFramerate(-1));
        ui.onClick(this.increaseFramerateButton, () -> this.stepFramerate(1));
        ui.onClick(this.vsyncCheckbox, () -> this.settings.setVsync(this.vsyncCheckbox.isChecked()));
        ui.onClick(this.fullscreenCheckbox, () -> this.settings.setFullscreen(this.fullscreenCheckbox.isChecked()));
    }

    public void bind(Settings settings) {
        this.settings = settings;
        this.framerateValue.setText(String.valueOf(settings.getCapFrameRate()));
        this.vsyncCheckbox.setChecked(settings.isVsync());
        this.fullscreenCheckbox.setChecked(settings.isFullscreen());
    }

    private void stepFramerate(int step) {
        this.settings.setCapFrameRate(Settings.FRAMERATES[SettingsTabs.step(Settings.FRAMERATES, this.settings.getCapFrameRate(), step)]);
        this.framerateValue.setText(String.valueOf(this.settings.getCapFrameRate()));
    }
}
