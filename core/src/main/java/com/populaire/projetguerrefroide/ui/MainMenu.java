package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.screen.MainMenuListener;

import java.util.Map;

public class MainMenu extends Table {

    public MainMenu(Skin skin, Skin skinFonts, Map<String, String> localisation, MainMenuListener listener) {
        Label.LabelStyle labelStyleJockey24GlowBlue = new Label.LabelStyle();
        labelStyleJockey24GlowBlue.font = skinFonts.getFont("jockey_24_glow_blue");

        Label.LabelStyle labelStyleJockey24GlowRed = new Label.LabelStyle();
        labelStyleJockey24GlowRed.font = skinFonts.getFont("Jockey_24_glow_red");

        Label.LabelStyle labelStyleJockey20GlowBlue = new Label.LabelStyle();
        labelStyleJockey20GlowBlue.font = skinFonts.getFont("Jockey_20_glow_blue");

        Drawable background = skin.getDrawable("frontend_mainmenu_bg");

        Button playButton = new Button(skin, "frontend_big");
        playButton.add(new Label(localisation.get("SINGLEPLAYER"), labelStyleJockey24GlowBlue)).padBottom(5);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSinglePlayerClicked();
            }
        });
        playButton.setX(52);
        playButton.setY(375);

        Button multiplayerButton = new Button(skin, "frontend_big");
        multiplayerButton.add(new Label(localisation.get("MULTIPLAYER"), labelStyleJockey24GlowBlue)).padBottom(5);
        multiplayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onMultiplayerClicked();
            }
        });
        multiplayerButton.setX(52);
        multiplayerButton.setY(295);

        Button optionsButton = new Button(skin, "frontend_small");
        optionsButton.add(new Label(localisation.get("OPTIONS"), labelStyleJockey20GlowBlue)).padBottom(5);
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        optionsButton.setX(52);
        optionsButton.setY(252);

        Button tutorialButton = new Button(skin, "frontend_small");
        tutorialButton.add(new Label(localisation.get("TUTORIAL"), labelStyleJockey20GlowBlue)).padBottom(5);
        tutorialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        tutorialButton.setX(52);
        tutorialButton.setY(210);

        Button creditsButton = new Button(skin, "frontend_small");
        creditsButton.add(new Label(localisation.get("CREDITS"), labelStyleJockey20GlowBlue)).padBottom(5);
        creditsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });
        creditsButton.setX(52);
        creditsButton.setY(168);

        Button exitButton = new Button(skin, "frontend_big_exit");
        exitButton.add(new Label(localisation.get("EXIT"), labelStyleJockey24GlowRed)).padBottom(5);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onExitClicked();
            }
        });
        exitButton.setX(52);
        exitButton.setY(35);

        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.addActor(playButton);
        this.addActor(multiplayerButton);
        this.addActor(optionsButton);
        this.addActor(tutorialButton);
        this.addActor(creditsButton);
        this.addActor(exitButton);
    }
}
