package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.widget.LabelUtils;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class AdjMember extends UiWindow {
    private final Image adjIcon;
    private final Label adjText;

    public AdjMember(UiBuilder ui) {
        super(ui, 64f, 16f);
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        this.adjIcon = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.PROVINCEVIEW_PASSABLE, -3f, -12f, UiAnchor.UPPER_LEFT);
        this.adjText = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 20f, -6f, 55f, 16f, Align.topLeft, UiAnchor.UPPER_LEFT);
    }

    public void setProvinceName(String name) {
        this.adjText.setText(name);
        LabelUtils.truncateLabel(this.adjText, 55f);
    }
}
