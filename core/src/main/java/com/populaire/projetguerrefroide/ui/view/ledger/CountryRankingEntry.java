package com.populaire.projetguerrefroide.ui.view.ledger;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.CharArray;
import com.populaire.projetguerrefroide.protocol.response.CountryRankings;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.LabelUtils;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class CountryRankingEntry extends UiWindow {
    // ledger_columns stripes are 30 px high
    static final float HEIGHT = 30f;
    static final int MAX_METRICS = 4;
    static final float COUNTRY_X = 96f;
    static final float COUNTRY_WIDTH = 500f;
    static final float METRIC_START_X = 620f;
    static final float METRIC_AREA_WIDTH = 360f;
    private static final float TEXT_Y = 3f;
    private final UiCatalog uiCatalog;
    private final TextFormat textFormat;
    private final CharArray text;
    private final Label.LabelStyle normalStyle;
    private final Label.LabelStyle playerStyle;
    private final FlagImage flag;
    private final Label rank;
    private final Label country;
    private final Label[] metrics;
    private LedgerPage page;

    CountryRankingEntry(UiBuilder ui, UiCatalog uiCatalog, TextFormat textFormat) {
        super(ui, 980f, HEIGHT);
        this.uiCatalog = uiCatalog;
        this.textFormat = textFormat;
        this.text = new CharArray(32);
        this.normalStyle = ui.font(FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE);
        this.playerStyle = ui.font(FontsSkin.Styles.Label.JOCKEY_16_YELLOW);
        Skin uiSkin = ui.skin(Skins.UI);
        this.rank = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 6f, TEXT_Y, 48f, 24f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.flag = ui.flag(this, uiSkin, UiSkin.Drawables.SMALL_FLAG_MASK, UiSkin.Drawables.SMALL_FLAG_OVERLAY, 64f, 7f, UiAnchor.UPPER_LEFT);
        this.country = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, COUNTRY_X, TEXT_Y, COUNTRY_WIDTH, 24f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
        this.metrics = new Label[MAX_METRICS];
        for (int i = 0; i < MAX_METRICS; i++) {
            this.metrics[i] = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE,
                METRIC_START_X, TEXT_Y, METRIC_AREA_WIDTH, 24f, Align.topRight, UiAnchor.UPPER_LEFT, 0f, 4f);
            this.metrics[i].setVisible(false);
        }
    }

    void bind(LedgerPage page, CountryRankings rankings, int position, int playerCountry) {
        if (this.page != page) {
            this.configure(page);
        }
        int countryIndex = rankings.countryIndex(position);
        boolean player = countryIndex == playerCountry;

        this.text.clear();
        this.rank.setText(this.text.append(position + 1));
        this.country.setText(this.uiCatalog.countryName(countryIndex));
        LabelUtils.truncateLabel(this.country, COUNTRY_WIDTH);
        this.flag.setFlag(this.uiCatalog.flag(countryIndex));
        for (int i = 0; i < page.columns().size(); i++) {
            page.columns().get(i).metric().append(this.text, rankings, position, this.textFormat, this.uiCatalog);
            this.metrics[i].setText(this.text);
            LabelUtils.truncateLabel(this.metrics[i], this.metrics[i].getWidth());
        }

        Label.LabelStyle style = player ? this.playerStyle : this.normalStyle;
        this.rank.setStyle(style);
        this.country.setStyle(style);
        for (int i = 0; i < page.columns().size(); i++) {
            this.metrics[i].setStyle(style);
        }
    }

    private void configure(LedgerPage page) {
        this.page = page;
        int metricCount = page.columns().size();
        float width = METRIC_AREA_WIDTH / metricCount;
        for (int i = 0; i < MAX_METRICS; i++) {
            Label metric = this.metrics[i];
            boolean visible = i < metricCount;
            metric.setVisible(visible);
            if (visible) {
                this.ui.placeLabel(this, metric, METRIC_START_X + i * width, TEXT_Y, width, 24f, UiAnchor.UPPER_LEFT, 0f, 4f);
            }
        }
    }
}
