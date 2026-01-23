package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.dto.CountryDto;
import com.populaire.projetguerrefroide.screen.TopBarListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class TopBar extends Table {
    private final WidgetFactory widgetFactory;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFlags;
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

    public TopBar(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation, String idCountry, String idColonizer, TopBarListener listener) {
        this.widgetFactory = widgetFactory;
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        Drawable background = skin.getDrawable("naked_topbar");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

        this.setCountryData(labelStylePool);
        this.setTabButtons(labelStylePool, localisation, listener);
        this.setDateSection(labelStylePool, listener);
        this.addActor(this.createFlagSection(idCountry, idColonizer));
        this.addActor(this.createPrestigeSection(labelStylePool));
    }

    private void setCountryData(LabelStylePool labelStylePool) {
        Label.LabelStyle labelStyleJockey16GlowBlue = labelStylePool.get("jockey_16_glow_blue");
        this.widgetFactory.createImage(this.skinUi, "icon_manpower_small_blue", 165, 114, this);
        this.population = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 210, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_manpower_army_small_blue", 290, 114, this);
        this.manpower = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 330, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_gdp_small_blue", 375, 114, this);
        this.grossDomesticProduct = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 415, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_money_small_blue", 470, 114, this);
        this.money = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 510, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_supplies_small_blue", 550, 114, this);
        this.supplies = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 590, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_fuel_small_blue", 630, 114, this);
        this.fuel = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 665, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_prestige_small_blue", 715, 114, this);
        this.diplomaticInfluence = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 750, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_nuke", 792, 116, this);
        this.uranium = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 830, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_dissent_small_blue", 870, 114, this);
        this.dissent = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 910, 130, this);
        this.widgetFactory.createImage(this.skinUi, "icon_unity_small_blue", 940, 114, this);
        this.nationalUnity = this.widgetFactory.createLabel("", labelStyleJockey16GlowBlue, 975, 130, this);
    }

    private void setDateSection(LabelStylePool labelStylePool, TopBarListener listener) {
        Label.LabelStyle labelStyleJockey16Dark = labelStylePool.get("jockey_16_dark");
        Button dateSection = this.widgetFactory.createButton(this.skin, "date_btn", 228, 40, this);
        dateSection.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onTogglePause());
            }
        });
        this.dateState = this.widgetFactory.createImage(this.skin, "speed_indicator_slice_0", 30, 14, dateSection);
        this.date = this.widgetFactory.createLabel("", labelStyleJockey16Dark, 55, 28, dateSection);
        this.addActor(dateSection);

        Button minusSpeed = this.widgetFactory.createButton(this.skin, "minus_speed", 228, 40, this);
        minusSpeed.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onSpeedDown());
            }
        });
        this.addActor(minusSpeed);

        Button plusSpeed = this.widgetFactory.createButton(this.skin, "plus_speed", 228, 64, this);
        plusSpeed.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onSpeedUp());
            }
        });
        this.addActor(plusSpeed);
    }

    private Actor createFlagSection(String idCountry, String idColonizer) {
        Table flagSection = this.widgetFactory.createBackgroundTable(this.skin, "small_naked_bar", 37, 40);
        FlagImage flagImage = this.widgetFactory.createFlagImage(this.skinUi, "flag_alpha", "flag_overlay");
        flagImage.setPosition(-10, -21);
        flagSection.addActor(flagImage);
        flagImage.setFlag(this.widgetFactory.getFlagTextureRegion(this.skinFlags, idCountry, idColonizer));
        this.widgetFactory.createFlatButton(this.skin, "stats", 96, 13, flagSection);
        this.widgetFactory.createFlatButton(this.skin, "menu", 126, 13, flagSection);
        this.defcon = this.widgetFactory.createImage(this.skin, "defcon_buttons_0", 156, 4, flagSection);
        return flagSection;
    }

    private Actor createPrestigeSection(LabelStylePool labelStylePool) {
        Label.LabelStyle labelStyleJockey18Yellow = labelStylePool.get("jockey_18", "yellow");
        Table prestigeSection = new Table();
        Drawable background = this.skin.getDrawable("prestige");
        prestigeSection.setBackground(background);
        prestigeSection.setSize(background.getMinWidth(), background.getMinHeight());
        prestigeSection.setPosition(0, 20);
        this.ranking = new Label("", labelStyleJockey18Yellow);
        prestigeSection.add(this.ranking).expand().center();

        return prestigeSection;
    }

    private void setTabButtons(LabelStylePool labelStylePool, Map<String, String> localisation, TopBarListener listener) {
        Label.LabelStyle labelStyleJockey16Dark = labelStylePool.get("jockey_16_dark");
        Button economyButton = this.widgetFactory.createButton(this.skin, "tab_economy", 425, 30, this);
        economyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onEconomyClicked();
            }
        });
        this.widgetFactory.createLabel(localisation.get("ECONOMY"), labelStyleJockey16Dark, 10, 20, economyButton);
        Button militaryButton = this.widgetFactory.createButton(this.skin, "tab_military", 522, 30, this);
        this.widgetFactory.createLabel(localisation.get("ARMAMENTS"), labelStyleJockey16Dark, 10, 20, militaryButton);
        Button techButton = this.widgetFactory.createButton(this.skin, "tab_tech", 619, 30, this);
        this.widgetFactory.createLabel(localisation.get("TECH"), labelStyleJockey16Dark, 10, 20, techButton);
        Button politicsButton = this.widgetFactory.createButton(this.skin, "tab_politics", 716, 30, this);
        this.widgetFactory.createLabel(localisation.get("POLITICS"), labelStyleJockey16Dark, 10, 20, politicsButton);
        Button diplomacyButton = this.widgetFactory.createButton(this.skin, "tab_diplomacy", 813, 30, this);
        this.widgetFactory.createLabel(localisation.get("DIPLOMACY"), labelStyleJockey16Dark, 10, 20, diplomacyButton);
        Button intelButton = this.widgetFactory.createButton(this.skin, "tab_intel", 910, 30, this);
        this.widgetFactory.createLabel(localisation.get("INTEL"), labelStyleJockey16Dark, 10, 20, intelButton);
    }

    public void setCountryData(CountryDto countryDto) {
        this.population.setText(countryDto.population());
        this.manpower.setText(String.valueOf(countryDto.manpower()));
        this.grossDomesticProduct.setText(countryDto.grossDomesticProduct());
        this.money.setText(String.valueOf(countryDto.money()));
        this.supplies.setText(String.valueOf(countryDto.supplies()));
        this.fuel.setText(String.valueOf(countryDto.fuel()));
        this.diplomaticInfluence.setText(String.valueOf(countryDto.diplomaticInfluence()));
        this.uranium.setText(String.valueOf(countryDto.uranium()));
        this.dissent.setText(countryDto.dissent());
        this.nationalUnity.setText(countryDto.nationalUnity());
    }

    public void setRanking(int ranking) {
        this.ranking.setText(String.valueOf(ranking));
        Table parent = (Table) this.ranking.getParent();
        parent.pack();
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
