package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
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

public class FactoryBuild extends UiWindow {
    private final Image factoryBuildBg;
    private final Label newFactoryBuildingTitle;
    private final Button nameButton;
    private final Button inputButton;
    private final Button outputButton;
    private final Button workforceButton;
    private final Button costButton;
    private final Button timeButton;
    private final UiList<NewFactoryEntry> newFactoryListbox;
    private final Button cancel;
    private final Label cancelLabel;

    public FactoryBuild(UiBuilder ui) {
        super(ui, 746f, 540f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.factoryBuildBg = ui.image(this, economySkin, UiEconomySkin.Drawables.BUILDINGS_BG, 31f, 85f, UiAnchor.UPPER_LEFT);
        this.newFactoryBuildingTitle = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_24, 260f, 87f, 320f, 80f, Align.top, UiAnchor.UPPER_LEFT, 4f, 0f);
        this.nameButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_SORT_1_BTN, 70f, 110f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.nameButton, "NAME", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.inputButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_SORT_OUTPUT_INPUT_BTN, 138f, 110f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.inputButton, "INPUT", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.outputButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_SORT_3_BTN, 302f, 110f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.outputButton, "OUTPUT", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.workforceButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_SORT_WORKFORCE_BTN, 470f, 110f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.workforceButton, "WORKFORCE_BUTTON", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.costButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_SORT_5_BTN, 552f, 110f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.costButton, "CONSTRUCTION_COST", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.timeButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_SORT_TYPE_DATE_BTN, 649f, 110f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.timeButton, "CONSTRUCTION_TIME", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.newFactoryListbox = ui.list(this, 28f, 148f, 749f, 372f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, NewFactoryEntry::new);
        this.cancel = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_BACK_BTN, 113f, 523f, UiAnchor.UPPER_LEFT);
        this.cancelLabel = ui.label(this, "CANCEL", FontsSkin.Styles.Label.JOCKEY_16_DARK, 140f, 531f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
    }
}
