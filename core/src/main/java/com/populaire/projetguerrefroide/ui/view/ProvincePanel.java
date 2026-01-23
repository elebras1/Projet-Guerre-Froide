package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.dto.DevelopementBuildingLevelDto;
import com.populaire.projetguerrefroide.service.LabelStylePool;
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

    public ProvincePanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation) {
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
        Drawable background = skin.getDrawable("bg_province");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.setHeader(labelStylePool);
        this.setDataOverview(labelStylePool);
        this.setCountriesCoreFlagImages();
    }

    private void setHeader(LabelStylePool labelStylePool) {
        this.terrainImage = new Image();
        this.terrainImage.setPosition(26, 315);
        this.addActor(this.terrainImage);

        Image overlay = this.widgetFactory.createImage(this.skin, "prov_overlay", 26, 315);
        this.addActor(overlay);

        Button closeButton = new Button(this.skinUi, "close_btn");
        closeButton.setPosition(354, 455);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ProvincePanel.this.remove();
            }
        });
        this.addActor(closeButton);

        Label.LabelStyle labelStyleJockey24 = labelStylePool.get("jockey_24");
        this.provinceName = new Label("", labelStyleJockey24);
        this.addActor(this.provinceName);

        this.flagImage = this.widgetFactory.createFlagImage(this.skinUi, "shield_big", "shield_big_overlay");
        this.addActor(this.flagImage);

        Label.LabelStyle labelStyleJockey14 = labelStylePool.get("jockey_14");
        for(int i = 0; i < 8; i++) {
            Label label = new Label("", labelStyleJockey14);
            this.provinceNamesRegion.add(label);
            this.addActor(label);
        }

        this.addSpecialBuildings(this.skin, 26, 25, this);
    }

    private void setDataOverview(LabelStylePool labelStylePool) {
        Table dataOverview = new Table();
        Drawable background = this.skin.getDrawable("bg_province_paper");
        dataOverview.setBackground(background);
        dataOverview.setSize(background.getMinWidth(), background.getMinHeight());
        dataOverview.setPosition(12, 75);

        Label.LabelStyle labelStyleJockey16Paper = labelStylePool.get("jockey_16_paper");
        Label.LabelStyle labelStyleDanger14 = labelStylePool.get("danger_14");
        Label.LabelStyle labelStyleDanger20Dark = labelStylePool.get("danger_20_dark");
        Label.LabelStyle labelStyleJockey16Blue = labelStylePool.get("jockey_16", "blue");

        this.regionName = this.widgetFactory.createLabel(labelStyleJockey16Paper, dataOverview);
        this.resourceImage = this.widgetFactory.createImage(dataOverview);
        this.resourceProduced = this.widgetFactory.createLabel(labelStyleDanger20Dark, dataOverview);
        this.widgetFactory.createImage(this.skin, "prov_pop_icon", 22, 67, dataOverview);
        this.populationRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, "icon_workers_small", 22, 44, dataOverview);
        this.workersRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, "prov_DI", 22, 22, dataOverview);
        this.developmentIndexRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, "icon_dollar_big", 145, 38, dataOverview);
        this.incomeRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, "icon_industry_small", 265, 53, dataOverview);
        this.industryRegion = this.widgetFactory.createLabel(labelStyleDanger14, dataOverview);
        this.widgetFactory.createImage(this.skin, "prov_build_infra", 162, 160, dataOverview);
        this.infrastructureValue = this.widgetFactory.createLabel(labelStyleDanger20Dark, dataOverview);
        this.widgetFactory.createImage(this.skinUi, "icon_manpower_small_blue", 27, 205, dataOverview);
        this.populationProvince = this.widgetFactory.createLabel(labelStyleJockey16Blue, dataOverview);
        this.widgetFactory.createImage(this.skinUi, "icon_money_small_blue", 125, 205, dataOverview);
        this.incomeProvince = this.widgetFactory.createLabel(labelStyleJockey16Blue, dataOverview);
        this.widgetFactory.createImage(this.skinUi, "icon_dissent_small_blue", 199, 205, dataOverview);
        this.revoltRisk = this.widgetFactory.createLabel(labelStyleJockey16Blue, dataOverview);
        this.widgetFactory.createImage(this.skin, "icon_militia_small", 282, 202, dataOverview);
        this.guerillaValue = this.widgetFactory.createLabel(labelStyleDanger20Dark, dataOverview);
        this.widgetFactory.createImage(this.skin, "prov_build_navalbase", 254, 173, dataOverview);
        this.addBuildingLevel(this.skin, this.navalBaseLevel, 285, 175, dataOverview);
        this.widgetFactory.createImage(this.skin, "prov_build_airfield", 254, 157, dataOverview);
        this.addBuildingLevel(this.skin, this.airBaseLevel, 285, 159, dataOverview);
        this.widgetFactory.createImage(this.skin, "prov_build_aa", 254, 141, dataOverview);
        this.addBuildingLevel(this.skin, this.antiAircraftGunsLevel, 285, 143, dataOverview);
        this.widgetFactory.createImage(this.skin, "prov_build_radar", 254, 125, dataOverview);
        this.addBuildingLevel(this.skin, this.radarStationLevel, 285, 127, dataOverview);
        this.addColorBuildings(this.skin, 269, 29, dataOverview);

        this.addActor(dataOverview);
    }

    private void setCountriesCoreFlagImages() {
        this.countriesCoreFlagImages = new ObjectList<>();
        int x = 35;
        int y = 193;
        for(int i = 0; i < 5; i++) {
            FlagImage flagImage = this.widgetFactory.createFlagImage(this.skinUi, "minimask", "minishield");
            flagImage.setPosition(x, y);
            this.countriesCoreFlagImages.add(flagImage);
            x += 38;
        }
    }

    private Table addBuildingLevel(Skin skin, List<Image> buildingLevel, float x, float y, Group parent) {
        return this.widgetFactory.createImageRow(skin, "prov_building_plupp_off", 10, 8, x, y, buildingLevel, parent);
    }

    private Table addColorBuildings(Skin skin, float x, float y, Group parent) {
        return this.widgetFactory.createImageRow(skin, "factory_plupp_nofactory", 6, 18, x, y, this.colorsBuildings, parent);
    }

    private Table addSpecialBuildings(Skin skin, float x, float y, Group parent) {
        return this.widgetFactory.createImageRow(skin, "empty_spec_building", 8, 60, x, y, this.specialBuildings, parent);
    }

    public void setData(ProvinceDto provinceDto) {
        this.setProvinceName(provinceDto.provinceId());
        this.setRegionName(provinceDto.regionId());
        this.setTerrainImage(provinceDto.terrainImage());
        this.setResourceImage(provinceDto.resourceImage());
        this.setPopulationRegion(provinceDto.populationRegion());
        this.setWorkersRegion(provinceDto.workersRegion());
        this.setDevelopmentIndexRegion(provinceDto.developmentIndexRegion());
        this.setIncomeRegion(provinceDto.incomeRegion());
        this.setIndustryRegion(provinceDto.industryRegion());
        this.setFlagImage(provinceDto.countryId(), provinceDto.colonizerId());
        this.setFlagCountriesCore(provinceDto.flagCountriesCore());
        this.setResourceProduced(provinceDto.resourceProduced());
        this.setInfrastructureValue(provinceDto.infrastructureValue());
        this.setGuerillaValue(provinceDto.guerillaValue());
        this.setPopulationProvince(provinceDto.populationProvince());
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
            this.resourceProduced.setText(ValueFormatter.formatValue(resourceProduced));
        }
        this.resourceProduced.setPosition(136 - this.resourceProduced.getMinWidth(), 178);
    }

    private void setResourceImage(String name) {
        Drawable resource;
        if(name != null) {
            resource = this.skinUi.getDrawable("resource_" + name + "_small");
        } else {
            resource = this.skinUi.getDrawable("resource_none_small");
        }

        this.resourceImage.setDrawable(resource);
        this.resourceImage.setSize(resource.getMinWidth(), resource.getMinHeight());
        this.resourceImage.setPosition(22, 160);
    }

    private void setProvinceName(String name) {
        this.provinceName.setText(this.localisation.get(name));
        this.provinceName.setPosition(this.getWidth() / 2 - this.provinceName.getMinWidth() / 2, 471);
    }

    private void setRegionName(String name) {
        this.regionName.setText(this.localisation.get(name));
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

    private void setFlagImage(String idCountry, String idColonizer) {
        this.flagImage.setFlag(this.widgetFactory.getFlagTextureRegion(this.skinFlags, idCountry, idColonizer));
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
                image.setDrawable(this.skin.getDrawable("prov_building_plupp_on"));
            } else {
                image.setDrawable(this.skin.getDrawable("prov_building_plupp_off"));
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
                image.setDrawable(this.skin.getDrawable("factory_plupp_nofactory"));
            }
        }
    }

    private void setSpecialBuildings(List<String> specialBuildingsName) {
        for(int i = 0; i < this.specialBuildings.size(); i++) {
            Image image = this.specialBuildings.get(i);
            if(i < specialBuildingsName.size()) {
                String name = specialBuildingsName.get(i);
                image.setDrawable(this.skin.getDrawable(name + "_building"));
            } else {
                image.setDrawable(this.skin.getDrawable("empty_spec_building"));
            }
        }
    }
}
