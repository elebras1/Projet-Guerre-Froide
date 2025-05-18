package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.populaire.projetguerrefroide.dto.CountryDto;
import com.populaire.projetguerrefroide.screen.TopBarListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;

import java.util.Map;

public class TopBar extends Table {
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

    public TopBar(Skin skin, Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation, String idCountry, TopBarListener listener) {
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        Drawable background = skin.getDrawable("naked_topbar");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

        this.setCountryData(labelStylePool);
        this.setTabButtons(labelStylePool, localisation);
        this.setDateSection(labelStylePool, listener);
        this.addActor(this.createFlagSection(idCountry));
        this.addActor(this.createPrestigeSection(labelStylePool));
    }

    private void setCountryData(LabelStylePool labelStylePool) {
        Label.LabelStyle labelStyleJockey16GlowBlue = labelStylePool.getLabelStyle("jockey_16_glow_blue");
        this.addImage(this.skinUi, "icon_manpower_small_blue", 165, 114, this);
        this.population = this.addLabel("", labelStyleJockey16GlowBlue, 210, 130, this);
        this.addImage(this.skinUi, "icon_manpower_army_small_blue", 290, 114, this);
        this.manpower = this.addLabel("", labelStyleJockey16GlowBlue, 330, 130, this);
        this.addImage(this.skinUi, "icon_gdp_small_blue", 375, 114, this);
        this.grossDomesticProduct = this.addLabel("", labelStyleJockey16GlowBlue, 415, 130, this);
        this.addImage(this.skinUi, "icon_money_small_blue", 470, 114, this);
        this.money = this.addLabel("", labelStyleJockey16GlowBlue, 510, 130, this);
        this.addImage(this.skinUi, "icon_supplies_small_blue", 550, 114, this);
        this.supplies = this.addLabel("", labelStyleJockey16GlowBlue, 590, 130, this);
        this.addImage(this.skinUi, "icon_fuel_small_blue", 630, 114, this);
        this.fuel = this.addLabel("", labelStyleJockey16GlowBlue, 665, 130, this);
        this.addImage(this.skinUi, "icon_prestige_small_blue", 715, 114, this);
        this.diplomaticInfluence = this.addLabel("", labelStyleJockey16GlowBlue, 750, 130, this);
        this.addImage(this.skinUi, "icon_nuke", 792, 116, this);
        this.uranium = this.addLabel("", labelStyleJockey16GlowBlue, 830, 130, this);
        this.addImage(this.skinUi, "icon_dissent_small_blue", 870, 114, this);
        this.dissent = this.addLabel("", labelStyleJockey16GlowBlue, 910, 130, this);
        this.addImage(this.skinUi, "icon_unity_small_blue", 940, 114, this);
        this.nationalUnity = this.addLabel("", labelStyleJockey16GlowBlue, 975, 130, this);
    }

