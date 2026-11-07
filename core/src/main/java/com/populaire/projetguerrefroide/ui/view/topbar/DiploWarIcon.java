package com.populaire.projetguerrefroide.ui.view.topbar;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class DiploWarIcon extends UiWindow {
    private final FlagImage flagIcon;

    public DiploWarIcon(UiBuilder ui) {
        super(ui, 24f, 16f);
        Skin uiSkin = ui.skin(Skins.UI);
        this.flagIcon = ui.flag(this, uiSkin, UiSkin.Drawables.SMALL_FLAG_MASK, UiSkin.Drawables.SMALL_FLAG_OVERLAY, 0f, -1f, UiAnchor.UPPER_LEFT);
    }

    public void setFlag(TextureRegion flag) {
        this.flagIcon.setFlag(flag);
    }
}
