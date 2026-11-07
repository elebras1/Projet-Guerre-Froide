package com.populaire.projetguerrefroide.ui.view.dialog;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class TextListItem extends UiWindow {
    private final Label text;

    public TextListItem(UiBuilder ui) {
        super(ui, 434f, 16f);
        this.text = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 0f, 0f, 434f, 16f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.text.setWrap(true);
    }

    public void setText(String text) {
        this.text.setText(text);
        this.text.layout();
        float height = Math.max(16f, this.text.getPrefHeight());
        this.text.setHeight(height);
        this.setHeight(height);
        this.invalidateHierarchy();
    }
}
