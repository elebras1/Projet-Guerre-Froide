package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.response.ProvinceDetails;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.view.BuildingUtils;
import com.populaire.projetguerrefroide.ui.widget.OverlappingBox;
import com.populaire.projetguerrefroide.ui.widget.SkinDrawables;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class FolderView extends UiWindow {
    private static final int BUILDING_ROWS = 4;
    private static final int FACTORY_SLOTS = 7;
    private static final int CLAIM_SLOTS = 8;
    private final Image folderBg;
    private final Image iconPop;
    private final Label textPop;
    private final Image iconIncome;
    private final Label textIncome;
    private final Image iconRevoltRisk;
    private final Label textRevoltRisk;
    private final Button draftMilitia;
    private final Label textSupporter;
    private final Image iconNoResource;
    private final Image iconResource;
    private final Label textResource;
    private final Label textInfra;
    private final UiList<BuildingMember> buildingList;
    private final OverlappingBox claims;
    private final OverlappingBox alliedCooperation;
    private final Image cooperationBg;
    private final Label coopLabel;
    private final UiList<AlliedCoopItem> cooperationList;
    private final Label regionText;
    private final Image regionPop;
    private final Image regionWorkers;
    private final Image regionDi;
    private final Label regionPopVal;
    private final Label regionWorkersVal;
    private final Label regionDiVal;
    private final Image iconArrow;
    private final Image bigDollar;
    private final Label regionIncome;
    private final Image regionIndustryIcon;
    private final Label regionIndustryText;
    private final OverlappingBox regionFactoryBox;
    private final UiCatalog uiCatalog;
    private final TextFormat textFormat;
    private final CharArray text;
    private final ProvClaimsEntry[] claimEntries;
    private final FactoryPluppEntry[] factoryEntries;
    private final Drawable[] resourceDrawables;
    private final Drawable noResourceDrawable;
    private final Drawable noFactoryDrawable;
    private int guerrillaValue;

    public FolderView(UiBuilder ui, UiCatalog uiCatalog, TextFormat textFormat, ProvincePanelListener listener) {
        super(ui, 388f, 228f);
        this.uiCatalog = uiCatalog;
        this.textFormat = textFormat;
        this.text = new CharArray(32);
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        Skin uiSkin = ui.skin(Skins.UI);
        this.folderBg = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.BG_PROVINCE_PAPER, -5f, -19f, UiAnchor.UPPER_LEFT);
        this.iconPop = ui.image(this, uiSkin, UiSkin.Drawables.ICON_MANPOWER_SMALL_BLUE, 15f, -3f, UiAnchor.UPPER_LEFT);
        this.textPop = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 6f, 4f, 140f, 16f, Align.top, UiAnchor.UPPER_LEFT);
        this.iconIncome = ui.image(this, uiSkin, UiSkin.Drawables.ICON_MONEY_SMALL_BLUE, 130f, -3f, UiAnchor.UPPER_LEFT);
        this.textIncome = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 163f, 4f, 57f, 16f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.iconRevoltRisk = ui.image(this, uiSkin, UiSkin.Drawables.ICON_DISSENT_SMALL_BLUE, 208f, -3f, UiAnchor.UPPER_LEFT);
        this.textRevoltRisk = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 235f, 4f, 37f, 16f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.draftMilitia = ui.flatButton(this, provinceSkin, UiProvinceSkin.Drawables.ICON_MILITIA_SMALL, 280f, -5f, UiAnchor.UPPER_LEFT);
        this.textSupporter = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14_DARK, 311f, 0f, 37f, 16f, Align.top, UiAnchor.UPPER_LEFT);
        this.iconNoResource = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 63f, 41f, UiAnchor.UPPER_LEFT);
        this.iconResource = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 21f, 45f, UiAnchor.UPPER_LEFT);
        this.textResource = ui.label(this, "", FontsSkin.Styles.Label.DANGER_20_DARK, 60f, 46f, 65f, 25f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.textInfra = ui.label(this, "", FontsSkin.Styles.Label.DANGER_20_DARK, 170f, 46f, 55f, 25f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.buildingList = ui.list(this, 248f, 41f, 200f, 100f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, BuildingMember::new);
        this.claims = ui.overlappingBox(this, 15f, 83f, 120f, 32f, 1f, false, UiAnchor.UPPER_LEFT);
        this.alliedCooperation = ui.overlappingBox(this, 128f, 160f, 90f, 32f, 1f, false, UiAnchor.UPPER_LEFT);
        this.cooperationBg = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.COOP_BG, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.coopLabel = ui.label(this, "COOP_LABEL", FontsSkin.Styles.Label.ARIAL12_BOLD, 255f, 22f, 190f, 20f, Align.top, UiAnchor.UPPER_LEFT);
        this.cooperationList = ui.list(this, 250f, 45f, 181f, 340f, UiAnchor.UPPER_LEFT, 2f, 0f, 0f, AlliedCoopItem::new);
        this.regionText = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_PAPER, 110f, 120f, 153f, 20f, Align.top, UiAnchor.UPPER_LEFT);
        this.regionPop = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_POP_ICON, 16f, 141f, UiAnchor.UPPER_LEFT);
        this.regionWorkers = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_EMPLOYEE_ICON, 16f, 163f, UiAnchor.UPPER_LEFT);
        this.regionDi = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_DI, 15f, 187f, UiAnchor.UPPER_LEFT);
        this.regionPopVal = ui.label(this, "", FontsSkin.Styles.Label.DANGER_14, 42f, 144f, 80f, 16f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.regionWorkersVal = ui.label(this, "", FontsSkin.Styles.Label.DANGER_14, 42f, 167f, 80f, 16f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.regionDiVal = ui.label(this, "", FontsSkin.Styles.Label.DANGER_14, 42f, 190f, 62f, 16f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.iconArrow = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.DI_ARROW_NEUTRAL, 115f, 190f, UiAnchor.UPPER_LEFT);
        this.bigDollar = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.ICON_DOLLAR_BIG, 138f, 156f, UiAnchor.UPPER_LEFT);
        this.regionIncome = ui.label(this, "000", FontsSkin.Styles.Label.DANGER_20, 125f, 163f, 115f, 24f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.regionIndustryIcon = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.ICON_INDUSTRY_SMALL, 260f, 144f, UiAnchor.UPPER_LEFT);
        this.regionIndustryText = ui.label(this, "", FontsSkin.Styles.Label.DANGER_14, 285f, 151f, 70f, 50f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.regionFactoryBox = ui.overlappingBox(this, 259f, 185f, 130f, 18f, 0.5f, false, UiAnchor.UPPER_LEFT);
        Catalog catalog = uiCatalog.catalog();
        this.resourceDrawables = new Drawable[catalog.goods().size()];
        this.noResourceDrawable = uiSkin.getDrawable(UiSkin.Drawables.GOOD_NONE_SMALL);
        for (int i = 0; i < this.resourceDrawables.length; i++) {
            String name = "good_" + catalog.goods().nameId(i) + "_small";
            this.resourceDrawables[i] = SkinDrawables.optional(uiSkin, name, this.noResourceDrawable);
        }
        this.noFactoryDrawable = provinceSkin.getDrawable(UiProvinceSkin.Drawables.FACTORY_PLUPP_NOFACTORY);
        this.buildingList.setRowCount(BUILDING_ROWS);
        this.buildingList.row(0).setBuilding(UiProvinceSkin.Drawables.PROV_BUILD_NAVALBASE);
        this.buildingList.row(1).setBuilding(UiProvinceSkin.Drawables.PROV_BUILD_AIRFIELD);
        this.buildingList.row(2).setBuilding(UiProvinceSkin.Drawables.PROV_BUILD_AA);
        this.buildingList.row(3).setBuilding(UiProvinceSkin.Drawables.PROV_BUILD_RADAR);
        this.claimEntries = new ProvClaimsEntry[CLAIM_SLOTS];
        for (int i = 0; i < this.claimEntries.length; i++) {
            this.claimEntries[i] = new ProvClaimsEntry(ui);
        }
        this.cooperationBg.setVisible(false);
        this.coopLabel.setVisible(false);
        this.alliedCooperation.setVisible(false);
        this.cooperationList.setVisible(false);
        this.factoryEntries = new FactoryPluppEntry[FACTORY_SLOTS];
        for (int i = 0; i < this.factoryEntries.length; i++) {
            this.factoryEntries[i] = new FactoryPluppEntry(ui);
            this.regionFactoryBox.addActor(this.factoryEntries[i]);
        }
        this.draftMilitia.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                listener.updateHoverTooltip(FolderView.this.ui.localise("REB") + " : " + FolderView.this.guerrillaValue);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                listener.hideUiTooltip();
            }
        });
    }

    public void setData(ProvinceDetails province) {
        this.text.clear();
        this.textPop.setText(this.textFormat.compact(this.text, province.populationProvince()));
        this.text.clear();
        this.textIncome.setText(TextFormat.appendDecimal(this.text, province.incomeProvince(), 1, '.'));
        this.text.clear();
        this.textRevoltRisk.setText(TextFormat.appendDecimal(this.text, province.revoltRisk(), 1, '.').append('%'));
        this.guerrillaValue = province.guerillaValue();
        this.setResource(province);
        this.text.clear();
        this.textInfra.setText(this.text.append(province.infrastructureValue()).append('%'));
        this.buildingList.row(0).setLevel(province.navalBaseLevel());
        this.buildingList.row(1).setLevel(province.airBaseLevel());
        this.buildingList.row(2).setLevel(province.antiAircraftGunsLevel());
        this.buildingList.row(3).setLevel(province.radarStationLevel());
        this.setCores(province);
        this.setRegion(province);
        this.setFactories(province);
    }

    private void setResource(ProvinceDetails province) {
        int goodIndex = province.resourceGoodIndex();
        Drawable resource = goodIndex >= 0 ? this.resourceDrawables[goodIndex] : this.noResourceDrawable;
        this.iconResource.setDrawable(resource);
        this.iconResource.setSize(resource.getMinWidth(), resource.getMinHeight());
        this.iconNoResource.setVisible(goodIndex < 0);
        if (province.resourceProduced() == -1f) {
            this.text.clear();
            this.text.append('-');
        } else {
            this.textFormat.fixed(this.text, province.resourceProduced(), 4);
        }
        this.textResource.setText(this.text);
    }

    private void setCores(ProvinceDetails province) {
        int count = Math.min(province.coreCount(), this.claimEntries.length);
        for (int i = 0; i < count; i++) {
            ProvClaimsEntry entry = this.claimEntries[i];
            entry.setFlag(this.uiCatalog.plainFlag(province.core(i)));
            if (entry.getParent() == null) {
                this.claims.addActor(entry);
            }
        }
        for (int i = count; i < this.claimEntries.length; i++) {
            if (this.claimEntries[i].getParent() != null) {
                this.claimEntries[i].remove();
            }
        }
    }

    private void setRegion(ProvinceDetails province) {
        this.regionText.setText(this.uiCatalog.regionName(province.regionIndex()));
        this.text.clear();
        this.regionPopVal.setText(this.textFormat.compact(this.text, province.populationRegion()));
        this.text.clear();
        this.regionWorkersVal.setText(this.textFormat.compact(this.text, province.workersRegion()));
        this.text.clear();
        this.regionDiVal.setText(TextFormat.integer(this.text, province.developmentIndexRegion()));
        this.text.clear();
        this.regionIncome.setText(TextFormat.integer(this.text, province.incomeRegion()));
        this.text.clear();
        this.regionIndustryText.setText(TextFormat.integer(this.text, province.industryRegion()));
    }

    private void setFactories(ProvinceDetails province) {
        Catalog catalog = this.uiCatalog.catalog();
        Skin provinceSkin = this.ui.skin(Skins.UI_PROVINCE);
        int shown = 0;
        for (int i = 0; i < province.economyBuildingCount() && shown < this.factoryEntries.length; i++) {
            String color = BuildingUtils.getColor(catalog.buildingTypes().nameId(province.economyBuildingType(i)));
            if (color == null) {
                continue;
            }
            this.factoryEntries[shown++].setFactory(SkinDrawables.optional(provinceSkin, "factory_plupp_" + color, this.noFactoryDrawable));
        }
        for (int i = shown; i < this.factoryEntries.length; i++) {
            this.factoryEntries[i].setFactory(this.noFactoryDrawable);
        }
    }
}
