package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.CharArray;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.request.SortType;
import com.populaire.projetguerrefroide.protocol.response.BuildingDetails;
import com.populaire.projetguerrefroide.protocol.response.RegionsBuildings;
import com.populaire.projetguerrefroide.screen.listener.EconomyPanelListener;
import com.populaire.projetguerrefroide.service.ColorDrawablePool;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiScrollbarsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.ClickableTable;
import com.populaire.projetguerrefroide.ui.widget.FillBar;
import com.populaire.projetguerrefroide.ui.widget.HoverScrollPane;
import com.populaire.projetguerrefroide.ui.widget.SkinDrawables;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.ColorUtils;
import com.populaire.projetguerrefroide.util.UiConstants;

import java.util.List;
import java.util.Map;

public class EconomyPanel extends Table {
    private static final int BUILDINGS_PER_ROW = 6;
    private static final int INPUT_SLOTS = 5;
    private final WidgetFactory widgetFactory;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFonts;
    private final ColorDrawablePool colorTexturePool;
    private final Map<String, String> localisation;
    private final EconomyPanelListener listener;
    private final UiCatalog uiCatalog;
    private final TextFormat textFormat;
    private final CharArray text;
    private final Drawable[] buildingDrawables;
    private final Drawable[] pinnedDrawables;
    private final Drawable[] goodDrawables;
    private final Drawable noGoodDrawable;
    private final Drawable suspendedDrawable;
    private final Drawable inputActiveBackground;
    private final Drawable inputEmptyBackground;
    private final Label.LabelStyle regionStyle;
    private final Label.LabelStyle buildingStyle;
    private final Label.LabelStyle levelStyle;
    private final List<RegionBlock> regionBlocks;
    private final boolean[] collapsedRegions;
    private int shownRegionCount;
    private Table buildingRegionsTable;
    private Table buildingSelectedTable;
    private long selectedBuildingId;
    private Image pinnedBuildingImage;
    private Label industryValueLabel;
    private Label productionValueLabel;
    private FillBar workersBar;
    private Button expandButton;
    private Button resumeButton;
    private Button suspendButton;
    private Button demolishButton;
    private Label provinceLabel;
    private Label buildingTypeLabel;
    private final Table[] inputIcons;
    private final Image[] inputGoodImages;
    private Image outputGoodImage;

