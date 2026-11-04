package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dto.CountrySummaryDto;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.LocalisationUtils;
import com.populaire.projetguerrefroide.util.ValueFormatter;

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

    public CountrySummaryPanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinPortraits, Skin skinFonts, Map<String, String> localisation) {
        this.skinFlags = skinFlags;
        this.skinPortraits = skinPortraits;
        this.widgetFactory = widgetFactory;
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

    public void update(CountrySummaryDto countrySummaryDto, Map<String, String> localisation) {
        this.countryName.setText(LocalisationUtils.getCountryNameLocalisation(localisation, countrySummaryDto.countryNameId(), countrySummaryDto.colonizerId()));
        this.flagImage.setFlag(widgetFactory.getFlagTextureRegion(this.skinFlags, countrySummaryDto.countryNameId(), countrySummaryDto.colonizerId()));
        this.government.setText(localisation.get(countrySummaryDto.government()));
        this.countryPopulation.setText(ValueFormatter.format(countrySummaryDto.population(), localisation));
        this.portrait.setDrawable(this.skinPortraits.getDrawable(countrySummaryDto.portrait()));
        this.leaderFullName.setText(countrySummaryDto.leaderFullName());
        List<String> allies = countrySummaryDto.allies();
        for (int i = this.alliesFlagImages.size() - 1; i >= 0; i--) {
            if (i < allies.size()) {
                this.alliesFlagImages.get(i).setFlag(widgetFactory.getFlagTextureRegion(this.skinFlags, allies.get(i), null));
                this.addActor(this.alliesFlagImages.get(i));
            } else {
                this.alliesFlagImages.get(i).remove();
            }
        }

        this.setVisible(true);
    }

    public void hide() {
        this.setVisible(false);
    }
}
