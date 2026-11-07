package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.populaire.projetguerrefroide.protocol.request.MapMode;
import com.populaire.projetguerrefroide.screen.GameFlowHandler;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.view.menubar.MenuBar;
import com.populaire.projetguerrefroide.ui.view.menubar.MenuBarListener;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;

public class MiniMapPresenter implements Presenter, MenuBarListener {
    private final UiContext uiContext;
    private final GameSession session;
    private final GameFlowHandler gameFlowHandler;
    private MenuBar menuBar;

    public MiniMapPresenter(GameSession session, UiContext uiContext, GameFlowHandler gameFlowHandler) {
        this.uiContext = uiContext;
        this.session = session;
        this.gameFlowHandler = gameFlowHandler;
    }

    @Override
    public void initialize(Stage stage) {
        UiBuilder ui = new UiBuilder(this.uiContext);
        this.menuBar = new MenuBar(ui, this);
        stage.addActor(this.menuBar);
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void moveCamera(int x, int y) {
        this.gameFlowHandler.moveCameraTo(x, y);
    }

    @Override
    public void zoomIn() {
        this.gameFlowHandler.zoomIn();
    }

    @Override
    public void zoomOut() {
        this.gameFlowHandler.zoomOut();
    }

    @Override
    public void changeMapMode(String mapMode) {
        MapMode mode = switch (mapMode) {
            case "mapmode_political" -> MapMode.POLITICAL;
            case "mapmode_strength" -> MapMode.IDEOLOGICAL;
            case "mapmode_diplomatic" -> MapMode.CULTURAL;
            case "mapmode_intel" -> MapMode.RELIGIOUS;
            case "mapmode_terrain" -> MapMode.TERRAIN;
            case "mapmode_terrain_2" -> MapMode.TERRAIN_2;
            case "mapmode_resources" -> MapMode.RESOURCES;
            case "mapmode_region" -> MapMode.REGION;
            case "mapmode_theatre" -> MapMode.RELATIONS;
            case "mapmode_weather" -> MapMode.POPULATION;
            default -> null;
        };
        if (mode != null) {
            this.session.uiState().mapMode(mode);
        }
    }

    @Override
    public void updateHoverTooltip(String content) {
        this.gameFlowHandler.showTooltip(content);
    }

    @Override
    public void hideUiTooltip() {
        this.gameFlowHandler.hideTooltip();
    }

    @Override
    public void dispose() {

    }
}
