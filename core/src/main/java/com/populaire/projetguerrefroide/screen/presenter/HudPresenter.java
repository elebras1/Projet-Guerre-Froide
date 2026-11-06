package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.protocol.command.ChoosePlayerCountryCommand;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.response.CountrySummary;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.screen.listener.LobbyBoxListener;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;
import com.populaire.projetguerrefroide.ui.view.*;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class HudPresenter extends RequestingPresenter implements LobbyBoxListener {
    private final UiContext uiContext;
    private final ScreenManager screenManager;
    private final TextFormat textFormat;
    private final WidgetFactory widgetFactory;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Skin skinScrollbars;
    private final Skin skinPortraits;
    private CountrySummaryPanel countrySummaryPanel;
    private LobbyBox lobbyBox;

    public HudPresenter(GameSession session, UiContext context, ScreenManager screenManager, WidgetFactory factory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinScrollbars, Skin skinPortraits, TextFormat textFormat) {
        super(session);
        this.uiContext = context;
        this.screenManager = screenManager;
        this.textFormat = textFormat;
        this.widgetFactory = factory;
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        this.skinScrollbars = skinScrollbars;
        this.skinPortraits = skinPortraits;
    }

    @Override
    public void initialize(Stage stage) {
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();

        ScenarioSavegameSelector scenarioSelector = new ScenarioSavegameSelector(skin, uiContext.getSkinFonts(), uiContext.getBookmark(), uiContext.getLocalisation());
        TitleBar titleBar = new TitleBar(widgetFactory, skin, uiContext.getSkinFonts(), uiContext.getLocalisation());
        this.countrySummaryPanel = new CountrySummaryPanel(widgetFactory, skin, skinUi, skinFlags, skinPortraits, uiContext.getSkinFonts(), uiContext.getLocalisation(), this.session.uiCatalog(), this.textFormat);

        topTable.add(scenarioSelector).align(Align.topLeft).expandX();
        topTable.add(titleBar).align(Align.top);
        topTable.add(countrySummaryPanel).align(Align.topRight).expandX();
        topTable.pad(5);

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom();

        this.lobbyBox = new LobbyBox(widgetFactory, skin, skinScrollbars, uiContext.getSkinFonts(), uiContext.getLocalisation(), this);

        bottomTable.add(lobbyBox).align(Align.bottom);
        bottomTable.pad(5);

        stage.addActor(topTable);
        stage.addActor(bottomTable);
        this.watch(RequestKind.COUNTRY_SUMMARY, new CountrySummary(), new CountrySummary(),
            () -> this.selectedCountry() >= 0, params -> params.index(this.selectedCountry()), RequestWatch.Staleness.EVERY_REVISION, this::applySummary);
    }

    private int selectedCountry() {
        return this.session.mapView().ownerOf(this.session.uiState().selectedProvince());
    }

    private void applySummary(CountrySummary summary) {
        if (summary.found()) {
            this.countrySummaryPanel.update(summary);
        }
    }

    @Override
    protected void afterRequests(float delta) {
        if (this.selectedCountry() < 0 && this.countrySummaryPanel.isVisible()) {
            this.countrySummaryPanel.hide();
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

    @Override public void dispose() {

    }
}
