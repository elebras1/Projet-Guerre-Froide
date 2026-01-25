package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.populaire.projetguerrefroide.dto.BuildingDto;
import com.populaire.projetguerrefroide.dto.RegionDto;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.screen.listener.EconomyPanelListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.ClickableTable;
import com.populaire.projetguerrefroide.ui.widget.HoverScrollPane;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.ValueFormatter;

import java.util.List;
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

        Button developpementIndexButton = widgetFactory.createButton(this.skin, "icon_di", 530, 677, mainTable);
        developpementIndexButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSortRegions(SortType.DEVELOPPEMENT_INDEX);
            }
        });
        Button populationButton = widgetFactory.createButton(this.skin, "icon_pop", 605, 677, mainTable);
        populationButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSortRegions(SortType.POPULATION);
            }
        });
        Button worforceButton = widgetFactory.createButton(this.skin, "icon_workforce", 685, 677, mainTable);
        worforceButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSortRegions(SortType.WORKFORCE);
            }
        });

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

    public void setData(RegionsBuildingsDto dto) {
        SnapshotArray<Actor> currentActors = this.buildingRegionsTable.getChildren();
        int index = 0;

        for (RegionDto region : dto.regions()) {
            if (index >= currentActors.size) {
                Table newRegionBlock = this.createRegionBlock(region);
                this.buildingRegionsTable.add(newRegionBlock).row();
            } else {
                this.updateRegionBlock((Table) currentActors.get(index), region);
            }
            index++;
        }

        this.cleanupExcessActors(currentActors, index);
    }

    private Table createRegionBlock(RegionDto region) {
        Table regionBlock = new Table();

        Table headerTable = this.createRegionHeader(region);
        Button minButton = headerTable.findActor("btn_min");
        Button maxButton = headerTable.findActor("btn_max");

        Table buildingsGrid = this.createBuildingsGrid(region.buildings());

        regionBlock.add(headerTable).row();
        Cell<Table> gridCell = regionBlock.add(buildingsGrid).padLeft(-6);

        regionBlock.setUserObject(buildingsGrid);

        this.setupCollapseListeners(minButton, maxButton, gridCell, buildingsGrid);

        return regionBlock;
    }

    private Table createRegionHeader(RegionDto region) {
        Table headerTable = new Table();
        Drawable regionBackground = this.skin.getDrawable("economy_region_plate_small");
        headerTable.setBackground(regionBackground);
        headerTable.setSize(regionBackground.getMinWidth(), regionBackground.getMinHeight());

        Label.LabelStyle style = this.labelStylePool.get("jockey_16_glow_blue");
        String regionName = this.localisation.get(region.regionId());

        Label nameLabel = this.widgetFactory.createLabel(regionName, style, 8, 7, headerTable);
        nameLabel.setName("lbl_name");

        Button minButton = this.widgetFactory.createButton(this.skin, "eco_plate_min", 684, 9, headerTable);
        Button maxButton = this.widgetFactory.createButton(this.skin, "eco_plate_max", 684, 9, headerTable);
        minButton.setName("btn_min");
        maxButton.setName("btn_max");
        minButton.setVisible(true);
        maxButton.setVisible(false);

        Label devIndexLabel = this.widgetFactory.createLabelCentered(region.developpementIndexValue() + "%", style, 477, 7, headerTable);
        devIndexLabel.setName("lbl_dev_index");

        Label popLabel = this.widgetFactory.createLabelCentered(ValueFormatter.format(region.populationAmount(), this.localisation), style, 545, 7, headerTable);
        popLabel.setName("lbl_pop");

        Label workLabel = this.widgetFactory.createLabelCentered(region.buildingWorkerAmount() + " (" + region.buildingWorkerRatio() + "%)", style, 638, 7, headerTable);
        workLabel.setName("lbl_work");

        return headerTable;
    }

    private Table createBuildingsGrid(List<BuildingDto> buildings) {
        Table buildingsTable = new Table();
        this.rebuildBuildingsGrid(buildingsTable, buildings);
        return buildingsTable;
    }

    private void rebuildBuildingsGrid(Table buildingsTable, List<BuildingDto> buildings) {
        buildingsTable.clearChildren();

        int count = 0;
        for (BuildingDto building : buildings) {
            Table buildingCell = this.createBuildingItem(building);
            buildingsTable.add(buildingCell).padRight(-10);

            count++;
            if (count % 6 == 0) {
                buildingsTable.row();
            }
        }

        this.fillEmptySlots(buildingsTable, count);
    }

    private Table createBuildingItem(BuildingDto building) {
        Table buildingTable = new ClickableTable();
        buildingTable.setUserObject(building.buildingId());

        Label.LabelStyle labelStyle = this.labelStylePool.get("jockey_16_glow_blue");
        Label.LabelStyle labelSmall = this.labelStylePool.get("jockey_14_tight");

        this.widgetFactory.applyBackgroundToTable(this.skin, "building_box_template", buildingTable);
        this.widgetFactory.createImage(this.skin, "building_" + building.buildingName(), 10, 55, buildingTable);

        this.widgetFactory.createLabelCentered(ValueFormatter.format(building.productionValue()), labelStyle, buildingTable.getWidth() / 2, 21, buildingTable);

        String levelText = building.buildingValue() + "/" + building.maxLevel();
        this.widgetFactory.createLabelCentered(levelText, labelSmall, buildingTable.getWidth() / 2, 2, buildingTable);

        return buildingTable;
    }

    private void fillEmptySlots(Table buildingsTable, int currentCount) {
        int slotsNeeded = 0;

        if (currentCount == 0) {
            slotsNeeded = 6;
        } else {
            int remainder = currentCount % 6;
            if (remainder != 0) {
                slotsNeeded = 6 - remainder;
            }
        }

        for (int i = 0; i < slotsNeeded; i++) {
            Table emptyTable = new Table();
            this.widgetFactory.applyBackgroundToTable(this.skin, "eco_build", emptyTable);
            buildingsTable.add(emptyTable).padRight(-10);
        }
    }

    private void setupCollapseListeners(Button minButton, Button maxButton, Cell<Table> gridCell, Table gridTable) {
        minButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gridCell.clearActor();
                minButton.setVisible(false);
                maxButton.setVisible(true);
            }
        });

        maxButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gridCell.setActor(gridTable);
                minButton.setVisible(true);
                maxButton.setVisible(false);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void updateRegionBlock(Table regionBlock, RegionDto region) {
        if (regionBlock.getChildren().size < 1) {
            return;
        }

        Table header = (Table) regionBlock.getChild(0);

        Label nameLabel = header.findActor("lbl_name");
        Label devIndexLabel = header.findActor("lbl_dev_index");
        Label popLabel = header.findActor("lbl_pop");
        Label workLabel = header.findActor("lbl_work");

        if (nameLabel != null) {
            nameLabel.setText(this.localisation.get(region.regionId()));
        }

        if (devIndexLabel != null) {
            devIndexLabel.setText(region.developpementIndexValue() + "%");
        }
        if (popLabel != null) {
            popLabel.setText(ValueFormatter.format(region.populationAmount(), this.localisation));
        }
        if (workLabel != null) {
            workLabel.setText(region.buildingWorkerAmount() + " (" + region.buildingWorkerRatio() + "%)");
        }

        Button minButton = header.findActor("btn_min");
        Button maxButton = header.findActor("btn_max");

        Table buildingsTable = (Table) regionBlock.getUserObject();

        if (buildingsTable != null) {
            this.rebuildBuildingsGrid(buildingsTable, region.buildings());
        }

        if (maxButton != null && maxButton.isVisible() && buildingsTable != null) {
            Cell<Table> cell = regionBlock.getCells().get(1);
            cell.setActor(buildingsTable);
            if(minButton != null) minButton.setVisible(true);
            maxButton.setVisible(false);
        }
    }

    private void cleanupExcessActors(SnapshotArray<Actor> actors, int startIndex) {
        while (startIndex < actors.size) {
            actors.get(startIndex).remove();
        }
        for (int i = actors.size - 1; i >= startIndex; i--) {
            actors.get(i).remove();
        }
    }
}
