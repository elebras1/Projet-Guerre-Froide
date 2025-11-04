package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.screen.EconomyPanelListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.HoverScrollPane;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class EconomyPanel extends Table {
    private final WidgetFactory widgetFactory;
    private final Skin skin;
    private final LabelStylePool labelStylePool;
    private final Map<String, String> localisation;
    private final EconomyPanelListener listener;
    private Table buildingRegionsTable;

    public EconomyPanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinScrollbars, LabelStylePool labelStylePool, Map<String, String> localisation, EconomyPanelListener listener) {
        this.widgetFactory = widgetFactory;
        this.skin = skin;
        this.labelStylePool = labelStylePool;
        this.localisation = localisation;
        this.listener = listener;
        Drawable background = skin.getDrawable("economy_bg_shadow");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.setMainContent(widgetFactory, skin, skinUi, skinScrollbars);
        this.setRightContent(skin);
    }

    private void setMainContent(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinScrollbars) {
        Table mainTable = new Table();
        Drawable background = skin.getDrawable("bg_economy_trade");
        mainTable.setBackground(background);
        mainTable.setFillParent(true);
        this.add(mainTable);

        Button aiButton = widgetFactory.createButton(skin, "eco_btn_ai", 83, 685.5f, mainTable);
        Button regionalButton = widgetFactory.createButton(skin, "eco_btn_regional", 773, 685.5f, mainTable);
        Button closeButton = widgetFactory.createButton(skinUi, "close_btn", 1057, 734, mainTable);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onCloseEconomyPanelClicked();
            }
        });
        this.buildingRegionsTable = new Table();
        HoverScrollPane scrollPane = new HoverScrollPane(this.buildingRegionsTable, skinScrollbars, "default");
        scrollPane.setSize(745, 450);
        scrollPane.setPosition(72, 230);
        mainTable.addActor(scrollPane);
    }

    private void setRightContent(Skin skin) {
        Table rightTable = new Table();
        Drawable background = skin.getDrawable("bg_eco_budget");
        rightTable.setBackground(background);
        rightTable.setSize(background.getMinWidth(), background.getMinHeight());
        rightTable.setPosition(821, 55);
        this.addActor(rightTable);
    }

    public void setData(RegionsBuildingsDto regionsBuildingsDto) {
        Label.LabelStyle labelStyleJockey20GlowBlue = this.labelStylePool.getLabelStyle("jockey_20_glow_blue");
        Drawable regionBackground = this.skin.getDrawable("economy_region_plate_small");

        SnapshotArray<Actor> actors = this.buildingRegionsTable.getChildren();
        int index = 0;
        for(String regionIds : regionsBuildingsDto.getRegionIds()) {
            String regionName = this.localisation.get(regionIds);
            if(index >= actors.size) {
                Table regionTable = new Table();
                regionTable.setBackground(regionBackground);
                regionTable.setSize(regionBackground.getMinWidth(), regionBackground.getMinHeight());
                this.widgetFactory.createLabel(regionName, labelStyleJockey20GlowBlue, 8, 5, regionTable);
                Button minButton = this.widgetFactory.createButton(this.skin, "eco_plate_min", 684, 8, regionTable);
                Button maxButton = this.widgetFactory.createButton(this.skin, "eco_plate_max", 684, 8, regionTable);
                minButton.setVisible(true);
                maxButton.setVisible(false);

                Table buildingsTable = new Table();
                for(int i = 0; i < 6; i++) {
                    Table buildingTable = new Table();
                    this.widgetFactory.applyBackgroundToTable(this.skin, "eco_build", buildingTable);
                    buildingsTable.add(buildingTable).padRight(-10);
                }

                Table regionsBuildingsTable = new Table();
                regionsBuildingsTable.add(regionTable).row();
                Cell<Table> buildingCell = regionsBuildingsTable.add(buildingsTable).padLeft(-6);

                minButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        buildingCell.clearActor();
                        minButton.setVisible(false);
                        maxButton.setVisible(true);
                    }
                });

                maxButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        buildingCell.setActor(buildingsTable);
                        minButton.setVisible(true);
                        maxButton.setVisible(false);
                    }
                });

                this.buildingRegionsTable.add(regionsBuildingsTable).row();
            } else {
                Actor actor = actors.get(index);
                if(actor instanceof Table regionsBuildingsTable && regionsBuildingsTable.getChild(0) instanceof Table regionTable && regionTable.getChild(0) instanceof Label regionLabel) {
                    regionLabel.setText(regionName);
                }
            }
            index++;
        }

        while(index < actors.size) {
            Actor actor = actors.get(index);
            actor.remove();
            index++;
        }
    }
}
