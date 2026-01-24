package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.populaire.projetguerrefroide.adapter.graphics.WgCustomStage;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.adapter.graphics.WgScreenViewport;
import com.populaire.projetguerrefroide.screen.input.GameInputHandler;
import com.populaire.projetguerrefroide.screen.listener.GameInputListener;
import com.populaire.projetguerrefroide.screen.presenter.HudPresenter;
import com.populaire.projetguerrefroide.screen.presenter.MainMenuInGamePresenter;
import com.populaire.projetguerrefroide.screen.presenter.TooltipPresenter;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.Debug;
import com.populaire.projetguerrefroide.ui.view.MainMenuInGame;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class NewGameScreen implements Screen, GameInputListener, GameFlowHandler {
    private final ScreenManager screenManager;
    private final GameContext gameContext;
    private final WorldService worldService;
    private final ConfigurationService configurationService;
    private final OrthographicCamera cam;
    private final WgProjection projection;
    private final Stage stage;
    private final InputMultiplexer multiplexer;
    private final GameInputHandler inputHandler;
    private final MainMenuInGamePresenter mainMenuInGamePresenter;
    private final HudPresenter hudPresenter;
    private final TooltipPresenter tooltipPresenter;
    private final Debug debug;
    private float time;
    private boolean paused;

    public NewGameScreen(ScreenManager screenManager, GameContext gameContext, WorldService worldService, ConfigurationService configurationService) {
        this.screenManager = screenManager;
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.configurationService = configurationService;
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.cam.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 1.4f, 0);
        this.projection = new WgProjection();

        AssetManager assetManager = gameContext.getAssetManager();
        this.configurationService.loadNewGameLocalisation(this.gameContext);
        Skin skinUi = assetManager.get("ui/ui_skin.json");
        Skin skinFlags = assetManager.get("flags/flags_skin.json");
        this.stage = new WgCustomStage(new WgScreenViewport(), skinUi, skinFlags);
        WidgetFactory widgetFactory = new WidgetFactory();
        this.hudPresenter = new HudPresenter(gameContext, worldService, screenManager, widgetFactory, assetManager.get("ui/newgame/newgame_skin.json"), skinUi, skinFlags, assetManager.get("ui/scrollbars/scrollbars_skin.json"), assetManager.get("portraits/portraits_skin.json"));
        this.mainMenuInGamePresenter = new MainMenuInGamePresenter(gameContext, configurationService, this, widgetFactory, assetManager.get("ui/mainmenu_ig/mainmenu_ig_skin.json"), skinUi, assetManager.get("ui/scrollbars/scrollbars_skin.json"), assetManager.get("ui/popup/popup_skin.json"), skinFlags);
        this.tooltipPresenter = new TooltipPresenter(gameContext, this.worldService, widgetFactory, skinUi, skinFlags);
        this.hudPresenter.initialize(this.stage);
        this.tooltipPresenter.initialize(this.stage);
        this.mainMenuInGamePresenter.initialize(this.stage);
        this.inputHandler = new GameInputHandler(this.cam, this);
        this.multiplexer = new InputMultiplexer();
        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);

        this.debug = new Debug(this.worldService.getNumberOfProvinces());
        this.debug.setPosition(100, 90);
        this.debug.setVisible(this.gameContext.getSettings().isDebugMode());
        this.stage.addActor(this.debug);

        this.paused = false;
    }

    @Override
    public void pause() {
        this.paused = true;
    }

    @Override
    public void resume() {
        this.paused = false;
    }

    @Override
    public void setInputEnabled(boolean enabled) {
        if (enabled) {
            this.multiplexer.addProcessor(this.inputHandler);
            this.setAllStageTouchable(Touchable.enabled);
        } else {
            this.multiplexer.removeProcessor(this.inputHandler);
            this.setAllStageTouchable(Touchable.disabled);
        }
    }

    @Override
    public void toggleEconomyPanel() {

    }

    @Override
    public void showTooltip(String content) {

    }

    @Override
    public void moveCameraTo(int x, int y) {

    }

    @Override
    public void zoomIn() {

    }

    @Override
    public void zoomOut() {

    }

    @Override
    public void onClick(int x, int y) {
        this.worldService.selectProvince(x, y);
        this.hudPresenter.refresh();
    }

    @Override
    public void onHover(int x, int y) {
        if(this.worldService.hoverLandProvince(x, y) && !this.isMouseOverUI()) {
            this.tooltipPresenter.update(x, y);
        } else {
            this.tooltipPresenter.hide();
        }
    }

    @Override
    public void onEscape() {
        if (!this.paused) {
            this.pause();
            this.mainMenuInGamePresenter.show();
        } else {
            this.mainMenuInGamePresenter.onCloseMainMenuInGameClicked();
        }
    }

    @Override
    public void render(float delta) {
        this.time += delta;
        this.cam.update();
        this.cam.position.x = (this.cam.position.x + WORLD_WIDTH) % WORLD_WIDTH;
        this.projection.setProjectionMatrix(this.cam.combined);

        WgScreenUtils.clear(1, 1, 1, 1);
        this.worldService.renderWorld(this.projection, this.cam, time);

        if(!this.paused) {
            this.inputHandler.setDelta(delta);
            this.inputHandler.handleInput();
            this.inputHandler.updateCamera();
        }

        this.debug.update(Gdx.graphics.getDeltaTime() * 1000);
        this.stage.act();
        this.stage.draw();
    }

    private void setAllStageTouchable(Touchable touchable) {
        Array<Actor> actors = this.stage.getActors();
        for (int i = 0; i < actors.size; i++) {
            Actor actor = actors.get(i);
            if (actor instanceof Table && this.isMenuContainer((Table)actor)) {
                continue;
            }
            actor.setTouchable(touchable);
        }
    }

    private boolean isMenuContainer(Table table) {
        for(Actor actor : table.getChildren()) {
            if(actor instanceof MainMenuInGame) {
                return true;
            }
        }
        return false;
    }

    private boolean isMouseOverUI() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return this.stage.hit(mouseX, mouseY, true) != null;
    }

    @Override public void resize(int width, int height) {
        this.cam.viewportWidth = width / 2f;
        this.cam.viewportHeight = height / 2f;
        this.cam.update();
        this.stage.getViewport().update(width, height, true);
        ((WgCustomStage) this.stage).updateRendererProjection();
    }

    @Override public void show() {
        gameContext.getCursorManager().defaultCursor();
    }

    @Override public void hide() {

    }

    @Override public void dispose() {
        this.stage.dispose();
    }
}
