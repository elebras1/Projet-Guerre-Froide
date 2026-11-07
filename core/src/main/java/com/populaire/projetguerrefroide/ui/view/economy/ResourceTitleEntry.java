package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ResourceTitleEntry extends UiWindow {
    private final Label resourceTitleText;

    public ResourceTitleEntry(UiBuilder ui) {
        super(ui, 229f, 48f);
        // resource_title_bg : GFX_resource_title (sans asset PGF)
        this.resourceTitleText = ui.label(this, "GROUP_TITLE", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 44f, 2f, 200f, 28f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
    }
}
