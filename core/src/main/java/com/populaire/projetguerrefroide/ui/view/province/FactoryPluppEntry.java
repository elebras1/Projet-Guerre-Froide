package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class FactoryPluppEntry extends UiWindow {
    private final Image factoryPlupp;
    private final Image factoryPluppSuspended;
    private final Image factoryPluppConstructed;

    public FactoryPluppEntry(UiBuilder ui) {
        super(ui, 18f, 16f);
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        this.factoryPlupp = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.FACTORY_PLUPP_RED, 5f, 0f, UiAnchor.UPPER_LEFT);
        this.factoryPluppSuspended = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.FACTORY_PLUPP_SUSPENDED, 5f, 0f, UiAnchor.UPPER_LEFT);
        this.factoryPluppConstructed = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.FACTORY_PLUPP_CONSTRUCTED, 5f, 0f, UiAnchor.UPPER_LEFT);
        this.factoryPluppSuspended.setVisible(false);
        this.factoryPluppConstructed.setVisible(false);
    }

    public void setFactory(Drawable drawable) {
        this.factoryPlupp.setDrawable(drawable);
        this.factoryPlupp.setSize(drawable.getMinWidth(), drawable.getMinHeight());
    }
}
