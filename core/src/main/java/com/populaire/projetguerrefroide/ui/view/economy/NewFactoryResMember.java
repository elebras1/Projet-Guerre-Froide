package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class NewFactoryResMember extends UiWindow {
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

    public NewFactoryResMember(UiBuilder ui) {
        super(ui, 43f, 33f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        Skin uiSkin = ui.skin(Skins.UI);
        this.back0 = ui.image(this, economySkin, UiEconomySkin.Drawables.RESOURCE_LIGHT_BLUE, 0f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp0 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 0f, 2f, UiAnchor.UPPER_LEFT);
        this.back1 = ui.image(this, economySkin, UiEconomySkin.Drawables.RESOURCE_LIGHT_BLUE, 32f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp1 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 32f, 2f, UiAnchor.UPPER_LEFT);
        this.back2 = ui.image(this, economySkin, UiEconomySkin.Drawables.RESOURCE_LIGHT_BLUE, 64f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp2 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 64f, 2f, UiAnchor.UPPER_LEFT);
        this.back3 = ui.image(this, economySkin, UiEconomySkin.Drawables.RESOURCE_LIGHT_BLUE, 96f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp3 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 96f, 2f, UiAnchor.UPPER_LEFT);
        this.back4 = ui.image(this, economySkin, UiEconomySkin.Drawables.RESOURCE_LIGHT_BLUE, 128f, 2f, UiAnchor.UPPER_LEFT);
        this.plupp4 = ui.image(this, uiSkin, UiSkin.Drawables.GOOD_NONE_SMALL, 128f, 2f, UiAnchor.UPPER_LEFT);
    }
}
