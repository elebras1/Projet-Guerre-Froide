package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.response.CountrySummary;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.OverlappingBox;
import com.populaire.projetguerrefroide.ui.widget.SkinDrawables;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class LobbySingleplayer extends UiWindow {
    private final Image frontendSelectedBg;
    private final FlagImage playerShield;
    private final Label selectedNationLabel;
    private final Label government;
    private final Image iconFactions;
    private final Label populationLabel;
    private final Label populationValueLabel;
    private final Label headOfStateLabel;
    private final Label headOfState;
    private final Image photoHeadOfState;
    private final Image photoHeadOfStateOverlay;
    private final Image legendColor;
    private final Image legendColorOverlay;
    private final Label warLabel;
    private final OverlappingBox warsBox;
    private final Label allyLabel;
    private final OverlappingBox alliesBox;
    private final UiCatalog uiCatalog;
    private final TextFormat textFormat;
    private final CharArray text;
    private final ObjectList<SmallFlag> allyFlags;
    private final Drawable[] portraits;
    private final Drawable defaultPortrait;

    public LobbySingleplayer(UiBuilder ui, UiCatalog uiCatalog, TextFormat textFormat) {
        super(ui, 200f, 340f);
        this.uiCatalog = uiCatalog;
        this.textFormat = textFormat;
        this.text = new CharArray(16);
        this.allyFlags = new ObjectList<>();
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        this.frontendSelectedBg = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.SELECTED_BG, 15f, 10f, UiAnchor.UPPER_LEFT);
        this.playerShield = ui.flag(this, uiSkin, UiSkin.Drawables.FLAG_ALPHA, UiSkin.Drawables.FLAG_OVERLAY, 18f, 55f, UiAnchor.UPPER_LEFT);
        this.selectedNationLabel = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_24, 41f, 25f, 230f, 20f, Align.top, UiAnchor.UPPER_LEFT, 1f, 1f);
        this.government = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14_DARK, 66f, 62f, 200f, 160f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconFactions = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.ICON_FACTION_NONALIGNED, 0f, 87f, UiAnchor.UPPER_LEFT);
        this.populationLabel = ui.label(this, "POPULATION", FontsSkin.Styles.Label.JOCKEY_14, 117f, 89f, 200f, 600f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.populationValueLabel = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 120f, 90f, 150f, 600f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.headOfStateLabel = ui.label(this, "HEAD_OF_STATE", FontsSkin.Styles.Label.JOCKEY_14_DARK, 112f, 130f, 160f, 32f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.headOfState = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 116f, 151f, 160f, 32f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.photoHeadOfState = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.EMPTY_POSITION, 23f, 135f, UiAnchor.UPPER_LEFT);
        this.photoHeadOfStateOverlay = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.TV_OVERLAY, 23f, 135f, UiAnchor.UPPER_LEFT);
        this.legendColor = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.POLITICS_IDEOLOGY_LIGHT, 101f, 135f, UiAnchor.UPPER_LEFT);
        this.legendColorOverlay = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.POLITICS_IDEOLOGY_LIGHT_OVERLAY, 101f, 135f, UiAnchor.UPPER_LEFT);
        this.warLabel = ui.label(this, "WARS", FontsSkin.Styles.Label.JOCKEY_14, 117f, 171f, 200f, 160f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.warsBox = ui.overlappingBox(this, 128f, 175f, 182f, 32f, -15f, true, UiAnchor.UPPER_LEFT);
        this.allyLabel = ui.label(this, "ALLIES", FontsSkin.Styles.Label.JOCKEY_14, 117f, 188f, 200f, 160f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.alliesBox = ui.overlappingBox(this, 128f, 193f, 182f, 32f, -15f, true, UiAnchor.UPPER_LEFT);
        this.iconFactions.setVisible(false);
        this.legendColor.setVisible(false);
        this.legendColorOverlay.setVisible(false);
        Skin portraitsSkin = ui.skin(Skins.PORTRAITS);
        Catalog catalog = uiCatalog.catalog();
        this.defaultPortrait = SkinDrawables.optional(portraitsSkin, "admin_type", newGameSkin.getDrawable(UiNewgameSkin.Drawables.EMPTY_POSITION));
        this.portraits = new Drawable[catalog.ministers().size()];
        for (int i = 0; i < this.portraits.length; i++) {
            String image = catalog.ministerImage(i);
            this.portraits[i] = image != null ? SkinDrawables.optional(portraitsSkin, image, this.defaultPortrait) : this.defaultPortrait;
        }
        this.photoHeadOfState.setSize(74f, 74f);
        this.invalidate();
    }

    public void bind(CountrySummary summary) {
        int countryIndex = summary.countryIndex();
        this.selectedNationLabel.setText(this.uiCatalog.countryName(countryIndex));
        this.playerShield.setFlag(this.uiCatalog.flag(countryIndex));
        int governmentIndex = summary.governmentIndex();
        this.government.setText(governmentIndex >= 0 ? this.uiCatalog.localized(this.uiCatalog.catalog().governments().nameId(governmentIndex)) : null);
        this.populationValueLabel.setText(this.textFormat.compact(this.text, summary.population()));
        int headOfStateIndex = summary.headOfStateIndex();
        this.photoHeadOfState.setDrawable(headOfStateIndex >= 0 ? this.portraits[headOfStateIndex] : this.defaultPortrait);
        this.headOfState.setText(headOfStateIndex >= 0 ? this.uiCatalog.catalog().ministers().nameId(headOfStateIndex) : null);
        while (this.allyFlags.size() < summary.allyCount()) {
            this.allyFlags.add(new SmallFlag(this.ui));
        }
        this.alliesBox.clearChildren();
        for (int i = 0; i < summary.allyCount(); i++) {
            SmallFlag allyFlag = this.allyFlags.get(i);
            allyFlag.setFlag(this.uiCatalog.plainFlag(summary.ally(i)));
            this.alliesBox.addActor(allyFlag);
        }
    }

    public TextureRegion flag(int countryIndex) {
        return this.uiCatalog.flag(countryIndex);
    }
}
