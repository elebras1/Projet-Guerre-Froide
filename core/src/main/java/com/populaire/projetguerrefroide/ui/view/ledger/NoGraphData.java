package com.populaire.projetguerrefroide.ui.view.ledger;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class NoGraphData extends UiWindow {
    private final Label noDataText;

    public NoGraphData(UiBuilder ui) {
        super(ui, 640f, 400f);
        this.noDataText = ui.label(this, "LEDGER_NO_GRAPH_DATA", FontsSkin.Styles.Label.ARIAL14, 220f, 180f, 200f, 40f, Align.top, UiAnchor.UPPER_LEFT);
    }
}
