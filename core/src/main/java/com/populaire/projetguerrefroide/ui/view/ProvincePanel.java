package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.dto.DevelopementBuildingLevelDto;
import com.populaire.projetguerrefroide.dto.SpecialBuildingDto;
import com.populaire.projetguerrefroide.screen.listener.ProvincePanelListener;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.LabelUtils;
import com.populaire.projetguerrefroide.util.ValueFormatter;

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
    private final List<SpecialBuildingDto> specialBuildingData;
    private ProvincePanelListener listener;

    public ProvincePanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinFonts, Map<String, String> localisation) {
        this.widgetFactory = widgetFactory;
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        this.localisation = localisation;
        this.provinceNamesRegion = new ObjectList<>();
        this.navalBaseLevel = new ObjectList<>();
        this.airBaseLevel = new ObjectList<>();
        this.radarStationLevel = new ObjectList<>();
        this.antiAircraftGunsLevel = new ObjectList<>();
        this.colorsBuildings = new ObjectList<>();
        this.specialBuildings = new ObjectList<>();
        this.specialBuildingData = new ObjectList<>();
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
                    if (listener != null && slot < specialBuildingData.size()) {
                        listener.updateHoverTooltip(specialBuildingTooltip(specialBuildingData.get(slot)));
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

    private String specialBuildingTooltip(SpecialBuildingDto building) {
        StringBuilder text = new StringBuilder(this.localisation.get(building.nameId()));
        text.append("\n").append(this.localisation.get("SPECIAL_BUILDING_LEVEL")).append(" : ").append(building.level()).append(" / ").append(building.maxLevel());
        if (building.staffPosts() > 0) {
            text.append("\n").append(this.localisation.get("SPECIAL_BUILDING_STAFF")).append(" : ").append(building.staffFilled()).append(" / ").append(building.staffPosts());
        }
        text.append("\n").append(this.localisation.get("SPECIAL_BUILDING_EFFECTIVE_LEVEL")).append(" : ").append(String.format("%.2f", building.effectiveLevel()));
        if (building.storageCapacity() > 0f) {
            text.append("\n").append(this.localisation.get("SPECIAL_BUILDING_CAPACITY")).append(" : ").append(Math.round(building.storageCapacity()));
        }
        return text.toString();
    }

    public void setData(ProvinceDto provinceDto) {
        this.setProvinceName(provinceDto.provinceNameId());
        this.setRegionName(provinceDto.regionNameId());
        this.setTerrainImage(provinceDto.terrainImage());
        this.setResourceImage(provinceDto.resourceImage());
        this.setPopulationRegion(ValueFormatter.format(provinceDto.populationRegion(), this.localisation));
        this.setWorkersRegion(ValueFormatter.format(provinceDto.workersRegion(), this.localisation));
        this.setDevelopmentIndexRegion(provinceDto.developmentIndexRegion());
        this.setIncomeRegion(provinceDto.incomeRegion());
        this.setIndustryRegion(provinceDto.industryRegion());
        this.setFlagImage(provinceDto.countryNameId(), provinceDto.colonizerNameId());
        this.setFlagCountriesCore(provinceDto.flagCountriesCore());
        this.setResourceProduced(provinceDto.resourceProduced());
        this.setInfrastructureValue(provinceDto.infrastructureValue());
        this.setGuerillaValue(provinceDto.guerillaValue());
        this.setPopulationProvince(ValueFormatter.format(provinceDto.populationProvince(), this.localisation));
        this.setIncomeProvince(provinceDto.incomeProvince());
        this.setRevoltRisk(provinceDto.revoltRisk());
        this.setProvinceNamesRegion(provinceDto.provinceIdsRegion());
        DevelopementBuildingLevelDto developementBuildingLevelDto = provinceDto.developmentBuildingLevel();
        this.setBuildingLevel(this.navalBaseLevel, developementBuildingLevelDto.navalBaseLevel());
        this.setBuildingLevel(this.airBaseLevel, developementBuildingLevelDto.airBaseLevel());
        this.setBuildingLevel(this.radarStationLevel, developementBuildingLevelDto.radarStationLevel());
        this.setBuildingLevel(this.antiAircraftGunsLevel, developementBuildingLevelDto.antiAircraftGunsLevel());
        this.setColorBuildings(provinceDto.colorBuildings());
        this.setSpecialBuildings(provinceDto.specialBuildings());
    }

    public void setResourceProduced(float resourceProduced) {
        if(resourceProduced == -1f) {
            this.resourceProduced.setText("-");
        } else {
            this.resourceProduced.setText(ValueFormatter.format(resourceProduced));
        }
        this.resourceProduced.setPosition(136 - this.resourceProduced.getMinWidth(), 178);
    }

    private void setResourceImage(String name) {
        Drawable resource;
        if(name != null) {
            resource = this.skinUi.getDrawable("good_" + name + "_small");
        } else {
            resource = this.skinUi.getDrawable(UiSkin.Drawables.GOOD_NONE_SMALL);
        }

        this.resourceImage.setDrawable(resource);
        this.resourceImage.setSize(resource.getMinWidth(), resource.getMinHeight());
        this.resourceImage.setPosition(22, 160);
    }

    private void setProvinceName(String nameId) {
        this.provinceName.setText(this.localisation.get(nameId));
        this.provinceName.setPosition(this.getWidth() / 2 - this.provinceName.getMinWidth() / 2, 471);
    }

    private void setRegionName(String nameId) {
        this.regionName.setText(this.localisation.get(nameId));
        this.regionName.setPosition(this.regionName.getParent().getWidth() / 2 - this.regionName.getMinWidth() / 2, 104);
    }

    private void setTerrainImage(String name) {
        Drawable terrain = this.skin.getDrawable("prov_terrain_" + name);
        this.terrainImage.setDrawable(terrain);
        this.terrainImage.setSize(terrain.getMinWidth(), terrain.getMinHeight());
    }

    private void setPopulationRegion(String population) {
        this.populationRegion.setText(population);
        this.populationRegion.setPosition(135 - this.populationRegion.getMinWidth(), 83);
    }

    private void setWorkersRegion(String workers) {
        this.workersRegion.setText(workers);
        this.workersRegion.setPosition(135 - this.workersRegion.getMinWidth(), 61);
    }

    private void setDevelopmentIndexRegion(int developmentIndex) {
        this.developmentIndexRegion.setText(developmentIndex);
        this.developmentIndexRegion.setPosition(125 - this.developmentIndexRegion.getMinWidth(), 38);
    }

    private void setIncomeRegion(int income) {
        this.incomeRegion.setText(income);
        this.incomeRegion.setPosition(248 - this.incomeRegion.getMinWidth(), 60);
    }

    private void setIndustryRegion(int industry) {
        this.industryRegion.setText(industry);
        this.industryRegion.setPosition(360 - this.industryRegion.getMinWidth(), 75);
    }

    private void setFlagImage(String countryNameId, String colonizerNameId) {
        this.flagImage.setFlag(this.widgetFactory.getFlagTextureRegion(this.skinFlags, countryNameId, colonizerNameId));
        this.flagImage.setPosition(36, 430);
    }

    private void setFlagCountriesCore(List<String> countriesCore) {
        for(int i = 0; i < this.countriesCoreFlagImages.size(); i++) {
            if(i < countriesCore.size()) {
                this.countriesCoreFlagImages.get(i).setFlag(this.skinFlags.getRegion(countriesCore.get(i)));
                this.addActor(this.countriesCoreFlagImages.get(i));
            } else {
                this.countriesCoreFlagImages.get(i).remove();
            }
        }
    }

    private void setInfrastructureValue(int infrastructureValue) {
        this.infrastructureValue.setText(infrastructureValue + "%");
        this.infrastructureValue.setPosition(245 - this.infrastructureValue.getMinWidth(), 178);
    }

    private void setGuerillaValue(float guerillaValue) {
        String text = "-";
        if(guerillaValue != 0f) {
            text = String.valueOf(guerillaValue);
        }
        this.guerillaValue.setText(text);
        this.guerillaValue.setPosition(355 - this.guerillaValue.getMinWidth(), 223);
    }

    private void setPopulationProvince(String populationProvince) {
        this.populationProvince.setText(populationProvince);
        this.populationProvince.setPosition(115 - this.populationProvince.getMinWidth(), 222);
    }

    private void setIncomeProvince(float incomeProvince) {
        this.incomeProvince.setText(String.valueOf(incomeProvince));
        this.incomeProvince.setPosition(185 - this.incomeProvince.getMinWidth(), 222);
    }

    private void setRevoltRisk(float revoltRisk) {
        this.revoltRisk.setText(revoltRisk + "%");
        this.revoltRisk.setPosition(275 - this.revoltRisk.getMinWidth(), 222);
    }

    private void setProvinceNamesRegion(List<String> provincesRegion) {
        float y = this.getHeight() - 77;
        for(int i = 0; i < this.provinceNamesRegion.size(); i++) {
            Label label = this.provinceNamesRegion.get(i);
            if(i < provincesRegion.size()) {
                label.setText(this.localisation.get(provincesRegion.get(i)));
                LabelUtils.truncateLabel(label, 85);
            } else {
                label.setText("");
            }
            label.setPosition(295, y);
            y -= 17;
        }
    }

    private void setBuildingLevel(List<Image> buildingLevel, byte level) {
        for(int i = 0; i < buildingLevel.size(); i++) {
            Image image = buildingLevel.get(i);
            if(i < level) {
                image.setDrawable(this.skin.getDrawable(UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_ON));
            } else {
                image.setDrawable(this.skin.getDrawable(UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF));
            }
        }
    }

    private void setColorBuildings(List<String> colorsBuildings) {
        for (int i = 0; i < this.colorsBuildings.size(); i++) {
            Image image = this.colorsBuildings.get(i);
            if (i < colorsBuildings.size()) {
                String color = colorsBuildings.get(i);
                image.setDrawable(this.skin.getDrawable("factory_plupp_" + color));
            } else {
                image.setDrawable(this.skin.getDrawable(UiProvinceSkin.Drawables.FACTORY_PLUPP_NOFACTORY));
            }
        }
    }

    private void setSpecialBuildings(List<SpecialBuildingDto> specialBuildings) {
        this.specialBuildingData.clear();
        this.specialBuildingData.addAll(specialBuildings);
        for(int i = 0; i < this.specialBuildings.size(); i++) {
            Image image = this.specialBuildings.get(i);
            if(i < specialBuildings.size()) {
                String name = specialBuildings.get(i).nameId();
                image.setDrawable(this.skin.getDrawable(name + "_building"));
            } else {
                image.setDrawable(this.skin.getDrawable(UiProvinceSkin.Drawables.EMPTY_SPEC_BUILDING));
            }
        }
    }
}
