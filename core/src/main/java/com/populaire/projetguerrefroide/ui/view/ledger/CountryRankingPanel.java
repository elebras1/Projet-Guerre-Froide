package com.populaire.projetguerrefroide.ui.view.ledger;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.populaire.projetguerrefroide.protocol.request.CountrySort;
import com.populaire.projetguerrefroide.protocol.response.CountryRankings;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiLedgerSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.LabelUtils;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class CountryRankingPanel extends UiWindow {
    // ledger_overlay (ledger_columns, at 51,140): header strip 143-196, then 15 stripes of 30 px from 198
    private static final float TABLE_X = 51f;
    private static final float TABLE_Y = 198f;
    private static final float TABLE_WIDTH = 980f;
    private static final float TABLE_HEIGHT = 450f;
    private static final float COLUMN_HEADER_Y = 155f;
    private final int playerCountry;
    private final CharArray text;
    private final Image bgPoliticsShadow;
    private final Image bgCountryskin;
    private final Label headlineStatistics;
    private final Image bgStatistics;
    private final Image ledgerOverlay;
    private final LinechartOptions linechartOptions;
    private final NoGraphData noGraphData;
    private final GraphBackground graphBackground;
    private final Label header;
    private final Label pageNumber;
    private final Button previousButton;
    private final Button nextButton;
    private final Label textboxAutosend;
    private final Button closeButton;
    private final Button ledgerOverview;
    private final Label ledgerOverviewLabel;
    private final Button ledgerEconomy;
    private final Label ledgerEconomyLabel;
    private final Button ledgerMisc;
    private final Label ledgerMiscLabel;
    private final Button ledgerMilitary;
    private final Label ledgerMilitaryLabel;
    private final Button ledgerHistory;
    private final Label ledgerHistoryLabel;
    private final Button[] tabButtons;
    private final Button.ButtonStyle tabNormalStyle;
    private final Button.ButtonStyle tabActiveStyle;
    private final Label rankHeader;
    private final Label countryHeader;
    private final Label[] metricHeaders;
    private final UiList<CountryRankingEntry> table;
    private int page;

    public CountryRankingPanel(UiBuilder ui, UiCatalog uiCatalog, TextFormat textFormat, int playerCountry, CountryRankingListener listener) {
        super(ui, 1120f, 768f);
        this.playerCountry = playerCountry;
        this.text = new CharArray(16);
        Skin ledgerSkin = ui.skin(Skins.UI_LEDGER);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.bgPoliticsShadow = ui.image(this, ledgerSkin, UiLedgerSkin.Drawables.BG_SHADOW, 14f, 22f, UiAnchor.UPPER_LEFT);
        this.bgCountryskin = ui.image(this, ledgerSkin, UiLedgerSkin.Drawables.COUNTRY_BG, 44f, 54f, UiAnchor.UPPER_LEFT);
        this.headlineStatistics = ui.label(this, "headline_statistics", FontsSkin.Styles.Label.JOCKEY_30_GLOW_GREEN, 45f, 23f, 300f, 64f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.bgStatistics = ui.image(this, ledgerSkin, UiLedgerSkin.Drawables.BG_STATISTICS, -22f, -5f, UiAnchor.UPPER_LEFT);
        this.ledgerOverlay = ui.image(this, ledgerSkin, UiLedgerSkin.Drawables.LEDGER_COLUMNS, 51f, 140f, UiAnchor.UPPER_LEFT);
        this.linechartOptions = ui.add(this, new LinechartOptions(ui), 61f, 138f, UiAnchor.UPPER_LEFT);
        this.linechartOptions.setVisible(false);
        this.noGraphData = ui.add(this, new NoGraphData(ui), 200f, 160f, UiAnchor.UPPER_LEFT);
        this.noGraphData.setVisible(false);
        this.graphBackground = ui.add(this, new GraphBackground(ui), 200f, 160f, UiAnchor.UPPER_LEFT);
        this.graphBackground.setVisible(false);
        this.header = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_24, 360f, 106f, 340f, 30f, Align.top, UiAnchor.UPPER_LEFT, 4f, 4f);
        // EvW 530,658 30x30 : élargi à la case entre les flèches pour « page/total »
        this.pageNumber = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_18, 515f, 658f, 72f, 30f, Align.top, UiAnchor.UPPER_LEFT, 4f, 4f);
        this.previousButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_LEFT_BTN, 493f, 655f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.previousButton, Input.Keys.LEFT);
        this.nextButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_RIGHT_BTN, 593f, 655f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.nextButton, Input.Keys.RIGHT);
        // checkbox_autosend : sprite GFX_checkbox sans asset PGF
        this.textboxAutosend = ui.label(this, "", FontsSkin.Styles.Label.ARIAL14, 2200f, 585f, 200f, 30f, Align.topLeft, UiAnchor.UPPER_LEFT, 1f, 1f);
        this.closeButton = ui.button(this, uiSkin, UiSkin.Styles.Button.CLOSE_BTN, 1035f, 29f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.closeButton, Input.Keys.ESCAPE);
        this.ledgerOverview = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_ECO_TRADE_BTN, 307f, 70f, UiAnchor.UPPER_LEFT);
        this.ledgerOverviewLabel = ui.label(this, "OVERVIEW", FontsSkin.Styles.Label.JOCKEY_16_DARK, 300f, 75f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.ledgerEconomy = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_ECO_TRADE_BTN, 398f, 70f, UiAnchor.UPPER_LEFT);
        this.ledgerEconomyLabel = ui.label(this, "ECONOMY", FontsSkin.Styles.Label.JOCKEY_16_DARK, 392f, 75f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.ledgerMisc = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_ECO_TRADE_BTN, 489f, 70f, UiAnchor.UPPER_LEFT);
        this.ledgerMiscLabel = ui.label(this, "MISC", FontsSkin.Styles.Label.JOCKEY_16_DARK, 482f, 75f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.ledgerMilitary = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_ECO_TRADE_BTN, 580f, 70f, UiAnchor.UPPER_LEFT);
        this.ledgerMilitaryLabel = ui.label(this, "MILITARY", FontsSkin.Styles.Label.JOCKEY_16_DARK, 574f, 75f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.ledgerHistory = ui.button(this, economySkin, UiEconomySkin.Styles.Button.ECO_ECO_TRADE_BTN, 671f, 70f, UiAnchor.UPPER_LEFT);
        this.ledgerHistoryLabel = ui.label(this, "HISTORY", FontsSkin.Styles.Label.JOCKEY_16_DARK, 665f, 75f, 90f, 28f, Align.top, UiAnchor.UPPER_LEFT, 0f, 4f);

        this.tabButtons = new Button[]{this.ledgerOverview, this.ledgerEconomy, this.ledgerMisc, this.ledgerMilitary, this.ledgerHistory};
        Label[] tabLabels = {this.ledgerOverviewLabel, this.ledgerEconomyLabel, this.ledgerMiscLabel, this.ledgerMilitaryLabel, this.ledgerHistoryLabel};
        for (int tab = 0; tab < this.tabButtons.length; tab++) {
            boolean hasPages = LedgerPages.firstPage(tab) >= 0;
            this.tabButtons[tab].setVisible(hasPages);
            tabLabels[tab].setVisible(hasPages);
        }
        this.tabNormalStyle = economySkin.get(UiEconomySkin.Styles.Button.ECO_ECO_TRADE_BTN, Button.ButtonStyle.class);
        this.tabActiveStyle = new Button.ButtonStyle(this.tabNormalStyle);
        this.tabActiveStyle.up = this.tabNormalStyle.down;

        this.rankHeader = ui.label(this, "LEDGER_RANK", FontsSkin.Styles.Label.JOCKEY_16_DARK, TABLE_X + 6f, COLUMN_HEADER_Y, 48f, 24f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.countryHeader = ui.label(this, "LEDGER_COUNTRY", FontsSkin.Styles.Label.JOCKEY_16_DARK, TABLE_X + CountryRankingEntry.COUNTRY_X, COLUMN_HEADER_Y, CountryRankingEntry.COUNTRY_WIDTH, 24f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.metricHeaders = new Label[CountryRankingEntry.MAX_METRICS];
        for (int i = 0; i < this.metricHeaders.length; i++) {
            this.metricHeaders[i] = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_DARK, TABLE_X + CountryRankingEntry.METRIC_START_X, COLUMN_HEADER_Y,
                CountryRankingEntry.METRIC_AREA_WIDTH, 24f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        }
        this.table = ui.list(this, TABLE_X, TABLE_Y, TABLE_WIDTH, TABLE_HEIGHT, UiAnchor.UPPER_LEFT, 0f, 0f, 0f,
            rowUi -> new CountryRankingEntry(rowUi, uiCatalog, textFormat));
        this.table.setScrollStep(CountryRankingEntry.HEIGHT);

        ui.onClick(this.closeButton, listener::onCloseCountryRanking);
        for (int tab = 0; tab < this.tabButtons.length; tab++) {
            int firstPage = LedgerPages.firstPage(tab);
            ui.onClick(this.tabButtons[tab], () -> this.showPage(firstPage, listener));
        }
        ui.onClick(this.previousButton, () -> this.showPage(this.page - 1, listener));
        ui.onClick(this.nextButton, () -> this.showPage(this.page + 1, listener));
        this.refreshPage();
    }

    public void resetPage() {
        this.page = 0;
        this.refreshPage();
    }

    public CountrySort sort() {
        return LedgerPages.PAGES[this.page].sort();
    }

    /** Binds the rankings sorted for the current page; null clears the table until they arrive. */
    public void setData(CountryRankings rankings) {
        if (rankings == null) {
            this.table.setRowCount(0);
            return;
        }
        LedgerPage current = LedgerPages.PAGES[this.page];
        int count = rankings.count();
        this.table.setRowCount(count);
        for (int position = 0; position < count; position++) {
            this.table.row(position).bind(current, rankings, position, this.playerCountry);
        }
    }

    private void showPage(int page, CountryRankingListener listener) {
        if (page < 0 || page >= LedgerPages.PAGES.length || page == this.page) {
            return;
        }
        this.page = page;
        this.refreshPage();
        listener.onCountryRankingPageChanged();
    }

    private void refreshPage() {
        LedgerPage current = LedgerPages.PAGES[this.page];
        this.header.setText(this.ui.localise(current.titleKey()));
        this.text.clear();
        this.text.append(this.page + 1).append('/').append(LedgerPages.PAGES.length);
        this.pageNumber.setText(this.text);
        setEnabled(this.previousButton, this.page > 0);
        setEnabled(this.nextButton, this.page + 1 < LedgerPages.PAGES.length);
        for (int tab = 0; tab < this.tabButtons.length; tab++) {
            this.tabButtons[tab].setStyle(tab == current.tab() ? this.tabActiveStyle : this.tabNormalStyle);
        }
        this.configureMetricHeaders(current);
        this.table.setScrollY(0f);
        this.table.updateVisualScroll();
    }

    private void configureMetricHeaders(LedgerPage current) {
        int metricCount = current.columns().size();
        float width = CountryRankingEntry.METRIC_AREA_WIDTH / metricCount;
        for (int i = 0; i < this.metricHeaders.length; i++) {
            Label metricHeader = this.metricHeaders[i];
            boolean visible = i < metricCount;
            metricHeader.setVisible(visible);
            if (visible) {
                this.ui.placeLabel(this, metricHeader, TABLE_X + CountryRankingEntry.METRIC_START_X + i * width, COLUMN_HEADER_Y,
                    width, 24f, UiAnchor.UPPER_LEFT, 0f, 4f);
                metricHeader.setText(this.ui.localise(current.columns().get(i).headerKey()));
                LabelUtils.truncateLabel(metricHeader, width);
            }
        }
    }

    private static void setEnabled(Button button, boolean enabled) {
        button.setDisabled(!enabled);
        button.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        button.getColor().a = enabled ? 1f : 0.35f;
    }
}
