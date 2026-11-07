package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class BuildingMember extends UiWindow {
    private final Button buildingIcon;
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
    private final Image[] plupps;

    public BuildingMember(UiBuilder ui) {
        super(ui, 64f, 16f);
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        this.buildingIcon = ui.flatButton(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILD_NAVALBASE, 2f, 0f, UiAnchor.UPPER_LEFT);
        this.plupp0 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 32f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp1 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 40f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp2 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 48f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp3 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 56f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp4 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 64f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp5 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 72f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp6 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 80f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp7 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 88f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp8 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 96f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp9 = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF, 104f, 2f, UiAnchor.UPPER_LEFT);
        this.plupps = new Image[]{this.plupp0, this.plupp1, this.plupp2, this.plupp3, this.plupp4, this.plupp5, this.plupp6, this.plupp7, this.plupp8, this.plupp9};
    }

    public void setBuilding(String drawable) {
        Drawable icon = this.ui.skin(Skins.UI_PROVINCE).getDrawable(drawable);
        this.buildingIcon.getStyle().up = icon;
        this.buildingIcon.setSize(icon.getMinWidth(), icon.getMinHeight());
        this.buildingIcon.invalidateHierarchy();
    }

    public void setLevel(int level) {
        Skin provinceSkin = this.ui.skin(Skins.UI_PROVINCE);
        Drawable on = provinceSkin.getDrawable(UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_ON);
        Drawable off = provinceSkin.getDrawable(UiProvinceSkin.Drawables.PROV_BUILDING_PLUPP_OFF);
        for (int i = 0; i < this.plupps.length; i++) {
            this.plupps[i].setDrawable(i < level ? on : off);
        }
    }
}
