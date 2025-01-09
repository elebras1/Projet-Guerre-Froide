package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.input.GameInputHandler;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class GameScreen implements Screen, GameInputListener, MainMenuInGameListener {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final OrthographicCamera cam;
    private final SpriteBatch batch;
    private final InputMultiplexer multiplexer;
    private final GameInputHandler inputHandler;
    private final Skin skinUi;
    private final Skin skinFlags;
    private final Skin skinPopup;
    private final Skin skinPortraits;
    private final Skin skinScrollbars;
    private final Skin skinMainMenuInGame;
    private final Map<String, String> localisation;
    private Stage stage;
    private final List<Table> uiTables;
    private Debug debug;
    private HoverBox hoverBox;
    private MainMenuInGame mainMenuInGame;
    private float time;
    private boolean paused;

    public GameScreen(ScreenManager screenManager, GameContext gameContext, WorldService worldService) {
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.cam.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        this.cam.update();
        this.batch = new SpriteBatch();
        this.multiplexer = new InputMultiplexer();
        this.inputHandler = new GameInputHandler(this.cam, this);
        AssetManager assetManager = gameContext.getAssetManager();
        assetManager.finishLoading();
        this.skinUi = assetManager.get("ui/ui_skin.json");
        this.skinFlags = assetManager.get("flags/flags_skin.json");
        this.skinPopup = assetManager.get("ui/popup/popup_skin.json");
        this.skinPortraits = assetManager.get("portraits/portraits_skin.json");
        this.skinScrollbars = assetManager.get("ui/scrollbars/scrollbars_skin.json");
        this.skinMainMenuInGame = assetManager.get("ui/mainmenu_ig/mainmenu_ig_skin.json");
        this.uiTables = new ArrayList<>();
        this.localisation = this.gameContext.getLocalisationManager().readNewgameCsv();
        this.initializeUi();
        this.paused = false;
    }

    private void initializeUi() {
        this.stage = new Stage(new ScreenViewport());
        //this.stage.setDebugAll(true);

        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);

        this.debug = new Debug(this.worldService.getNumberOfProvinces());
        this.debug.setPosition(100, 90);
        this.debug.setVisible(this.gameContext.getSettings().isDebugMode());

        this.hoverBox = new HoverBox(this.skinUi, this.gameContext.getLabelStylePool());

        this.mainMenuInGame = new MainMenuInGame(this.skinMainMenuInGame, this.skinUi, this.skinScrollbars, this.gameContext.getLabelStylePool(), this.localisation, this);
        this.mainMenuInGame.setVisible(false);
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        centerTable.add(this.mainMenuInGame).center();

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        topTable.pad(5);

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom();
        bottomTable.pad(5);

        this.stage.addActor(this.hoverBox);
        this.stage.addActor(topTable);
        this.stage.addActor(bottomTable);
        this.stage.addActor(centerTable);
        this.stage.addActor(this.debug);
    }

    @Override
    public void onClick(short x, short y) {
        this.worldService.selectProvince(x, y);
    }

    @Override
    public void onHover(short x, short y) {
        if(this.worldService.hoverProvince(x, y)) {
            this.updateHoverBox(this.localisation.get(String.valueOf(this.worldService.getProvinceId(x, y))),
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
    public Settings onShowSettingsClicked() {
        return this.gameContext.getSettings().clone();
    }

    @Override
    public void onApplySettingsClicked(Settings settings) {
        this.gameContext.setSettings(settings);
        this.gameContext.getConfigurationManager().saveSettings(settings);
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
        Popup popup = new Popup(this.skinPopup, this.gameContext.getLabelStylePool(), this.localisation,
            this.localisation.get("QUIT_TITLE"), this.localisation.get("QUIT_DESC"), true, false, listener);
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

    public void updateHoverBox(String provinceName, String countryName, String countryId) {
        Vector2 screenPosition = new Vector2(Gdx.input.getX(), (Gdx.graphics.getHeight() - Gdx.input.getY()));
        this.hoverBox.update(provinceName + " (" + countryName + ")", this.skinFlags.getDrawable(countryId));
        this.hoverBox.setPosition(screenPosition.x + (float) this.gameContext.getCursorManager().getWidth(),
            screenPosition.y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverBox.setVisible(true);
    }

    public void hideHoverBox() {
        this.hoverBox.setVisible(false);
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
            this.inputHandler.handleInput(this.uiTables);
        }

        this.debug.actualize(renderTimeMs);

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
