package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.view.Debug;

public class DebugPresenter implements Presenter {
    private final UiContext uiContext;
    private final GameSession session;
    private Debug debug;

    public DebugPresenter(UiContext uiContext, GameSession session) {
        this.uiContext = uiContext;
        this.session = session;
    }

    @Override
    public void initialize(Stage stage) {
        this.debug = new Debug(this.session.uiCatalog().catalog().provinces().size());
        this.debug.setPosition(100, 90);
        this.debug.setVisible(this.uiContext.getSettings().isDebugMode());
        stage.addActor(this.debug);
    }

    @Override
    public void update(float delta) {
        this.debug.update(Gdx.graphics.getDeltaTime() * 1000);
    }

    @Override
    public void dispose() {

    }
}
