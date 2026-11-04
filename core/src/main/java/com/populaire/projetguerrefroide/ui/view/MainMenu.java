package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.populaire.projetguerrefroide.screen.listener.MainMenuListener;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class MainMenu extends Table {

    public MainMenu(WidgetFactory widgetFactory, Skin skin, Skin skinFonts, Map<String, String> localisation, MainMenuListener listener) {
        this.setMenu(widgetFactory, skin, skinFonts, localisation, listener);
    }

    public void setMenu(WidgetFactory widgetFactory, Skin skin, Skin skinFonts, Map<String, String> localisation, MainMenuListener listener) {
        Label.LabelStyle labelStyleJockey24GlowBlue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_24_GLOW_BLUE, Label.LabelStyle.class);
        Label.LabelStyle labelStyleJockey24GlowRed = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_24_GLOW_RED, Label.LabelStyle.class);
        Label.LabelStyle labelStyleJockey20GlowBlue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, Label.LabelStyle.class);

        widgetFactory.applyBackgroundToTable(skin, UiMainmenuSkin.Drawables.FRONTEND_MAINMENU_BG, this);

        Button playButton = widgetFactory.createButton(skin, UiMainmenuSkin.Styles.Button.FRONTEND_BIG_BTN, 52, 375, this);
        playButton.add(new Label(localisation.get("SINGLEPLAYER"), labelStyleJockey24GlowBlue)).padBottom(5);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSinglePlayerClicked();
            }
        });

        Button multiplayerButton = widgetFactory.createButton(skin, UiMainmenuSkin.Styles.Button.FRONTEND_BIG_BTN, 52, 295, this);
        multiplayerButton.add(new Label(localisation.get("MULTIPLAYER"), labelStyleJockey24GlowBlue)).padBottom(5);
        multiplayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onMultiplayerClicked();
            }
        });

        Button optionsButton = widgetFactory.createButton(skin, UiMainmenuSkin.Styles.Button.FRONTEND_SMALL_BTN, 52, 252, this);
        optionsButton.add(new Label(localisation.get("OPTIONS"), labelStyleJockey20GlowBlue)).padBottom(5);
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button tutorialButton = widgetFactory.createButton(skin, UiMainmenuSkin.Styles.Button.FRONTEND_SMALL_BTN, 52, 210, this);
        tutorialButton.add(new Label(localisation.get("TUTORIAL"), labelStyleJockey20GlowBlue)).padBottom(5);
        tutorialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button creditsButton = widgetFactory.createButton(skin, UiMainmenuSkin.Styles.Button.FRONTEND_SMALL_BTN, 52, 168, this);    ;
        creditsButton.add(new Label(localisation.get("CREDITS"), labelStyleJockey20GlowBlue)).padBottom(5);
        creditsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button exitButton = widgetFactory.createButton(skin, UiMainmenuSkin.Styles.Button.FRONTEND_EXIT_BTN, 52, 35, this);
        exitButton.add(new Label(localisation.get("EXIT"), labelStyleJockey24GlowRed)).padBottom(5);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onExitClicked();
            }
        });
    }
}
