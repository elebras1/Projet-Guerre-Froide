package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.List;
import java.util.Map;

public class ProvincePanel extends Table {
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Map<String, String> localisation;
    private Image terrainImage;
    private Image resourceImage;
    private Label provinceName;
    private Label regionName;
    private Label populationRegion;
    private Label workersRegion;
    private Label developmentIndexRegion;
    private Label incomeRegion;
    private Label industryRegion;
    private FlagImage flagImage;
    private List<FlagImage> countriesCoreFlagImages;

    public ProvincePanel(Skin skin, Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation) {
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        this.localisation = localisation;
        Drawable background = skin.getDrawable("bg_province");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.setHeader(skin, skinUi, labelStylePool);
        this.setDataOverview(skin, labelStylePool);
        this.setCountriesCoreFlagImages();
    }

    private void setHeader(Skin skin, Skin skinUi, LabelStylePool labelStylePool) {
        this.terrainImage = new Image();
        this.terrainImage.setPosition(26, 315);

        Image overlay = this.createImage(skin, "prov_overlay", 26, 315);

        Button closeButton = new Button(skinUi, "close_btn");
        closeButton.setPosition(354, 455);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ProvincePanel.this.remove();
            }
        });

        Label.LabelStyle labelStyleJockey24 = labelStylePool.getLabelStyle("jockey_24");
        this.provinceName = new Label("", labelStyleJockey24);

        this.flagImage = this.createFlagImage(skinUi, "shield_big", "shield_big_overlay", (short) 64, (short) 64);

        this.addActor(this.terrainImage);
        this.addActor(overlay);
        this.addActor(closeButton);
        this.addActor(this.provinceName);
        this.addActor(this.flagImage);
    }

    private void setDataOverview(Skin skin, LabelStylePool labelStylePool) {
        Table dataOverview = new Table();
        Drawable background = skin.getDrawable("bg_province_paper");
        dataOverview.setBackground(background);
        dataOverview.setSize(background.getMinWidth(), background.getMinHeight());
        dataOverview.setPosition(12, 75);

        Label.LabelStyle labelStyleJockey16Paper = labelStylePool.getLabelStyle("jockey_16_paper");
        Label.LabelStyle labelStyleDanger14 = labelStylePool.getLabelStyle("danger_14");
        Label.LabelStyle labelStyleDanger20Dark = labelStylePool.getLabelStyle("danger_20_dark");

        this.regionName = new Label("", labelStyleJockey16Paper);
        dataOverview.addActor(this.regionName);
        this.resourceImage = new Image();
        dataOverview.addActor(this.resourceImage);
        dataOverview.addActor(this.createImage(skin, "prov_pop_icon", 22, 67));
        this.populationRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.populationRegion);
        dataOverview.addActor(this.createImage(skin, "icon_workers_small", 22, 44));
        this.workersRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.workersRegion);
        dataOverview.addActor(this.createImage(skin, "prov_DI", 22, 22));
        this.developmentIndexRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.developmentIndexRegion);
        dataOverview.addActor(this.createImage(skin, "icon_dollar_big", 145, 38));
        this.incomeRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.incomeRegion);
        dataOverview.addActor(this.createImage(skin, "icon_industry_small", 265, 53));
        this.industryRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.industryRegion);
        dataOverview.addActor(this.createImage(skin, "prov_build_infra", 162, 160));
        dataOverview.addActor(this.createImage(skin, "icon_militia_small", 285, 202));

        this.addActor(dataOverview);
    }

    private void setCountriesCoreFlagImages() {
        this.countriesCoreFlagImages = new ObjectList<>();
        int x = 35;
        int y = 193;
        for(int i = 0; i < 5; i++) {
            FlagImage flagImage = this.createFlagImage(this.skinUi, "minimask", "minishield", (short) 32, (short) 32);
            flagImage.setPosition(x, y);
            this.countriesCoreFlagImages.add(flagImage);
            x += 38;
        }
    }

    private Image createImage(Skin skin, String drawableName, float x, float y) {
        Image image = new Image(skin.getDrawable(drawableName));
        image.setPosition(x, y);
        return image;
    }

    private FlagImage createFlagImage(Skin skinUi, String alphaFlagName, String overlayFlagName, short height, short width) {
        TextureRegion alphaFlag = skinUi.getRegion(alphaFlagName);
        TextureRegion overlayFlag = skinUi.getRegion(overlayFlagName);
        Pixmap defaultPixmapFlag = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        TextureRegionDrawable defaultFlag = new TextureRegionDrawable(new Texture(defaultPixmapFlag));
        defaultPixmapFlag.dispose();
        return new FlagImage(defaultFlag, overlayFlag, alphaFlag);
    }

    public void setResourceImage(String name) {
        if(name != null) {
            Drawable resource = this.skinUi.getDrawable("resource_" + name + "_small");
            this.resourceImage.setDrawable(resource);
            this.resourceImage.setSize(resource.getMinWidth(), resource.getMinHeight());
            this.resourceImage.setPosition(22, 160);
        }
    }

    public void setProvinceName(String name) {
        this.provinceName.setText(this.localisation.get(name));
        this.provinceName.setPosition(this.getWidth() / 2 - this.provinceName.getMinWidth() / 2, 471);
    }

    public void setRegionName(String name) {
        this.regionName.setText(this.localisation.get(name));
        this.regionName.setPosition(this.regionName.getParent().getWidth() / 2 - this.regionName.getMinWidth() / 2, 104);
    }

    public void setTerrainImage(String name) {
        Drawable terrain = this.skin.getDrawable("prov_terrain_" + name);
        this.terrainImage.setDrawable(terrain);
        this.terrainImage.setSize(terrain.getMinWidth(), terrain.getMinHeight());
    }

    public void setPopulationRegion(String population) {
        this.populationRegion.setText(population);
        this.populationRegion.setPosition(135 - this.populationRegion.getMinWidth(), 83);
    }

    public void setWorkersRegion(String workers) {
        this.workersRegion.setText(workers);
        this.workersRegion.setPosition(135 - this.workersRegion.getMinWidth(), 61);
    }

    public void setDevelopmentIndexRegion(int developmentIndex) {
        this.developmentIndexRegion.setText(developmentIndex);
        this.developmentIndexRegion.setPosition(125 - this.developmentIndexRegion.getMinWidth(), 38);
    }

    public void setIncomeRegion(int income) {
        this.incomeRegion.setText(income);
        this.incomeRegion.setPosition(248 - this.incomeRegion.getMinWidth(), 60);
    }

    public void setIndustryRegion(int industry) {
        this.industryRegion.setText(industry);
        this.industryRegion.setPosition(360 - this.industryRegion.getMinWidth(), 75);
    }

    public void setFlagImage(String idCountry) {
        this.flagImage.setFlag(this.skinFlags.getRegion(idCountry));
        this.flagImage.setPosition(36, 430);
    }

    public void setFlagCountriesCore(List<String> countriesCore) {
        for(int i = 0; i < this.countriesCoreFlagImages.size(); i++) {
            if(i < countriesCore.size()) {
                this.countriesCoreFlagImages.get(i).setFlag(this.skinFlags.getRegion(countriesCore.get(i)));
                this.addActor(this.countriesCoreFlagImages.get(i));
            } else {
                this.countriesCoreFlagImages.get(i).remove();
            }
        }
    }
}
