package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class MainMenuPanel extends UiWindow {
    private final Image frontendMainmenuBackground;
    private final Button singlePlayerButton;
    private final Label singlePlayerLabel;
    private final Button multiPlayerButton;
    private final Label multiPlayerLabel;
    private final Button settingsButton;
    private final Label settingsLabel;
    private final Button tutorialButton;
    private final Label tutorialLabel;
    private final Button creditsButton;
    private final Label creditsLabel;
    private final Button exitButton;
    private final Label exitLabel;
    private final Button nudgeButton;

    public MainMenuPanel(UiBuilder ui, MainMenu menu) {
        super(ui, 1024f, 768f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.frontendMainmenuBackground = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.FRONTEND_MAINMENU_BG, -185f, -321f, UiAnchor.CENTER);
        this.singlePlayerButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BIG_BTN, -133f, -183f, UiAnchor.CENTER);
        this.singlePlayerLabel = ui.label(this, "SINGLEPLAYER", FontsSkin.Styles.Label.JOCKEY_24_GLOW_BLUE, 400f, 250f, 200f, 200f, Align.top, UiAnchor.UPPER_LEFT, 9f, -21f);
        this.multiPlayerButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BIG_BTN, -133f, -102f, UiAnchor.CENTER);
        this.multiPlayerLabel = ui.label(this, "MULTIPLAYER", FontsSkin.Styles.Label.JOCKEY_24_GLOW_BLUE, 400f, 331f, 200f, 50f, Align.top, UiAnchor.UPPER_LEFT, 9f, -21f);
        this.settingsButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_SMALL_BTN, -133f, -26f, UiAnchor.CENTER);
        this.settingsLabel = ui.label(this, "OPTIONS", FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, 450f, 395f, 100f, 50f, Align.top, UiAnchor.UPPER_LEFT, 9f, -21f);
        this.tutorialButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_SMALL_BTN, -133f, 16f, UiAnchor.CENTER);
        this.tutorialLabel = ui.label(this, "TUTORIAL", FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, 450f, 437f, 100f, 50f, Align.top, UiAnchor.UPPER_LEFT, 9f, -21f);
        this.creditsButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_SMALL_BTN, -133f, 58f, UiAnchor.CENTER);
        this.creditsLabel = ui.label(this, "CREDITS", FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, 450f, 479f, 100f, 50f, Align.top, UiAnchor.UPPER_LEFT, 9f, -21f);
        this.exitButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_EXIT_BTN, -133f, 157f, UiAnchor.CENTER);
        this.exitLabel = ui.label(this, "EXIT", FontsSkin.Styles.Label.JOCKEY_24_GLOW_RED, 400f, 590f, 200f, 200f, Align.top, UiAnchor.UPPER_LEFT, 9f, -21f);
        this.nudgeButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BIG_BTN, 210f, 75f, UiAnchor.CENTER);
        ui.buttonLabel(this.nudgeButton, "Nudge", FontsSkin.Styles.Label.JOCKEY_24_GLOW_BLUE);
        this.nudgeButton.setVisible(false);
        ui.onClick(this.singlePlayerButton, menu::onSinglePlayerClicked);
        ui.onClick(this.multiPlayerButton, menu::showMultiplayerMenu);
        ui.onClick(this.settingsButton, menu::showSettings);
        ui.onClick(this.tutorialButton, menu::showTutorial);
        ui.onClick(this.creditsButton, menu::showCredits);
        ui.onClick(this.exitButton, menu::onExitClicked);
    }
}
