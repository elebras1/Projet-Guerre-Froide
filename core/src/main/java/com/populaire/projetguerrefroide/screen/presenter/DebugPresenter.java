package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.Debug;

public class DebugPresenter implements Presenter {
    private final GameContext gameContext;
    private final WorldService worldService;
    private Debug debug;

    public DebugPresenter(GameContext gameContext, WorldService worldService) {
        this.gameContext = gameContext;
        this.worldService = worldService;
    }

    @Override
    public void initialize(Stage stage) {
        this.debug = new Debug(this.worldService.getNumberOfProvinces());
        this.debug.setPosition(100, 90);
        this.debug.setVisible(this.gameContext.getSettings().isDebugMode());
        stage.addActor(this.debug);
    }

    @Override
    public void refresh() {

    }

    @Override
    public void update(float delta) {
        this.debug.update(Gdx.graphics.getDeltaTime() * 1000);
    }

    @Override
    public void dispose() {

    }
}
