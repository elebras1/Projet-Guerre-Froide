package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class LogText extends UiWindow {
    private final Label text;

    public LogText(UiBuilder ui) {
        super(ui, 700f, 16f);
        this.text = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12_ALPHA, 10f, 0f, 430f, 16f, Align.topLeft, UiAnchor.UPPER_LEFT);
    }

    public void setText(String text) {
        this.text.setText(text);
    }
}
