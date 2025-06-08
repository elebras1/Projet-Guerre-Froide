package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.input.GameInputHandler;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.*;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class NewGameScreen implements Screen, GameInputListener, MainMenuInGameListener, LobbyBoxListener {
    private final ScreenManager screenManager;
    private final GameContext gameContext;
    private final WorldService worldService;
    private final ConfigurationService configurationService;
    private final OrthographicCamera cam;
    private final SpriteBatch batch;
    private final InputMultiplexer multiplexer;
    private final GameInputHandler inputHandler;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Skin skinPopup;
    private final Skin skinPortraits;
    private final Skin skinScrollbars;
    private final Skin skinMainMenuInGame;
    private Stage stage;
    private Debug debug;
    private HoverTooltip hoverTooltip;
    private CountrySummaryPanel countrySummaryPanel;
    private MainMenuInGame mainMenuInGame;
    private float time;
    private boolean paused;

    public NewGameScreen(ScreenManager screenManager, GameContext gameContext, WorldService worldService, ConfigurationService configurationService) {
        this.screenManager = screenManager;
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.configurationService = configurationService;
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.cam.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 1.4f, 0);
        this.cam.update();
        this.batch = new SpriteBatch();
        this.multiplexer = new InputMultiplexer();
        this.inputHandler = new GameInputHandler(this.cam, this);
        AssetManager assetManager = gameContext.getAssetManager();
        this.skin = assetManager.get("ui/newgame/newgame_skin.json");
        this.skinUi = assetManager.get("ui/ui_skin.json");
        this.skinFlags = assetManager.get("flags/flags_skin.json");
        this.skinPopup = assetManager.get("ui/popup/popup_skin.json");
        this.skinPortraits = assetManager.get("portraits/portraits_skin.json");
        this.skinScrollbars = assetManager.get("ui/scrollbars/scrollbars_skin.json");
        this.skinMainMenuInGame = assetManager.get("ui/mainmenu_ig/mainmenu_ig_skin.json");
        this.configurationService.loadNewGameLocalisation(this.gameContext);
        this.initializeUi();
        this.paused = false;
    }

    private void initializeUi() {
        this.stage = new Stage(new ScreenViewport());

        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);

        this.debug = new Debug(this.worldService.getNumberOfProvinces());
        this.debug.setPosition(100, 90);
        this.debug.setVisible(this.gameContext.getSettings().isDebugMode());

        WidgetFactory widgetFactory = new WidgetFactory();
        this.hoverTooltip = new HoverTooltip(this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation());
        this.mainMenuInGame = new MainMenuInGame(widgetFactory, this.skinMainMenuInGame, this.skinUi, this.skinScrollbars, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this);
        this.mainMenuInGame.setVisible(false);
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        centerTable.add(this.mainMenuInGame).center();

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        ScenarioSavegameSelector scenarioSavegameSelector = new ScenarioSavegameSelector(this.skin, this.gameContext.getLabelStylePool(), this.gameContext.getBookmark(), this.gameContext.getLocalisation());
        TitleBar titleBar = new TitleBar(widgetFactory, this.skin, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation());
        LobbyBox lobbyBox = new LobbyBox(widgetFactory, this.skin, this.skinScrollbars, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(), this);
        this.countrySummaryPanel = new CountrySummaryPanel(widgetFactory, this.skin, this.skinUi, this.skinFlags, this.skinPortraits, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation());
        topTable.add(scenarioSavegameSelector).align(Align.topLeft).expandX();
        topTable.add(titleBar).align(Align.top);
        topTable.add(this.countrySummaryPanel).align(Align.topRight).expandX();
        topTable.pad(5);

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom();
        bottomTable.add(lobbyBox).align(Align.bottom);
        bottomTable.pad(5);

        this.stage.addActor(this.hoverTooltip);
        this.stage.addActor(topTable);
        this.stage.addActor(bottomTable);
        this.stage.addActor(centerTable);
        this.stage.addActor(this.debug);
    }

    @Override
    public void onClick(short x, short y) {
        this.worldService.selectProvince(x, y);
        this.updateCountrySelected();
    }

    @Override
    public void onHover(short x, short y) {
        if(this.worldService.hoverProvince(x, y) && !this.isMouseOverUI()) {
            this.updateHoverBox(this.worldService.getProvinceId(x, y),
                this.worldService.getCountryNameOfHoveredProvince(x, y),
                this.worldService.getCountryIdOfHoveredProvince(x, y));
        } else {
            this.hideHoverBox();
        }
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
    public void onPlayClicked() {
        if(this.worldService.setCountryPlayer()) {
            this.screenManager.showGameScreen(this.worldService);
        }
    }

    @Override
    public void onBackClicked() {
        this.screenManager.showMainMenuScreen();
        this.worldService.dispose();
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
    public void onCloseClicked() {
        this.paused = false;
        this.mainMenuInGame.setVisible(false);
        this.multiplexer.addProcessor(this.inputHandler);
        this.setActorsTouchable(true);
    }

    @Override
    public void onQuitClicked(PopupListener listener) {
        Popup popup = new Popup(this.skinPopup, this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation(),
            "QUIT_TITLE", "QUIT_DESC", true, false, listener);
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

    public void setActorsTouchable(boolean touchable) {
        for (int i = 0; i < this.stage.getActors().size; i++) {
            Actor actor = this.stage.getActors().get(i);

            if (actor instanceof Table table) {
                boolean containsMainMenu = false;

                for (Actor child : table.getChildren()) {
                    if (child instanceof MainMenuInGame) {
                        containsMainMenu = true;
                        break;
                    }
                }

                if (!containsMainMenu) {
                    actor.setTouchable(touchable ? Touchable.childrenOnly : Touchable.disabled);
                }
            } else {
                actor.setTouchable(touchable ? Touchable.childrenOnly : Touchable.disabled);
            }
        }
    }

    private boolean isMouseOverUI() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return this.stage.hit(mouseX, mouseY, true) != null;
    }

    public void updateCountrySelected() {
        if(this.worldService.isProvinceSelected()) {
            this.countrySummaryPanel.update(this.worldService.prepareCountrySummaryDto(this.gameContext.getLocalisation()), this.gameContext.getLocalisation());
        } else {
            this.countrySummaryPanel.hide();
        }
    }

    public void updateHoverBox(short provinceId, String countryName, String countryId) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverTooltip.update(provinceId, countryName, countryId);
        this.hoverTooltip.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(),
            y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverTooltip.setVisible(true);
    }

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
        this.batch.setProjectionMatrix(this.cam.combined);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);

        this.worldService.renderWorld(this.batch, this.cam, time);

        if(!this.paused) {
            this.inputHandler.setDelta(delta);
            this.inputHandler.handleInput();
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
        this.batch.dispose();
    }
}
