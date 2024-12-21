package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.Map;

public class MainMenuInGame extends Table {
    public MainMenuInGame(Skin skin, Skin skinFonts, Map<String, String> localisation) {
        this.setMenu(skin);
    }

    public void setMenu(Skin skin) {
        Drawable background = skin.getDrawable("menu_background");

        Button savegameButton = new Button(skin, "menu_button");
        savegameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        savegameButton.setX(55);
        savegameButton.setY(360);

        Button gameOptionsButton = new Button(skin, "menu_button");
        gameOptionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        gameOptionsButton.setX(55);
        gameOptionsButton.setY(318);

        Button resignButton = new Button(skin, "menu_button");
        resignButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        resignButton.setX(55);
        resignButton.setY(276);

        Button messageSettingsButton = new Button(skin, "menu_button");
        messageSettingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        messageSettingsButton.setX(55);
        messageSettingsButton.setY(234);

        Button quitButton = new Button(skin, "menu_button_quit");
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        quitButton.setX(55);
        quitButton.setY(103);

        Button cancelButton = new Button(skin, "menu_button_cancel");
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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

    public void setSettingsAudio(Skin skin) {
        Drawable background = skin.getDrawable("ingame_settings_audio_naked");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }

    public void setSettingsControls(Skin skin) {
        Drawable background = skin.getDrawable("ingame_settings_controls_naked");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }

    public void settingsGame(Skin skin) {
        Drawable background = skin.getDrawable("ingame_settings_game_naked");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }

    public void setSettingsVideo(Skin skin) {
        Drawable background = skin.getDrawable("ingame_settings_video_naked");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }
}
