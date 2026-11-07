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
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class NewSpecialBuildingEntry extends UiWindow {
    private final Button newSpecBuildingButton;
    private final Button newSpecBuildingImage;
    private final Label newSpecBuildingName;
    private final Label newSpecBuildingDesc;

    public NewSpecialBuildingEntry(UiBuilder ui) {
        super(ui, 328f, 62f);
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        this.newSpecBuildingButton = ui.flatButton(this, provinceSkin, UiProvinceSkin.Drawables.SPECIAL_BUILDING_PLATE, -3f, 0f, UiAnchor.UPPER_LEFT);
        this.newSpecBuildingImage = ui.flatButton(this, provinceSkin, UiProvinceSkin.Drawables.EMPTY_SPEC_BUILDING, 0f, 4f, UiAnchor.UPPER_LEFT);
        this.newSpecBuildingName = ui.label(this, "000", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 60f, 10f, 252f, 14f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.newSpecBuildingDesc = ui.label(this, "000", FontsSkin.Styles.Label.ARIAL12, 67f, 30f, 252f, 24f, Align.topRight, UiAnchor.UPPER_LEFT);
    }
}
