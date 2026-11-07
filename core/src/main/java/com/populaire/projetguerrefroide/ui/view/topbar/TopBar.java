package com.populaire.projetguerrefroide.ui.view.topbar;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.populaire.projetguerrefroide.protocol.response.CountryOverview;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiTopbarSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.OverlappingBox;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class TopBar extends UiWindow {
    private static final int SPEED_STATES = 6;
    private static final int DEFCON_LEVELS = 6;
    private final Image topbarBg;
    private final Image iconPopulation;
    private final Label populationNumber;
    private final Image iconManpower;
    private final Label manpowerNumber;
    private final Image iconGdp;
    private final Label gdpNumber;
    private final Image iconMoney;
    private final Label moneyNumber;
    private final Image iconSupplies;
    private final Label suppliesNumber;
    private final Image iconFuel;
    private final Label fuelNumber;
    private final Image iconPrestige;
    private final Label diplomaticInfluenceNumber;
    private final Image iconUranium;
    private final Label uraniumNumber;
    private final Image iconDissent;
    private final Label dissentNumber;
    private final Image iconNationalUnity;
    private final Label nationalUnityNumber;
    private final Button topbarbuttonEconomy;
    private final Label economyLabel;
    private final Label economyMoneychange;
    private final Button topbarbuttonProd;
    private final Label aRMSLabel;
    private final Image armsProgress;
    private final Image armsProjIcon;
    private final Image progressOverlay;
    private final Label armsProjText;
    private final Label armsProgressText;
    private final Button topbarbuttonTech;
    private final Label tECHLabel;
    private final Image techProgress;
    private final Image techProjIcon;
    private final Image progressOverlay2;
    private final Label techProjText;
    private final Label techProgressText;
    private final Button topbarbuttonPolitics;
    private final Label pOLITICSLabel;
    private final Label polElectionYear;
    private final Button topbarbuttonDiplo;
    private final Label dIPLOLabel;
    private final Label diploPeace;
    private final OverlappingBox diploStatus;
    private final Button topbarbuttonIntelligence;
    private final Label iNTELLabel;
    private final Image intSatIcon;
    private final Label intSatNumber;
    private final Image intSpyIcon;
    private final Label intSpyNumber;
    private final Button topbarbuttonStatistics;
    private final Button topbarMenubutton;
    private final Image topbarOutlinerbuttonBg;
    private final Button topbarOutlinerbutton;
    private final Button pauseBg;
    private final Button buttonSpeedup;
    private final Button buttonSpeeddown;
    private final Label dateText;
    private final Image speedIndicator;
    private final Image defcon;
    private final FlagImage playerFlag;
    private final Image prestigeBg;
    private final Label ranking;
    private final Drawable[] speedStates;
    private final Drawable[] defconLevels;
    private final TextFormat textFormat;
    private final CharArray text;

    public TopBar(UiBuilder ui, TextFormat textFormat, TopBarListener listener) {
        super(ui);
        this.textFormat = textFormat;
        this.text = new CharArray(32);
        Skin topbarSkin = ui.skin(Skins.UI_TOPBAR);
        Skin uiSkin = ui.skin(Skins.UI);
        this.topbarBg = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.NAKED_TOPBAR, 5f, -7f, UiAnchor.UPPER_LEFT);
        // doomsday_clock : GFX_doomsday_clock (Doomsday Clock écartée du projet)
        this.iconPopulation = ui.image(this, uiSkin, UiSkin.Drawables.ICON_MANPOWER_SMALL_BLUE, 170f, 3f, UiAnchor.UPPER_LEFT);
        this.populationNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 200f, 5f, 55f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconManpower = ui.image(this, uiSkin, UiSkin.Drawables.ICON_MANPOWER_ARMY_SMALL_BLUE, 280f, 3f, UiAnchor.UPPER_LEFT);
        this.manpowerNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 315f, 5f, 55f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconGdp = ui.image(this, uiSkin, UiSkin.Drawables.ICON_GDP_SMALL_BLUE, 375f, 3f, UiAnchor.UPPER_LEFT);
        this.gdpNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 415f, 5f, 55f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconMoney = ui.image(this, uiSkin, UiSkin.Drawables.ICON_MONEY_SMALL_BLUE, 475f, 3f, UiAnchor.UPPER_LEFT);
        this.moneyNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 503f, 5f, 60f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconSupplies = ui.image(this, uiSkin, UiSkin.Drawables.ICON_SUPPLIES_SMALL_BLUE, 555f, 3f, UiAnchor.UPPER_LEFT);
        this.suppliesNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 582f, 5f, 45f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconFuel = ui.image(this, uiSkin, UiSkin.Drawables.ICON_FUEL_SMALL_BLUE, 640f, 3f, UiAnchor.UPPER_LEFT);
        this.fuelNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 670f, 5f, 45f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconPrestige = ui.image(this, uiSkin, UiSkin.Drawables.ICON_PRESTIGE_SMALL_BLUE, 720f, 3f, UiAnchor.UPPER_LEFT);
        this.diplomaticInfluenceNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 750f, 5f, 45f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconUranium = ui.image(this, uiSkin, UiSkin.Drawables.ICON_URANIUM_SMALL_BLUE, 795f, 3f, UiAnchor.UPPER_LEFT);
        this.uraniumNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 835f, 5f, 35f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconDissent = ui.image(this, uiSkin, UiSkin.Drawables.ICON_DISSENT_SMALL_BLUE, 873f, 3f, UiAnchor.UPPER_LEFT);
        this.dissentNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 900f, 5f, 45f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.iconNationalUnity = ui.image(this, uiSkin, UiSkin.Drawables.ICON_UNITY_SMALL_BLUE, 938f, 3f, UiAnchor.UPPER_LEFT);
        this.nationalUnityNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 970f, 5f, 45f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.topbarbuttonEconomy = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.TAB_ECONOMY_BTN, 436f, 35f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.topbarbuttonEconomy, Input.Keys.F1);
        this.economyLabel = ui.label(this, "ECONOMY", FontsSkin.Styles.Label.JOCKEY_16_DARK, 443f, 67f, 75f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.economyMoneychange = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 456f, 88f, 65f, 30f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.topbarbuttonProd = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.TAB_MILITARY_BTN, 532f, 35f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.topbarbuttonProd, Input.Keys.F2);
        this.aRMSLabel = ui.label(this, "ARMAMENTS", FontsSkin.Styles.Label.JOCKEY_16_DARK, 539f, 67f, 75f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.armsProgress = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.TOPBAR_PROGRESS, 541f, 94f, UiAnchor.UPPER_LEFT);
        this.armsProjIcon = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.TOPBAR_FOLDER, 539f, 89f, UiAnchor.UPPER_LEFT);
        this.progressOverlay = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.TOPBAR_PROGRESS_OVERLAY, 538f, 92f, UiAnchor.UPPER_LEFT);
        this.armsProjText = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 558f, 88f, 20f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.armsProgressText = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 596f, 88f, 50f, 30f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.topbarbuttonTech = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.TAB_TECH_BTN, 628f, 35f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.topbarbuttonTech, Input.Keys.F3);
        this.tECHLabel = ui.label(this, "TECH", FontsSkin.Styles.Label.JOCKEY_16_DARK, 636f, 67f, 75f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.techProgress = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.TOPBAR_PROGRESS, 637f, 94f, UiAnchor.UPPER_LEFT);
        this.techProjIcon = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.TOPBAR_FOLDER, 638f, 89f, UiAnchor.UPPER_LEFT);
        this.progressOverlay2 = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.TOPBAR_PROGRESS_OVERLAY, 634f, 92f, UiAnchor.UPPER_LEFT);
        this.techProjText = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 657f, 88f, 20f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.techProgressText = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 686f, 88f, 50f, 30f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.topbarbuttonPolitics = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.TAB_POLITICS_BTN, 724f, 35f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.topbarbuttonPolitics, Input.Keys.F4);
        this.pOLITICSLabel = ui.label(this, "POLITICS", FontsSkin.Styles.Label.JOCKEY_16_DARK, 733f, 67f, 75f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.polElectionYear = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 737f, 88f, 60f, 30f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.topbarbuttonDiplo = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.TAB_DIPLOMACY_BTN, 820f, 35f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.topbarbuttonDiplo, Input.Keys.F5);
        this.dIPLOLabel = ui.label(this, "DIPLOMACY", FontsSkin.Styles.Label.JOCKEY_16_DARK, 828f, 67f, 75f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.diploPeace = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 817f, 88f, 94f, 30f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.diploStatus = ui.overlappingBox(this, 817f, 94f, 94f, 32f, 1f, false, UiAnchor.UPPER_LEFT);
        this.topbarbuttonIntelligence = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.TAB_INTEL_BTN, 916f, 35f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.topbarbuttonIntelligence, Input.Keys.F6);
        this.iNTELLabel = ui.label(this, "INTEL", FontsSkin.Styles.Label.JOCKEY_16_DARK, 924f, 67f, 75f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.intSatIcon = ui.image(this, uiSkin, UiSkin.Drawables.ICON_SATELLITE_BLUE, 926f, 91f, UiAnchor.UPPER_LEFT);
        this.intSatNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 946f, 88f, 30f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.intSpyIcon = ui.image(this, uiSkin, UiSkin.Drawables.ICON_SPY_BLUE, 963f, 90f, UiAnchor.UPPER_LEFT);
        this.intSpyNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 988f, 88f, 60f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.topbarbuttonStatistics = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.STATS_BTN, 135f, 64f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.topbarbuttonStatistics, Input.Keys.F7);
        this.topbarMenubutton = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.MENU_BTN, 159f, 64f, UiAnchor.UPPER_LEFT);
        this.topbarOutlinerbuttonBg = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.OOB_UNIT_TREE_BG, -30f, 118f, UiAnchor.UPPER_RIGHT);
        this.topbarOutlinerbutton = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.OOB_UNIT_TREE_BTN, -30f, 119f, UiAnchor.UPPER_RIGHT);
        this.pauseBg = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.DATE_BTN, 242f, 53f, UiAnchor.UPPER_LEFT);
        this.buttonSpeedup = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.PLUS_SPEED_BTN, 221f, 53f, UiAnchor.UPPER_LEFT);
        this.buttonSpeeddown = ui.button(this, topbarSkin, UiTopbarSkin.Styles.Button.MINUS_SPEED_BTN, 221f, 75f, UiAnchor.UPPER_LEFT);
        this.dateText = ui.label(this, "", FontsSkin.Styles.Label.ARIAL14, 278f, 68f, 190f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.speedIndicator = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.SPEED_INDICATOR_SLICE_0, 246f, 65f, UiAnchor.UPPER_LEFT);
        this.defcon = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.DEFCON_BUTTONS_0, 187f, 57f, UiAnchor.UPPER_LEFT);
        // doomsday_clock_overlay : GFX_doomsday_clock_overlay (Doomsday Clock écartée du projet)
        this.playerFlag = ui.flag(this, uiSkin, UiSkin.Drawables.FLAG_ALPHA, UiSkin.Drawables.FLAG_OVERLAY, 37f, 49f, UiAnchor.UPPER_LEFT);
        this.prestigeBg = ui.image(this, topbarSkin, UiTopbarSkin.Drawables.PRESTIGE, 5f, 60f, UiAnchor.UPPER_LEFT);
        this.ranking = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_18_YELLOW, 10f, 85f, 40f, 20f, Align.top, UiAnchor.UPPER_LEFT);
        this.speedStates = new Drawable[SPEED_STATES];
        for (int state = 0; state < SPEED_STATES; state++) {
            this.speedStates[state] = topbarSkin.getDrawable("speed_indicator_slice_" + state);
        }
        this.defconLevels = new Drawable[DEFCON_LEVELS];
        for (int level = 0; level < DEFCON_LEVELS; level++) {
            this.defconLevels[level] = topbarSkin.getDrawable("defcon_buttons_" + level);
        }
        this.speedIndicator.setTouchable(Touchable.disabled);
        this.armsProgress.setVisible(false);
        this.techProgress.setVisible(false);
        ui.onClick(this.topbarbuttonEconomy, listener::onEconomyClicked);
        ui.onClick(this.topbarMenubutton, listener::onMenuClicked);
        ui.onClick(this.pauseBg, () -> this.setSpeedState(listener.onTogglePause()));
        ui.onClick(this.buttonSpeedup, () -> this.setSpeedState(listener.onSpeedUp()));
        ui.onClick(this.buttonSpeeddown, () -> this.setSpeedState(listener.onSpeedDown()));
    }

    public void setFlag(TextureRegion flag) {
        this.playerFlag.setFlag(flag);
    }

    public void setCountry(CountryOverview country) {
        this.populationNumber.setText(this.textFormat.compact(this.text, country.population()));
        this.manpowerNumber.setText(TextFormat.integer(this.text, country.manpower()));
        this.gdpNumber.setText(this.textFormat.compact(this.text, country.grossDomesticProduct()));
        this.moneyNumber.setText(TextFormat.integer(this.text, country.money()));
        this.suppliesNumber.setText(TextFormat.integer(this.text, country.supplies()));
        this.fuelNumber.setText(TextFormat.integer(this.text, country.fuel()));
        this.text.clear();
        this.diplomaticInfluenceNumber.setText(TextFormat.appendDecimal(this.text, country.diplomaticInfluence(), 1, '.'));
        this.uraniumNumber.setText(TextFormat.integer(this.text, country.uranium()));
        this.dissentNumber.setText(this.textFormat.compact(this.text, country.dissent()));
        this.nationalUnityNumber.setText(this.textFormat.compact(this.text, country.nationalUnity()));
        TextFormat.integer(this.text, country.ranking());
        if (!this.ranking.textEquals(this.text)) {
            this.ranking.setText(this.text);
        }
    }

    public void setDate(CharSequence date) {
        this.dateText.setText(date);
    }

    public void setSpeedState(int state) {
        this.speedIndicator.setDrawable(this.speedStates[state]);
    }

    public void setDefcon(int level) {
        this.defcon.setDrawable(this.defconLevels[level]);
    }
}
