package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class SpecBuildings extends UiWindow {
    private final Label specBuildingsLabel;
    private final UiList<NewSpecialBuildingEntry> newSpecBuildings;
    private final Button newBuildingCloseButton;

    public SpecBuildings(UiBuilder ui) {
        super(ui, 388f, 228f);
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        this.specBuildingsLabel = ui.label(this, "PROV_VIEW_SPEC_BUILDINGS", FontsSkin.Styles.Label.JOCKEY_24, 10f, 16f, 320f, 16f, Align.top, UiAnchor.UPPER_LEFT);
        this.newSpecBuildings = ui.list(this, 4f, 48f, 330f, 188f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, NewSpecialBuildingEntry::new);
        this.newBuildingCloseButton = ui.button(this, provinceSkin, UiProvinceSkin.Styles.Button.CLOSE_BTN_SMALL_BTN, 330f, 17f, UiAnchor.UPPER_LEFT);
    }
}
