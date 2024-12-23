package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.screen.MainMenuInGameListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class MainMenuInGame extends Table {
    public MainMenuInGame(Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, MainMenuInGameListener listener) {
        this.setMenu(skin, labelStylePool, localisation, listener);
    }

    private void setMenu(Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, MainMenuInGameListener listener) {
        Drawable background = skin.getDrawable("menu_background");

        LabelStyle labelStyleJockey20GlowBlue = labelStylePool.getLabelStyle("jockey_20_glow_blue");
        LabelStyle labelStyleJockey16GlowRed = labelStylePool.getLabelStyle("jockey_16_glow_red");
        LabelStyle labelStyleJockey24GlowRed = labelStylePool.getLabelStyle("jockey_24_glow_red");

        Button savegameButton = new Button(skin, "menu_button");
        savegameButton.add(new Label(localisation.get("SAVE_GAME"), labelStyleJockey20GlowBlue)).padBottom(5);
        savegameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        savegameButton.setX(55);
        savegameButton.setY(360);

        Button gameOptionsButton = new Button(skin, "menu_button");
        gameOptionsButton.add(new Label(localisation.get("GAME_OPTIONS"), labelStyleJockey20GlowBlue)).padBottom(5);
        gameOptionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        gameOptionsButton.setX(55);
        gameOptionsButton.setY(318);

        Button resignButton = new Button(skin, "menu_button");
        resignButton.add(new Label(localisation.get("GAME_RESIGN"), labelStyleJockey20GlowBlue)).padBottom(5);
        resignButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        resignButton.setX(55);
        resignButton.setY(276);

        Button messageSettingsButton = new Button(skin, "menu_button");
        messageSettingsButton.add(new Label(localisation.get("MESSAGE_SETTINGS"), labelStyleJockey20GlowBlue)).padBottom(5);
        messageSettingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        messageSettingsButton.setX(55);
        messageSettingsButton.setY(234);

        Button quitButton = new Button(skin, "menu_button_quit");
        quitButton.add(new Label(localisation.get("QUIT"), labelStyleJockey24GlowRed)).padBottom(5);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        quitButton.setX(55);
        quitButton.setY(103);

        Button cancelButton = new Button(skin, "menu_button_cancel");
        cancelButton.add(new Label(localisation.get("CLOSE"), labelStyleJockey16GlowRed)).padBottom(10);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onCloseClicked();
            }
        });
        cancelButton.setX(107);
        cancelButton.setY(39);

        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.addActor(savegameButton);
        this.addActor(gameOptionsButton);
        this.addActor(resignButton);
        this.addActor(messageSettingsButton);
        this.addActor(quitButton);
        this.addActor(cancelButton);
    }

    private void setSettingsAudio(Skin skin) {
        Drawable background = skin.getDrawable("ingame_settings_audio_naked");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }

    private void setSettingsControls(Skin skin) {
        Drawable background = skin.getDrawable("ingame_settings_controls_naked");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }

    private void settingsGame(Skin skin) {
        Drawable background = skin.getDrawable("ingame_settings_game_naked");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }

    private void setSettingsVideo(Skin skin) {
        Drawable background = skin.getDrawable("ingame_settings_video_naked");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }
}
