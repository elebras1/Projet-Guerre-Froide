package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.screen.GameFlowHandler;
import com.populaire.projetguerrefroide.screen.listener.TimeListener;
import com.populaire.projetguerrefroide.screen.listener.TopBarListener;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.TimeService;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.TopBar;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.DateUtils;

import java.time.LocalDate;

public class TopBarPresenter implements Presenter, TopBarListener, TimeListener {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final TimeService timeService;
    private final GameFlowHandler gameFlowHandler;
    private final WidgetFactory widgetFactory;
    private final Skin skinTopBar;
    private final Skin skinUi;
    private final Skin skinFlags;
    private TopBar topBar;

    public TopBarPresenter(GameContext gameContext, WorldService worldService, TimeService timeService, GameFlowHandler gameFlowHandler, WidgetFactory widgetFactory, Skin skinTopBar, Skin skinUi, Skin skinFlags) {
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.timeService = timeService;
        this.gameFlowHandler = gameFlowHandler;
        this.widgetFactory = widgetFactory;
        this.skinTopBar = skinTopBar;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
    }

    @Override
    public void initialize(Stage stage) {
        this.topBar = new TopBar(this.widgetFactory, this.skinTopBar, this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this.worldService.getCountryPlayerNameId(), this.worldService.getColonizerNameIdOfSelectedProvince(), this);
        this.topBar.setPosition(0, Gdx.graphics.getHeight() - this.topBar.getHeight());
        this.topBar.setCountryData(this.worldService.buildCountryDetails());
        stage.addActor(this.topBar);
    }

    @Override
    public int onSpeedUp() {
        return this.timeService.upSpeed();
    }

    @Override
    public int onSpeedDown() {
        return this.timeService.downSpeed();
    }

    @Override
    public int onTogglePause() {
        return this.timeService.togglePause();
    }

    @Override
    public void onEconomyClicked() {
        this.gameFlowHandler.toggleEconomyPanel();
    }

    @Override
    public void onNewDay(LocalDate date) {
        this.topBar.setDate(DateUtils.formatDate(date, this.gameContext.getLocalisation(), this.gameContext.getSettings().getLanguage()));
    }

    @Override
    public void dispose() {

    }
}
