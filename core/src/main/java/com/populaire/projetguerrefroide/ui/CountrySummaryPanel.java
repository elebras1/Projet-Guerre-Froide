package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.dto.CountrySummaryDto;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class CountrySummaryPanel extends Table {
    private final Label countryName;
    private final Label government;
    private final Label countryPopulation;
    private final Label leaderFullName;
    private final FlagImage flagImage;
    private final Image portrait;
    private final Skin skinFlags;
    private final Skin skinPortraits;

    public CountrySummaryPanel(Skin skin, Skin skinUi, Skin skinFlags, Skin skinPortraits, LabelStylePool labelStylePool, Map<String, String> localisation) {
        this.skinFlags = skinFlags;
        this.skinPortraits = skinPortraits;
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

        Label.LabelStyle labelStyleJockey24 = labelStylePool.getLabelStyle("jockey_24");
        Label.LabelStyle labelStyleJockey14Dark = labelStylePool.getLabelStyle("jockey_14_dark");
        Label.LabelStyle labelStyleJockey14 = labelStylePool.getLabelStyle("jockey_14");
        Label.LabelStyle labelStyleJockey14Yellow = labelStylePool.getLabelStyle("jockey_14", "yellow");
        Label.LabelStyle labelStyleJockey14GlowBlue = labelStylePool.getLabelStyle("jockey_14_glow_blue");

        this.countryName = new Label("", labelStyleJockey24);
        this.countryName.setBounds(0, 168, background.getMinWidth(), 20);
        this.countryName.setAlignment(Align.center);

        this.government = new Label("", labelStyleJockey14Dark);
        this.government.setBounds(110, 137, 160, 30);
        this.government.setAlignment(Align.right);

        Label countryPopulationDescription = new Label(localisation.get("POPULATION"), labelStyleJockey14);
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

    public void update(CountrySummaryDto countrySummaryDto, Map<String, String> localisation) {
        this.countryName.setText(countrySummaryDto.getCountryName());
        this.flagImage.setFlag(this.skinFlags.getRegion(countrySummaryDto.getIdCountry()));
        this.government.setText(localisation.get(countrySummaryDto.getGovernment()));
        this.countryPopulation.setText(countrySummaryDto.getPopulation());
        this.portrait.setDrawable(this.skinPortraits.getDrawable(countrySummaryDto.getPortrait()));
        this.leaderFullName.setText(countrySummaryDto.getLeaderFullName());
        this.setVisible(true);
    }

    public void hide() {
        this.setVisible(false);
    }

    public void dispose() {
        this.flagImage.dispose();
    }
}
