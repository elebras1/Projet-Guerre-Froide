package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ResourceEntry extends UiWindow {
    private final Button resourceBg;
    private final Image resourceIcon;
    private final Label supplyNumber;
    private final Label demandNumber;
    private final Label basePrice;
    private final Label currentPrice;
    private final Label availableNumber;

    public ResourceEntry(UiBuilder ui) {
        super(ui, 231f, 48f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        Skin uiSkin = ui.skin(Skins.UI);
        this.resourceBg = ui.flatButton(this, economySkin, UiEconomySkin.Drawables.ECONOMY_GOODS_BG, 37f, -3f, UiAnchor.UPPER_LEFT);
        // background_color : GFX_resource_colors_io (sans asset PGF)
        this.resourceIcon = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 46f, 9f, UiAnchor.UPPER_LEFT);
        this.supplyNumber = ui.label(this, "000", FontsSkin.Styles.Label.ARIAL12, 85f, 3f, 90f, 28f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.demandNumber = ui.label(this, "000", FontsSkin.Styles.Label.ARIAL12, 85f, 23f, 90f, 28f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.basePrice = ui.label(this, "000", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 188f, 6f, 58f, 28f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.currentPrice = ui.label(this, "000", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 188f, 6f, 58f, 28f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.availableNumber = ui.label(this, "000", FontsSkin.Styles.Label.ARIAL12, 188f, 27f, 63f, 28f, Align.topRight, UiAnchor.UPPER_LEFT);
    }
}