    public EconomyPanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinScrollbars, Skin skinFonts, ColorDrawablePool colorTexturePool, Map<String, String> localisation, UiCatalog uiCatalog, TextFormat textFormat, EconomyPanelListener listener) {
        this.widgetFactory = widgetFactory;
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFonts = skinFonts;
        this.colorTexturePool = colorTexturePool;
        this.localisation = localisation;
        this.listener = listener;
        this.uiCatalog = uiCatalog;
        this.textFormat = textFormat;
        this.text = new CharArray(32);
        Catalog catalog = uiCatalog.catalog();
        this.noGoodDrawable = skinUi.getDrawable(UiSkin.Drawables.GOOD_NONE_SMALL);
        this.buildingDrawables = new Drawable[catalog.buildingTypes().size()];
        this.pinnedDrawables = new Drawable[catalog.buildingTypes().size()];
        for (int i = 0; i < this.buildingDrawables.length; i++) {
            String name = catalog.buildingTypes().nameId(i);
            this.buildingDrawables[i] = SkinDrawables.optional(skin, "building_" + name, null);
            this.pinnedDrawables[i] = SkinDrawables.optional(skin, "pinned_" + name, null);
        }
        this.goodDrawables = new Drawable[catalog.goods().size()];
        for (int i = 0; i < this.goodDrawables.length; i++) {
            this.goodDrawables[i] = SkinDrawables.optional(skinUi, "good_" + catalog.goods().nameId(i) + "_small", this.noGoodDrawable);
        }
        this.suspendedDrawable = skin.getDrawable(UiEconomySkin.Drawables.ECONMY_SUSPENDED_ICON1);
        this.inputActiveBackground = skin.getDrawable(UiEconomySkin.Drawables.ECO_BOX_METAL_GREEN);
        this.inputEmptyBackground = skin.getDrawable(UiEconomySkin.Drawables.ECO_BOX_METAL_GREY);
        this.regionStyle = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, Label.LabelStyle.class);
        this.buildingStyle = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, Label.LabelStyle.class);
        this.levelStyle = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_14_TIGHT, Label.LabelStyle.class);
        this.regionBlocks = new ObjectList<>();
        this.collapsedRegions = new boolean[catalog.regions().size()];
        this.inputIcons = new Table[INPUT_SLOTS];
        this.inputGoodImages = new Image[INPUT_SLOTS];
        Drawable background = skin.getDrawable(UiEconomySkin.Drawables.ECONOMY_BG_SHADOW);
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.setMainContent(widgetFactory, skin, skinUi, skinScrollbars);
        this.setRightContent(skin);
        this.selectedBuildingId = 0;
    }

    private void setMainContent(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinScrollbars) {
        Table mainTable = new Table();
        Drawable background = skin.getDrawable(UiEconomySkin.Drawables.BG_ECONOMY_TRADE);
        mainTable.setBackground(background);
        mainTable.setFillParent(true);
        this.add(mainTable);

        Button developpementIndexButton = widgetFactory.createButton(this.skin, UiEconomySkin.Styles.Button.ICON_DI_BTN, 530, 677, mainTable);
        developpementIndexButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSortRegions(SortType.DEVELOPPEMENT_INDEX);
            }
        });
        Button populationButton = widgetFactory.createButton(this.skin, UiEconomySkin.Styles.Button.ICON_POP_BTN, 605, 677, mainTable);
        populationButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSortRegions(SortType.POPULATION);
            }
        });
        Button worforceButton = widgetFactory.createButton(this.skin, UiEconomySkin.Styles.Button.ICON_WORKFORCE_BTN, 685, 677, mainTable);
        worforceButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSortRegions(SortType.WORKFORCE);
            }
        });

        widgetFactory.createButton(skin, UiEconomySkin.Styles.Button.ECO_AI_TGL, 83, 685.5f, mainTable);
        widgetFactory.createButton(skin, UiEconomySkin.Styles.Button.ECO_REGIONAL_TGL, 773, 685.5f, mainTable);
        Button closeButton = widgetFactory.createButton(skinUi, UiSkin.Styles.Button.CLOSE_BTN, 1057, 734, mainTable);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onCloseEconomyPanelClicked();
            }
        });
        this.buildingRegionsTable = new Table();
        HoverScrollPane scrollPane = new HoverScrollPane(this.buildingRegionsTable, skinScrollbars, UiScrollbarsSkin.Styles.Scrollpane.DEFAULT);
        scrollPane.setSize(745, 450);
        scrollPane.setPosition(72, 230);

        this.createSelectedBuildingInfoBlock();
        this.buildingSelectedTable.setPosition(60, 52);
        mainTable.addActor(this.buildingSelectedTable);
        mainTable.addActor(scrollPane);
    }

    private void setRightContent(Skin skin) {
        Table rightTable = new Table();
        Drawable background = skin.getDrawable(UiEconomySkin.Drawables.BG_ECO_BUDGET);
        rightTable.setBackground(background);
        rightTable.setSize(background.getMinWidth(), background.getMinHeight());
        rightTable.setPosition(821, 55);
        this.addActor(rightTable);
    }

    private void createSelectedBuildingInfoBlock() {
        this.buildingSelectedTable = new Table();
        Drawable background = this.skin.getDrawable(UiEconomySkin.Drawables.ECONOMY_PAPER);
        this.buildingSelectedTable.setBackground(background);
        this.buildingSelectedTable.setSize(background.getMinWidth(), background.getMinHeight());
        this.buildingSelectedTable.setVisible(false);

        Label.LabelStyle jockey16Paper = this.skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_PAPER, Label.LabelStyle.class);
        this.widgetFactory.createLabelCentered(this.localisation.get("INPUT"), jockey16Paper, 400, 142, this.buildingSelectedTable);
        int inputIconX = 290;
        int inputIconY = 95;
        for (int i = 0; i < INPUT_SLOTS; i++) {
            Table inputBgIcon = this.widgetFactory.createBackgroundTable(this.skin, UiEconomySkin.Drawables.ECO_BOX_METAL_GREY, inputIconX, inputIconY, this.buildingSelectedTable);
            this.inputIcons[i] = inputBgIcon;
            this.inputGoodImages[i] = this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.GOOD_NONE_SMALL, 8, 11, inputBgIcon);
            inputIconX += 43;
        }
        this.widgetFactory.createLabelCentered(this.localisation.get("OUTPUT"), jockey16Paper, 400, 66, this.buildingSelectedTable);
        Table outputBgIcon = this.widgetFactory.createBackgroundTable(this.skin, UiEconomySkin.Drawables.ECO_BOX_METAL_BLUE, 290, 20, this.buildingSelectedTable);
        this.outputGoodImage = this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.GOOD_NONE_SMALL, 8, 11, outputBgIcon);

        this.provinceLabel = this.widgetFactory.createLabelCentered(UiConstants.TMP, jockey16Paper, 631, 142, this.buildingSelectedTable);
        this.buildingTypeLabel = this.widgetFactory.createLabelCentered(UiConstants.TMP, jockey16Paper, 631, 122, this.buildingSelectedTable);

        Label.LabelStyle jockey16Dark = this.skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_DARK, Label.LabelStyle.class);
        this.expandButton = this.widgetFactory.createButton(this.skin, UiEconomySkin.Styles.Button.ECO_FOLDER_YELLOW_BTN, 636, 94, this.buildingSelectedTable);
        this.expandButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onExpandBuildingClicked(selectedBuildingId);
            }
        });
        this.widgetFactory.createLabel(this.localisation.get("EXPAND"), jockey16Dark, 26, 0, this.expandButton);

        this.resumeButton = this.widgetFactory.createButton(this.skin, UiEconomySkin.Styles.Button.ECO_FOLDER_YELLOW_BTN, 636, 70, this.buildingSelectedTable);
        this.resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onResumeBuildingClicked(selectedBuildingId);
            }
        });
        this.widgetFactory.createLabel(this.localisation.get("RESUME"), jockey16Dark, 23, 0, this.resumeButton);

        this.suspendButton = this.widgetFactory.createButton(this.skin, UiEconomySkin.Styles.Button.ECO_FOLDER_YELLOW_BTN, 636, 70, this.buildingSelectedTable);
        this.suspendButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSuspendBuildingClicked(selectedBuildingId);
            }
        });
        this.widgetFactory.createLabel(this.localisation.get("SUSPEND"), jockey16Dark, 23, 0, this.suspendButton);

        this.demolishButton = this.widgetFactory.createButton(this.skin, UiEconomySkin.Styles.Button.ECO_FOLDER_RED_BTN, 636, 22, this.buildingSelectedTable);
        this.demolishButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onDemolishBuildingClicked(selectedBuildingId);
            }
        });
        this.widgetFactory.createLabel(this.localisation.get("DEMOLISH"), jockey16Dark, 23, 0, this.demolishButton);

        this.widgetFactory.createImage(this.skin, UiEconomySkin.Drawables.ECO_ICON_INDUSTRY, 520, 87, this.buildingSelectedTable);
        this.industryValueLabel = this.widgetFactory.createLabelAlignRight("0%", jockey16Dark, 625, 88, this.buildingSelectedTable);

        this.widgetFactory.createImage(this.skin, UiEconomySkin.Drawables.PROD_ICON, 520, 56, this.buildingSelectedTable);
        this.productionValueLabel = this.widgetFactory.createLabelAlignRight("0%", jockey16Dark, 625, 57, this.buildingSelectedTable);

        this.widgetFactory.createImage(this.skin, UiEconomySkin.Drawables.ECO_ICON_WORKERS_2, 513, 22, this.buildingSelectedTable);
        this.workersBar = this.widgetFactory.createFillBar(this.colorTexturePool.get(ColorUtils.GREEN_RGBA), this.colorTexturePool.get(ColorUtils.RED_RGBA), 543, 32, 81, 12, this.buildingSelectedTable);

        this.pinnedBuildingImage = this.widgetFactory.createImage(28, 23, this.buildingSelectedTable);
    }

    public void setData(RegionsBuildings regions) {
        int regionCount = regions.regionCount();
        boolean structureChanged = regionCount != this.shownRegionCount;
        while (this.regionBlocks.size() < regionCount) {
            this.regionBlocks.add(new RegionBlock());
        }
        for (int position = 0; position < regionCount; position++) {
            RegionBlock block = this.regionBlocks.get(position);
            int regionIndex = regions.regionIndex(position);
            if (block.regionIndex != regionIndex) {
                structureChanged = true;
                block.assignRegion(regionIndex);
            }
            block.updateHeader(regions, position);
            block.updateBuildings(regions, position);
        }
        if (structureChanged) {
            this.buildingRegionsTable.clearChildren();
            for (int position = 0; position < regionCount; position++) {
                this.buildingRegionsTable.add(this.regionBlocks.get(position).root).row();
            }
            this.shownRegionCount = regionCount;
        }
    }

    public void updateSelectedBuildingInfoBlock(BuildingDetails building) {
        this.selectedBuildingId = building.buildingId();
        this.suspendButton.setVisible(!building.suspended());
        this.resumeButton.setVisible(building.suspended());
        this.setActionEnabled(this.expandButton, building.stateControlled());
        this.setActionEnabled(this.suspendButton, building.stateControlled());
        this.setActionEnabled(this.resumeButton, building.stateControlled());
        this.setActionEnabled(this.demolishButton, building.stateControlled());

        this.provinceLabel.setText(this.uiCatalog.regionName(building.regionIndex()));
        this.buildingTypeLabel.setText(this.uiCatalog.buildingTypeName(building.buildingTypeIndex()));

        int typeIndex = building.buildingTypeIndex();
        Drawable pinned = typeIndex >= 0 ? this.pinnedDrawables[typeIndex] : null;
        if (this.pinnedBuildingImage.getDrawable() != pinned) {
            this.pinnedBuildingImage.setDrawable(pinned);
            this.pinnedBuildingImage.pack();
        }

        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (i < building.inputGoodCount()) {
                this.inputIcons[i].setBackground(this.inputActiveBackground);
                this.inputGoodImages[i].setDrawable(this.goodDrawable(building.inputGood(i)));
            } else {
                this.inputIcons[i].setBackground(this.inputEmptyBackground);
                this.inputGoodImages[i].setDrawable(this.noGoodDrawable);
            }
        }
        this.outputGoodImage.setDrawable(this.goodDrawable(building.outputGoodIndex()));
        this.workersBar.setValue(building.workers(), building.maxWorkers());

        this.buildingSelectedTable.setVisible(true);
    }

    private Drawable goodDrawable(int goodIndex) {
        return goodIndex >= 0 ? this.goodDrawables[goodIndex] : this.noGoodDrawable;
    }

    private void setActionEnabled(Button button, boolean enabled) {
        button.setDisabled(!enabled);
        button.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
    }

    public long getSelectedBuildingId() {
        return this.selectedBuildingId;
    }

    public void clearSelectedBuildingInfoBlock() {
        this.selectedBuildingId = 0;
        this.buildingSelectedTable.setVisible(false);
    }

    private final class RegionBlock {
        private final Table root;
        private final Table grid;
        private final Cell<Table> gridCell;
        private final Label nameLabel;
        private final Label developmentIndexLabel;
        private final Label populationLabel;
        private final Label workLabel;
        private final Button minButton;
        private final Button maxButton;
        private final List<BuildingCell> cells;
        private final List<Table> emptySlots;
        private int regionIndex;
        private int shownBuildings;

        private RegionBlock() {
            this.regionIndex = -1;
            this.shownBuildings = -1;
            this.cells = new ObjectList<>();
            this.emptySlots = new ObjectList<>();
            this.root = new Table();
            Table header = new Table();
            Drawable regionBackground = skin.getDrawable(UiEconomySkin.Drawables.ECONOMY_REGION_PLATE_SMALL);
            header.setBackground(regionBackground);
            header.setSize(regionBackground.getMinWidth(), regionBackground.getMinHeight());
            this.nameLabel = widgetFactory.createLabel("", regionStyle, 8, 7, header);
            this.minButton = widgetFactory.createButton(skin, UiEconomySkin.Styles.Button.ECO_PLATE_MIN_BTN, 684, 9, header);
            this.maxButton = widgetFactory.createButton(skin, UiEconomySkin.Styles.Button.ECO_PLATE_MAX_BTN, 684, 9, header);
            this.developmentIndexLabel = widgetFactory.createLabelCentered("", regionStyle, 477, 7, header);
            this.populationLabel = widgetFactory.createLabelCentered("", regionStyle, 545, 7, header);
            this.workLabel = widgetFactory.createLabelCentered("", regionStyle, 638, 7, header);
            this.grid = new Table();
            this.root.add(header).row();
            this.gridCell = this.root.add(this.grid).padLeft(-6);
            this.minButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    setCollapsed(true);
                }
            });
            this.maxButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    setCollapsed(false);
                }
            });
        }

        private void assignRegion(int regionIndex) {
            this.regionIndex = regionIndex;
            this.nameLabel.setText(uiCatalog.regionName(regionIndex));
            this.applyCollapsed(regionIndex >= 0 && collapsedRegions[regionIndex]);
        }

        private void setCollapsed(boolean collapsed) {
            if (this.regionIndex >= 0) {
                collapsedRegions[this.regionIndex] = collapsed;
            }
            this.applyCollapsed(collapsed);
        }

        private void applyCollapsed(boolean collapsed) {
            if (collapsed) {
                this.gridCell.clearActor();
            } else if (this.gridCell.getActor() != this.grid) {
                this.gridCell.setActor(this.grid);
            }
            this.minButton.setVisible(!collapsed);
            this.maxButton.setVisible(collapsed);
        }

        private void updateHeader(RegionsBuildings regions, int position) {
            text.clear();
            this.developmentIndexLabel.setText(text.append(regions.regionDevelopmentIndex(position)).append('%'));
            this.populationLabel.setText(textFormat.compact(text, regions.regionPopulation(position)));
            text.clear();
            this.workLabel.setText(text.append(regions.regionWorkers(position)).append(" (").append(regions.regionWorkerRatio(position)).append("%)"));
        }

        private void updateBuildings(RegionsBuildings regions, int position) {
            int count = regions.regionBuildingCount(position);
            while (this.cells.size() < count) {
                this.cells.add(new BuildingCell());
            }
            if (count != this.shownBuildings) {
                this.rebuildGrid(count);
            }
            int first = regions.regionBuildingStart(position);
            for (int i = 0; i < count; i++) {
                this.cells.get(i).update(regions, first + i);
            }
        }

        private void rebuildGrid(int count) {
            this.grid.clearChildren();
            for (int i = 0; i < count; i++) {
                this.grid.add(this.cells.get(i).table).padRight(-10);
                if ((i + 1) % BUILDINGS_PER_ROW == 0) {
                    this.grid.row();
                }
            }
            int emptyNeeded = count == 0 ? BUILDINGS_PER_ROW : (BUILDINGS_PER_ROW - count % BUILDINGS_PER_ROW) % BUILDINGS_PER_ROW;
            while (this.emptySlots.size() < emptyNeeded) {
                Table emptyTable = new Table();
                widgetFactory.applyBackgroundToTable(skin, UiEconomySkin.Drawables.ECO_BUILD, emptyTable);
                this.emptySlots.add(emptyTable);
            }
            for (int i = 0; i < emptyNeeded; i++) {
                this.grid.add(this.emptySlots.get(i)).padRight(-10);
            }
            this.shownBuildings = count;
        }
    }

    private final class BuildingCell {
        private final ClickableTable table;
        private final Image buildingImage;
        private final Image suspendedImage;
        private final Label productionLabel;
        private final Label levelLabel;
        private long buildingId;

        private BuildingCell() {
            this.table = new ClickableTable();
            widgetFactory.applyBackgroundToTable(skin, UiEconomySkin.Drawables.BUILDING_BOX_TEMPLATE, this.table);
            this.buildingImage = widgetFactory.createImage(10, 55, this.table);
            this.suspendedImage = widgetFactory.createImage(38, 61, this.table);
            this.productionLabel = widgetFactory.createLabelCentered("", buildingStyle, this.table.getWidth() / 2, 21, this.table);
            this.levelLabel = widgetFactory.createLabelCentered("", levelStyle, this.table.getWidth() / 2, 2, this.table);
            this.table.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    listener.onBuildingClicked(buildingId);
                }
            });
        }

        private void update(RegionsBuildings regions, int building) {
            this.buildingId = regions.buildingId(building);
            int typeIndex = regions.buildingType(building);
            Drawable buildingDrawable = typeIndex >= 0 ? buildingDrawables[typeIndex] : null;
            if (this.buildingImage.getDrawable() != buildingDrawable) {
                this.buildingImage.setDrawable(buildingDrawable);
                if (buildingDrawable != null) {
                    this.buildingImage.setSize(buildingDrawable.getMinWidth(), buildingDrawable.getMinHeight());
                }
            }
            Drawable suspended = regions.buildingSuspended(building) ? suspendedDrawable : null;
            if (this.suspendedImage.getDrawable() != suspended) {
                this.suspendedImage.setDrawable(suspended);
                if (suspended != null) {
                    this.suspendedImage.setSize(suspended.getMinWidth(), suspended.getMinHeight());
                }
            }
            this.productionLabel.setText(textFormat.fixed(text, regions.buildingProduction(building), 4));
            text.clear();
            text.append(regions.buildingSize(building));
            int levelsQueued = regions.buildingLevelsQueued(building);
            if (levelsQueued > 0) {
                text.append(" (").append(levelsQueued).append(')');
            }
            text.append('/').append(regions.buildingMaxLevel(building));
            this.levelLabel.setText(text);
        }
    }
}
