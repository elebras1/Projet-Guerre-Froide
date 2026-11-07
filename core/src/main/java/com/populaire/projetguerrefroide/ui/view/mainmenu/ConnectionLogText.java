package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ConnectionLogText extends UiWindow {
    private final Label text;

    public ConnectionLogText(UiBuilder ui) {
        super(ui, 200f, 18f);
        this.text = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12_BOLD, 0f, 0f, 200f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT, 1f, 1f);
        this.text.setWrap(true);
    }

    public void setText(String text) {
        this.text.setText(text);
        this.text.setHeight(this.text.getPrefHeight());
        this.setHeight(Math.max(18f, this.text.getPrefHeight() + 2f));
        this.invalidateHierarchy();
    }
}
