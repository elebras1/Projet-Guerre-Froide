package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.response.ProvinceDetails;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.OverlappingBox;
import com.populaire.projetguerrefroide.ui.widget.SkinDrawables;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ProvincePanel extends UiWindow {
    private static final int REGION_PROVINCE_SLOTS = 8;
    private static final int SPECIAL_BUILDING_SLOTS = 6;
    private final Image background;
    private final Label textTitle;
    private final Button closeButton;
    private final Image terrain;
    private final Image overlay;
    private final FlagImage ownerFlag;
    private final FlagImage occupierFlag;
    private final Button prioStar;
    private final UiList<AdjMember> adj;
    private final Label temperatureTxt;
    private final FolderView folderView;
    private final OverlappingBox regionSpecBuildingBox;
    private final UiCatalog uiCatalog;
    private final TextFormat textFormat;
    private final Skin provinceSkin;
    private final SpecialBuildingEntry[] specialEntries;
    private final Drawable[] terrainDrawables;
    private final Drawable emptySpecialBuildingDrawable;
    private final int[] specialTypes;
    private final int[] specialLevels;
    private final int[] specialMaxLevels;
    private final int[] specialStaffPosts;
    private final int[] specialStaffFilled;
    private final float[] specialEffectiveLevels;
    private final float[] specialStorageCapacities;

    public ProvincePanel(UiBuilder ui, UiCatalog uiCatalog, TextFormat textFormat, ProvincePanelListener listener) {
        super(ui, 396f, 474f);
        this.uiCatalog = uiCatalog;
        this.textFormat = textFormat;
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        Skin uiSkin = ui.skin(Skins.UI);
        this.background = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.BG_PROVINCE, -10f, -40f, UiAnchor.UPPER_LEFT);
        this.textTitle = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_24, 35f, 0f, 292f, 16f, Align.top, UiAnchor.UPPER_LEFT);
        this.closeButton = ui.button(this, provinceSkin, UiProvinceSkin.Styles.Button.CLOSE_BTN_SMALL_BTN, 345f, 1f, UiAnchor.UPPER_LEFT);
        this.terrain = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_TERRAIN_PLAINS, 14f, 29f, UiAnchor.UPPER_LEFT);
        this.overlay = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_OVERLAY, 17f, 32f, UiAnchor.UPPER_LEFT);
        this.ownerFlag = ui.flag(this, uiSkin, UiSkin.Drawables.SHIELD_BIG, UiSkin.Drawables.SHIELD_BIG_OVERLAY, 25f, -9f, UiAnchor.UPPER_LEFT);
        this.occupierFlag = ui.flag(this, uiSkin, UiSkin.Drawables.MINIMASK, UiSkin.Drawables.MINISHIELD, 218f, 42f, UiAnchor.UPPER_LEFT);
        this.prioStar = ui.button(this, provinceSkin, UiProvinceSkin.Styles.Button.PRIO_STAR_TGL, 250f, 37f, UiAnchor.UPPER_LEFT);
        this.adj = ui.list(this, 295f, 46f, 200f, 140f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, AdjMember::new);
        // day_night : GFX_prov_daynight (météo écartée du panneau de province)
        // weather_icon : GFX_icon_weatherstrip (météo écartée du panneau de province)
        this.temperatureTxt = ui.label(this, "+3C", FontsSkin.Styles.Label.ARIAL12, 77f, 157f, 46f, 16f, Align.topLeft, UiAnchor.UPPER_LEFT);
        // wind_icon : GFX_wind_icon (météo écartée du panneau de province)
        // wind_status : supply_progress (météo écartée du panneau de province)
        // ground_status_icon : GFX_ground_icon (météo écartée du panneau de province)
        this.folderView = ui.add(this, new FolderView(ui, uiCatalog, textFormat, listener), 7f, 180f, UiAnchor.UPPER_LEFT);
        this.folderView.setVisible(false);
        this.regionSpecBuildingBox = ui.overlappingBox(this, 15f, 404f, 378f, 54f, 0f, false, UiAnchor.UPPER_LEFT);
        this.temperatureTxt.setVisible(false);
        this.folderView.setVisible(true);
        this.provinceSkin = provinceSkin;
        Catalog catalog = uiCatalog.catalog();
        this.terrainDrawables = new Drawable[catalog.terrains().size()];
        for (int i = 0; i < this.terrainDrawables.length; i++) {
            String name = "prov_terrain_" + catalog.terrains().nameId(i);
            this.terrainDrawables[i] = SkinDrawables.optional(provinceSkin, name, null);
        }
        this.emptySpecialBuildingDrawable = provinceSkin.getDrawable(UiProvinceSkin.Drawables.EMPTY_SPEC_BUILDING);
        this.specialTypes = new int[SPECIAL_BUILDING_SLOTS];
        this.specialLevels = new int[SPECIAL_BUILDING_SLOTS];
        this.specialMaxLevels = new int[SPECIAL_BUILDING_SLOTS];
        this.specialStaffPosts = new int[SPECIAL_BUILDING_SLOTS];
        this.specialStaffFilled = new int[SPECIAL_BUILDING_SLOTS];
        this.specialEffectiveLevels = new float[SPECIAL_BUILDING_SLOTS];
        this.specialStorageCapacities = new float[SPECIAL_BUILDING_SLOTS];
        this.specialEntries = new SpecialBuildingEntry[SPECIAL_BUILDING_SLOTS];
        for (int i = 0; i < this.specialEntries.length; i++) {
            this.specialEntries[i] = new SpecialBuildingEntry(ui);
            this.specialEntries[i].setTooltipListener(listener);
        }
        ui.onClick(this.closeButton, () -> this.setVisible(false));
    }

    public void setData(ProvinceDetails province) {
        this.textTitle.setText(this.uiCatalog.provinceName(province.provinceIndex()));
        this.setTerrain(province.terrainIndex());
        this.ownerFlag.setFlag(this.uiCatalog.flag(province.ownerIndex()));
        this.setRegionProvinces(province);
        this.folderView.setData(province);
        this.setSpecialBuildings(province);
    }

    private void setTerrain(int terrainIndex) {
        Drawable terrain = terrainIndex >= 0 ? this.terrainDrawables[terrainIndex] : null;
        this.terrain.setDrawable(terrain);
        if (terrain != null) {
            this.terrain.setSize(terrain.getMinWidth(), terrain.getMinHeight());
        }
    }

    private void setRegionProvinces(ProvinceDetails province) {
        int count = Math.min(province.regionProvinceCount(), REGION_PROVINCE_SLOTS);
        this.adj.setRowCount(count);
        for (int i = 0; i < count; i++) {
            this.adj.row(i).setProvinceName(this.uiCatalog.provinceName(province.regionProvince(i)));
        }
    }

    private void setSpecialBuildings(ProvinceDetails province) {
        for (SpecialBuildingEntry entry : this.specialEntries) {
            if (entry.getParent() != null) {
                entry.remove();
            }
        }
        int count = Math.min(province.specialCount(), SPECIAL_BUILDING_SLOTS);
        for (int i = 0; i < count; i++) {
            int type = province.specialType(i);
            this.specialTypes[i] = type;
            this.specialLevels[i] = province.specialLevel(i);
            this.specialMaxLevels[i] = province.specialMaxLevel(i);
            this.specialStaffPosts[i] = province.specialStaffPosts(i);
            this.specialStaffFilled[i] = province.specialStaffFilled(i);
            this.specialEffectiveLevels[i] = province.specialEffectiveLevel(i);
            this.specialStorageCapacities[i] = province.specialStorageCapacity(i);
            String drawableName = this.uiCatalog.catalog().buildingTypes().nameId(type) + "_building";
            Drawable image = SkinDrawables.optional(this.provinceSkin, drawableName, this.emptySpecialBuildingDrawable);
            SpecialBuildingEntry entry = this.specialEntries[i];
            entry.setBuilding(image, this.specialBuildingTooltip(i));
            this.regionSpecBuildingBox.addActor(entry);
        }
    }

    private String specialBuildingTooltip(int slot) {
        StringBuilder tooltip = new StringBuilder(this.uiCatalog.buildingTypeName(this.specialTypes[slot]));
        tooltip.append("\n").append(this.ui.localise("SPECIAL_BUILDING_LEVEL")).append(" : ").append(this.specialLevels[slot]).append(" / ").append(this.specialMaxLevels[slot]);
        if (this.specialStaffPosts[slot] > 0) {
            tooltip.append("\n").append(this.ui.localise("SPECIAL_BUILDING_STAFF")).append(" : ").append(this.specialStaffFilled[slot]).append(" / ").append(this.specialStaffPosts[slot]);
        }
        tooltip.append("\n").append(this.ui.localise("SPECIAL_BUILDING_EFFECTIVE_LEVEL")).append(" : ").append(String.format("%.2f", this.specialEffectiveLevels[slot]));
        if (this.specialStorageCapacities[slot] > 0f) {
            tooltip.append("\n").append(this.ui.localise("SPECIAL_BUILDING_CAPACITY")).append(" : ").append(Math.round(this.specialStorageCapacities[slot]));
        }
        return tooltip.toString();
    }
}
