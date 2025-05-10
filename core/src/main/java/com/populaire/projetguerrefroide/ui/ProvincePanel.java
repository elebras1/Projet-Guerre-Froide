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
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.util.LabelUtils;

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

    public ProvincePanel(Skin skin, Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation) {
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        this.localisation = localisation;
        this.navalBaseLevel = new ObjectList<>();
        this.airBaseLevel = new ObjectList<>();
        this.radarStationLevel = new ObjectList<>();
        this.antiAircraftGunsLevel = new ObjectList<>();
        this.colorsBuildings = new ObjectList<>();
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

        Image overlay = this.createImage(this.skin, "prov_overlay", 26, 315);

        Button closeButton = new Button(this.skinUi, "close_btn");
        closeButton.setPosition(354, 455);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ProvincePanel.this.remove();
            }
        });

        Label.LabelStyle labelStyleJockey24 = labelStylePool.getLabelStyle("jockey_24");
        this.provinceName = new Label("", labelStyleJockey24);

        this.flagImage = this.createFlagImage(this.skinUi, "shield_big", "shield_big_overlay", (short) 64, (short) 64);

        this.addActor(this.terrainImage);
        this.addActor(overlay);
        this.addActor(closeButton);
        this.addActor(this.provinceName);
        this.addActor(this.flagImage);

        Label.LabelStyle labelStyleJockey14 = labelStylePool.getLabelStyle("jockey_14");
        this.provinceNamesRegion = new ObjectList<>();
        for(int i = 0; i < 8; i++) {
            Label label = new Label("", labelStyleJockey14);
            this.provinceNamesRegion.add(label);
            this.addActor(label);
        }
    }

    private void setDataOverview(LabelStylePool labelStylePool) {
        Table dataOverview = new Table();
        Drawable background = this.skin.getDrawable("bg_province_paper");
        dataOverview.setBackground(background);
        dataOverview.setSize(background.getMinWidth(), background.getMinHeight());
        dataOverview.setPosition(12, 75);

        Label.LabelStyle labelStyleJockey16Paper = labelStylePool.getLabelStyle("jockey_16_paper");
        Label.LabelStyle labelStyleDanger14 = labelStylePool.getLabelStyle("danger_14");
        Label.LabelStyle labelStyleDanger20Dark = labelStylePool.getLabelStyle("danger_20_dark");
        Label.LabelStyle labelStyleJockey16Blue = labelStylePool.getLabelStyle("jockey_16", "blue");

        this.regionName = new Label("", labelStyleJockey16Paper);
        dataOverview.addActor(this.regionName);
        this.resourceImage = new Image();
        dataOverview.addActor(this.resourceImage);
        this.resourceProduced = new Label("", labelStyleDanger20Dark);
        dataOverview.addActor(this.resourceProduced);
        dataOverview.addActor(this.createImage(this.skin, "prov_pop_icon", 22, 67));
        this.populationRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.populationRegion);
        dataOverview.addActor(this.createImage(this.skin, "icon_workers_small", 22, 44));
        this.workersRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.workersRegion);
        dataOverview.addActor(this.createImage(this.skin, "prov_DI", 22, 22));
        this.developmentIndexRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.developmentIndexRegion);
        dataOverview.addActor(this.createImage(this.skin, "icon_dollar_big", 145, 38));
        this.incomeRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.incomeRegion);
        dataOverview.addActor(this.createImage(this.skin, "icon_industry_small", 265, 53));
        this.industryRegion = new Label("", labelStyleDanger14);
        dataOverview.addActor(this.industryRegion);
        dataOverview.addActor(this.createImage(this.skin, "prov_build_infra", 162, 160));
        this.infrastructureValue = new Label("", labelStyleDanger20Dark);
        dataOverview.addActor(this.infrastructureValue);
        dataOverview.addActor(this.createImage(this.skinUi, "icon_manpower_small_blue", 27, 205));
        this.populationProvince = new Label("", labelStyleJockey16Blue);
        dataOverview.addActor(this.populationProvince);
        dataOverview.addActor(this.createImage(this.skinUi, "icon_money_small_blue", 125, 205));
        this.incomeProvince = new Label("", labelStyleJockey16Blue);
        dataOverview.addActor(this.incomeProvince);
        dataOverview.addActor(this.createImage(this.skinUi, "icon_dissent_small_blue", 199, 205));
        this.revoltRisk = new Label("", labelStyleJockey16Blue);
        dataOverview.addActor(this.revoltRisk);
        dataOverview.addActor(this.createImage(this.skin, "icon_militia_small", 282, 202));
        this.guerillaValue = new Label("", labelStyleDanger20Dark);
        dataOverview.addActor(this.guerillaValue);
        dataOverview.addActor(this.createImage(this.skin, "prov_build_navalbase", 254, 173));
        dataOverview.addActor(this.createBuildingLevel(this.skin, this.navalBaseLevel, 285, 175));
        dataOverview.addActor(this.createImage(this.skin, "prov_build_airfield", 254, 157));
        dataOverview.addActor(this.createBuildingLevel(this.skin, this.airBaseLevel, 285, 159));
        dataOverview.addActor(this.createImage(this.skin, "prov_build_aa", 254, 141));
        dataOverview.addActor(this.createBuildingLevel(this.skin, this.antiAircraftGunsLevel, 285, 143));
        dataOverview.addActor(this.createImage(this.skin, "prov_build_radar", 254, 125));
        dataOverview.addActor(this.createBuildingLevel(this.skin, this.radarStationLevel, 285, 127));
        dataOverview.addActor(this.createColorBuildings(this.skin, 269, 29));

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

    private Table createBuildingLevel(Skin skin, List<Image> buildingLevel, float x, float y) {
        Table table = new Table();
        float imageX = 0;
        float imageY = 0;
        for(int i = 0; i < 10; i++) {
            Image image = this.createImage(skin, "prov_building_plupp_off", imageX, imageY);
            buildingLevel.add(image);
            table.addActor(image);
            imageX += 8;
        }
        table.setPosition(x, y);
        return table;
    }

    private Table createColorBuildings(Skin skin, float x, float y) {
        Table table = new Table();
        float imageX = 0;
        float imageY = 0;
        for(int i = 0; i < 6; i++) {
            Image image = this.createImage(skin, "factory_plupp_nofactory", imageX, imageY);
            this.colorsBuildings.add(image);
            table.addActor(image);
            imageX += 18;
        }
        table.setPosition(x, y);
        return table;
    }

    public void setData(ProvinceDto provinceDto) {
        this.setProvinceName(provinceDto.getProvinceId());
        this.setRegionName(provinceDto.getRegionId());
        this.setTerrainImage(provinceDto.getTerrainImage());
        this.setResourceImage(provinceDto.getResourceImage());
        this.setPopulationRegion(provinceDto.getPopulationRegion());
        this.setWorkersRegion(provinceDto.getWorkersRegion());
        this.setDevelopmentIndexRegion(provinceDto.getDevelopmentIndexRegion());
        this.setIncomeRegion(provinceDto.getIncomeRegion());
        this.setIndustryRegion(provinceDto.getIndustryRegion());
        this.setFlagImage(provinceDto.getFlagImage());
        this.setFlagCountriesCore(provinceDto.getFlagCountriesCore());
        this.setResourceProduced(provinceDto.getResourceProduced());
        this.setInfrastructureValue(provinceDto.getInfrastructureValue());
        this.setGuerillaValue(provinceDto.getGuerillaValue());
        this.setPopulationProvince(provinceDto.getPopulationProvince());
        this.setIncomeProvince(provinceDto.getIncomeProvince());
        this.setRevoltRisk(provinceDto.getRevoltRisk());
        this.setProvinceNamesRegion(provinceDto.getProvinceIdsRegion());
        this.setBuildingLevel(this.navalBaseLevel, provinceDto.getNavalBaseLevel());
        this.setBuildingLevel(this.airBaseLevel, provinceDto.getAirBaseLevel());
        this.setBuildingLevel(this.radarStationLevel, provinceDto.getRadarStationLevel());
        this.setBuildingLevel(this.antiAircraftGunsLevel, provinceDto.getAntiAircraftGunsLevel());
        this.setColorBuildings(provinceDto.getColorsBuildings());
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

    private void setFlagImage(String idCountry) {
        this.flagImage.setFlag(this.skinFlags.getRegion(idCountry));
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

    private void setResourceProduced(float resourceProduced) {
        this.resourceProduced.setText(String.valueOf(resourceProduced));
        this.resourceProduced.setPosition(136 - this.resourceProduced.getMinWidth(), 178);
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
}
