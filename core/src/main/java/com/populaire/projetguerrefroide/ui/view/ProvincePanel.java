package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.CharArray;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.response.ProvinceDetails;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.SkinDrawables;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.ui.widget.LabelUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProvincePanel extends Table {
    private final WidgetFactory widgetFactory;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Map<String, String> localisation;
    private Image terrainImage;
    private Image resourceImage;
    private Label provinceName;
    private Label regionName;
    private Label resourceProduced;
    private Label infrastructureValue;
    private Label guerillaValue;
    private Label populationRegion;
    private Label workersRegion;
    private Label developmentIndexRegion;
    private Label incomeRegion;
    private Label industryRegion;
    private Label populationProvince;
    private Label incomeProvince;
    private Label revoltRisk;
    private List<Label> provinceNamesRegion;
    private FlagImage flagImage;
    private List<FlagImage> countriesCoreFlagImages;
    private List<Image> navalBaseLevel;
    private List<Image> airBaseLevel;
    private List<Image> radarStationLevel;
    private List<Image> antiAircraftGunsLevel;
    private List<Image> colorsBuildings;
    private List<Image> specialBuildings;
    private final UiCatalog uiCatalog;
    private final TextFormat textFormat;
    private final CharArray text;
    private final Drawable[] terrainDrawables;
    private final Drawable[] resourceDrawables;
    private final Drawable[] factoryColorDrawables;
    private final Drawable[] specialBuildingDrawables;
    private final Drawable noResourceDrawable;
    private final Drawable levelOnDrawable;
    private final Drawable levelOffDrawable;
    private final Drawable noFactoryDrawable;
    private final Drawable emptySpecialBuildingDrawable;
    private final int[] specialTypes;
    private final int[] specialLevels;
    private final int[] specialMaxLevels;
    private final int[] specialStaffPosts;
    private final int[] specialStaffFilled;
    private final float[] specialEffectiveLevels;
    private final float[] specialStorageCapacities;
    private int specialCount;
    private final int[] regionProvincesShown;
    private ProvincePanelListener listener;

    public ProvincePanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinFonts, Map<String, String> localisation, UiCatalog uiCatalog, TextFormat textFormat) {
        this.widgetFactory = widgetFactory;
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        this.localisation = localisation;
        this.uiCatalog = uiCatalog;
        this.textFormat = textFormat;
        this.text = new CharArray(32);
        Catalog catalog = uiCatalog.catalog();
        this.terrainDrawables = new Drawable[catalog.terrains().size()];
        for (int i = 0; i < this.terrainDrawables.length; i++) {
            String name = "prov_terrain_" + catalog.terrains().nameId(i);
            this.terrainDrawables[i] = SkinDrawables.optional(skin, name, null);
        }
        this.noResourceDrawable = skinUi.getDrawable(UiSkin.Drawables.GOOD_NONE_SMALL);
        this.resourceDrawables = new Drawable[catalog.goods().size()];
        for (int i = 0; i < this.resourceDrawables.length; i++) {
            String name = "good_" + catalog.goods().nameId(i) + "_small";
            this.resourceDrawables[i] = SkinDrawables.optional(skinUi, name, this.noResourceDrawable);
        }
        this.factoryColorDrawables = new Drawable[catalog.buildingTypes().size()];
        this.specialBuildingDrawables = new Drawable[catalog.buildingTypes().size()];
        for (int i = 0; i < this.factoryColorDrawables.length; i++) {
            String color = BuildingUtils.getColor(catalog.buildingTypes().nameId(i));
            this.factoryColorDrawables[i] = color != null ? SkinDrawables.optional(skin, "factory_plupp_" + color, null) : null;
            String specialName = catalog.buildingTypes().nameId(i) + "_building";
            this.specialBuildingDrawables[i] = SkinDrawables.optional(skin, specialName, null);
        }
        this.levelOnDrawable = skin.getDrawable(UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_ON);
        this.levelOffDrawable = skin.getDrawable(UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF);
        this.noFactoryDrawable = skin.getDrawable(UiProvinceSkin.Drawables.FACTORY_PLUPP_NOFACTORY);
        this.emptySpecialBuildingDrawable = skin.getDrawable(UiProvinceSkin.Drawables.EMPTY_SPEC_BUILDING);
        this.specialTypes = new int[8];
        this.specialLevels = new int[8];
        this.specialMaxLevels = new int[8];
        this.specialStaffPosts = new int[8];
        this.specialStaffFilled = new int[8];
        this.specialEffectiveLevels = new float[8];
        this.specialStorageCapacities = new float[8];
        this.regionProvincesShown = new int[8];
        Arrays.fill(this.regionProvincesShown, -2);
        this.provinceNamesRegion = new ObjectList<>();
        this.navalBaseLevel = new ObjectList<>();
        this.airBaseLevel = new ObjectList<>();
        this.radarStationLevel = new ObjectList<>();
        this.antiAircraftGunsLevel = new ObjectList<>();
        this.colorsBuildings = new ObjectList<>();
        this.specialBuildings = new ObjectList<>();
        Drawable background = skin.getDrawable(UiProvinceSkin.Drawables.BG_PROVINCE);
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.setHeader(skinFonts);
        this.setDataOverview(skinFonts);
        this.setCountriesCoreFlagImages();
    }

    private void setHeader(Skin skinFonts) {
        this.terrainImage = new Image();
        this.terrainImage.setPosition(26, 315);
        this.addActor(this.terrainImage);

        Image overlay = this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.PROV_OVERLAY, 26, 315);
        this.addActor(overlay);

        Button closeButton = new Button(this.skinUi, UiSkin.Styles.Button.CLOSE_BTN);
        closeButton.setPosition(354, 455);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        this.addActor(closeButton);

        Label.LabelStyle labelStyleJockey24 = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_24, Label.LabelStyle.class);
        this.provinceName = new Label("", labelStyleJockey24);
        this.addActor(this.provinceName);

        this.flagImage = this.widgetFactory.createFlagImage(this.skinUi, UiSkin.Drawables.SHIELD_BIG, UiSkin.Drawables.SHIELD_BIG_OVERLAY);
        this.addActor(this.flagImage);

        Label.LabelStyle labelStyleJockey14 = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_14, Label.LabelStyle.class);
        for(int i = 0; i < 8; i++) {
            Label label = new Label("", labelStyleJockey14);
            this.provinceNamesRegion.add(label);
            this.addActor(label);
        }

        this.addSpecialBuildings(this.skin, 26, 25, this);
    }

    private void setDataOverview(Skin skinFonts) {
        Table dataOverview = new Table();
        Drawable background = this.skin.getDrawable(UiProvinceSkin.Drawables.BG_PROVINCE_PAPER);
        dataOverview.setBackground(background);
        dataOverview.setSize(background.getMinWidth(), background.getMinHeight());
        dataOverview.setPosition(12, 75);

        Label.LabelStyle labelStyleJockey16Paper = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_PAPER, Label.LabelStyle.class);
        Label.LabelStyle labelStyleDanger14 = skinFonts.get(FontsSkin.Styles.Label.DANGER_14, Label.LabelStyle.class);
        Label.LabelStyle labelStyleDanger20Dark = skinFonts.get(FontsSkin.Styles.Label.DANGER_20_DARK, Label.LabelStyle.class);
        Label.LabelStyle labelStyleJockey16Blue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_BLUE, Label.LabelStyle.class);

        this.regionName = this.widgetFactory.createLabel(labelStyleJockey16Paper, dataOverview);
        this.resourceImage = this.widgetFactory.createImage(dataOverview);
        this.resourceProduced = this.widgetFactory.createLabel(labelStyleDanger20Dark, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.PROV_POP_ICON, 22, 67, dataOverview);
        this.populationRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.ICON_WORKERS_SMALL, 22, 44, dataOverview);
        this.workersRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.PROV_DI, 22, 22, dataOverview);
        this.developmentIndexRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.ICON_DOLLAR_BIG, 145, 38, dataOverview);
        this.incomeRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.ICON_INDUSTRY_SMALL, 265, 53, dataOverview);
        this.industryRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.PROV_BUILD_INFRA, 162, 160, dataOverview);
        this.infrastructureValue = this.widgetFactory.createLabel(labelStyleDanger20Dark, dataOverview);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_MANPOWER_SMALL_BLUE, 27, 205, dataOverview);
        this.populationProvince = this.widgetFactory.createLabel(labelStyleJockey16Blue, dataOverview);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_MONEY_SMALL_BLUE, 125, 205, dataOverview);
        this.incomeProvince = this.widgetFactory.createLabel(labelStyleJockey16Blue, dataOverview);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_DISSENT_SMALL_BLUE, 199, 205, dataOverview);
        this.revoltRisk = this.widgetFactory.createLabel(labelStyleJockey16Blue, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.ICON_MILITIA_SMALL, 282, 202, dataOverview);
        this.guerillaValue = this.widgetFactory.createLabel(labelStyleDanger20Dark, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.PROV_BUILD_NAVALBASE, 254, 173, dataOverview);
        this.addBuildingLevel(this.skin, this.navalBaseLevel, 285, 175, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.PROV_BUILD_AIRFIELD, 254, 157, dataOverview);
        this.addBuildingLevel(this.skin, this.airBaseLevel, 285, 159, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.PROV_BUILD_AA, 254, 141, dataOverview);
        this.addBuildingLevel(this.skin, this.antiAircraftGunsLevel, 285, 143, dataOverview);
        this.widgetFactory.createImage(this.skin, UiProvinceSkin.Drawables.PROV_BUILD_RADAR, 254, 125, dataOverview);
        this.addBuildingLevel(this.skin, this.radarStationLevel, 285, 127, dataOverview);
        this.addColorBuildings(this.skin, 269, 29, dataOverview);

        this.addActor(dataOverview);
    }

    private void setCountriesCoreFlagImages() {
        this.countriesCoreFlagImages = new ObjectList<>();
        int x = 35;
        int y = 193;
        for(int i = 0; i < 5; i++) {
            FlagImage flagImage = this.widgetFactory.createFlagImage(this.skinUi, UiSkin.Drawables.MINIMASK, UiSkin.Drawables.MINISHIELD);
            flagImage.setPosition(x, y);
            this.countriesCoreFlagImages.add(flagImage);
            x += 38;
        }
    }

    private Table addBuildingLevel(Skin skin, List<Image> buildingLevel, float x, float y, Group parent) {
        return this.widgetFactory.createImageRow(skin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 10, 8, x, y, buildingLevel, parent);
    }

    private Table addColorBuildings(Skin skin, float x, float y, Group parent) {
        return this.widgetFactory.createImageRow(skin, UiProvinceSkin.Drawables.FACTORY_PLUPP_NOFACTORY, 6, 18, x, y, this.colorsBuildings, parent);
    }

    private Table addSpecialBuildings(Skin skin, float x, float y, Group parent) {
        Table row = this.widgetFactory.createImageRow(skin, UiProvinceSkin.Drawables.EMPTY_SPEC_BUILDING, 8, 60, x, y, this.specialBuildings, parent);
        for (int i = 0; i < this.specialBuildings.size(); i++) {
            int slot = i;
            this.specialBuildings.get(i).addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (listener != null && slot < specialCount) {
                        listener.updateHoverTooltip(specialBuildingTooltip(slot));
                    }
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (listener != null) {
                        listener.hideUiTooltip();
                    }
                }
            });
        }
        return row;
    }

    public void setListener(ProvincePanelListener listener) {
        this.listener = listener;
    }

    private String specialBuildingTooltip(int slot) {
        StringBuilder tooltip = new StringBuilder(this.uiCatalog.buildingTypeName(this.specialTypes[slot]));
        tooltip.append("\n").append(this.localisation.get("SPECIAL_BUILDING_LEVEL")).append(" : ").append(this.specialLevels[slot]).append(" / ").append(this.specialMaxLevels[slot]);
        if (this.specialStaffPosts[slot] > 0) {
            tooltip.append("\n").append(this.localisation.get("SPECIAL_BUILDING_STAFF")).append(" : ").append(this.specialStaffFilled[slot]).append(" / ").append(this.specialStaffPosts[slot]);
        }
        tooltip.append("\n").append(this.localisation.get("SPECIAL_BUILDING_EFFECTIVE_LEVEL")).append(" : ").append(String.format("%.2f", this.specialEffectiveLevels[slot]));
        if (this.specialStorageCapacities[slot] > 0f) {
            tooltip.append("\n").append(this.localisation.get("SPECIAL_BUILDING_CAPACITY")).append(" : ").append(Math.round(this.specialStorageCapacities[slot]));
        }
        return tooltip.toString();
    }

    public void setData(ProvinceDetails province) {
        this.setProvinceName(this.uiCatalog.provinceName(province.provinceIndex()));
        this.setRegionName(this.uiCatalog.regionName(province.regionIndex()));
        this.setTerrainImage(province.terrainIndex());
        this.setResourceImage(province.resourceGoodIndex());
        this.setRightAligned(this.populationRegion, this.textFormat.compact(this.text, province.populationRegion()), 135, 83);
        this.setRightAligned(this.workersRegion, this.textFormat.compact(this.text, province.workersRegion()), 135, 61);
        this.setRightAligned(this.developmentIndexRegion, TextFormat.integer(this.text, province.developmentIndexRegion()), 125, 38);
        this.setRightAligned(this.incomeRegion, TextFormat.integer(this.text, province.incomeRegion()), 248, 60);
        this.setRightAligned(this.industryRegion, TextFormat.integer(this.text, province.industryRegion()), 360, 75);
        this.flagImage.setFlag(this.uiCatalog.flag(province.ownerIndex()));
        this.flagImage.setPosition(36, 430);
        this.setFlagCountriesCore(province);
        this.setResourceProduced(province.resourceProduced());
        this.text.clear();
        this.setRightAligned(this.infrastructureValue, this.text.append(province.infrastructureValue()).append('%'), 245, 178);
        this.text.clear();
        if (province.guerillaValue() != 0) {
            this.text.append(province.guerillaValue());
        } else {
            this.text.append('-');
        }
        this.setRightAligned(this.guerillaValue, this.text, 355, 223);
        this.setRightAligned(this.populationProvince, this.textFormat.compact(this.text, province.populationProvince()), 115, 222);
        this.text.clear();
        this.setRightAligned(this.incomeProvince, TextFormat.appendDecimal(this.text, province.incomeProvince(), 1, '.'), 185, 222);
        this.text.clear();
        this.setRightAligned(this.revoltRisk, TextFormat.appendDecimal(this.text, province.revoltRisk(), 1, '.').append('%'), 275, 222);
        this.setProvinceNamesRegion(province);
        this.setBuildingLevel(this.navalBaseLevel, province.navalBaseLevel());
        this.setBuildingLevel(this.airBaseLevel, province.airBaseLevel());
        this.setBuildingLevel(this.radarStationLevel, province.radarStationLevel());
        this.setBuildingLevel(this.antiAircraftGunsLevel, province.antiAircraftGunsLevel());
        this.setColorBuildings(province);
        this.setSpecialBuildings(province);
    }

    private void setRightAligned(Label label, CharArray value, float right, float y) {
        if (!label.textEquals(value)) {
            label.setText(value);
        }
        label.setPosition(right - label.getMinWidth(), y);
    }

    private void setResourceProduced(float resourceProduced) {
        if (resourceProduced == -1f) {
            this.text.clear();
            this.text.append('-');
        } else {
            this.textFormat.fixed(this.text, resourceProduced, 4);
        }
        this.setRightAligned(this.resourceProduced, this.text, 136, 178);
    }

    private void setResourceImage(int goodIndex) {
        Drawable resource = goodIndex >= 0 ? this.resourceDrawables[goodIndex] : this.noResourceDrawable;
        this.resourceImage.setDrawable(resource);
        this.resourceImage.setSize(resource.getMinWidth(), resource.getMinHeight());
        this.resourceImage.setPosition(22, 160);
    }

    private void setProvinceName(String name) {
        this.provinceName.setText(name);
        this.provinceName.setPosition(this.getWidth() / 2 - this.provinceName.getMinWidth() / 2, 471);
    }

    private void setRegionName(String name) {
        this.regionName.setText(name);
        this.regionName.setPosition(this.regionName.getParent().getWidth() / 2 - this.regionName.getMinWidth() / 2, 104);
    }

    private void setTerrainImage(int terrainIndex) {
        Drawable terrain = terrainIndex >= 0 ? this.terrainDrawables[terrainIndex] : null;
        this.terrainImage.setDrawable(terrain);
        if (terrain != null) {
            this.terrainImage.setSize(terrain.getMinWidth(), terrain.getMinHeight());
        }
    }

    private void setFlagCountriesCore(ProvinceDetails province) {
        for (int i = 0; i < this.countriesCoreFlagImages.size(); i++) {
            FlagImage coreFlag = this.countriesCoreFlagImages.get(i);
            if (i < province.coreCount()) {
                coreFlag.setFlag(this.uiCatalog.plainFlag(province.core(i)));
                if (coreFlag.getParent() != this) {
                    this.addActor(coreFlag);
                }
            } else if (coreFlag.getParent() != null) {
                coreFlag.remove();
            }
        }
    }

    private void setProvinceNamesRegion(ProvinceDetails province) {
        float y = this.getHeight() - 77;
        for (int i = 0; i < this.provinceNamesRegion.size(); i++) {
            Label label = this.provinceNamesRegion.get(i);
            int provinceIndex = i < province.regionProvinceCount() ? province.regionProvince(i) : -1;
            if (this.regionProvincesShown[i] != provinceIndex) {
                this.regionProvincesShown[i] = provinceIndex;
                if (provinceIndex >= 0) {
                    label.setText(this.uiCatalog.provinceName(provinceIndex));
                    LabelUtils.truncateLabel(label, 85);
                } else {
                    label.setText("");
                }
            }
            label.setPosition(295, y);
            y -= 17;
        }
    }

    private void setBuildingLevel(List<Image> buildingLevel, int level) {
        for (int i = 0; i < buildingLevel.size(); i++) {
            buildingLevel.get(i).setDrawable(i < level ? this.levelOnDrawable : this.levelOffDrawable);
        }
    }

    private void setColorBuildings(ProvinceDetails province) {
        int shown = 0;
        for (int i = 0; i < province.economyBuildingCount() && shown < this.colorsBuildings.size(); i++) {
            Drawable color = this.factoryColorDrawables[province.economyBuildingType(i)];
            if (color != null) {
                this.colorsBuildings.get(shown++).setDrawable(color);
            }
        }
        for (int i = shown; i < this.colorsBuildings.size(); i++) {
            this.colorsBuildings.get(i).setDrawable(this.noFactoryDrawable);
        }
    }

    private void setSpecialBuildings(ProvinceDetails province) {
        this.specialCount = Math.min(province.specialCount(), this.specialTypes.length);
        for (int i = 0; i < this.specialCount; i++) {
            this.specialTypes[i] = province.specialType(i);
            this.specialLevels[i] = province.specialLevel(i);
            this.specialMaxLevels[i] = province.specialMaxLevel(i);
            this.specialStaffPosts[i] = province.specialStaffPosts(i);
            this.specialStaffFilled[i] = province.specialStaffFilled(i);
            this.specialEffectiveLevels[i] = province.specialEffectiveLevel(i);
            this.specialStorageCapacities[i] = province.specialStorageCapacity(i);
        }
        for (int i = 0; i < this.specialBuildings.size(); i++) {
            Image image = this.specialBuildings.get(i);
            Drawable drawable = i < this.specialCount ? this.specialBuildingDrawables[this.specialTypes[i]] : null;
            image.setDrawable(drawable != null ? drawable : this.emptySpecialBuildingDrawable);
        }
    }
}
