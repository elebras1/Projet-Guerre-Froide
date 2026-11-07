package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class DomesticPanelSlider extends UiWindow {
    private final Image sliderIconButton;
    private final Label value;
    private final Label title;
    private final Slider actualSlider;

    public DomesticPanelSlider(UiBuilder ui) {
        super(ui, 175f, 48f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.sliderIconButton = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_ICON_INFRA, -25f, -4f, UiAnchor.UPPER_LEFT);
        this.value = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 142f, -6f, 50f, 32f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.title = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 10f, -6f, 160f, 20f, Align.topLeft, UiAnchor.UPPER_LEFT);
        // need_progress : slider_need_progress (sprite absent des .gfx EvW)
        this.actualSlider = ui.slider(this, 8f, 4f, 201f, 16f, UiAnchor.UPPER_LEFT, 0f, 100f, 0.001f, 0f);
    }
}
