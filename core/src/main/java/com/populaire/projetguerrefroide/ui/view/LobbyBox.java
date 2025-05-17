package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.screen.LobbyBoxListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.HoverScrollPane;

import java.util.Map;

public class LobbyBox extends Table {

    public LobbyBox(Skin skin, Skin skinScrollbars, LabelStylePool labelStylePool, Map<String, String> localisation, LobbyBoxListener listener) {
        Drawable background = skin.getDrawable("bottom_bg");

        Label.LabelStyle labelStyleJockey24GlowBlue = labelStylePool.getLabelStyle("jockey_24_glow_blue");
        Label.LabelStyle labelStyleJockey16GlowBlue = labelStylePool.getLabelStyle("jockey_16_glow_blue");
        Label.LabelStyle labelStyleArial172 = labelStylePool.getLabelStyle("arial_17_2");

        Button playButton = new Button(skin, "play");
        playButton.add(new Label(localisation.get("PLAY"), labelStyleJockey24GlowBlue));
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onPlayClicked();
            }
        });
        Button backButton = new Button(skin, "gen");
        backButton.add(new Label(localisation.get("BACK"), labelStyleArial172));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onBackClicked();
            }
        });

        Table buttonsTable = new Table();
        buttonsTable.add(playButton).expand().padRight(37).padBottom(10);
        buttonsTable.row();
        buttonsTable.add(backButton).padRight(32).padBottom(20);

        Label introLabel = new Label(localisation.get("INTRODUCTION"), labelStyleJockey16GlowBlue);
        introLabel.setWrap(true);
        introLabel.setSize(735, 120);
        HoverScrollPane scrollPane = new HoverScrollPane(introLabel, skinScrollbars, "default");
        Table scrollTable = new Table();
        scrollTable.add(scrollPane).width(735).height(120);

        this.add(scrollTable).width(793);
        this.add(buttonsTable).expand().fill();
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

    }
}
