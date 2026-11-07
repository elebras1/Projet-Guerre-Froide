package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class FactoryResMember extends UiWindow {
    static final int SLOTS = 5;
    private final Image back0;
    private final Image plupp0;
    private final Image back1;
    private final Image plupp1;
    private final Image back2;
    private final Image plupp2;
    private final Image back3;
    private final Image plupp3;
    private final Image back4;
    private final Image plupp4;
    private final Image[] backs;
    private final Image[] goods;

    public FactoryResMember(UiBuilder ui) {
        super(ui, 65f, 52f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        Skin uiSkin = ui.skin(Skins.UI);
        this.back0 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BOX_METAL_BLUE, 44f, -1f, UiAnchor.UPPER_LEFT);
        this.plupp0 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 50f, 10f, UiAnchor.UPPER_LEFT);
        this.back1 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BOX_METAL_BLUE, 88f, -1f, UiAnchor.UPPER_LEFT);
        this.plupp1 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 94f, 10f, UiAnchor.UPPER_LEFT);
        this.back2 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BOX_METAL_BLUE, 132f, -1f, UiAnchor.UPPER_LEFT);
        this.plupp2 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 138f, 10f, UiAnchor.UPPER_LEFT);
        this.back3 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BOX_METAL_BLUE, 176f, -1f, UiAnchor.UPPER_LEFT);
        this.plupp3 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 182f, 10f, UiAnchor.UPPER_LEFT);
        this.back4 = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BOX_METAL_BLUE, 220f, -1f, UiAnchor.UPPER_LEFT);
        this.plupp4 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 226f, 10f, UiAnchor.UPPER_LEFT);
        this.backs = new Image[]{this.back0, this.back1, this.back2, this.back3, this.back4};
        this.goods = new Image[]{this.plupp0, this.plupp1, this.plupp2, this.plupp3, this.plupp4};
    }

    void setSlot(int slot, Drawable back, Drawable good) {
        this.backs[slot].setVisible(true);
        this.backs[slot].setDrawable(back);
        this.goods[slot].setVisible(true);
        this.goods[slot].setDrawable(good);
    }

    void hideSlot(int slot) {
        this.backs[slot].setVisible(false);
        this.goods[slot].setVisible(false);
    }
}
