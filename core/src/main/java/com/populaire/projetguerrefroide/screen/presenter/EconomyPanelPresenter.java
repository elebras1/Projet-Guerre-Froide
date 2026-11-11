package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.populaire.projetguerrefroide.protocol.command.DemolishBuildingCommand;
import com.populaire.projetguerrefroide.protocol.command.ExpandBuildingCommand;
import com.populaire.projetguerrefroide.protocol.command.ResumeBuildingCommand;
import com.populaire.projetguerrefroide.protocol.command.SuspendBuildingCommand;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.request.SortType;
import com.populaire.projetguerrefroide.protocol.response.BuildingDetails;
import com.populaire.projetguerrefroide.protocol.response.RegionsBuildings;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.UiState;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;
import com.populaire.projetguerrefroide.ui.view.economy.EconomyPanel;
import com.populaire.projetguerrefroide.ui.view.economy.EconomyPanelListener;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class EconomyPanelPresenter extends RequestingPresenter implements EconomyPanelListener {
    private final UiContext uiContext;
    private final TextFormat textFormat;
    private EconomyPanel economyPanel;
    private RequestWatch<RegionsBuildings> regionsWatch;
    private boolean opening;

    public EconomyPanelPresenter(GameSession session, UiContext uiContext, TextFormat textFormat) {
        super(session);
        this.uiContext = uiContext;
        this.textFormat = textFormat;
    }

    @Override
    public void initialize(Stage stage) {
        UiBuilder ui = new UiBuilder(this.uiContext);
        UiWindow layer = new UiWindow(ui);
        layer.setSize(stage.getWidth(), stage.getHeight());
        this.economyPanel = ui.add(layer, new EconomyPanel(ui, this.session.uiCatalog(), this.textFormat, this), -555f, -340f, UiAnchor.CENTER);
        this.economyPanel.setVisible(false);
        stage.addActor(layer);
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

    public void close() {
        this.session.uiState().economyPanelOpen(false);
        this.opening = false;
        this.economyPanel.setTouchable(Touchable.disabled);
        this.economyPanel.setVisible(false);
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
        this.close();
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
