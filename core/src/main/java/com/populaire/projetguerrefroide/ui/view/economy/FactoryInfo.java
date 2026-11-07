package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.protocol.response.BuildingDetails;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class FactoryInfo extends UiWindow {
    private final Image factoryImage;
    private final Label factoryName;
    private final Label regionName;
    private final Label workforce;
    private final Label input;
    private final Label output;
    private final UiList<FactoryResMember> factoryResInputListbox;
    private final UiList<FactoryResMember> factoryResOutputListbox;
    private final Image efficiencyIcon;
    private final Label efficiencyNumber;
    private final Image productivityIcon;
    private final Label productivityNumber;
    private final Image workforceIcon;
    private final UiList<FactoryWorkforceMember> factoryWrkfrcLvlListbox;
    private final Button expandButton;
    private final Button suspendButton;
    private final Button demolishButton;
    private final Label suspendLabel;
    private final UiCatalog uiCatalog;
    private final EconomyDrawables drawables;
    private long buildingId;
    private boolean suspended;

    public FactoryInfo(UiBuilder ui, UiCatalog uiCatalog, EconomyDrawables drawables, EconomyPanelListener listener) {
        super(ui, 500f, 130f);
        this.uiCatalog = uiCatalog;
        this.drawables = drawables;
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.factoryImage = ui.image(this, economySkin, UiEconomySkin.Drawables.PINNED_STEEL_FACTORY, -111f, 208f, UiAnchor.UPPER_LEFT);
        this.factoryName = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_PAPER, 380f, 198f, 210f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.regionName = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_PAPER, 410f, 218f, 150f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.workforce = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 950f, 265f, 120f, 28f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.input = ui.label(this, "INPUT", FontsSkin.Styles.Label.JOCKEY_16_PAPER, 205f, 198f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.output = ui.label(this, "OUTPUT", FontsSkin.Styles.Label.JOCKEY_16_PAPER, 205f, 274f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.factoryResInputListbox = ui.list(this, 100f, 228f, 260f, 100f, UiAnchor.UPPER_LEFT, 15f, 0f, 0f, FactoryResMember::new);
        this.factoryResOutputListbox = ui.list(this, 100f, 299f, 260f, 70f, UiAnchor.UPPER_LEFT, 15f, 0f, 0f, FactoryResMember::new);
        this.efficiencyIcon = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_ICON_INDUSTRY, 380f, 247f, UiAnchor.UPPER_LEFT);
        this.efficiencyNumber = ui.label(this, "", FontsSkin.Styles.Label.DANGER_14, 360f, 252f, 120f, 28f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.productivityIcon = ui.image(this, economySkin, UiEconomySkin.Drawables.PROD_ICON, 380f, 279f, UiAnchor.UPPER_LEFT);
        this.productivityNumber = ui.label(this, "", FontsSkin.Styles.Label.DANGER_14, 360f, 283f, 120f, 28f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.workforceIcon = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_ICON_WORKERS_2, 375f, 313f, UiAnchor.UPPER_LEFT);
        this.factoryWrkfrcLvlListbox = ui.list(this, 360f, 320f, 118f, 18f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, FactoryWorkforceMember::new);
        this.expandButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_FOLDER_YELLOW_BTN, 497f, 247f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.expandButton, "EXPAND", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.suspendButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_FOLDER_YELLOW_BTN, 497f, 271f, UiAnchor.UPPER_LEFT);
        this.suspendLabel = ui.buttonLabel(this.suspendButton, "SUSPEND", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.demolishButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_FOLDER_RED_BTN, 497f, 319f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.demolishButton, "DEMOLISH", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        // paper_clip_icon : GFX_tech_folder_paper_clip (sans asset PGF)
        this.workforce.setVisible(false);
        this.efficiencyNumber.setVisible(false);
        this.productivityNumber.setVisible(false);
        this.factoryResInputListbox.setRowCount(1);
        this.factoryResOutputListbox.setRowCount(1);
        this.factoryWrkfrcLvlListbox.setRowCount(1);
        ui.onClick(this.expandButton, () -> listener.onExpandBuildingClicked(this.buildingId));
        ui.onClick(this.suspendButton, () -> {
            if (this.suspended) {
                listener.onResumeBuildingClicked(this.buildingId);
            } else {
                listener.onSuspendBuildingClicked(this.buildingId);
            }
        });
        ui.onClick(this.demolishButton, () -> listener.onDemolishBuildingClicked(this.buildingId));
    }

    long buildingId() {
        return this.buildingId;
    }

    void bind(BuildingDetails building) {
        this.buildingId = building.buildingId();
        if (this.suspended != building.suspended()) {
            this.suspended = building.suspended();
            this.suspendLabel.setText(this.ui.localise(this.suspended ? "RESUME" : "SUSPEND"));
        }
        boolean enabled = building.stateControlled();
        this.setActionEnabled(this.expandButton, enabled);
        this.setActionEnabled(this.suspendButton, enabled);
        this.setActionEnabled(this.demolishButton, enabled);
        int type = building.buildingTypeIndex();
        this.factoryName.setText(this.uiCatalog.buildingTypeName(type));
        this.regionName.setText(this.uiCatalog.regionName(building.regionIndex()));
        Drawable pinned = this.drawables.pinned(type);
        this.factoryImage.setVisible(pinned != null);
        if (pinned != null && this.factoryImage.getDrawable() != pinned) {
            this.factoryImage.setDrawable(pinned);
        }
        FactoryResMember inputs = this.factoryResInputListbox.row(0);
        for (int slot = 0; slot < FactoryResMember.SLOTS; slot++) {
            if (slot < building.inputGoodCount()) {
                inputs.setSlot(slot, this.drawables.metalGreen(), this.drawables.good(building.inputGood(slot)));
            } else {
                inputs.setSlot(slot, this.drawables.metalGrey(), this.drawables.noGood());
            }
        }
        FactoryResMember output = this.factoryResOutputListbox.row(0);
        output.setSlot(0, this.drawables.metalBlue(), this.drawables.good(building.outputGoodIndex()));
        for (int slot = 1; slot < FactoryResMember.SLOTS; slot++) {
            output.hideSlot(slot);
        }
        this.factoryWrkfrcLvlListbox.row(0).setLevel(building.workers(), building.maxWorkers());
    }

    private void setActionEnabled(Button button, boolean enabled) {
        button.setDisabled(!enabled);
        button.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
    }
}
