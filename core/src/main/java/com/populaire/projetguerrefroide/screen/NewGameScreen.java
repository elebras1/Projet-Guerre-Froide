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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.populaire.projetguerrefroide.entity.Minister;
import com.populaire.projetguerrefroide.input.GameInputHandler;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.*;
import com.populaire.projetguerrefroide.data.DataManager;
import com.populaire.projetguerrefroide.util.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class NewGameScreen implements Screen, GameInputListener {
    private final DataManager dataManager;
    private final GameContext gameContext;
    private final WorldService worldService;
    private final OrthographicCamera cam;
    private final SpriteBatch batch;
    private final InputMultiplexer multiplexer;
    private final GameInputHandler inputHandler;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFonts;
    private final Skin skinFlags;
    private final Skin skinPortraits;
    private final Skin skinScrollbars;
    private final Skin skinMainMenuInGame;
    private final Map<String, String> localisation;
    private Stage stage;
    private final List<Table> uiTables;
    private Debug debug;
    private HoverBox hoverBox;
    private CountrySelected countrySelectedUi;
    private MainMenuInGame mainMenuInGame;
    private float time;
    private boolean paused;

    public NewGameScreen(ScreenManager screenManager, GameContext gameContext, WorldService worldService) {
        this.dataManager = new DataManager();
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.cam.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        this.cam.update();
        this.batch = new SpriteBatch();
        this.multiplexer = new InputMultiplexer();
        this.inputHandler = new GameInputHandler(this.cam, this);
        AssetManager assetManager = gameContext.getAssetManager();
        assetManager.load("ui/newgame/newgame_skin.json", Skin.class);
        assetManager.load("flags/flags_skin.json", Skin.class);
        assetManager.load("portraits/portraits_skin.json", Skin.class);
        assetManager.load("ui/mainmenu_ig/mainmenu_ig_skin.json", Skin.class);
        assetManager.finishLoading();
        this.skin = assetManager.get("ui/newgame/newgame_skin.json");
        this.skinUi = assetManager.get("ui/ui_skin.json");
        this.skinFonts = assetManager.get("ui/fonts/fonts_skin.json");
        this.skinFlags = assetManager.get("flags/flags_skin.json");
        this.skinPortraits = assetManager.get("portraits/portraits_skin.json");
        this.skinScrollbars = assetManager.get("ui/scrollbars/scrollbars_skin.json");
        this.skinMainMenuInGame = assetManager.get("ui/mainmenu_ig/mainmenu_ig_skin.json");
        this.uiTables = new ArrayList<>();
        this.localisation = this.dataManager.readNewgameLocalisationCsv();
        this.localisation.putAll(this.dataManager.readBookmarkLocalisationCsv());
        this.localisation.putAll(this.dataManager.readPoliticsLocalisationCsv());
        this.localisation.putAll(this.dataManager.readMainMenuInGameCsv());
        this.initializeUi();
        this.paused = false;
    }

    private void initializeUi() {
        this.stage = new Stage(new ScreenViewport());
        //this.stage.setDebugAll(true);

        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);

        this.debug = new Debug();
        this.debug.setPosition(100, 40);
        this.hoverBox = new HoverBox(this.skinUi, this.skinFonts);
        this.stage.addActor(this.hoverBox);

        this.mainMenuInGame = new MainMenuInGame(this.skinMainMenuInGame, gameContext.getLabelStylePool(), this.localisation);
        this.mainMenuInGame.setPosition(Gdx.graphics.getWidth() / 2f - this.mainMenuInGame.getWidth() / 2,
            Gdx.graphics.getHeight() / 2f - this.mainMenuInGame.getHeight() / 2);
        this.mainMenuInGame.setVisible(false);

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        ScenarioSavegameSelector scenarioSavegameSelector = new ScenarioSavegameSelector(this.skin, this.skinFonts, this.dataManager.readBookmarkJson(), this.localisation);
        this.uiTables.add(scenarioSavegameSelector);
        TitleBar titleBar = new TitleBar(this.skin, this.skinFonts, this.localisation);
        this.uiTables.add(titleBar);
        LobbyBox lobbyBox = new LobbyBox(this.skin, this.skinScrollbars, this.skinFonts, this.localisation);
        this.uiTables.add(lobbyBox);
        this.countrySelectedUi = new CountrySelected(this.skin, this.skinUi, this.skinFonts, this.localisation);
        this.uiTables.add(this.countrySelectedUi);
        topTable.add(scenarioSavegameSelector).align(Align.topLeft).expandX();
        topTable.add(titleBar).align(Align.top);
        topTable.add(this.countrySelectedUi).align(Align.topRight).expandX();
        topTable.pad(5);

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom();
        bottomTable.add(lobbyBox).align(Align.bottom);
        bottomTable.pad(5);

        this.stage.addActor(topTable);
        this.stage.addActor(this.mainMenuInGame);
        this.stage.addActor(bottomTable);
        this.stage.addActor(this.debug);
    }

    @Override
    public void onClick(short x, short y) {
        this.worldService.selectProvince(x, y);
        this.updateCountrySelected();
    }

    @Override
    public void onHover(short x, short y) {
        if(this.worldService.hoverProvince(x, y)) {
            this.updateHoverBox(this.worldService.getNameOfHoveredProvince(x, y),
                this.worldService.getCountryNameOfHoveredProvince(x, y),
                this.worldService.getCountryIdOfHoveredProvince(x, y));
        } else {
            this.hideHoverBox();
        }
    }

    @Override
    public void onEscape() {
        this.paused = !this.paused;
        this.mainMenuInGame.setVisible(this.paused);
        if(this.paused) {
            this.multiplexer.removeProcessor(this.inputHandler);
            this.setActorsTouchable(false);
        }
    }

    public void setActorsTouchable(boolean touchable) {
        for (int i = 0; i < this.stage.getActors().size; i++) {
            Actor actor = this.stage.getActors().get(i);
            if(actor != this.mainMenuInGame) {
                actor.setTouchable(touchable ? Touchable.enabled : Touchable.disabled);
            }
        }
    }

    public void updateCountrySelected() {
        if(this.worldService.isProvinceSelected()) {
            Minister headOfState = this.worldService.getHeadOfStateOfSelectedCountry();
            Drawable portrait;
            try {
                portrait = this.skinPortraits.getDrawable(headOfState.getImageNameFile());
            } catch (com.badlogic.gdx.utils.GdxRuntimeException e) {
                portrait = this.skinPortraits.getDrawable("admin_type");
            }
            this.countrySelectedUi.update(
                this.worldService.getNameOfSelectedCountry(),
                this.skinFlags.getRegion(this.worldService.getIdOfSelectedCountry()),
                ValueFormatter.formatValue(this.worldService.getPopulationSizeOfSelectedCountry()),
                this.worldService.getGovernmentOfSelectedCountry(), portrait, headOfState.getName(), this.localisation);
        } else {
            this.countrySelectedUi.hide();
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
