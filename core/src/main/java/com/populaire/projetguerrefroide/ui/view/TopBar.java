package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.dto.CountryDto;
import com.populaire.projetguerrefroide.screen.listener.TopBarListener;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiTopbarSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.ValueFormatter;

import java.util.Map;

public class TopBar extends Table {
    private final WidgetFactory widgetFactory;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Map<String, String> localisation;
    private Label population;
    private Label manpower;
    private Label grossDomesticProduct;
    private Label money;
    private Label supplies;
    private Label fuel;
    private Label diplomaticInfluence;
    private Label uranium;
    private Label dissent;
    private Label nationalUnity;
    private Label ranking;
    private Image defcon;
    private Image dateState;
    private Label date;

    public TopBar(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinFonts, Map<String, String> localisation, String countryNameId, String colonizerNameId, TopBarListener listener) {
        this.widgetFactory = widgetFactory;
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        this.localisation = localisation;
        Drawable background = skin.getDrawable(UiTopbarSkin.Drawables.NAKED_TOPBAR);
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

        this.setCountryData(skinFonts);
        this.setTabButtons(skinFonts, listener);
        this.setDateSection(skinFonts, listener);
        this.addActor(this.createFlagSection(countryNameId, colonizerNameId));
        this.addActor(this.createPrestigeSection(skinFonts));
    }

