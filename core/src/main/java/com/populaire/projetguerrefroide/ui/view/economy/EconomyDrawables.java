package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.view.BuildingUtils;
import com.populaire.projetguerrefroide.ui.widget.SkinDrawables;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;

/**
 * Images de l'économie résolues une fois par type de bâtiment et par bien. EvW n'a que sept catégories d'usines
 * (images composites 129×121) ; PGF a une image `building_<type>` (108×57) posée dans le cadre
 * `building_box_template`, une image `pinned_<type>` (246×137) pour la fiche et la barre de couleur de sa
 * catégorie.
 */
final class EconomyDrawables {
    private final Drawable[] buildings;
    private final Drawable[] pinned;
    private final Drawable[] colorBars;
    private final Drawable[] goods;
    private final Drawable buildingTemplate;
    private final Drawable box;
    private final Drawable emptySlot;
    private final Drawable noGood;
    private final Drawable metalGreen;
    private final Drawable metalGrey;
    private final Drawable metalBlue;

    EconomyDrawables(UiBuilder ui, Catalog catalog) {
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        Skin uiSkin = ui.skin(Skins.UI);
        this.buildingTemplate = economySkin.getDrawable(UiEconomySkin.Drawables.BUILDING_TEMPLATE);
        this.box = economySkin.getDrawable(UiEconomySkin.Drawables.BUILDING_BOX_TEMPLATE);
        this.emptySlot = economySkin.getDrawable(UiEconomySkin.Drawables.ECO_BUILD);
        this.noGood = uiSkin.getDrawable(UiSkin.Drawables.GOOD_NONE_SMALL);
        this.metalGreen = economySkin.getDrawable(UiEconomySkin.Drawables.ECO_BOX_METAL_GREEN);
        this.metalGrey = economySkin.getDrawable(UiEconomySkin.Drawables.ECO_BOX_METAL_GREY);
        this.metalBlue = economySkin.getDrawable(UiEconomySkin.Drawables.ECO_BOX_METAL_BLUE);
        int buildingTypes = catalog.buildingTypes().size();
        this.buildings = new Drawable[buildingTypes];
        this.pinned = new Drawable[buildingTypes];
        this.colorBars = new Drawable[buildingTypes];
        for (int i = 0; i < buildingTypes; i++) {
            String name = catalog.buildingTypes().nameId(i);
            this.buildings[i] = SkinDrawables.optional(economySkin, "building_" + name, this.buildingTemplate);
            this.pinned[i] = SkinDrawables.optional(economySkin, "pinned_" + name, null);
            String color = BuildingUtils.getColor(name);
            this.colorBars[i] = color != null ? SkinDrawables.optional(economySkin, "eco_building_" + color, null) : null;
        }
        this.goods = new Drawable[catalog.goods().size()];
        for (int i = 0; i < this.goods.length; i++) {
            this.goods[i] = SkinDrawables.optional(uiSkin, "good_" + catalog.goods().nameId(i) + "_small", this.noGood);
        }
    }

    Drawable building(int type) {
        return type >= 0 ? this.buildings[type] : this.buildingTemplate;
    }

    Drawable pinned(int type) {
        return type >= 0 ? this.pinned[type] : null;
    }

    Drawable colorBar(int type) {
        return type >= 0 ? this.colorBars[type] : null;
    }

    Drawable good(int goodIndex) {
        return goodIndex >= 0 ? this.goods[goodIndex] : this.noGood;
    }

    Drawable noGood() {
        return this.noGood;
    }

    Drawable box() {
        return this.box;
    }

    Drawable emptySlot() {
        return this.emptySlot;
    }

    Drawable metalGreen() {
        return this.metalGreen;
    }

    Drawable metalGrey() {
        return this.metalGrey;
    }

    Drawable metalBlue() {
        return this.metalBlue;
    }
}
