package com.populaire.projetguerrefroide.ui.view.settings;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class SettingsWindow extends UiWindow {
    private final SettingsAudioTab menuSettingsAUDIO;
    private final SettingsControlsTab menuSettingsCONTROLS;
    private final SettingsVideoTab menuSettingsVIDEO;
    private final SettingsGameTab menuSettingsGAME;
    private final Label settingsLabel;
    private final Button settingsTab1;
    private final Button settingsTab2;
    private final Button settingsTab3;
    private final Button settingsTab4;
    private final Button applyButton;
    private final Label applyLabel;
    private final Button backButton;
    private final Label backLabel;
    private Settings settings;

    public SettingsWindow(UiBuilder ui, Runnable onApply, Runnable onBack) {
        super(ui, 557f, 598f);
        Skin uiSkin = ui.skin(Skins.UI);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        this.menuSettingsAUDIO = ui.add(this, new SettingsAudioTab(ui), 0f, 0f, UiAnchor.UPPER_LEFT);
        this.menuSettingsAUDIO.setVisible(false);
        this.menuSettingsCONTROLS = ui.add(this, new SettingsControlsTab(ui), 0f, 0f, UiAnchor.UPPER_LEFT);
        this.menuSettingsCONTROLS.setVisible(false);
        this.menuSettingsVIDEO = ui.add(this, new SettingsVideoTab(ui), 0f, 0f, UiAnchor.UPPER_LEFT);
        this.menuSettingsVIDEO.setVisible(false);
        this.menuSettingsGAME = ui.add(this, new SettingsGameTab(ui), 0f, 0f, UiAnchor.UPPER_LEFT);
        this.menuSettingsGAME.setVisible(false);
        this.settingsLabel = ui.label(this, "SETTINGS", FontsSkin.Styles.Label.JOCKEY_24, 30f, 34f, 310f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.settingsTab1 = ui.button(this, uiSkin, UiSkin.Styles.Button.SETTINGS_TAB_1_BTN, 28f, 65f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.settingsTab1, "GAME", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.settingsTab2 = ui.button(this, uiSkin, UiSkin.Styles.Button.SETTINGS_TAB_1_BTN, 106f, 65f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.settingsTab2, "VIDEO", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.settingsTab3 = ui.button(this, uiSkin, UiSkin.Styles.Button.SETTINGS_TAB_1_BTN, 184f, 65f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.settingsTab3, "AUDIO", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.settingsTab4 = ui.button(this, uiSkin, UiSkin.Styles.Button.SETTINGS_TAB_1_BTN, 262f, 65f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.settingsTab4, "CONTROLS", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.applyButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.GEN_BTN, 405f, 481f, UiAnchor.UPPER_LEFT);
        this.applyLabel = ui.label(this, "APPLY", FontsSkin.Styles.Label.JOCKEY_16_DARK, 320f, 490f, 310f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.backButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.GEN_BTN, 405f, 522f, UiAnchor.UPPER_LEFT);
        this.backLabel = ui.label(this, "BACK", FontsSkin.Styles.Label.JOCKEY_16_DARK, 320f, 531f, 310f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        ui.onClick(this.settingsTab1, () -> this.showTab(this.menuSettingsGAME));
        ui.onClick(this.settingsTab2, () -> this.showTab(this.menuSettingsVIDEO));
        ui.onClick(this.settingsTab3, () -> this.showTab(this.menuSettingsAUDIO));
        ui.onClick(this.settingsTab4, () -> this.showTab(this.menuSettingsCONTROLS));
        ui.onClick(this.applyButton, onApply);
        ui.onClick(this.backButton, onBack);
        this.showTab(this.menuSettingsGAME);
    }

    public void show(Settings settings) {
        this.settings = settings;
        this.menuSettingsGAME.bind(settings);
        this.menuSettingsVIDEO.bind(settings);
        this.menuSettingsAUDIO.bind(settings);
        this.showTab(this.menuSettingsGAME);
    }

    public Settings getSettings() {
        return this.settings;
    }

    private void showTab(UiWindow tab) {
        this.menuSettingsGAME.setVisible(tab == this.menuSettingsGAME);
        this.menuSettingsVIDEO.setVisible(tab == this.menuSettingsVIDEO);
        this.menuSettingsAUDIO.setVisible(tab == this.menuSettingsAUDIO);
        this.menuSettingsCONTROLS.setVisible(tab == this.menuSettingsCONTROLS);
    }
}
