package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.CharArray;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.response.CountryOverview;
import com.populaire.projetguerrefroide.screen.GameFlowHandler;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.UiState;
import com.populaire.projetguerrefroide.ui.format.DateText;
import com.populaire.projetguerrefroide.ui.format.LocaleUtils;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;
import com.populaire.projetguerrefroide.ui.view.topbar.TopBar;
import com.populaire.projetguerrefroide.ui.view.topbar.TopBarListener;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;

public class TopBarPresenter extends RequestingPresenter implements TopBarListener {
    private final UiContext uiContext;
    private final GameFlowHandler gameFlowHandler;
    private final TextFormat textFormat;
    private final DateText dateText;
    private final CharArray dateChars;
    private TopBar topBar;
    private long shownEpochDay;

    public TopBarPresenter(GameSession session, UiContext uiContext, GameFlowHandler gameFlowHandler, TextFormat textFormat) {
        super(session);
        this.uiContext = uiContext;
        this.gameFlowHandler = gameFlowHandler;
        this.textFormat = textFormat;
        String language = uiContext.getSettings().getLanguage();
        this.dateText = new DateText(uiContext.getLocalisation().get("DATE_FORMAT"), LocaleUtils.getLocale(language));
        this.dateChars = new CharArray(32);
        this.shownEpochDay = Long.MIN_VALUE;
    }

    @Override
    public void initialize(Stage stage) {
        UiState uiState = this.session.uiState();
        UiBuilder ui = new UiBuilder(this.uiContext);
        this.topBar = new TopBar(ui, this.textFormat, this);
        this.topBar.setFlag(this.session.uiCatalog().flag(uiState.playerCountry()));
        this.topBar.setSpeedState(uiState.displaySpeed());
        stage.addActor(this.topBar);
        this.watch(RequestKind.COUNTRY_OVERVIEW, new CountryOverview(), new CountryOverview(),
            () -> true, params -> params.index(this.session.uiState().playerCountry()), RequestWatch.Staleness.EVERY_REVISION, this::applyCountry);
    }

    private void applyCountry(CountryOverview country) {
        if (country.found()) {
            this.topBar.setCountry(country);
        }
    }

    @Override
    protected void afterRequests(float delta) {
        SimulationHeader header = this.session.header();
        long epochDay = header.epochDay();
        if (epochDay != this.shownEpochDay) {
            this.shownEpochDay = epochDay;
            this.topBar.setDate(this.dateText.format(this.dateChars, epochDay));
        }
    }

    @Override
    public int onSpeedUp() {
        UiState uiState = this.session.uiState();
        uiState.requestedSpeed(Math.min(SimulationHeader.MAX_SPEED, uiState.requestedSpeed() + 1));
        this.session.channel().requestSpeed(uiState.requestedSpeed());
        return uiState.displaySpeed();
    }

    @Override
    public int onSpeedDown() {
        UiState uiState = this.session.uiState();
        uiState.requestedSpeed(Math.max(SimulationHeader.MIN_SPEED, uiState.requestedSpeed() - 1));
        this.session.channel().requestSpeed(uiState.requestedSpeed());
        return uiState.displaySpeed();
    }

    @Override
    public int onTogglePause() {
        UiState uiState = this.session.uiState();
        uiState.requestedPaused(!uiState.requestedPaused());
        this.session.channel().requestPaused(uiState.requestedPaused());
        return uiState.displaySpeed();
    }

    @Override
    public void onEconomyClicked() {
        this.gameFlowHandler.toggleEconomyPanel();
    }

    @Override
    public void onMenuClicked() {
        this.gameFlowHandler.openInGameMenu();
    }

    @Override
    public void dispose() {
    }
}
