package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class ProvincePanel extends Table {
    private final Skin skin;
    private final Map<String, String> localisation;
    private Image terrainImage;
    private Label provinceName;
    private Label regionName;

    public ProvincePanel(Skin skin, Skin skinUi, LabelStylePool labelStylePool, Map<String, String> localisation) {
        this.skin = skin;
        this.localisation = localisation;
        Drawable background = skin.getDrawable("bg_province");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.setHeader(skin, skinUi, labelStylePool);
        this.setDataOverview(skin, skinUi, labelStylePool);
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

        this.addActor(this.terrainImage);
        this.addActor(overlay);
        this.addActor(closeButton);
        this.addActor(this.provinceName);
    }

    private void setDataOverview(Skin skin, Skin skinUi, LabelStylePool labelStylePool) {
        Table dataOverview = new Table();
        Drawable background = skin.getDrawable("bg_province_paper");
        dataOverview.setBackground(background);
        dataOverview.setSize(background.getMinWidth(), background.getMinHeight());
        dataOverview.setPosition(12, 75);

        Label.LabelStyle labelStyleJockey16Paper = labelStylePool.getLabelStyle("jockey_16_paper");

        this.regionName = new Label("", labelStyleJockey16Paper);
        dataOverview.addActor(this.regionName);

        dataOverview.addActor(this.createImage(skin, "prov_pop_icon", 22, 67));
        dataOverview.addActor(this.createImage(skin, "icon_workers_small", 22, 44));
        dataOverview.addActor(this.createImage(skin, "prov_DI", 22, 22));
        dataOverview.addActor(this.createImage(skin, "icon_dollar_big", 145, 38));
        dataOverview.addActor(this.createImage(skin, "icon_industry_small", 265, 53));
        dataOverview.addActor(this.createImage(skin, "prov_build_infra", 165, 160));
        dataOverview.addActor(this.createImage(skin, "icon_militia_small", 285, 202));

        this.addActor(dataOverview);
    }

    private Image createImage(Skin skin, String drawableName, float x, float y) {
        Image image = new Image(skin.getDrawable(drawableName));
        image.setPosition(x, y);
        return image;
    }

    public void setProvinceName(String name) {
        this.provinceName.setText(this.localisation.get(name));
        this.provinceName.setPosition(this.getWidth() / 2 - this.provinceName.getMinWidth() / 2, 471);
    }

    public void setRegionName(String name) {
        this.regionName.setText(this.localisation.get(name));
        this.regionName.setPosition(this.regionName.getParent().getWidth() / 2 - this.regionName.getMinWidth() / 2, 105);
    }

    public void setTerrainImage(String name) {
        Drawable terrain = this.skin.getDrawable("prov_terrain_" + name);
        this.terrainImage.setDrawable(terrain);
        this.terrainImage.setSize(terrain.getMinWidth(), terrain.getMinHeight());
    }

}
