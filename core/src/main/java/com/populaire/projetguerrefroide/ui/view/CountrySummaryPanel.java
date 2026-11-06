package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.response.CountrySummary;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.SkinDrawables;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.List;
import java.util.Map;

public class CountrySummaryPanel extends Table {
    private final Label countryName;
    private final Label government;
    private final Label countryPopulation;
    private final Label leaderFullName;
    private final FlagImage flagImage;
    private final Image portrait;
    private final Skin skinFlags;
    private final Skin skinPortraits;
    private final List<FlagImage> alliesFlagImages;
    private final WidgetFactory widgetFactory;
    private final UiCatalog uiCatalog;
    private final TextFormat textFormat;
    private final CharArray text;
    private final Drawable[] portraits;
    private final Drawable defaultPortrait;

    public CountrySummaryPanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinPortraits, Skin skinFonts, Map<String, String> localisation, UiCatalog uiCatalog, TextFormat textFormat) {
        this.skinFlags = skinFlags;
        this.skinPortraits = skinPortraits;
        this.widgetFactory = widgetFactory;
        this.uiCatalog = uiCatalog;
        this.textFormat = textFormat;
        this.text = new CharArray(16);
        Catalog catalog = uiCatalog.catalog();
        this.defaultPortrait = SkinDrawables.optional(skinPortraits, "admin_type", null);
        this.portraits = new Drawable[catalog.ministers().size()];
        for (int i = 0; i < this.portraits.length; i++) {
            String image = catalog.ministerImage(i);
            this.portraits[i] = image != null ? SkinDrawables.optional(skinPortraits, image, this.defaultPortrait) : this.defaultPortrait;
        }
        Drawable background = widgetFactory.applyBackgroundToTable(skin, UiNewgameSkin.Drawables.SELECTED_BG, this);

        this.flagImage = widgetFactory.createFlagImage(skinUi, UiSkin.Drawables.FLAG_ALPHA, UiSkin.Drawables.FLAG_OVERLAY);
        this.flagImage.setPosition(6, 85);
        this.addActor(this.flagImage);

        Label.LabelStyle labelStyleJockey24 = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_24, Label.LabelStyle.class);
        Label.LabelStyle labelStyleJockey14Dark = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_14_DARK, Label.LabelStyle.class);
        Label.LabelStyle labelStyleJockey14 = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_14, Label.LabelStyle.class);
        Label.LabelStyle labelStyleJockey14Yellow = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_14_YELLOW, Label.LabelStyle.class);
        Label.LabelStyle labelStyleJockey14GlowBlue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, Label.LabelStyle.class);

        this.countryName = widgetFactory.createLabel("", labelStyleJockey24, 0, 168, background.getMinWidth(), 20, Align.center, this);
        this.government = widgetFactory.createLabel("", labelStyleJockey14Dark, 110, 137, 160, 30, Align.right, this);
        widgetFactory.createLabel(localisation.get("POPULATION"), labelStyleJockey14, 110, 109, 156, 20, Align.left, this);
        this.countryPopulation = widgetFactory.createLabel("", labelStyleJockey14Yellow, 110, 109, 156, 20, Align.right, this);
        this.portrait = widgetFactory.createImageWithOverlay(skin, UiNewgameSkin.Drawables.TV_OVERLAY, 6, 6, 78, 78, this);
        widgetFactory.createLabel(localisation.get("HEAD_OF_STATE"), labelStyleJockey14Dark, 105, 72, 160, 20, Align.center, this);
        this.leaderFullName = widgetFactory.createLabel("", labelStyleJockey14GlowBlue, 105, 48, 160, 20, Align.center, this);
        widgetFactory.createLabel(localisation.get("WARS"), labelStyleJockey14, 105, 29, 160, 20, Align.left, this);
        widgetFactory.createLabel(localisation.get("ALLIES"), labelStyleJockey14, 105, 10, 160, 20, Align.left, this);

        this.alliesFlagImages = new ObjectList<>();
        this.setAlliesFlags(widgetFactory, skinUi);

        this.setVisible(false);
    }

    public void setAlliesFlags(WidgetFactory widgetFactory, Skin skinUi) {
        int x = 140;
        int y = 9;
        for(int i = 0; i < 7; i++) {
            FlagImage flagImage = widgetFactory.createFlagImage(skinUi, UiSkin.Drawables.SMALL_FLAG_MASK, UiSkin.Drawables.SMALL_FLAG_OVERLAY);
            flagImage.setPosition(x, y);
            this.alliesFlagImages.add(flagImage);
            x += 18;
        }
    }

    public void update(CountrySummary summary) {
        int countryIndex = summary.countryIndex();
        this.countryName.setText(this.uiCatalog.countryName(countryIndex));
        this.flagImage.setFlag(this.uiCatalog.flag(countryIndex));
        int governmentIndex = summary.governmentIndex();
        this.government.setText(governmentIndex >= 0 ? this.uiCatalog.localized(this.uiCatalog.catalog().governments().nameId(governmentIndex)) : null);
        this.countryPopulation.setText(this.textFormat.compact(this.text, summary.population()));
        int headOfState = summary.headOfStateIndex();
        this.portrait.setDrawable(headOfState >= 0 ? this.portraits[headOfState] : this.defaultPortrait);
        this.leaderFullName.setText(headOfState >= 0 ? this.uiCatalog.catalog().ministers().nameId(headOfState) : null);
        for (int i = this.alliesFlagImages.size() - 1; i >= 0; i--) {
            FlagImage allyFlag = this.alliesFlagImages.get(i);
            if (i < summary.allyCount()) {
                allyFlag.setFlag(this.uiCatalog.plainFlag(summary.ally(i)));
                if (allyFlag.getParent() != this) {
                    this.addActor(allyFlag);
                }
            } else if (allyFlag.getParent() != null) {
                allyFlag.remove();
            }
        }

        this.setVisible(true);
    }

    public void hide() {
        this.setVisible(false);
    }
}
