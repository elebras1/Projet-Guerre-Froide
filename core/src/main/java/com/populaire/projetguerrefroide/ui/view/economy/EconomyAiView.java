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

public class EconomyAiView extends UiWindow {
    private final Image bgAi;
    private final Label infraOptions;
    private final Label productionOptions;
    private final Label factoriesOption;
    private final Label specialOption;
    private final Label nuclearOption;
    private final Label upgradeOption;
    private final Label unitOption;
    private final Label installationOption;
    private final Button factoriesOptionCheck;
    private final Button specialOptionCheck;
    private final Button nuclearOptionCheck;
    private final Button upgradeOptionCheck;
    private final Button unitOptionCheck;
    private final Button installationOptionCheck;
    private final Label moneyPoolLabel;
    private final Label aiMoneypool;
    private final UiList<AiBuildingEntry> aiBuildings;

    public EconomyAiView(UiBuilder ui) {
        super(ui, 300f, 400f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.bgAi = ui.image(this, economySkin, UiEconomySkin.Drawables.BG_ECO_AI, 15f, 43f, UiAnchor.UPPER_LEFT);
        this.infraOptions = ui.label(this, "INVESTMENTS_LABEL", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 32f, 45f, 150f, 28f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.productionOptions = ui.label(this, "PRODUCTION", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 32f, 195f, 150f, 28f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.factoriesOption = ui.label(this, "AI_FACTORIES", FontsSkin.Styles.Label.JOCKEY_14_DARK, 38f, 75f, 300f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.specialOption = ui.label(this, "AI_SPECIALBUILDINGS", FontsSkin.Styles.Label.JOCKEY_14_DARK, 38f, 104f, 300f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.nuclearOption = ui.label(this, "AI_NUCLEAR", FontsSkin.Styles.Label.JOCKEY_14_DARK, 38f, 133f, 300f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.upgradeOption = ui.label(this, "AI_UPGRADE", FontsSkin.Styles.Label.JOCKEY_14_DARK, 38f, 162f, 300f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.unitOption = ui.label(this, "AI_UNITS", FontsSkin.Styles.Label.JOCKEY_14_DARK, 38f, 225f, 300f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.installationOption = ui.label(this, "AI_INSTALLATIONS", FontsSkin.Styles.Label.JOCKEY_14_DARK, 38f, 254f, 300f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.factoriesOptionCheck = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_AIKNOB_TGL, 209f, 66f, UiAnchor.UPPER_LEFT);
        this.specialOptionCheck = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_AIKNOB_TGL, 209f, 95f, UiAnchor.UPPER_LEFT);
        this.nuclearOptionCheck = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_AIKNOB_TGL, 209f, 124f, UiAnchor.UPPER_LEFT);
        this.upgradeOptionCheck = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_AIKNOB_TGL, 209f, 153f, UiAnchor.UPPER_LEFT);
        this.unitOptionCheck = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_AIKNOB_TGL, 209f, 216f, UiAnchor.UPPER_LEFT);
        this.installationOptionCheck = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_AIKNOB_TGL, 209f, 245f, UiAnchor.UPPER_LEFT);
        this.moneyPoolLabel = ui.label(this, "AI_AIPOOL", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 32f, 287f, 300f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.aiMoneypool = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16, -40f, 287f, 300f, 64f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.aiBuildings = ui.list(this, 32f, 330f, 150f, 130f, UiAnchor.UPPER_LEFT, 15f, 0f, 0f, AiBuildingEntry::new);
        this.infraOptions.setVisible(false);
        this.productionOptions.setVisible(false);
        this.factoriesOption.setVisible(false);
        this.specialOption.setVisible(false);
        this.nuclearOption.setVisible(false);
        this.upgradeOption.setVisible(false);
        this.unitOption.setVisible(false);
        this.installationOption.setVisible(false);
        this.factoriesOptionCheck.setVisible(false);
        this.specialOptionCheck.setVisible(false);
        this.nuclearOptionCheck.setVisible(false);
        this.upgradeOptionCheck.setVisible(false);
        this.unitOptionCheck.setVisible(false);
        this.installationOptionCheck.setVisible(false);
        this.moneyPoolLabel.setVisible(false);
        this.aiMoneypool.setVisible(false);
        this.aiBuildings.setVisible(false);
    }
}
