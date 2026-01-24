package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.dto.CountrySummaryDto;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.screen.listener.LobbyBoxListener;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.*;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class HudPresenter implements Presenter, LobbyBoxListener {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final ScreenManager screenManager;
    private final WidgetFactory widgetFactory;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Skin skinScrollbars;
    private final Skin skinPortraits;
    private CountrySummaryPanel countrySummaryPanel;
    private LobbyBox lobbyBox;

    public HudPresenter(GameContext context, WorldService worldService, ScreenManager screenManager, WidgetFactory factory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinScrollbars, Skin skinPortraits) {
        this.gameContext = context;
        this.worldService = worldService;
        this.screenManager = screenManager;
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

        ScenarioSavegameSelector scenarioSelector = new ScenarioSavegameSelector(skin, gameContext.getLabelStylePool(), gameContext.getBookmark(), gameContext.getLocalisation());
        TitleBar titleBar = new TitleBar(widgetFactory, skin, gameContext.getLabelStylePool(), gameContext.getLocalisation());
        this.countrySummaryPanel = new CountrySummaryPanel(widgetFactory, skin, skinUi, skinFlags, skinPortraits, gameContext.getLabelStylePool(), gameContext.getLocalisation());

        topTable.add(scenarioSelector).align(Align.topLeft).expandX();
        topTable.add(titleBar).align(Align.top);
        topTable.add(countrySummaryPanel).align(Align.topRight).expandX();
        topTable.pad(5);

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom();

        this.lobbyBox = new LobbyBox(widgetFactory, skin, skinScrollbars, gameContext.getLabelStylePool(), gameContext.getLocalisation(), this);

        bottomTable.add(lobbyBox).align(Align.bottom);
        bottomTable.pad(5);

        stage.addActor(topTable);
        stage.addActor(bottomTable);
    }

    public void updateCountrySelection(boolean isSelected, CountrySummaryDto summary) {
        if (isSelected && summary != null) {
            this.countrySummaryPanel.update(summary, gameContext.getLocalisation());
            this.countrySummaryPanel.setVisible(true);
        } else {
            this.countrySummaryPanel.hide();
        }
    }

    @Override
    public void onPlayClicked() {
        if(this.worldService.setCountryPlayer()) {
            this.screenManager.showGameScreen(this.worldService);
        }
    }

    @Override
    public void onBackClicked() {
        this.worldService.dispose();
        this.screenManager.showMainMenuScreen();
    }

    @Override public void dispose() {}
}