    private void setDateSection(LabelStylePool labelStylePool, TopBarListener listener) {
        Label.LabelStyle labelStyleJockey16Dark = labelStylePool.getLabelStyle("jockey_16_dark");
        Button dateSection = this.addButton("date_btn", 228, 40);
        dateSection.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onTogglePause());
            }
        });
        this.dateState = this.addImage("speed_indicator_slice_0", 30, 14, dateSection);
        this.date = this.addLabel("", labelStyleJockey16Dark, 55, 28, dateSection);
        this.addActor(dateSection);

        Button minusSpeed = this.addButton("minus_speed", 228, 40);
        minusSpeed.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onSpeedDown());
            }
        });
        this.addActor(minusSpeed);

        Button plusSpeed = this.addButton("plus_speed", 228, 64);
        plusSpeed.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setDateState(listener.onSpeedUp());
            }
        });
        this.addActor(plusSpeed);
    }

    private Actor createFlagSection(String idCountry) {
        Table flagSection = this.addBackground("small_naked_bar", 37, 40);
        FlagImage flagImage = this.addFlagImage(this.skinUi, "flag_alpha", "flag_overlay", 109, 77);
        flagImage.setPosition(-10, -21);
        flagSection.addActor(flagImage);
        flagImage.setFlag(this.skinFlags.getRegion(idCountry));
        this.addBasicButton("stats", 96, 13, flagSection);
        this.addBasicButton("menu", 126, 13, flagSection);
        this.defcon = this.addImage("defcon_buttons_0", 156, 4, flagSection);
        return flagSection;
    }

    private Actor createPrestigeSection(LabelStylePool labelStylePool) {
        Label.LabelStyle labelStyleJockey18Yellow = labelStylePool.getLabelStyle("jockey_18", "yellow");
        Table prestigeSection = new Table();
        Drawable background = this.skin.getDrawable("prestige");
        prestigeSection.setBackground(background);
        prestigeSection.setSize(background.getMinWidth(), background.getMinHeight());
        prestigeSection.setPosition(0, 20);
        this.ranking = new Label("", labelStyleJockey18Yellow);
        prestigeSection.add(this.ranking).expand().center();

        return prestigeSection;
    }

    private void setTabButtons(LabelStylePool labelStylePool, Map<String, String> localisation) {
        Label.LabelStyle labelStyleJockey16Dark = labelStylePool.getLabelStyle("jockey_16_dark");
        Button economyButton = this.addButton("tab_economy", 425, 30);
        this.addLabel(localisation.get("ECONOMY"), labelStyleJockey16Dark, 10, 20, economyButton);
        Button militaryButton = this.addButton("tab_military", 522, 30);
        this.addLabel(localisation.get("ARMAMENTS"), labelStyleJockey16Dark, 10, 20, militaryButton);
        Button techButton = this.addButton("tab_tech", 619, 30);
        this.addLabel(localisation.get("TECH"), labelStyleJockey16Dark, 10, 20, techButton);
        Button politicsButton = this.addButton("tab_politics", 716, 30);
        this.addLabel(localisation.get("POLITICS"), labelStyleJockey16Dark, 10, 20, politicsButton);
        Button diplomacyButton = this.addButton("tab_diplomacy", 813, 30);
        this.addLabel(localisation.get("DIPLOMACY"), labelStyleJockey16Dark, 10, 20, diplomacyButton);
        Button intelButton = this.addButton("tab_intel", 910, 30);
        this.addLabel(localisation.get("INTEL"), labelStyleJockey16Dark, 10, 20, intelButton);
    }

    public void setCountryData(CountryDto countryDto) {
        this.population.setText(countryDto.getPopulation());
        this.manpower.setText(String.valueOf(countryDto.getManpower()));
        this.grossDomesticProduct.setText(countryDto.getGrossDomesticProduct());
        this.money.setText(String.valueOf(countryDto.getMoney()));
        this.supplies.setText(String.valueOf(countryDto.getSupplies()));
        this.fuel.setText(String.valueOf(countryDto.getFuel()));
        this.diplomaticInfluence.setText(String.valueOf(countryDto.getDiplomaticInfluence()));
        this.uranium.setText(String.valueOf(countryDto.getUranium()));
        this.dissent.setText(countryDto.getDissent());
        this.nationalUnity.setText(countryDto.getNationalUnity());
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

    private Table addBackground(String backgroundName, float x, float y) {
        Drawable background = this.skin.getDrawable(backgroundName);
        Table table = new Table();
        table.setBackground(background);
        table.setSize(background.getMinWidth(), background.getMinHeight());
        table.setPosition(x, y);
        return table;
    }

    private Button addButton(String styleName, float x, float y, Group parent) {
        Button button = new Button(this.skin, styleName);
        button.setPosition(x, y);
        parent.addActor(button);
        return button;
    }

    private Button addButton(String styleName, float x, float y) {
        return this.addButton(styleName, x, y, this);
    }

    private Button addBasicButton(String drawableName, float x, float y, Group parent) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = this.skin.getDrawable(drawableName);
        Button button = new Button(style);
        button.setPosition(x, y);
        parent.addActor(button);
        return button;
    }

    private Image addImage(String drawableName, float x, float y, Group parent) {
        Image image = new Image(this.skin.getDrawable(drawableName));
        image.setPosition(x, y);
        parent.addActor(image);
        return image;
    }

    private Image addImage(Skin skin, String drawableName, float x, float y, Group parent) {
        Image image = new Image(skin.getDrawable(drawableName));
        image.setPosition(x, y);
        parent.addActor(image);
        return image;
    }

    private FlagImage addFlagImage(Skin skinUi, String alphaFlagName, String overlayFlagName, int width, int height) {
        TextureRegion alphaFlag = skinUi.getRegion(alphaFlagName);
        TextureRegion overlayFlag = skinUi.getRegion(overlayFlagName);
        Pixmap defaultPixmapFlag = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        TextureRegionDrawable defaultFlag = new TextureRegionDrawable(new Texture(defaultPixmapFlag));
        defaultPixmapFlag.dispose();
        return new FlagImage(defaultFlag, overlayFlag, alphaFlag);
    }

    private Label addLabel(String text, Label.LabelStyle style, float x, float y, Group parent) {
        Label label = new Label(text, style);
        label.setPosition(x, y);
        parent.addActor(label);
        return label;
    }
}
