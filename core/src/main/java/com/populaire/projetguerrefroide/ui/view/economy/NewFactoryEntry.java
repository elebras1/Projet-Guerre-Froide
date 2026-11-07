package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class NewFactoryEntry extends UiWindow {
    private final Image newFactoryBg;
    private final Label workforceNumber;
    private final Label costNumber;
    private final Label dateNumber;
    private final UiList<NewFactoryResMember> newFactoryResInputListbox;
    private final UiList<NewFactoryResMember> newFactoryResOutputListbox;
    private final UiList<NewFactoryCostMember> factoryCostListbox;

    public NewFactoryEntry(UiBuilder ui) {
        super(ui, 719f, 62f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.newFactoryBg = ui.image(this, economySkin, UiEconomySkin.Drawables.BUILDINGS_BG, 33f, -3f, UiAnchor.UPPER_LEFT);
        this.workforceNumber = ui.label(this, "000", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 446f, 5f, 60f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.costNumber = ui.label(this, "000", FontsSkin.Styles.Label.JOCKEY_14_GREEN, 545f, 5f, 40f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.dateNumber = ui.label(this, "000", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 622f, 5f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.newFactoryResInputListbox = ui.list(this, 108f, 14f, 129f, 68f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, NewFactoryResMember::new);
        this.newFactoryResOutputListbox = ui.list(this, 276f, 14f, 129f, 68f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, NewFactoryResMember::new);
        this.factoryCostListbox = ui.list(this, 426f, 2f, 129f, 33f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, NewFactoryCostMember::new);
    }
}
