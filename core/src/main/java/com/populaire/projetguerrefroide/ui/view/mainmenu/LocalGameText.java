package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class LocalGameText extends UiWindow {
    private final Label text;

    public LocalGameText(UiBuilder ui) {
        super(ui, 128f, 18f);
        this.text = ui.label(this, "FE_LOCAL_GAMES", FontsSkin.Styles.Label.ARIAL12_BOLD, 0f, 0f, 720f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT, 1f, 1f);
    }
}
