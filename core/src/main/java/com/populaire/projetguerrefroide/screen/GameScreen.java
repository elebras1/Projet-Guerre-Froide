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
import com.populaire.projetguerrefroide.protocol.catalog.ProvinceTable;
import com.populaire.projetguerrefroide.screen.input.GameInputHandler;
import com.populaire.projetguerrefroide.screen.presenter.*;
import com.populaire.projetguerrefroide.ui.UiResources;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.view.menubar.InGameMenu;
import com.populaire.projetguerrefroide.screen.input.GameInputListener;

import java.util.List;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class GameScreen implements Screen, GameInputListener, GameFlowHandler {
    private final ScreenManager screenManager;
    private final UiContext uiContext;
    private final GameSession session;
    private final UiResources uiResources;
    private final OrthographicCamera cam;
    private final WgProjection projection;
    private final InputMultiplexer multiplexer;
    private final GameInputHandler inputHandler;
    private final MainMenuInGamePresenter mainMenuInGamePresenter;
    private final TopBarPresenter topBarPresenter;
    private final ProvincePanelPresenter provincePanelPresenter;
    private final TooltipPresenter tooltipPresenter;
    private final EconomyPanelPresenter economyPanelPresenter;
    private final CountryRankingPresenter countryRankingPresenter;
    private final MiniMapPresenter minimapPresenter;
    private final List<Presenter> presenters;
    private final Stage stage;
    private float time;
    private boolean paused;

    public GameScreen(ScreenManager screenManager, UiContext uiContext, GameSession session, UiResources uiResources) {
        this.screenManager = screenManager;
        this.uiContext = uiContext;
        this.session = session;
        this.uiResources = uiResources;
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.projection = new WgProjection();
        int capital = session.mapView().capitalOf(session.uiState().playerCountry());
        if (capital >= 0) {
            ProvinceTable provinces = session.uiCatalog().catalog().provinces();
            this.cam.position.set(provinces.positionX(capital), provinces.positionY(capital), 0);
        }
        this.cam.update();
        this.multiplexer = new InputMultiplexer();
        this.inputHandler = new GameInputHandler(this.cam, this);
        this.uiResources.loadGameLocalisation(this.uiContext);
        this.presenters = new ObjectList<>();

        AssetManager assetManager = uiContext.getAssetManager();
        Skin skinUi = assetManager.get(Skins.UI);
        Skin skinFlags = assetManager.get(Skins.FLAGS);
        this.stage = new WgCustomStage(new WgScreenViewport(), skinUi, skinFlags);
        TextFormat textFormat = new TextFormat(uiContext.getLocalisation());

        this.mainMenuInGamePresenter = new MainMenuInGamePresenter(uiContext, uiResources, this, screenManager);
        this.topBarPresenter = new TopBarPresenter(session, this.uiContext, this, textFormat);
        this.provincePanelPresenter = new ProvincePanelPresenter(session, this.uiContext, this, textFormat);
        this.tooltipPresenter = new TooltipPresenter(session, this.uiContext, skinUi, skinFlags);
        this.economyPanelPresenter = new EconomyPanelPresenter(session, this.uiContext, textFormat);
        this.countryRankingPresenter = new CountryRankingPresenter(session, this.uiContext, textFormat);
        this.minimapPresenter = new MiniMapPresenter(session, this.uiContext, this);
        this.initializeDebug();

        this.mainMenuInGamePresenter.initialize(this.stage);
        this.topBarPresenter.initialize(this.stage);
        this.provincePanelPresenter.initialize(this.stage);
        this.tooltipPresenter.initialize(this.stage);
        this.economyPanelPresenter.initialize(this.stage);
        this.minimapPresenter.initialize(this.stage);
        this.countryRankingPresenter.initialize(this.stage);
        this.presenters.add(this.topBarPresenter);
        this.presenters.add(this.provincePanelPresenter);
        this.presenters.add(this.tooltipPresenter);
        this.presenters.add(this.economyPanelPresenter);
        this.presenters.add(this.minimapPresenter);
        this.presenters.add(this.countryRankingPresenter);
        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);
        this.paused = false;
    }

    private void initializeDebug() {
        if(this.uiContext.getSettings().isDebugMode()) {
            DebugPresenter debugPresenter = new DebugPresenter(uiContext, this.session);
            debugPresenter.initialize(this.stage);
            this.presenters.add(debugPresenter);
        }
    }

    @Override
    public void onClick(int x, int y) {
        int provinceIndex = this.session.mapView().provinceIndexAt(x, y);
        this.session.uiState().selectProvince(provinceIndex);
        this.session.mapView().highlightProvince(provinceIndex);
        this.provincePanelPresenter.open(provinceIndex);
    }

    @Override
    public void onHover(int x, int y) {
        int provinceIndex = this.session.mapView().provinceIndexAt(x, y);
        if (provinceIndex >= 0 && !this.isMouseOverUI()) {
            this.tooltipPresenter.updateMapTooltip(provinceIndex);
        } else {
            this.tooltipPresenter.hideMapTooltip();
        }
    }

    @Override
    public void onEscape() {
        if (!this.paused) {
            this.pause();
            this.mainMenuInGamePresenter.show();
        } else {
            this.mainMenuInGamePresenter.close();
        }
    }

    @Override
    public void openInGameMenu() {
        this.pause();
        this.mainMenuInGamePresenter.show();
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
        this.uiContext.getCursorManager().defaultCursor();
    }

    @Override
    public void render(float delta) {
        this.session.beginFrame();
        this.time += delta;

        this.cam.update();
        float camX = this.cam.position.x;
        camX = (camX + WORLD_WIDTH) % WORLD_WIDTH;
        this.cam.position.x = camX;
        this.projection.setProjectionMatrix(this.cam.combined);

        WgScreenUtils.clear(1, 1, 1, 1);

        this.session.mapView().update();
        this.session.mapView().render(this.projection, this.cam, this.time);

        if(!this.paused) {
            this.inputHandler.setDelta(delta);
            this.inputHandler.handleInput();
            this.inputHandler.updateCamera();
        }

        for (int i = 0; i < this.presenters.size(); i++) {
            this.presenters.get(i).update(delta);
        }

        this.stage.act();
        this.stage.draw();
        this.session.endFrame();
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
        this.countryRankingPresenter.close();
        this.economyPanelPresenter.open();
    }

    @Override
    public void toggleCountryRankingPanel() {
        this.economyPanelPresenter.close();
        this.countryRankingPresenter.toggle();
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
        if (actor instanceof InGameMenu) {
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
