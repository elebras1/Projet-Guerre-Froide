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
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.populaire.projetguerrefroide.adapter.graphics.WgCustomStage;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.adapter.graphics.WgScreenViewport;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.input.GameInputHandler;
import com.populaire.projetguerrefroide.map.MapMode;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.DateService;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.*;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.DateUtils;

import java.time.LocalDate;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class GameScreen implements Screen, GameInputListener, DateListener, TopBarListener, MainMenuInGameListener, MinimapListener, EconomyPanelListener {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final ConfigurationService configurationService;
    private final DateService dateService;
    private final OrthographicCamera cam;
    private final WgProjection projection;
    private final InputMultiplexer multiplexer;
    private final GameInputHandler inputHandler;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Skin skinPopup;
    private final Skin skinPortraits;
    private final Skin skinScrollbars;
    private final Skin skinProvince;
    private final Skin skinMainMenuInGame;
    private final Skin skinMinimap;
    private final Skin skinTopBar;
    private final Skin skinEconomy;
    private final Stage stage;
    private final Debug debug;
    private final TopBar topBar;
    private final ProvincePanel provincePanel;
    private HoverTooltip hoverTooltip;
    private MainMenuInGame mainMenuInGame;
    private EconomyPanel economyPanel;
    private WidgetFactory widgetFactory;
    private float time;
    private boolean paused;

    public GameScreen(ScreenManager screenManager, GameContext gameContext, WorldService worldService, ConfigurationService configurationService) {
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.configurationService = configurationService;
        this.dateService = new DateService(this.gameContext.getBookmark().getDate());
        this.dateService.addListener(this.worldService);
        this.dateService.addListener(this);
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.projection = new WgProjection();
        int capitalPosition = this.worldService.getPositionOfCapitalOfSelectedCountry();
        short capitalX = (short) (capitalPosition >> 16);
        short capitalY = (short) (capitalPosition & 0xFFFF);
        this.cam.position.set(capitalX, capitalY, 0);
        this.cam.update();
        this.multiplexer = new InputMultiplexer();
        this.inputHandler = new GameInputHandler(this.cam, this);
        AssetManager assetManager = gameContext.getAssetManager();
        this.skinUi = assetManager.get("ui/ui_skin.json");
        this.skinFlags = assetManager.get("flags/flags_skin.json");
        this.skinPopup = assetManager.get("ui/popup/popup_skin.json");
        this.skinTopBar = assetManager.get("ui/topbar/topbar_skin.json");
        this.skinMinimap = assetManager.get("ui/minimap/minimap_skin.json");
        this.skinProvince = assetManager.get("ui/province/province_skin.json");
        this.skinPortraits = assetManager.get("portraits/portraits_skin.json");
        this.skinScrollbars = assetManager.get("ui/scrollbars/scrollbars_skin.json");
        this.skinMainMenuInGame = assetManager.get("ui/mainmenu_ig/mainmenu_ig_skin.json");
        this.skinEconomy = assetManager.get("ui/economy/economy_skin.json");

        this.configurationService.loadGameLocalisation(this.gameContext);

        this.widgetFactory = new WidgetFactory();
        this.topBar = new TopBar(this.widgetFactory, this.skinTopBar, this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this.worldService.getCountryIdPlayer(), this.worldService.getColonizerIdOfSelectedProvince(), this);
        this.provincePanel = new ProvincePanel(this.widgetFactory, this.skinProvince, this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation());
        this.debug = new Debug(this.worldService.getNumberOfProvinces());
        this.stage = new WgCustomStage(new WgScreenViewport(), this.skinUi, this.skinFlags);
        this.initializeUi();
        this.worldService.initializeEconomy();
        this.dateService.initialize();

        this.paused = false;
    }

    private void initializeUi() {
        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);

        this.topBar.setPosition(0, Gdx.graphics.getHeight() - this.topBar.getHeight());
        this.topBar.setCountryData(this.worldService.prepareCountryDto(this.gameContext.getLocalisation()));
        this.topBar.setRanking(this.worldService.getRankingOfSelectedCountry());
        this.stage.addActor(this.topBar);

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
        this.mainMenuInGame = new MainMenuInGame(this.widgetFactory, this.skinMainMenuInGame, this.skinUi, this.skinScrollbars, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this);
        Stack stack = new Stack();
        stack.add(this.economyPanel);
        Table mainMenuTable = new Table();
        mainMenuTable.add(this.mainMenuInGame);
        stack.add(mainMenuTable);
        this.economyPanel.setVisible(false);
        this.mainMenuInGame.setVisible(false);
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
    public void onClick(short x, short y) {
        if(this.worldService.selectProvince(x, y)) {
            this.showProvincePanel();
        }
    }

    @Override
    public void onHover(short x, short y) {
        if(this.worldService.hoverProvince(x, y) && this.isMouseOverUI()) {
            if(this.worldService.getMapMode().equals(MapMode.CULTURAL)) {
                this.updateHoverTooltip(this.worldService.getProvinceId(x, y), this.worldService.getCountryIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y), this.worldService.getCulturesOfHoveredProvince(x, y));
            } else if(this.worldService.getMapMode().equals(MapMode.RELIGIOUS)) {
                this.updateHoverTooltip(this.worldService.getProvinceId(x, y), this.worldService.getCountryIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y), this.worldService.getReligionsOfHoveredProvince(x, y));
            } else {
                this.updateHoverTooltip(this.worldService.getProvinceId(x, y), this.worldService.getCountryIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y));
            }
        } else if(this.isMouseOverUI()) {
            this.hideHoverBox();
        }
    }

    @Override
    public void onNewDay(LocalDate date) {
        this.topBar.setDate(DateUtils.formatDate(date, this.gameContext.getLocalisation(), this.gameContext.getSettings().getLanguage()));
        this.provincePanel.setResourceProduced(this.worldService.getResourceGoodsProduction());
    }

    @Override
    public int onSpeedUp() {
        return this.dateService.upSpeed();
    }

    @Override
    public int onSpeedDown() {
        return this.dateService.downSpeed();
    }

    @Override
    public int onTogglePause() {
        return this.dateService.togglePause();
    }

    @Override
    public void onEconomyClicked() {
        this.economyPanel.setTouchable(Touchable.enabled);
        this.economyPanel.setVisible(true);
        RegionsBuildingsDto regionsBuildingsDto = this.worldService.prepareRegionsBuildingsDto();
        this.economyPanel.setData(regionsBuildingsDto);
    }

    @Override
    public void onEscape() {
        this.paused = true;
        this.mainMenuInGame.setVisible(true);
        if(this.paused) {
            this.multiplexer.removeProcessor(this.inputHandler);
            this.setActorsTouchable(false);
        }
    }

    @Override
    public Settings onShowSettingsClicked() {
        return this.gameContext.getSettings().clone();
    }

    @Override
    public void onApplySettingsClicked(Settings settings) {
        this.gameContext.setSettings(settings);
        this.configurationService.saveSettings(settings);
    }

    @Override
    public void onCloseMainMenuInGameClicked() {
        this.paused = false;
        this.mainMenuInGame.setVisible(false);
        this.multiplexer.addProcessor(this.inputHandler);
        this.setActorsTouchable(true);
    }

    @Override
    public void onQuitClicked(PopupListener listener) {
        Popup popup = new Popup(this.widgetFactory, this.skinPopup, this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(),
            "QUIT_TITLE", "QUIT_DESC", this.worldService.getCountryIdPlayer(), this.worldService.getColonizerIdOfCountryPlayer(), true, false, listener);
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        centerTable.add(popup).center();
        this.stage.addActor(centerTable);
        this.mainMenuInGame.setTouchable(Touchable.disabled);
    }

    @Override
    public void onOkPopupClicked() {
        Gdx.app.exit();
    }

    @Override
    public void onCancelPopupClicked() {
        this.stage.getActors().removeValue(this.stage.getActors().peek(), true);
        this.mainMenuInGame.setTouchable(Touchable.enabled);
    }

    @Override
    public void onCloseEconomyPanelClicked() {
        this.economyPanel.setTouchable(Touchable.disabled);
        this.economyPanel.setVisible(false);
    }

    @Override
    public void onSortRegions(SortType sortType) {
        RegionsBuildingsDto regionsBuildingsDto = this.worldService.prepareRegionsBuildingsDtoSorted(sortType);
        this.economyPanel.setData(regionsBuildingsDto);
    }

    public void setActorsTouchable(boolean touchable) {
        for (int i = 0; i < this.stage.getActors().size; i++) {
            Actor actor = this.stage.getActors().get(i);

            if (!this.containsMainMenuInGame(actor)) {
                actor.setTouchable(touchable ? Touchable.childrenOnly : Touchable.disabled);
            }
        }
        this.economyPanel.setTouchable(touchable ? Touchable.enabled : Touchable.disabled);
    }

    private boolean containsMainMenuInGame(Actor actor) {
        if (actor instanceof MainMenuInGame) {
            return true;
        }

        if (actor instanceof Group group) {
            for (Actor child : group.getChildren()) {
                if (this.containsMainMenuInGame(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isMouseOverUI() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return this.stage.hit(mouseX, mouseY, true) == null;
    }

    private void showProvincePanel() {
        ProvinceDto provinceDto = this.worldService.prepareProvinceDto(this.gameContext.getLocalisation());
        this.provincePanel.setData(provinceDto);
        this.stage.addActor(this.provincePanel);
    }

    @Override
    public void updateHoverTooltip(String text) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverTooltip.update(text);
        this.hoverTooltip.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(),
                y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverTooltip.setVisible(true);
        this.hoverTooltip.toFront();
    }

    public void updateHoverTooltip(short provinceId, String countryId, String colonizerId) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverTooltip.update(provinceId, countryId, colonizerId);
        this.hoverTooltip.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(),
                y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverTooltip.setVisible(true);
        this.hoverTooltip.toBack();
    }

    public void updateHoverTooltip(short provinceId, String countryId, String colonizerId, ObjectIntMap<String> elements) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverTooltip.update(provinceId, countryId, colonizerId, elements);
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

        this.dateService.update(delta);

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

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        this.stage.dispose();
    }
}
