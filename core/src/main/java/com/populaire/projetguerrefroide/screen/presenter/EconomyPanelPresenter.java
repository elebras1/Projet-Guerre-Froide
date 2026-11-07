package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.populaire.projetguerrefroide.protocol.command.DemolishBuildingCommand;
import com.populaire.projetguerrefroide.protocol.command.ExpandBuildingCommand;
import com.populaire.projetguerrefroide.protocol.command.ResumeBuildingCommand;
import com.populaire.projetguerrefroide.protocol.command.SuspendBuildingCommand;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.request.SortType;
import com.populaire.projetguerrefroide.protocol.response.BuildingDetails;
import com.populaire.projetguerrefroide.protocol.response.RegionsBuildings;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.view.EconomyPanelListener;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.UiState;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;
import com.populaire.projetguerrefroide.ui.view.EconomyPanel;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class EconomyPanelPresenter extends RequestingPresenter implements EconomyPanelListener {
    private final UiContext uiContext;
    private final WidgetFactory widgetFactory;
    private final Skin skinEconomy;
    private final Skin skinUi;
    private final Skin skinScrollbars;
    private final TextFormat textFormat;
    private EconomyPanel economyPanel;
    private RequestWatch<RegionsBuildings> regionsWatch;
    private boolean opening;

    public EconomyPanelPresenter(GameSession session, UiContext uiContext, WidgetFactory widgetFactory, Skin skinEconomy, Skin skinUi, Skin skinScrollbars, TextFormat textFormat) {
        super(session);
        this.uiContext = uiContext;
        this.widgetFactory = widgetFactory;
        this.skinEconomy = skinEconomy;
        this.skinUi = skinUi;
        this.skinScrollbars = skinScrollbars;
        this.textFormat = textFormat;
    }

    @Override
    public void initialize(Stage stage) {
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        this.economyPanel = new EconomyPanel(this.widgetFactory, this.skinEconomy, this.skinUi, this.skinScrollbars, this.uiContext.getSkinFonts(), this.uiContext.getColorTexturePool(), this.uiContext.getLocalisation(), this.session.uiCatalog(), this.textFormat, this);
        this.economyPanel.setVisible(false);
        centerTable.add(this.economyPanel).center();
        stage.addActor(centerTable);
        UiState uiState = this.session.uiState();
        this.regionsWatch = this.watch(RequestKind.REGIONS_BUILDINGS, new RegionsBuildings(), new RegionsBuildings(),
            uiState::economyPanelOpen, params -> params.option(uiState.regionSort().ordinal()), RequestWatch.Staleness.EVERY_REVISION, this::applyRegions);
        this.watch(RequestKind.BUILDING_DETAILS, new BuildingDetails(), new BuildingDetails(),
            () -> uiState.economyPanelOpen() && uiState.selectedBuilding() != 0, params -> params.id(uiState.selectedBuilding()), RequestWatch.Staleness.EVERY_REVISION, this::applyBuilding);
    }

    public void open() {
        UiState uiState = this.session.uiState();
        uiState.regionSort(SortType.DEFAULT);
        uiState.economyPanelOpen(true);
        this.opening = true;
        this.regionsWatch.invalidate();
    }

    private void applyRegions(RegionsBuildings regions) {
        if (!regions.found()) {
            return;
        }
        this.economyPanel.setData(regions);
        if (this.opening) {
            this.opening = false;
            this.economyPanel.setTouchable(Touchable.enabled);
            this.economyPanel.setVisible(true);
        }
    }

    private void applyBuilding(BuildingDetails building) {
        if (!building.found()) {
            this.session.uiState().selectBuilding(0);
            this.economyPanel.clearSelectedBuildingInfoBlock();
            return;
        }
        this.economyPanel.updateSelectedBuildingInfoBlock(building);
    }

    @Override
    public void onCloseEconomyPanelClicked() {
        this.session.uiState().economyPanelOpen(false);
        this.opening = false;
        this.economyPanel.setTouchable(Touchable.disabled);
        this.economyPanel.setVisible(false);
    }

    @Override
    public void onSortRegions(SortType sortType) {
        this.session.uiState().regionSort(sortType);
    }

    @Override
    public void onBuildingClicked(long buildingId) {
        this.session.uiState().selectBuilding(buildingId);
    }

    @Override
    public void onExpandBuildingClicked(long buildingId) {
        this.session.channel().send(new ExpandBuildingCommand(buildingId));
    }

    @Override
    public void onSuspendBuildingClicked(long buildingId) {
        this.session.channel().send(new SuspendBuildingCommand(buildingId));
    }

    @Override
    public void onResumeBuildingClicked(long buildingId) {
        this.session.channel().send(new ResumeBuildingCommand(buildingId));
    }

    @Override
    public void onDemolishBuildingClicked(long buildingId) {
        this.session.channel().send(new DemolishBuildingCommand(buildingId));
    }

    @Override
    public void dispose() {
    }
}
