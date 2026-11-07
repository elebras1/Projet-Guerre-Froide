package com.populaire.projetguerrefroide.ui.view.settings;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class SettingsGameTab extends UiWindow {
    private final Image gameBackground;
    private final Label languageLabel;
    private final Label languageValue;
    private final Button decreaseLanguageButton;
    private final Button increaseLanguageButton;
    private final Label debugModeLabel;
    private final Button debugModeCheckbox;
    private Settings settings;

    public SettingsGameTab(UiBuilder ui) {
        super(ui, 370f, 598f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        this.gameBackground = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.FRONTEND_SETTINGS_GAME_BG, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.languageLabel = ui.label(this, "LANGUAGE", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 61f, 141f, 127f, 15f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.languageValue = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_18_YELLOW, 186f, 139f, 106f, 15f, Align.top, UiAnchor.UPPER_LEFT);
        this.decreaseLanguageButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_LEFT_BTN, 179f, 131f, UiAnchor.UPPER_LEFT);
        this.increaseLanguageButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_RIGHT_BTN, 287f, 131f, UiAnchor.UPPER_LEFT);
        this.debugModeLabel = ui.label(this, "DEBUG_MODE", FontsSkin.Styles.Label.JOCKEY_14_DARK, 55f, 486f, 170f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.debugModeCheckbox = ui.button(this, uiSkin, UiSkin.Styles.Button.CHECKBUTTON_TGL, 283f, 483f, UiAnchor.UPPER_LEFT);
        ui.onClick(this.decreaseLanguageButton, () -> this.stepLanguage(-1));
        ui.onClick(this.increaseLanguageButton, () -> this.stepLanguage(1));
        ui.onClick(this.debugModeCheckbox, () -> this.settings.setDebugMode(this.debugModeCheckbox.isChecked()));
    }

    public void bind(Settings settings) {
        this.settings = settings;
        this.languageValue.setText(this.ui.localise(settings.getLanguage()));
        this.debugModeCheckbox.setChecked(settings.isDebugMode());
    }

    private void stepLanguage(int step) {
        this.settings.setLanguage(Settings.LANGUAGES[SettingsTabs.step(Settings.LANGUAGES, this.settings.getLanguage(), step)]);
        this.languageValue.setText(this.ui.localise(this.settings.getLanguage()));
    }
}
