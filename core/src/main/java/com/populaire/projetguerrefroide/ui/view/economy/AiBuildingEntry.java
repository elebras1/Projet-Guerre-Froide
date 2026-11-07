package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class AiBuildingEntry extends UiWindow {
    private final Label buildingName;
    private final Label buildingProv;

    public AiBuildingEntry(UiBuilder ui) {
        super(ui, 150f, 22f);
        this.buildingName = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 0f, 0f, 140f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 4f, 2f);
        this.buildingProv = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 80f, 2f, 140f, 32f, Align.topRight, UiAnchor.UPPER_LEFT, 4f, 2f);
    }
}
