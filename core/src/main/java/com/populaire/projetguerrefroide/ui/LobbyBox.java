package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.Map;

public class LobbyBox extends Table {

    public LobbyBox(Skin skin, Skin skinScrollbars, Skin skinFonts, Map<String, String> localisation) {
        Drawable background = skin.getDrawable("bottom_bg");

        Label.LabelStyle labelStyleJockey24GlowBlue = new Label.LabelStyle();
        labelStyleJockey24GlowBlue.font = skinFonts.getFont("jockey_24_glow_blue");
        Label.LabelStyle labelStyleJockey16GlowBlue = new Label.LabelStyle();
        labelStyleJockey16GlowBlue.font = skinFonts.getFont("Jockey_16_glow_blue");
        Label.LabelStyle labelStyleArial172 = new Label.LabelStyle();
        labelStyleArial172.font = skinFonts.getFont("Arial_17_2");

        Button playButton = new Button(skin, "play");
        playButton.add(new Label(localisation.get("PLAY"), labelStyleJockey24GlowBlue));
        Button backButton = new Button(skin, "gen");
        backButton.add(new Label(localisation.get("BACK"), labelStyleArial172));

        Table buttonsTable = new Table();
        buttonsTable.add(playButton).expand().padRight(37).padBottom(10);
        buttonsTable.row();
        buttonsTable.add(backButton).padRight(32).padBottom(20);

        Label introLabel = new Label(localisation.get("INTRODUCTION"), labelStyleJockey16GlowBlue);
        introLabel.setWrap(true);
        introLabel.setSize(722, 120);
        HoverScrollPane scrollPane = new HoverScrollPane(introLabel, skinScrollbars);
        Table scrollTable = new Table();
        scrollTable.add(scrollPane).width(722).height(120);

        this.add(scrollTable).width(793);
        this.add(buttonsTable).expand().fill();
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

    }
}
