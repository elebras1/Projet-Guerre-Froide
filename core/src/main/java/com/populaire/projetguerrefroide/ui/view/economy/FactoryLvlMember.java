package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class FactoryLvlMember extends UiWindow {
    private final Image plupp0;
    private final Image plupp1;
    private final Image plupp2;
    private final Image plupp3;
    private final Image plupp4;
    private final Image plupp5;
    private final Image plupp6;
    private final Image plupp7;
    private final Image plupp8;
    private final Image plupp9;

    public FactoryLvlMember(UiBuilder ui) {
        super(ui, 17f, 14f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.plupp0 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 42f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp1 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 47f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp2 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 52f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp3 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 57f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp4 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 62f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp5 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 67f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp6 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 72f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp7 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 77f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp8 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 82f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp9 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_LIGHT, 87f, 2f, UiAnchor.UPPER_LEFT);
    }
}
