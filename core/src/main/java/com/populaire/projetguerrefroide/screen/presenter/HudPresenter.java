package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.populaire.projetguerrefroide.protocol.command.ChoosePlayerCountryCommand;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.response.CountrySummary;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.screen.LobbySetup;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;
import com.populaire.projetguerrefroide.ui.view.lobby.Lobby;
import com.populaire.projetguerrefroide.ui.view.lobby.LobbyListener;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;

public class HudPresenter extends RequestingPresenter implements LobbyListener {
    private final UiContext uiContext;
    private final ScreenManager screenManager;
    private final TextFormat textFormat;
    private final LobbySetup lobbySetup;
    private Lobby lobby;

    public HudPresenter(GameSession session, UiContext context, ScreenManager screenManager, TextFormat textFormat, LobbySetup lobbySetup) {
        super(session);
        this.uiContext = context;
        this.screenManager = screenManager;
        this.textFormat = textFormat;
        this.lobbySetup = lobbySetup;
    }

    @Override
    public void initialize(Stage stage) {
        UiBuilder ui = new UiBuilder(this.uiContext);
        this.lobby = new Lobby(ui, this, this.session.uiCatalog(), this.textFormat, this.uiContext.getBookmark(), this.lobbySetup.multiplayer());
        stage.addActor(this.lobby);
        if (this.lobbySetup.multiplayer()) {
            this.lobby.setHost(this.lobbySetup.playerName());
            this.lobby.addChatLine(this.lobbySetup.gameName());
            this.lobby.addChatLine(ui.localise("FE_NETWORK_UNAVAILABLE"));
        } else {
            this.lobby.addChatLine(ui.localise("INTRODUCTION"));
        }
        this.watch(RequestKind.COUNTRY_SUMMARY, new CountrySummary(), new CountrySummary(),
            () -> this.selectedCountry() >= 0, params -> params.index(this.selectedCountry()), RequestWatch.Staleness.EVERY_REVISION, this::applySummary);
    }

    private int selectedCountry() {
        return this.session.mapView().ownerOf(this.session.uiState().selectedProvince());
    }

    private void applySummary(CountrySummary summary) {
        if (summary.found()) {
            this.lobby.showCountry(summary);
        }
    }

    @Override
    protected void afterRequests(float delta) {
        if (this.selectedCountry() < 0 && this.lobby.isCountryShown()) {
            this.lobby.hideCountry();
        }
    }

    @Override
    public void onPlayClicked() {
        int countryIndex = this.selectedCountry();
        if (countryIndex < 0) {
            return;
        }
        this.session.uiState().playerCountry(countryIndex);
        this.session.channel().send(new ChoosePlayerCountryCommand(this.session.uiCatalog().catalog().countries().entityId(countryIndex)));
        this.screenManager.showGameScreen();
    }

    @Override
    public void onBackClicked() {
        this.screenManager.showMainMenuScreen();
    }

    @Override
    public void onChatMessage(String message) {
        this.lobby.addChatLine(this.lobbySetup.playerName() + " : " + message);
    }

    @Override
    public void dispose() {

    }
}