    private void setCountryData(Skin skinFonts) {
        Label.LabelStyle labelStyleJockey16GlowBlue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, Label.LabelStyle.class);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_MANPOWER_SMALL_BLUE, 165, 114, this);
        this.population = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 210, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_MANPOWER_ARMY_SMALL_BLUE, 290, 114, this);
        this.manpower = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 330, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_GDP_SMALL_BLUE, 375, 114, this);
        this.grossDomesticProduct = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 415, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_MONEY_SMALL_BLUE, 470, 114, this);
        this.money = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 510, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_SUPPLIES_SMALL_BLUE, 550, 114, this);
        this.supplies = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 590, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_FUEL_SMALL_BLUE, 630, 114, this);
        this.fuel = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 665, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_PRESTIGE_SMALL_BLUE, 715, 114, this);
        this.diplomaticInfluence = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 750, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_NUKE, 792, 116, this);
        this.uranium = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 830, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_DISSENT_SMALL_BLUE, 870, 114, this);
        this.dissent = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 910, 130, this);
        this.widgetFactory.createImage(this.skinUi, UiSkin.Drawables.ICON_UNITY_SMALL_BLUE, 940, 114, this);
        this.nationalUnity = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 975, 130, this);
    }

    private void setDateSection(Skin skinFonts, TopBarListener listener) {
        Label.LabelStyle labelStyleJockey16Dark = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_DARK, Label.LabelStyle.class);
        Button dateSection = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.DATE_BTN, 228, 40, this);
        dateSection.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onTogglePause());
            }
        });
        this.dateState = this.widgetFactory.createImage(this.skin, UiTopbarSkin.Drawables.SPEED_INDICATOR_SLICE_0, 30, 14, dateSection);
        this.date = this.widgetFactory.createLabel("", labelStyleJockey16Dark, 55, 28, dateSection);
        this.addActor(dateSection);

        Button minusSpeed = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.MINUS_SPEED_BTN, 228, 40, this);
        minusSpeed.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onSpeedDown());
            }
        });
        this.addActor(minusSpeed);

        Button plusSpeed = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.PLUS_SPEED_BTN, 228, 64, this);
        plusSpeed.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onSpeedUp());
            }
        });
        this.addActor(plusSpeed);
    }

    private Actor createFlagSection(String countryNameId, String colonizeNameId) {
        Table flagSection = this.widgetFactory.createBackgroundTable(this.skin, UiTopbarSkin.Drawables.SMALL_NAKED_BAR, 37, 40);
        FlagImage flagImage = this.widgetFactory.createFlagImage(this.skinUi, UiSkin.Drawables.FLAG_ALPHA, UiSkin.Drawables.FLAG_OVERLAY);
        flagImage.setPosition(-10, -21);
        flagSection.addActor(flagImage);
        flagImage.setFlag(this.widgetFactory.getFlagTextureRegion(this.skinFlags, countryNameId, colonizeNameId));
        this.widgetFactory.createFlatButton(this.skin, UiTopbarSkin.Drawables.STATS_BTN, 96, 13, flagSection);
        this.widgetFactory.createFlatButton(this.skin, UiTopbarSkin.Drawables.MENU_BTN, 126, 13, flagSection);
        this.defcon = this.widgetFactory.createImage(this.skin, UiTopbarSkin.Drawables.DEFCON_BUTTONS_0, 156, 4, flagSection);
        return flagSection;
    }

    private Actor createPrestigeSection(Skin skinFonts) {
        Label.LabelStyle labelStyleJockey18Yellow = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_18_YELLOW, Label.LabelStyle.class);
        Table prestigeSection = new Table();
        Drawable background = this.skin.getDrawable(UiTopbarSkin.Drawables.PRESTIGE);
        prestigeSection.setBackground(background);
        prestigeSection.setSize(background.getMinWidth(), background.getMinHeight());
        prestigeSection.setPosition(0, 20);
        this.ranking = new Label("", labelStyleJockey18Yellow);
        prestigeSection.add(this.ranking).expand().center();

        return prestigeSection;
    }

    private void setTabButtons(Skin skinFonts, TopBarListener listener) {
        Label.LabelStyle labelStyleJockey16Dark = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_DARK, Label.LabelStyle.class);
        Button economyButton = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.TAB_ECONOMY_BTN, 425, 30, this);
        economyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onEconomyClicked();
            }
        });
        this.widgetFactory.createLabel(this.localisation.get("ECONOMY"), labelStyleJockey16Dark, 10, 20, economyButton);
        Button militaryButton = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.TAB_MILITARY_BTN, 522, 30, this);
        this.widgetFactory.createLabel(this.localisation.get("ARMAMENTS"), labelStyleJockey16Dark, 10, 20, militaryButton);
        Button techButton = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.TAB_TECH_BTN, 619, 30, this);
        this.widgetFactory.createLabel(this.localisation.get("TECH"), labelStyleJockey16Dark, 10, 20, techButton);
        Button politicsButton = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.TAB_POLITICS_BTN, 716, 30, this);
        this.widgetFactory.createLabel(this.localisation.get("POLITICS"), labelStyleJockey16Dark, 10, 20, politicsButton);
        Button diplomacyButton = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.TAB_DIPLOMACY_BTN, 813, 30, this);
        this.widgetFactory.createLabel(this.localisation.get("DIPLOMACY"), labelStyleJockey16Dark, 10, 20, diplomacyButton);
        Button intelButton = this.widgetFactory.createButton(this.skin, UiTopbarSkin.Styles.Button.TAB_INTEL_BTN, 910, 30, this);
        this.widgetFactory.createLabel(this.localisation.get("INTEL"), labelStyleJockey16Dark, 10, 20, intelButton);
    }

    public void setCountryData(CountryDto countryDto) {
        this.population.setText(ValueFormatter.format(countryDto.population(), this.localisation));
        this.manpower.setText(String.valueOf(countryDto.manpower()));
        this.grossDomesticProduct.setText(ValueFormatter.format(countryDto.grossDomesticProduct(), this.localisation));
        this.money.setText(String.valueOf(countryDto.money()));
        this.supplies.setText(String.valueOf(countryDto.supplies()));
        this.fuel.setText(String.valueOf(countryDto.fuel()));
        this.diplomaticInfluence.setText(String.valueOf(countryDto.diplomaticInfluence()));
        this.uranium.setText(String.valueOf(countryDto.uranium()));
        this.dissent.setText(ValueFormatter.format(countryDto.dissent(), this.localisation));
        this.nationalUnity.setText(ValueFormatter.format(countryDto.nationalUnity(), this.localisation));
        this.ranking.setText(String.valueOf(countryDto.ranking()));
        ((Table) this.ranking.getParent()).pack();
    }

    public void setDefcon(int defcon) {
        this.defcon.setDrawable(this.skin, "defcon_buttons_" + defcon);
    }

    public void setDate(String date) {
        this.date.setText(date);
    }

    private void setDateState(int state) {
        this.dateState.setDrawable(this.skin, "speed_indicator_slice_" + state);
    }
}
