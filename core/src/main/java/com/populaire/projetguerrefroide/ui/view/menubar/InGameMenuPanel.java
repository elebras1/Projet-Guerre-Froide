package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuIgSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class InGameMenuPanel extends UiWindow {
    private final Image menu;
    private final Button menuSaveButton;
    private final Label svBtnLabel;
    private final Button menuOptionsButton;
    private final Label optBtnLabel;
    private final Button menuResignButton;
    private final Label resBtnLabel;
    private final Button menuMessageButton;
    private final Label msgBtnLabel;
    private final Button menuQuitButton;
    private final Label quitBtnLabel;
    private final Button menuCloseButton;
    private final Label closeBtnLabel;

    public InGameMenuPanel(UiBuilder ui, InGameMenu menu, InGameMenuListener listener) {
        super(ui, 420f, 500f);
        Skin ingameSkin = ui.skin(Skins.UI_MAINMENU_IG);
        this.menu = ui.image(this, ingameSkin, UiMainmenuIgSkin.Drawables.MENU_BACKGROUND, 27f, -85f, UiAnchor.UPPER_LEFT);
        this.menuSaveButton = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MENU_BTN, 82f, 105f, UiAnchor.UPPER_LEFT);
        this.svBtnLabel = ui.label(this, "SAVE_GAME", FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, 82f, 120f, 265f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        this.menuOptionsButton = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MENU_BTN, 82f, 147f, UiAnchor.UPPER_LEFT);
        this.optBtnLabel = ui.label(this, "GAME_OPTIONS", FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, 82f, 162f, 265f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        this.menuResignButton = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MENU_BTN, 82f, 189f, UiAnchor.UPPER_LEFT);
        this.resBtnLabel = ui.label(this, "GAME_RESIGN", FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, 82f, 204f, 265f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        this.menuMessageButton = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MENU_BTN, 82f, 231f, UiAnchor.UPPER_LEFT);
        this.msgBtnLabel = ui.label(this, "MESSAGE_SETTINGS", FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, 82f, 246f, 265f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        this.menuQuitButton = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MENU_QUIT_BTN, 82f, 328f, UiAnchor.UPPER_LEFT);
        this.quitBtnLabel = ui.label(this, "QUIT", FontsSkin.Styles.Label.JOCKEY_24_GLOW_RED, 75f, 354f, 266f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        this.menuCloseButton = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MENU_CANCEL_BTN, 134f, 436f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.menuCloseButton, Input.Keys.ESCAPE);
        this.closeBtnLabel = ui.label(this, "CLOSE", FontsSkin.Styles.Label.JOCKEY_16_GLOW_RED, 130f, 446f, 161f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        ui.onClick(this.menuSaveButton, menu::showSave);
        ui.onClick(this.menuOptionsButton, listener::onGameOptionsClicked);
        ui.onClick(this.menuResignButton, listener::onResignClicked);
        ui.onClick(this.menuMessageButton, menu::showMessageSettings);
        ui.onClick(this.menuQuitButton, listener::onQuitClicked);
        ui.onClick(this.menuCloseButton, listener::onCloseClicked);
    }
}
