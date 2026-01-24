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
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.populaire.projetguerrefroide.adapter.graphics.WgCustomStage;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.adapter.graphics.WgScreenViewport;
import com.populaire.projetguerrefroide.component.Position;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.screen.input.GameInputHandler;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.screen.listener.*;
import com.populaire.projetguerrefroide.screen.presenter.MainMenuInGamePresenter;
import com.populaire.projetguerrefroide.screen.presenter.TopBarPresenter;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.TimeService;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.*;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.DateUtils;

import java.time.LocalDate;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class GameScreen implements Screen, GameInputListener, TimeListener, MinimapListener, EconomyPanelListener, GameFlowHandler {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final ConfigurationService configurationService;
    private final TimeService timeService;
    private final OrthographicCamera cam;
    private final WgProjection projection;
    private final InputMultiplexer multiplexer;
    private final GameInputHandler inputHandler;
    private final MainMenuInGamePresenter mainMenuInGamePresenter;
    private final TopBarPresenter topBarPresenter;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Skin skinPortraits;
    private final Skin skinScrollbars;
    private final Skin skinProvince;
    private final Skin skinMinimap;
    private final Skin skinTopBar;
    private final Skin skinEconomy;
    private final Stage stage;
    private final Debug debug;
    private final ProvincePanel provincePanel;
    private HoverTooltip hoverTooltip;
    private EconomyPanel economyPanel;
    private WidgetFactory widgetFactory;
    private float time;
    private boolean paused;

    public GameScreen(ScreenManager screenManager, GameContext gameContext, WorldService worldService, ConfigurationService configurationService) {
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.configurationService = configurationService;
        this.timeService = new TimeService(this.gameContext.getBookmark().date());
        this.timeService.addListener(this);
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.projection = new WgProjection();
        Position capitalPosition = this.worldService.getCapitalPositionOfSelectedCountry();
        this.cam.position.set(capitalPosition.x(), capitalPosition.y(), 0);
        this.cam.update();
        this.multiplexer = new InputMultiplexer();
        this.inputHandler = new GameInputHandler(this.cam, this);
        AssetManager assetManager = gameContext.getAssetManager();
        this.skinUi = assetManager.get("ui/ui_skin.json");
        this.skinFlags = assetManager.get("flags/flags_skin.json");
        Skin skinPopup = assetManager.get("ui/popup/popup_skin.json");
        this.skinTopBar = assetManager.get("ui/topbar/topbar_skin.json");
        this.skinMinimap = assetManager.get("ui/minimap/minimap_skin.json");
        this.skinProvince = assetManager.get("ui/province/province_skin.json");
        this.skinPortraits = assetManager.get("portraits/portraits_skin.json");
        this.skinScrollbars = assetManager.get("ui/scrollbars/scrollbars_skin.json");
        this.skinEconomy = assetManager.get("ui/economy/economy_skin.json");
        this.configurationService.loadGameLocalisation(this.gameContext);

        this.widgetFactory = new WidgetFactory();
        this.mainMenuInGamePresenter = new MainMenuInGamePresenter(gameContext, configurationService, this, this.widgetFactory, assetManager.get("ui/mainmenu_ig/mainmenu_ig_skin.json"), skinUi, skinScrollbars, skinPopup, skinFlags);
        this.topBarPresenter = new TopBarPresenter(this.gameContext, this.worldService, this.timeService, this, this.widgetFactory, this.skinTopBar, this.skinUi, this.skinFlags);
        this.timeService.addListener(this.topBarPresenter);
        this.provincePanel = new ProvincePanel(this.widgetFactory, this.skinProvince, this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation());
        this.debug = new Debug(this.worldService.getNumberOfProvinces());
        this.stage = new WgCustomStage(new WgScreenViewport(), this.skinUi, this.skinFlags);
        this.mainMenuInGamePresenter.initialize(this.stage);
        this.topBarPresenter.initialize(this.stage);
        this.initializeUi();
        this.timeService.initialize();

        this.paused = false;
    }

    private void initializeUi() {
        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);

        this.provincePanel.setPosition(0, 0);

        this.hoverTooltip = new HoverTooltip(this.widgetFactory, this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation());
        this.stage.addActor(this.hoverTooltip);

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        topTable.pad(5);
        this.stage.addActor(topTable);

        Minimap minimap = new Minimap(this.skinMinimap, this.skinUi, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this);
        minimap.setPosition(Gdx.graphics.getWidth() - minimap.getWidth(), 0);
        this.stage.addActor(minimap);

        Table centerTable = new Table();
        centerTable.setFillParent(true);
        this.economyPanel = new EconomyPanel(this.widgetFactory, this.skinEconomy, this.skinUi, this.skinScrollbars, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this);
        Stack stack = new Stack();
        stack.add(this.economyPanel);
        this.economyPanel.setVisible(false);
        centerTable.add(stack).center();
        this.stage.addActor(centerTable);

        this.debug.setPosition(100, 90);
        this.debug.setVisible(this.gameContext.getSettings().isDebugMode());
        this.stage.addActor(this.debug);
    }

    @Override
    public void moveCamera(short x, short y) {
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
    public void changeMapMode(String mapMode) {
        this.worldService.changeMapMode(mapMode);
    }

    @Override
    public IntObjectMap<String> getInformationsMapMode(String mapMode) {
        IntObjectMap<String> informations = new IntObjectMap<>();
        return null;
    }

    @Override
    public void onClick(int x, int y) {
        if(this.worldService.selectProvince(x, y)) {
            this.showProvincePanel();
        }
    }

    @Override
    public void onHover(int x, int y) {
        if(this.worldService.hoverLandProvince(x, y) && this.isMouseOverUI()) {
            if(this.worldService.getMapMode().equals(MapMode.CULTURAL)) {
                this.updateHoverTooltip(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y), this.worldService.getCulturesOfHoveredProvince(x, y));
            } else if(this.worldService.getMapMode().equals(MapMode.RELIGIOUS)) {
                this.updateHoverTooltip(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y), this.worldService.getReligionsOfHoveredProvince(x, y));
            } else {
                this.updateHoverTooltip(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y));
            }
        } else if(this.isMouseOverUI()) {
            this.hideHoverBox();
        }
    }

    @Override
    public void onNewDay(LocalDate date) {
        this.gameContext.getEcsWorld().progress(1f);
        this.provincePanel.setResourceProduced(this.worldService.getResourceGoodsProduction());
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
    public void onCloseEconomyPanelClicked() {
        this.economyPanel.setTouchable(Touchable.disabled);
        this.economyPanel.setVisible(false);
    }

    @Override
    public void onSortRegions(SortType sortType) {
        RegionsBuildingsDto regionsBuildingsDto = this.worldService.prepareRegionsBuildingsDto(sortType);
        this.economyPanel.setData(regionsBuildingsDto);
    }

    private boolean isMouseOverUI() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return this.stage.hit(mouseX, mouseY, true) == null;
    }

    private void showProvincePanel() {
        ProvinceDto provinceDto = this.worldService.buildProvinceDetails();
        this.provincePanel.setData(provinceDto);
        this.stage.addActor(this.provincePanel);
    }

    @Override
    public void updateHoverTooltip(String text) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverTooltip.update(text);
        this.hoverTooltip.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(), y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverTooltip.setVisible(true);
        this.hoverTooltip.toFront();
    }

    public void updateHoverTooltip(String provinceNameId, String countryNameId, String colonizerId) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverTooltip.update(provinceNameId, countryNameId, colonizerId);
        this.hoverTooltip.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(), y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverTooltip.setVisible(true);
        this.hoverTooltip.toBack();
    }

    public void updateHoverTooltip(String provinceNameId, String countryNameId, String colonizerId, ObjectIntMap<String> elements) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverTooltip.update(provinceNameId, countryNameId, colonizerId, elements);
        this.hoverTooltip.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(),
            y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverTooltip.setVisible(true);
        this.hoverTooltip.toBack();
    }

    @Override
    public void hideHoverBox() {
        this.hoverTooltip.setVisible(false);
    }

    @Override
    public void show() {
        this.gameContext.getCursorManager().defaultCursor();
    }

    @Override
    public void render(float delta) {
        this.time += delta;

        float renderTimeMs = Gdx.graphics.getDeltaTime() * 1000;

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

        this.debug.update(renderTimeMs);

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
            this.setAllStageTouchable(Touchable.enabled);
        } else {
            this.multiplexer.removeProcessor(this.inputHandler);
            this.setAllStageTouchable(Touchable.disabled);
        }
    }

    @Override
    public void toggleEconomyPanel() {
        this.economyPanel.setTouchable(Touchable.enabled);
        this.economyPanel.setVisible(true);
        RegionsBuildingsDto regionsBuildingsDto = this.worldService.prepareRegionsBuildingsDto(SortType.DEFAULT);
        this.economyPanel.setData(regionsBuildingsDto);
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

    private boolean isMenuContainer(Table t) {
        for(Actor c : t.getChildren()) {
            if(c instanceof MainMenuInGame) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        this.stage.dispose();
    }
}
