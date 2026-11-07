package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.populaire.projetguerrefroide.protocol.response.RegionsBuildings;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class RegionEntryTitle extends UiWindow {
    private final Image regionBgSmall;
    private final Label regionName;
    private final Label dINumber;
    private final Label populationNumber;
    private final Label workersNumber;
    private final Button showButton;
    private Runnable onToggle;

    public RegionEntryTitle(UiBuilder ui) {
        super(ui, 720f, 42f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.regionBgSmall = ui.image(this, economySkin, UiEconomySkin.Drawables.ECONOMY_REGION_PLATE_SMALL, 260f, 0f, UiAnchor.UPPER_LEFT);
        this.regionName = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 270f, 12f, 180f, 22f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.dINumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 696f, 12f, 60f, 22f, Align.top, UiAnchor.UPPER_LEFT);
        this.populationNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 770f, 12f, 60f, 22f, Align.top, UiAnchor.UPPER_LEFT);
        this.workersNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 839f, 12f, 95f, 22f, Align.top, UiAnchor.UPPER_LEFT);
        this.showButton = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_PLATE_MIN_BTN, 943f, 7f, UiAnchor.UPPER_LEFT);
        ui.onClick(this.showButton, () -> this.onToggle.run());
    }

    void setOnToggle(Runnable onToggle) {
        this.onToggle = onToggle;
    }

    void setRegionName(CharSequence name) {
        this.regionName.setText(name);
    }

    void bind(RegionsBuildings regions, int position, TextFormat textFormat, CharArray text) {
        text.clear();
        this.dINumber.setText(text.append(regions.regionDevelopmentIndex(position)).append('%'));
        this.populationNumber.setText(textFormat.compact(text, regions.regionPopulation(position)));
        text.clear();
        this.workersNumber.setText(text.append(regions.regionWorkers(position)).append(" (").append(regions.regionWorkerRatio(position)).append("%)"));
    }

    void setCollapsed(boolean collapsed) {
        Skin economySkin = this.ui.skin(Skins.UI_ECONOMY);
        String style = collapsed ? UiEconomySkin.Styles.Button.ECO_PLATE_MAX_BTN : UiEconomySkin.Styles.Button.ECO_PLATE_MIN_BTN;
        this.showButton.setStyle(economySkin.get(style, Button.ButtonStyle.class));
    }
}
