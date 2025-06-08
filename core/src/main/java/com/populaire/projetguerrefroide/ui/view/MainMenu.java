package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.screen.MainMenuListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class MainMenu extends Table {

    public MainMenu(WidgetFactory widgetFactory, Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, MainMenuListener listener) {
        Label.LabelStyle labelStyleJockey24GlowBlue = labelStylePool.getLabelStyle("jockey_24_glow_blue");
        Label.LabelStyle labelStyleJockey24GlowRed = labelStylePool.getLabelStyle("jockey_24_glow_red");
        Label.LabelStyle labelStyleJockey20GlowBlue = labelStylePool.getLabelStyle("jockey_20_glow_blue");

        widgetFactory.applyBackgroundToTable(skin, "frontend_mainmenu_bg", this);

        Button playButton = new Button(skin, "frontend_big");
        playButton.add(new Label(localisation.get("SINGLEPLAYER"), labelStyleJockey24GlowBlue)).padBottom(5);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSinglePlayerClicked();
            }
        });
        playButton.setPosition(52, 375);
        this.addActor(playButton);

        Button multiplayerButton = new Button(skin, "frontend_big");
        multiplayerButton.add(new Label(localisation.get("MULTIPLAYER"), labelStyleJockey24GlowBlue)).padBottom(5);
        multiplayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onMultiplayerClicked();
            }
        });
        multiplayerButton.setPosition(52, 295);
        this.addActor(multiplayerButton);

        Button optionsButton = new Button(skin, "frontend_small");
        optionsButton.add(new Label(localisation.get("OPTIONS"), labelStyleJockey20GlowBlue)).padBottom(5);
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        optionsButton.setPosition(52, 252);
        this.addActor(optionsButton);

        Button tutorialButton = new Button(skin, "frontend_small");
        tutorialButton.add(new Label(localisation.get("TUTORIAL"), labelStyleJockey20GlowBlue)).padBottom(5);
        tutorialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        tutorialButton.setPosition(52, 210);
        this.addActor(tutorialButton);

        Button creditsButton = new Button(skin, "frontend_small");
        creditsButton.add(new Label(localisation.get("CREDITS"), labelStyleJockey20GlowBlue)).padBottom(5);
        creditsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        creditsButton.setPosition(52, 168);
        this.addActor(creditsButton);

        Button exitButton = new Button(skin, "frontend_big_exit");
        exitButton.add(new Label(localisation.get("EXIT"), labelStyleJockey24GlowRed)).padBottom(5);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onExitClicked();
            }
        });
        exitButton.setPosition(52, 35);
        this.addActor(exitButton);
    }
}
