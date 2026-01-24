package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tommyettinger.ds.IntObjectMap;
import com.populaire.projetguerrefroide.screen.GameFlowHandler;
import com.populaire.projetguerrefroide.screen.listener.MinimapListener;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.Minimap;

public class MiniMapPresenter implements Presenter, MinimapListener {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final GameFlowHandler gameFlowHandler;
    private final Skin skinMinimap;
    private final Skin skinUi;

    public MiniMapPresenter(GameContext gameContext, WorldService worldService, GameFlowHandler gameFlowHandler, Skin skinMinimap, Skin skinUi) {
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.gameFlowHandler = gameFlowHandler;
        this.skinMinimap = skinMinimap;
        this.skinUi = skinUi;
    }

    @Override
    public void initialize(Stage stage) {
        Minimap minimap = new Minimap(this.skinMinimap, this.skinUi, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this);
        minimap.setPosition(Gdx.graphics.getWidth() - minimap.getWidth(), 0);
        stage.addActor(minimap);
    }

    @Override
    public void refresh() {

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
        this.worldService.changeMapMode(mapMode);
    }

    @Override
    public IntObjectMap<String> getInformationsMapMode(String mapMode) {
        return null;
    }

    @Override
    public void updateHoverTooltip(String content) {
        this.gameFlowHandler.showTooltip(content);
    }

    @Override
    public void hideHoverBox() {
    }

    @Override
    public void dispose() {

    }
}
