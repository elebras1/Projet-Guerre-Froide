package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.screen.listener.MainMenuListener;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.ui.view.MainMenu;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class MainMenuPresenter implements Presenter, MainMenuListener {
    private final ScreenManager screenManager;
    private final GameContext gameContext;

    public MainMenuPresenter(ScreenManager screenManager, GameContext gameContext) {
        this.screenManager = screenManager;
        this.gameContext = gameContext;
    }

    @Override
    public void initialize(Stage stage) {
        AssetManager assetManager = gameContext.getAssetManager();
        Skin skin = assetManager.get("ui/mainmenu/mainmenu_skin.json");
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(skin.getDrawable("frontend_main_bg"));
        WidgetFactory widgetFactory = new WidgetFactory();
        MainMenu menu = new MainMenu(widgetFactory, skin, gameContext.getLabelStylePool(), gameContext.getLocalisation(), this);
        rootTable.add(menu).center().padLeft(menu.getWidth() / 3);
        stage.addActor(rootTable);
    }

    @Override
    public void refresh() {

    }

    @Override
    public void onSinglePlayerClicked() {
        this.screenManager.showLoadScreen();
    }

    @Override
    public void onMultiplayerClicked() {

    }

    @Override
    public void onExitClicked() {
        Gdx.app.exit();
    }

    @Override
    public void dispose() {

    }
}
