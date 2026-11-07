package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class FactoryWorkforceMember extends UiWindow {
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
    private final Drawable full;
    private final Drawable empty;

    public FactoryWorkforceMember(UiBuilder ui) {
        super(ui, 16f, 14f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.plupp0 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 45f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp1 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 53f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp2 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 61f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp3 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 69f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp4 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 77f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp5 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 85f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp6 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 93f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp7 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 101f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp8 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 109f, 5f, UiAnchor.UPPER_LEFT);
        this.plupp9 = ui.image(this, economySkin, UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP, 117f, 5f, UiAnchor.UPPER_LEFT);
        this.plupps = new Image[]{this.plupp0, this.plupp1, this.plupp2, this.plupp3, this.plupp4, this.plupp5, this.plupp6, this.plupp7, this.plupp8, this.plupp9};
        TextureRegion strip = economySkin.getRegion(UiEconomySkin.Drawables.FACTORY_WORKFORCE_PLUPP);
        int frameWidth = strip.getRegionWidth() / 2;
        this.full = new TextureRegionDrawable(new TextureRegion(strip, 0, 0, frameWidth, strip.getRegionHeight()));
        this.empty = new TextureRegionDrawable(new TextureRegion(strip, frameWidth, 0, frameWidth, strip.getRegionHeight()));
        for (Image plupp : this.plupps) {
            plupp.setDrawable(this.empty);
            plupp.setWidth(frameWidth);
        }
    }

    void setLevel(int workers, int maxWorkers) {
        int lit = maxWorkers > 0 ? Math.round(workers * (float) this.plupps.length / maxWorkers) : 0;
        for (int i = 0; i < this.plupps.length; i++) {
            this.plupps[i].setDrawable(i < lit ? this.full : this.empty);
        }
    }
}
