package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.response.CountryRankings;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.UiState;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;
import com.populaire.projetguerrefroide.ui.view.ledger.CountryRankingListener;
import com.populaire.projetguerrefroide.ui.view.ledger.CountryRankingPanel;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class CountryRankingPresenter extends RequestingPresenter implements CountryRankingListener {
    private final UiContext uiContext;
    private final TextFormat textFormat;
    private CountryRankingPanel countryRankingPanel;
    private RequestWatch<CountryRankings> rankingsWatch;
    private boolean opening;

    public CountryRankingPresenter(GameSession session, UiContext uiContext, TextFormat textFormat) {
        super(session);
        this.uiContext = uiContext;
        this.textFormat = textFormat;
    }

    @Override
    public void initialize(Stage stage) {
        UiBuilder ui = new UiBuilder(this.uiContext);
        UiWindow layer = new UiWindow(ui);
        layer.setSize(stage.getWidth(), stage.getHeight());
        this.countryRankingPanel = ui.add(layer, new CountryRankingPanel(ui, this.session.uiCatalog(), this.textFormat,
            this.session.uiState().playerCountry(), this), -555f, -340f, UiAnchor.CENTER);
        this.countryRankingPanel.setVisible(false);
        stage.addActor(layer);
        UiState uiState = this.session.uiState();
        this.rankingsWatch = this.watch(RequestKind.COUNTRY_RANKINGS, new CountryRankings(), new CountryRankings(),
            uiState::countryRankingOpen, params -> params.option(this.countryRankingPanel.sort().ordinal()), RequestWatch.Staleness.MONTHLY, this::applyRankings);
    }

    public void toggle() {
        if (this.session.uiState().countryRankingOpen()) {
            this.close();
        } else {
            this.open();
        }
    }

    public void open() {
        UiState uiState = this.session.uiState();
        this.countryRankingPanel.resetPage();
        uiState.countryRankingOpen(true);
        this.opening = true;
        this.rankingsWatch.invalidate();
    }

    public void close() {
        UiState uiState = this.session.uiState();
        uiState.countryRankingOpen(false);
        this.opening = false;
        this.countryRankingPanel.setTouchable(Touchable.disabled);
        this.countryRankingPanel.setVisible(false);
    }

    private void applyRankings(CountryRankings rankings) {
        this.countryRankingPanel.setData(rankings);
        if (this.opening) {
            this.opening = false;
            this.countryRankingPanel.setTouchable(Touchable.enabled);
            this.countryRankingPanel.setVisible(true);
        }
    }

    @Override
    public void onCloseCountryRanking() {
        this.close();
    }

    @Override
    public void onCountryRankingPageChanged() {
        // same sort: rebind the shown response at once; other sort: the watch requests it, rows wait for it
        this.countryRankingPanel.setData(this.rankingsWatch.hasCurrentResponse() ? this.rankingsWatch.response() : null);
    }

    @Override
    public void dispose() {
    }
}
