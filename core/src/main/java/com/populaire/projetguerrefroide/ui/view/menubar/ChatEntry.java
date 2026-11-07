package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ChatEntry extends UiWindow {
    private final FlagImage shield;
    private final Label textShadow;
    private final Label text;

    public ChatEntry(UiBuilder ui) {
        super(ui, 512f, 18f);
        Skin uiSkin = ui.skin(Skins.UI);
        this.shield = ui.flag(this, uiSkin, UiSkin.Drawables.SMALL_FLAG_MASK, UiSkin.Drawables.SMALL_FLAG_OVERLAY, 0f, -5f, UiAnchor.UPPER_LEFT);
        this.textShadow = ui.label(this, "", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 33f, 5f, 512f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.text = ui.label(this, "", FontsSkin.Styles.Label.ARIAL14, 32f, 4f, 512f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT);
    }
}
