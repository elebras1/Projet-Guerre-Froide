package com.populaire.projetguerrefroide.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.CpuSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.populaire.projetguerrefroide.entities.Minister;
import com.populaire.projetguerrefroide.map.Country;
import com.populaire.projetguerrefroide.map.LandProvince;
import com.populaire.projetguerrefroide.map.World;
import com.populaire.projetguerrefroide.ui.*;
import com.populaire.projetguerrefroide.utils.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class NewGameScreen implements Screen {
    private final World world;
    private final Game game;
    private final OrthographicCamera cam;
    private final CpuSpriteBatch batch;
    private final NewGameInputHandler<NewGameScreen> inputHandler;
    private final AssetManager assetManager;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinFonts;
    private final Skin skinFlags;
    private final Skin skinPortraits;
    private final Skin skinScrollbars;
    private Stage stage;
    private List<Table> uiTables;
    private CursorManager cursorChanger;
    private Debug debug;
    private float time;
    private HoverBox hoverBox;
    private CountrySelected countrySelectedUi;

    public NewGameScreen(World world, Game game) {
        this.world = world;
        this.game = game;
        this.cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        this.cam.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        this.cam.update();
        this.batch = new CpuSpriteBatch();
        this.inputHandler = new NewGameInputHandler<>(this.cam, world, this);
        this.assetManager = new AssetManager();
        this.assetManager.load("ui/newgame/newgame_skin.json", Skin.class);
        this.assetManager.load("ui/ui_skin.json", Skin.class);
        this.assetManager.load("ui/fonts/fonts_skin.json", Skin.class);
        this.assetManager.load("flags/flags_skin.json", Skin.class);
        this.assetManager.load("portraits/portraits_skin.json", Skin.class);
        this.assetManager.load("ui/scrollbars/scrollbars_skin.json", Skin.class);
        this.assetManager.finishLoading();
        this.skin = this.assetManager.get("ui/newgame/newgame_skin.json");
        this.skinUi = this.assetManager.get("ui/ui_skin.json");
        this.skinFonts = this.assetManager.get("ui/fonts/fonts_skin.json");
        this.skinFlags = this.assetManager.get("flags/flags_skin.json");
        this.skinPortraits = this.assetManager.get("portraits/portraits_skin.json");
        this.skinScrollbars = this.assetManager.get("ui/scrollbars/scrollbars_skin.json");
        this.uiTables = new ArrayList<>();
        this.initializeUi();
    }

    private void initializeUi() {
        this.stage = new Stage(new ScreenViewport());
        //this.stage.setDebugAll(true);

        this.cursorChanger = new CursorManager();
        this.cursorChanger.defaultCursor();
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this.stage);
        multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(multiplexer);

        this.debug = new Debug(100, 40);
        this.hoverBox = new HoverBox(this.skinUi, this.skinFonts);
        this.stage.addActor(this.hoverBox);

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        Map<String, String> localisation = this.world.getDataManager().readNewgameLocalisationCsv();
        localisation.putAll(this.world.getDataManager().readBookmarkLocalisationCsv());
        ScenarioSavegameSelector scenarioSavegameSelector = new ScenarioSavegameSelector(this.skin, this.skinFonts, this.world.getDataManager().readBookmarkJson(), localisation);
        this.uiTables.add(scenarioSavegameSelector);
        TitleBar titleBar = new TitleBar(this.skin, this.skinFonts, localisation);
        this.uiTables.add(titleBar);
        LobbyBox lobbyBox = new LobbyBox(this.skin, this.skinScrollbars, this.skinFonts, localisation);
        this.uiTables.add(lobbyBox);
        this.countrySelectedUi = new CountrySelected(this.skin, this.skinUi, this.skinFonts, localisation);
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
        this.stage.addActor(bottomTable);
        this.stage.addActor(this.debug);
    }

    public void updateCountrySelected() {
        Country country = this.world.getSelectedCountry();
        if(country != null) {
            Minister headOfState = country.getHeadOfState();
            Drawable portrait;
            try {
                portrait = this.skinPortraits.getDrawable(headOfState.getImageNameFile());
            } catch (com.badlogic.gdx.utils.GdxRuntimeException e) {
                portrait = this.skinPortraits.getDrawable("admin_type");
            }
            this.countrySelectedUi.update(country.getName(), this.skinFlags.getRegion(country.getId()),
                    ValueFormatter.formatValue(country.getPopulationSize()), country.getGovernment(), portrait, headOfState.getName());
        } else {
            this.countrySelectedUi.hide();
        }
    }

    public void updateHoverBox(LandProvince province) {
        Vector2 screenPosition = new Vector2(Gdx.input.getX(), (Gdx.graphics.getHeight() - Gdx.input.getY()));
        this.hoverBox.update(province.getName() + " (" + province.getCountryOwner().getName() + ")",
                this.skinFlags.getDrawable(province.getCountryOwner().getId()));
        this.hoverBox.setPosition(screenPosition.x + (float) this.cursorChanger.getWidth(),
                screenPosition.y - this.cursorChanger.getHeight() * 1.5f);
        this.hoverBox.setVisible(true);
    }

    public void hideHoverBox() {
        this.hoverBox.setVisible(false);
    }


    public Game getGame() {
        return this.game;
    }

    @Override
    public void show() {

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

        this.world.render(this.batch, this.cam, time);

        this.inputHandler.setDelta(delta);
        this.inputHandler.handleInput(this.uiTables);

        this.debug.actualize(renderTimeMs);

        this.stage.act();
        this.stage.draw();
    }


    @Override
    public void resize(int width, int height) {
        this.cam.viewportWidth = width / 2f;
        this.cam.viewportHeight = height / 2f;
        this.cam.update();

        this.stage.getViewport().update(width, height);
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
        this.assetManager.dispose();
    }
}
