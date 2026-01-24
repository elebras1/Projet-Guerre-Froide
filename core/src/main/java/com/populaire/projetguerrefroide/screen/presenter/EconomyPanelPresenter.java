package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.screen.GameFlowHandler;
import com.populaire.projetguerrefroide.screen.listener.EconomyPanelListener;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.EconomyPanel;
import com.populaire.projetguerrefroide.ui.view.SortType;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class EconomyPanelPresenter implements Presenter, EconomyPanelListener {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final GameFlowHandler gameFlowHandler;
    private final WidgetFactory widgetFactory;
    private final Skin skinEconomy;
    private final Skin skinUi;
    private final Skin skinScrollbars;
    private EconomyPanel economyPanel;

    public EconomyPanelPresenter(GameContext gameContext, WorldService worldService, GameFlowHandler gameFlowHandler, WidgetFactory widgetFactory, Skin skinEconomy, Skin skinUi, Skin skinScrollbars) {
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.gameFlowHandler = gameFlowHandler;
        this.widgetFactory = widgetFactory;
        this.skinEconomy = skinEconomy;
        this.skinUi = skinUi;
        this.skinScrollbars = skinScrollbars;
    }

    @Override
    public void initialize(Stage stage) {
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        this.economyPanel = new EconomyPanel(this.widgetFactory, this.skinEconomy, this.skinUi, this.skinScrollbars, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this);
        Stack stack = new Stack();
        stack.add(this.economyPanel);
        this.economyPanel.setVisible(false);
        centerTable.add(stack).center();
        stage.addActor(centerTable);
    }

    @Override
    public void onCloseEconomyPanelClicked() {
        this.economyPanel.setTouchable(Touchable.disabled);
        this.economyPanel.setVisible(false);
    }

    @Override
    public void onSortRegions(SortType sortType) {
        RegionsBuildingsDto regionsBuildingsDto = this.worldService.prepareRegionsBuildingsDto(sortType);
        this.economyPanel.setData(regionsBuildingsDto);
    }

    @Override
    public void refresh() {
        this.economyPanel.setTouchable(Touchable.enabled);
        this.economyPanel.setVisible(true);
        RegionsBuildingsDto regionsBuildingsDto = this.worldService.prepareRegionsBuildingsDto(SortType.DEFAULT);
        this.economyPanel.setData(regionsBuildingsDto);
    }

    @Override
    public void dispose() {

    }
}
