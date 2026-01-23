package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.populaire.projetguerrefroide.screen.MainMenuListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class MainMenu extends Table {

    public MainMenu(WidgetFactory widgetFactory, Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, MainMenuListener listener) {
        this.setMenu(widgetFactory, skin, labelStylePool, localisation, listener);
    }

    public void setMenu(WidgetFactory widgetFactory, Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, MainMenuListener listener) {
        Label.LabelStyle labelStyleJockey24GlowBlue = labelStylePool.get("jockey_24_glow_blue");
        Label.LabelStyle labelStyleJockey24GlowRed = labelStylePool.get("jockey_24_glow_red");
        Label.LabelStyle labelStyleJockey20GlowBlue = labelStylePool.get("jockey_20_glow_blue");

        widgetFactory.applyBackgroundToTable(skin, "frontend_mainmenu_bg", this);

        Button playButton = widgetFactory.createButton(skin, "frontend_big", 52, 375, this);
        playButton.add(new Label(localisation.get("SINGLEPLAYER"), labelStyleJockey24GlowBlue)).padBottom(5);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSinglePlayerClicked();
            }
        });

        Button multiplayerButton = widgetFactory.createButton(skin, "frontend_big", 52, 295, this);
        multiplayerButton.add(new Label(localisation.get("MULTIPLAYER"), labelStyleJockey24GlowBlue)).padBottom(5);
        multiplayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onMultiplayerClicked();
            }
        });

        Button optionsButton = widgetFactory.createButton(skin, "frontend_small", 52, 252, this);
        optionsButton.add(new Label(localisation.get("OPTIONS"), labelStyleJockey20GlowBlue)).padBottom(5);
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button tutorialButton = widgetFactory.createButton(skin, "frontend_small", 52, 210, this);
        tutorialButton.add(new Label(localisation.get("TUTORIAL"), labelStyleJockey20GlowBlue)).padBottom(5);
        tutorialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button creditsButton = widgetFactory.createButton(skin, "frontend_small", 52, 168, this);    ;
        creditsButton.add(new Label(localisation.get("CREDITS"), labelStyleJockey20GlowBlue)).padBottom(5);
        creditsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button exitButton = widgetFactory.createButton(skin, "frontend_big_exit", 52, 35, this);
        exitButton.add(new Label(localisation.get("EXIT"), labelStyleJockey24GlowRed)).padBottom(5);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onExitClicked();
            }
        });
    }
}
