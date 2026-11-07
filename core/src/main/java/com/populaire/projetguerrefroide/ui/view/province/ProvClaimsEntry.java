package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ProvClaimsEntry extends UiWindow {
    private final FlagImage flag;

    public ProvClaimsEntry(UiBuilder ui) {
        super(ui, 32f, 32f);
        Skin uiSkin = ui.skin(Skins.UI);
        this.flag = ui.flag(this, uiSkin, UiSkin.Drawables.MINIMASK, UiSkin.Drawables.MINISHIELD, 0f, 0f, UiAnchor.UPPER_LEFT);
    }

    public void setFlag(TextureRegion flag) {
        this.flag.setFlag(flag);
    }
}
