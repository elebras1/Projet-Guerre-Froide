package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class AlliedCoopItem extends UiWindow {
    private final Button objective;
    private final FlagImage flag;
    private final Label countryName;

    public AlliedCoopItem(UiBuilder ui) {
        super(ui, 64f, 24f);
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        Skin uiSkin = ui.skin(Skins.UI);
        this.objective = ui.button(this, provinceSkin, UiProvinceSkin.Styles.Button.COOP_CHECKBOX_TGL, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.flag = ui.flag(this, uiSkin, UiSkin.Drawables.MINIMASK, UiSkin.Drawables.MINISHIELD_SMALL, 3f, -3f, UiAnchor.UPPER_LEFT);
        this.countryName = ui.label(this, "United Kingdom test bla", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 35f, 5f, 140f, 16f, Align.topLeft, UiAnchor.UPPER_LEFT);
    }
}
