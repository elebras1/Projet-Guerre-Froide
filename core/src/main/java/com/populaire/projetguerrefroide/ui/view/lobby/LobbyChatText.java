package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class LobbyChatText extends UiWindow {
    private final Label messagelogtext;

    public LobbyChatText(UiBuilder ui) {
        super(ui, 730f, 18f);
        this.messagelogtext = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 16f, 0f, 690f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT, 1f, 1f);
        this.messagelogtext.setWrap(true);
    }

    public void setText(String text) {
        this.messagelogtext.setText(text);
        this.messagelogtext.layout();
        float height = Math.max(18f, this.messagelogtext.getPrefHeight());
        this.messagelogtext.setHeight(height);
        this.setHeight(height);
        this.invalidateHierarchy();
    }
}
