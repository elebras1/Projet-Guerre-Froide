package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.ds.ObjectList;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.populaire.projetguerrefroide.adapter.graphics.WgCustomStage;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.adapter.graphics.WgScreenViewport;
import com.populaire.projetguerrefroide.command.CommandBus;
import com.populaire.projetguerrefroide.component.Position;
import com.populaire.projetguerrefroide.screen.input.GameInputHandler;
import com.populaire.projetguerrefroide.screen.listener.*;
import com.populaire.projetguerrefroide.screen.presenter.*;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.TimeService;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.*;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.time.LocalDate;
import java.util.List;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class GameScreen implements Screen, GameInputListener, TimeListener, GameFlowHandler {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final ConfigurationService configurationService;
    private final CommandBus commandBus;
    private final TimeService timeService;
    private final OrthographicCamera cam;
    private final WgProjection projection;
    private final InputMultiplexer multiplexer;
    private final GameInputHandler inputHandler;
    private final MainMenuInGamePresenter mainMenuInGamePresenter;
    private final TopBarPresenter topBarPresenter;
    private final ProvincePanelPresenter provincePanelPresenter;
    private final TooltipPresenter tooltipPresenter;
    private final EconomyPanelPresenter economyPanelPresenter;
    private final MiniMapPresenter minimapPresenter;
    private final List<Presenter> presenters;
    private final Stage stage;
    private float time;
    private boolean paused;

    public GameScreen(ScreenManager screenManager, GameContext gameContext, WorldService worldService, TimeService timeService, ConfigurationService configurationService, CommandBus commandBus) {
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.configurationService = configurationService;
        this.timeService = timeService;
        this.commandBus = commandBus;
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.projection = new WgProjection();
        Position capitalPosition = this.worldService.getCapitalPositionOfSelectedCountry();
        this.cam.position.set(capitalPosition.x(), capitalPosition.y(), 0);
        this.cam.update();
        this.multiplexer = new InputMultiplexer();
        this.inputHandler = new GameInputHandler(this.cam, this);
        this.configurationService.loadGameLocalisation(this.gameContext);
        this.presenters = new ObjectList<>();

        AssetManager assetManager = gameContext.getAssetManager();
        Skin skinUi = assetManager.get("ui/ui_skin.json");
        Skin skinFlags = assetManager.get("flags/flags_skin.json");
        Skin skinPopup = assetManager.get("ui/popup/popup_skin.json");
        Skin skinTopBar = assetManager.get("ui/topbar/topbar_skin.json");
        Skin skinMinimap = assetManager.get("ui/minimap/minimap_skin.json");
        Skin skinProvince = assetManager.get("ui/province/province_skin.json");
        Skin skinScrollbars = assetManager.get("ui/scrollbars/scrollbars_skin.json");
        Skin skinEconomy = assetManager.get("ui/economy/economy_skin.json");
        this.stage = new WgCustomStage(new WgScreenViewport(), skinUi, skinFlags);
        WidgetFactory widgetFactory = new WidgetFactory();

        this.mainMenuInGamePresenter = new MainMenuInGamePresenter(gameContext, configurationService, this, widgetFactory, assetManager.get("ui/mainmenu_ig/mainmenu_ig_skin.json"), skinUi, skinScrollbars, skinPopup, skinFlags);
        this.topBarPresenter = new TopBarPresenter(this.gameContext, this.worldService, this.timeService, this, widgetFactory, skinTopBar, skinUi, skinFlags);
        this.provincePanelPresenter = new ProvincePanelPresenter(this.gameContext, this.worldService, widgetFactory, skinProvince, skinUi, skinFlags);
        this.tooltipPresenter = new TooltipPresenter(this.gameContext, this.worldService, widgetFactory, skinUi, skinFlags);
        this.economyPanelPresenter = new EconomyPanelPresenter(this.gameContext, this.worldService, commandBus, this, widgetFactory, skinEconomy, skinUi, skinScrollbars);
        this.minimapPresenter = new MiniMapPresenter(this.gameContext, this.worldService, this, skinMinimap, skinUi);
        this.initializeDebug();

        this.mainMenuInGamePresenter.initialize(this.stage);
        this.topBarPresenter.initialize(this.stage);
        this.provincePanelPresenter.initialize(this.stage);
        this.tooltipPresenter.initialize(this.stage);
        this.economyPanelPresenter.initialize(this.stage);
        this.minimapPresenter.initialize(this.stage);
        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);
        this.timeService.addListener(this);
        this.timeService.addListener(this.topBarPresenter);
        this.timeService.addListener(this.provincePanelPresenter);
        this.timeService.initialize();
        this.paused = false;
    }

    private void initializeDebug() {
        if(this.gameContext.getSettings().isDebugMode()) {
            DebugPresenter debugPresenter = new DebugPresenter(gameContext, worldService);
            debugPresenter.initialize(this.stage);
            this.presenters.add(debugPresenter);
        }
    }

    @Override
    public void onClick(int x, int y) {
        this.worldService.selectProvince(x, y);
        this.provincePanelPresenter.refresh();
    }

    @Override
    public void onHover(int x, int y) {
        if(this.worldService.hoverLandProvince(x, y) && !this.isMouseOverUI()) {
            this.tooltipPresenter.updateMapTooltip(x, y);
        } else {
            this.tooltipPresenter.hideMapTooltip();
        }
    }

    @Override
    public void onNewDay(LocalDate date) {
        this.commandBus.process();
        this.gameContext.getEcsWorld().progress(1f);
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

    private boolean isMouseOverUI() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return this.stage.hit(mouseX, mouseY, true) != null;
    }

    @Override
    public void showTooltip(String content) {
        this.tooltipPresenter.updateUiTooltip(content);
    }

    @Override
    public void hideTooltip() {
        this.tooltipPresenter.hideUiTooltip();
    }

    @Override
    public void moveCameraTo(int x, int y) {
        this.inputHandler.moveCamera(x, y);
    }

    @Override
    public void zoomIn() {
        this.inputHandler.zoomIn();
    }

    @Override
    public void zoomOut() {
        this.inputHandler.zoomOut();
    }

    @Override
    public void show() {
        this.gameContext.getCursorManager().defaultCursor();
    }

    @Override
    public void render(float delta) {
        this.time += delta;

        this.cam.update();
        float camX = this.cam.position.x;
        camX = (camX + WORLD_WIDTH) % WORLD_WIDTH;
        this.cam.position.x = camX;
        this.projection.setProjectionMatrix(this.cam.combined);

        WgScreenUtils.clear(1, 1, 1, 1);

        this.worldService.renderWorld(this.projection, this.cam, this.time);

        this.timeService.update(delta);

        if(!this.paused) {
            this.inputHandler.setDelta(delta);
            this.inputHandler.handleInput();
            this.inputHandler.updateCamera();
        }

        for(Presenter presenter : this.presenters) {
            presenter.update(delta);
        }

        this.stage.act();
        this.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        this.cam.viewportWidth = width / 2f;
        this.cam.viewportHeight = height / 2f;
        this.cam.update();

        this.stage.getViewport().update(width, height, true);
        ((WgCustomStage) this.stage).updateRendererProjection();
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
    public void hide() {

    }

    @Override
    public void setInputEnabled(boolean enabled) {
        if (enabled) {
            this.multiplexer.addProcessor(this.inputHandler);
            this.setAllStageTouchable(true);
        } else {
            this.multiplexer.removeProcessor(this.inputHandler);
            this.setAllStageTouchable(false);
        }
    }

    @Override
    public void toggleEconomyPanel() {
        this.economyPanelPresenter.refresh();
    }

    private void setAllStageTouchable(boolean touchable) {
        for (int i = 0; i < this.stage.getActors().size; i++) {
            Actor actor = this.stage.getActors().get(i);
            if (!this.isMenuContainer(actor)) {
                actor.setTouchable(touchable ? Touchable.childrenOnly : Touchable.disabled);
            }
        }
    }

    private boolean isMenuContainer(Actor actor) {
        if (actor instanceof MainMenuInGame) {
            return true;
        }

        if (actor instanceof Group group) {
            for (Actor child : group.getChildren()) {
                if (this.isMenuContainer(child)) {
                    return true;

                }
            }
        }

        return false;
    }

    @Override
    public void dispose() {
        this.stage.dispose();
    }
}
