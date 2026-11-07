package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ChatEntryOther extends UiWindow {
    private final Label textShadow;
    private final Label text;

    public ChatEntryOther(UiBuilder ui) {
        super(ui, 512f, 18f);
        this.textShadow = ui.label(this, "", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 33f, 5f, 512f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.text = ui.label(this, "", FontsSkin.Styles.Label.ARIAL14, 32f, 4f, 512f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
    }
}
