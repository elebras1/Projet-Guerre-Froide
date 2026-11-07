package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.populaire.projetguerrefroide.protocol.response.RegionsBuildings;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class FactoryEntry extends UiWindow {
    private final Button factoryCheckbox;
    private final Image factoryColorbar;
    private final Image suspendedIcon;
    private final Label productionNumber;
    private final Image constructionProgressOverlay;
    private final Label buildProgressText;
    private final UiList<FactoryLvlMember> factoryLvlListbox;
    private final Image buildingImage;
    private final Label levelText;
    private final TextFormat textFormat;
    private final EconomyDrawables drawables;
    private final CharArray text;
    private long buildingId;

    public FactoryEntry(UiBuilder ui, TextFormat textFormat, EconomyDrawables drawables, EconomyPanelListener listener) {
        super(ui, 118f, 124f);
        this.textFormat = textFormat;
        this.drawables = drawables;
        this.text = new CharArray(16);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.factoryCheckbox = ui.flatButton(this, economySkin, UiEconomySkin.Drawables.BUILDING_BOX_TEMPLATE, 33f, -3f, UiAnchor.UPPER_LEFT);
        this.buildingImage = ui.add(this, new Image(drawables.building(-1)), 43f, 6f, UiAnchor.UPPER_LEFT);
        this.factoryColorbar = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILDING_BLUE, 43f, 66f, UiAnchor.UPPER_LEFT);
        this.suspendedIcon = ui.image(this, economySkin, UiEconomySkin.Drawables.ECONMY_SUSPENDED_ICON1, 73f, 15f, UiAnchor.UPPER_LEFT);
        this.productionNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 80f, 71f, 120f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        // construction_progress : GFX_building_construction_progress (sprite absent des .gfx EvW)
        this.constructionProgressOverlay = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_BUILD_PROGRESS_OVERLAY, 130f, 75f, UiAnchor.UPPER_LEFT);
        // income_val_icon : GFX_eco_arrow_multiframe (sans asset PGF)
        this.buildProgressText = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12_BOLD, 40f, 72f, 120f, 30f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.factoryLvlListbox = ui.list(this, 31f, 94f, 108f, 18f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, FactoryLvlMember::new);
        this.constructionProgressOverlay.setVisible(false);
        this.buildProgressText.setVisible(false);
        this.factoryLvlListbox.setVisible(false);
        this.buildingImage.setTouchable(Touchable.disabled);
        this.factoryColorbar.setTouchable(Touchable.disabled);
        this.suspendedIcon.setTouchable(Touchable.disabled);
        this.levelText = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14_TIGHT, 43f, 94f, 108f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        ui.onClick(this.factoryCheckbox, () -> {
            if (this.buildingId != 0) {
                listener.onBuildingClicked(this.buildingId);
            }
        });
    }

    void bind(RegionsBuildings regions, int building) {
        this.buildingId = regions.buildingId(building);
        int type = regions.buildingType(building);
        this.setBox(this.drawables.box());
        Drawable picture = this.drawables.building(type);
        if (this.buildingImage.getDrawable() != picture) {
            this.buildingImage.setDrawable(picture);
        }
        this.buildingImage.setVisible(true);
        Drawable colorBar = this.drawables.colorBar(type);
        this.factoryColorbar.setVisible(colorBar != null);
        if (colorBar != null && this.factoryColorbar.getDrawable() != colorBar) {
            this.factoryColorbar.setDrawable(colorBar);
        }
        this.suspendedIcon.setVisible(regions.buildingSuspended(building));
        this.productionNumber.setVisible(true);
        this.productionNumber.setText(this.textFormat.fixed(this.text, regions.buildingProduction(building), 4));
        this.text.clear();
        this.text.append(regions.buildingSize(building));
        int levelsQueued = regions.buildingLevelsQueued(building);
        if (levelsQueued > 0) {
            this.text.append(" (").append(levelsQueued).append(')');
        }
        this.text.append('/').append(regions.buildingMaxLevel(building));
        this.levelText.setVisible(true);
        this.levelText.setText(this.text);
    }

    void bindEmpty() {
        this.buildingId = 0;
        this.setBox(this.drawables.emptySlot());
        this.buildingImage.setVisible(false);
        this.factoryColorbar.setVisible(false);
        this.suspendedIcon.setVisible(false);
        this.productionNumber.setVisible(false);
        this.levelText.setVisible(false);
    }

    private void setBox(Drawable box) {
        Button.ButtonStyle style = this.factoryCheckbox.getStyle();
        if (style.up != box) {
            style.up = box;
            this.factoryCheckbox.setStyle(style);
        }
    }
}
