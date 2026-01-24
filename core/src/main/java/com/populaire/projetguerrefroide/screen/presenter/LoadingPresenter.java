package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.service.GameContext;

import java.util.Random;

public class LoadingPresenter implements Presenter {
    private final GameContext gameContext;

    public LoadingPresenter(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    @Override
    public void initialize(Stage stage) {
        AssetManager assetManager = gameContext.getAssetManager();
        assetManager.finishLoading();
        Skin skin = assetManager.get("loadingscreens/loadingscreens_skin.json", Skin.class);
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        Random random = new Random();
        Drawable background = skin.getDrawable("load_" + random.nextInt(1, 12));
        rootTable.setBackground(background);
        stage.addActor(rootTable);
    }

    @Override
    public void refresh() {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void dispose() {
    }
}
