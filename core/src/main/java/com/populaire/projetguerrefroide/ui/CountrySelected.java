package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.util.Map;

public class CountrySelected extends Table {
    private Label countryName;
    private Label government;
    private Label countryPopulation;
    private Label leaderFullName;
    private FlagImage flagImage;
    private Image portrait;

    public CountrySelected(Skin skin, Skin skinUi, Skin skinFonts, Map<String, String> localisation) {
        Drawable background = skin.getDrawable("selected_bg");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

        TextureRegion alphaFlag = skinUi.getRegion("flag_alpha");
        TextureRegion overlayFlag = skinUi.getRegion("flag_overlay");
        Pixmap defaultPixmapFlag = new Pixmap(109, 77, Pixmap.Format.RGBA8888);
        TextureRegionDrawable defaultFlag = new TextureRegionDrawable(new Texture(defaultPixmapFlag));
        defaultPixmapFlag.dispose();

        this.flagImage = new FlagImage(defaultFlag, overlayFlag, alphaFlag);
        this.flagImage.setPosition(6, 85);

        Label.LabelStyle labelStyleJockey24 = new Label.LabelStyle();
        labelStyleJockey24.font = skinFonts.getFont("jockey_24");
        Label.LabelStyle labelStyleJockey14Dark = new Label.LabelStyle();
        labelStyleJockey14Dark.font = skinFonts.getFont("jockey_14_dark");
        Label.LabelStyle labelStyleJockey14 = new Label.LabelStyle();
        labelStyleJockey14.font = skinFonts.getFont("jockey_14");
        Label.LabelStyle labelStyleJockey14Yellow = new Label.LabelStyle();
        labelStyleJockey14Yellow.font = skinFonts.getFont("jockey_14");
        labelStyleJockey14Yellow.fontColor = skinFonts.getColor("yellow");
        Label.LabelStyle labelStyleJockey14GlowBlue = new Label.LabelStyle();
        labelStyleJockey14GlowBlue.font = skinFonts.getFont("jockey_14_glow_blue");

        this.countryName = new Label("", labelStyleJockey24);
        this.countryName.setBounds(0, 168, background.getMinWidth(), 20);
        this.countryName.setAlignment(Align.center);

        this.government = new Label("", labelStyleJockey14Dark);
        this.government.setBounds(110, 137, 160, 30);
        this.government.setAlignment(Align.right);

        Label countryPopulationDescription = new Label("Population:", labelStyleJockey14);
        countryPopulationDescription.setBounds(110, 109, 156, 20);
        countryPopulationDescription.setAlignment(Align.left);

        this.countryPopulation = new Label("", labelStyleJockey14Yellow);
        this.countryPopulation.setBounds(110, 109, 156, 20);
        this.countryPopulation.setAlignment(Align.right);

        this.portrait = new Image();
        Stack portraitStack = new Stack();
        portraitStack.add(this.portrait);
        portraitStack.add(new Image(skin.getDrawable("tv_overlay")));
        portraitStack.setBounds(6, 6, 78, 78);

        Label leaderDescription = new Label(localisation.get("HEAD_OF_STATE"), labelStyleJockey14Dark);
        leaderDescription.setBounds(105, 72, 160, 20);
        leaderDescription.setAlignment(Align.center);

        this.leaderFullName = new Label("", labelStyleJockey14GlowBlue);
        this.leaderFullName.setBounds(105, 48, 160, 20);
        this.leaderFullName.setAlignment(Align.center);

        Label wars = new Label(localisation.get("WARS"), labelStyleJockey14);
        wars.setBounds(105, 29, 160, 20);
        wars.setAlignment(Align.left);

        Label allies = new Label(localisation.get("ALLIES"), labelStyleJockey14);
        allies.setBounds(105, 10, 160, 20);
        allies.setAlignment(Align.left);

        this.addActor(this.countryName);
        this.addActor(this.flagImage);
        this.addActor(this.government);
        this.addActor(countryPopulationDescription);
        this.addActor(this.countryPopulation);
        this.addActor(portraitStack);
        this.addActor(leaderDescription);
        this.addActor(this.leaderFullName);
        this.addActor(wars);
        this.addActor(allies);

        this.setVisible(false);
    }

    public void update(String name, TextureRegion flag, String population, String government, Drawable portrait, String leaderFullName) {
        this.countryName.setText(name);
        this.flagImage.setFlag(flag);
        this.government.setText(government);
        this.countryPopulation.setText(population);
        this.portrait.setDrawable(portrait);
        this.leaderFullName.setText(leaderFullName);
        this.setVisible(true);
    }

    public void hide() {
        this.setVisible(false);
    }

    public void dispose() {
        this.flagImage.dispose();
    }
}
