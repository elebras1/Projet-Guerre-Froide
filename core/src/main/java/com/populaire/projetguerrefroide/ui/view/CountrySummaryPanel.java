package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dto.CountrySummaryDto;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.LocalisationUtils;

import java.util.List;
import java.util.Map;

public class CountrySummaryPanel extends Table implements Disposable {
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

    public CountrySummaryPanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinPortraits, LabelStylePool labelStylePool, Map<String, String> localisation) {
        this.skinFlags = skinFlags;
        this.skinPortraits = skinPortraits;
        this.widgetFactory = widgetFactory;
        Drawable background = widgetFactory.applyBackgroundToTable(skin, "selected_bg", this);

        this.flagImage = widgetFactory.createFlagImage(skinUi, "flag_alpha", "flag_overlay", 109, 77);
        this.flagImage.setPosition(6, 85);
        this.addActor(this.flagImage);

        Label.LabelStyle labelStyleJockey24 = labelStylePool.getLabelStyle("jockey_24");
        Label.LabelStyle labelStyleJockey14Dark = labelStylePool.getLabelStyle("jockey_14_dark");
        Label.LabelStyle labelStyleJockey14 = labelStylePool.getLabelStyle("jockey_14");
        Label.LabelStyle labelStyleJockey14Yellow = labelStylePool.getLabelStyle("jockey_14", "yellow");
        Label.LabelStyle labelStyleJockey14GlowBlue = labelStylePool.getLabelStyle("jockey_14_glow_blue");

        this.countryName = widgetFactory.createLabel("", labelStyleJockey24, 0, 168, background.getMinWidth(), 20, Align.center, this);
        this.government = widgetFactory.createLabel("", labelStyleJockey14Dark, 110, 137, 160, 30, Align.right, this);
        widgetFactory.createLabel(localisation.get("POPULATION"), labelStyleJockey14, 110, 109, 156, 20, Align.left, this);
        this.countryPopulation = widgetFactory.createLabel("", labelStyleJockey14Yellow, 110, 109, 156, 20, Align.right, this);
        this.portrait = widgetFactory.createImageWithOverlay(skin, "tv_overlay", 6, 6, 78, 78, this);
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
            FlagImage flagImage = widgetFactory.createFlagImage(skinUi, "small_flag_mask", "small_flag_overlay", 24, 16);
            flagImage.setPosition(x, y);
            this.alliesFlagImages.add(flagImage);
            x += 18;
        }
    }

    public void update(CountrySummaryDto countrySummaryDto, Map<String, String> localisation) {
        this.countryName.setText(LocalisationUtils.getCountryNameLocalisation(localisation, countrySummaryDto.getIdCountry(), countrySummaryDto.getColonizerId()));
        this.flagImage.setFlag(widgetFactory.getFlagTextureRegion(this.skinFlags, countrySummaryDto.getIdCountry(), countrySummaryDto.getColonizerId()));
        this.government.setText(localisation.get(countrySummaryDto.getGovernment()));
        this.countryPopulation.setText(countrySummaryDto.getPopulation());
        this.portrait.setDrawable(this.skinPortraits.getDrawable(countrySummaryDto.getPortrait()));
        this.leaderFullName.setText(countrySummaryDto.getLeaderFullName());
        List<String> allies = countrySummaryDto.getAllies();
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

    @Override
    public void dispose() {
        this.flagImage.dispose();
    }
}
