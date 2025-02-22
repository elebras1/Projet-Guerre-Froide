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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.input.GameInputHandler;
import com.populaire.projetguerrefroide.map.MapMode;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.*;

import java.util.Map;
import java.util.stream.Collectors;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class GameScreen implements Screen, GameInputListener, MainMenuInGameListener, MinimapListener {
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
    private final Skin skinProvince;
    private final Skin skinMainMenuInGame;
    private final Skin skinMinimap;
    private final Map<String, String> localisation;
    private final Stage stage;
    private final Debug debug;
    private final ProvincePanel provincePanel;
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
        assetManager.load("ui/minimap/minimap_skin.json", Skin.class);
        assetManager.load("ui/province/province_skin.json", Skin.class);
        assetManager.finishLoading();
        this.skinUi = assetManager.get("ui/ui_skin.json");
        this.skinFlags = assetManager.get("flags/flags_skin.json");
        this.skinPopup = assetManager.get("ui/popup/popup_skin.json");
        this.skinMinimap = assetManager.get("ui/minimap/minimap_skin.json");
        this.skinProvince = assetManager.get("ui/province/province_skin.json");
        this.skinPortraits = assetManager.get("portraits/portraits_skin.json");
        this.skinScrollbars = assetManager.get("ui/scrollbars/scrollbars_skin.json");
        this.skinMainMenuInGame = assetManager.get("ui/mainmenu_ig/mainmenu_ig_skin.json");

        this.localisation = this.gameContext.getLocalisationManager().readPoliticsCsv();
        this.localisation.putAll(this.gameContext.getLocalisationManager().readMainMenuInGameCsv());
        this.localisation.putAll(this.gameContext.getLocalisationManager().readPopupCsv());
        this.localisation.putAll(this.gameContext.getLocalisationManager().readProvincesCsv());
        this.localisation.putAll(this.gameContext.getLocalisationManager().readRegionsCsv());
        this.localisation.putAll(this.gameContext.getLocalisationManager().readLanguageCsv());
        this.localisation.putAll(this.gameContext.getLocalisationManager().readInterfaceCsv());
        System.out.println(this.localisation);

        this.provincePanel = new ProvincePanel(this.skinProvince, this.skinUi, this.gameContext.getLabelStylePool(), this.localisation);
        this.debug = new Debug(this.worldService.getNumberOfProvinces());
        this.stage = new Stage(new ScreenViewport());
        this.initializeUi();

        this.paused = false;
    }

    private void initializeUi() {
        //this.stage.setDebugAll(true);

        this.multiplexer.addProcessor(this.stage);
        this.multiplexer.addProcessor(this.inputHandler);
        Gdx.input.setInputProcessor(this.multiplexer);

        this.debug.setPosition(100, 90);
        this.debug.setVisible(this.gameContext.getSettings().isDebugMode());
        this.provincePanel.setPosition(0, 0);

        this.hoverBox = new HoverBox(this.skinUi, this.gameContext.getLabelStylePool());

        Table centerTable = new Table();
        centerTable.setFillParent(true);
        this.mainMenuInGame = new MainMenuInGame(this.skinMainMenuInGame, this.skinUi, this.skinScrollbars, this.gameContext.getLabelStylePool(), this.localisation, this);
        this.mainMenuInGame.setVisible(false);
        centerTable.add(this.mainMenuInGame).center();

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        topTable.pad(5);

        Minimap minimap = new Minimap(this.skinMinimap, this.skinUi, this.gameContext.getLabelStylePool(), this.localisation, this);
        minimap.setPosition(Gdx.graphics.getWidth() - minimap.getWidth(), 0);

        this.stage.addActor(this.hoverBox);
        this.stage.addActor(topTable);
        this.stage.addActor(minimap);
        this.stage.addActor(centerTable);
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
            String mainText = this.localisation.get(String.valueOf(this.worldService.getProvinceId(x, y))) + " (" + this.worldService.getCountryNameOfHoveredProvince(x, y) + ")";
            if(this.worldService.getMapMode().equals(MapMode.CULTURAL)) {
                ObjectIntMap<String> cultures = this.worldService.getCulturesOfHoveredProvince(x, y);
                mainText = this.localisation.get(String.valueOf(this.worldService.getProvinceId(x, y))) + " (" + this.worldService.getCountryNameOfHoveredProvince(x, y) + ")";
                String subText = cultures.keySet().stream()
                    .map(culture -> this.localisation.get(culture) + " (" + cultures.get(culture) + "%)")
                    .collect(Collectors.joining("\n"));
                this.updateHoverBox(mainText, subText, this.worldService.getCountryIdOfHoveredProvince(x, y));

            } else if(this.worldService.getMapMode().equals(MapMode.RELIGIOUS)) {
                ObjectIntMap<String> religions = this.worldService.getReligionsOfHoveredProvince(x, y);
                mainText = this.localisation.get(String.valueOf(this.worldService.getProvinceId(x, y))) + " (" + this.worldService.getCountryNameOfHoveredProvince(x, y) + ")";
                String subText = religions.keySet().stream()
                    .map(religion -> this.localisation.get(religion) + " (" + religions.get(religion) + "%)")
                    .collect(Collectors.joining("\n"));
                this.updateHoverBox(mainText, subText, this.worldService.getCountryIdOfHoveredProvince(x, y));
            } else {
                this.updateHoverBox(mainText, this.worldService.getCountryIdOfHoveredProvince(x, y));
            }
        } else if(this.isMouseOverUI()) {
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

    private boolean isMouseOverUI() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return this.stage.hit(mouseX, mouseY, true) == null;
    }

    private void showProvincePanel() {
        this.provincePanel.setProvinceName(this.worldService.getSelectedProvinceId());
        this.provincePanel.setRegionName(this.worldService.getRegionIdOfSelectedProvince());
        this.provincePanel.setTerrainImage(this.worldService.getTerrainOfSelectedProvince());
        this.stage.addActor(this.provincePanel);
    }

    @Override
    public void updateHoverBox(String text) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverBox.update(text);
        this.hoverBox.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(),
                y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverBox.setVisible(true);
        this.hoverBox.toFront();
    }

    public void updateHoverBox(String text, String countryId) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverBox.update(text, this.skinFlags.getDrawable(countryId));
        this.hoverBox.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(),
                y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverBox.setVisible(true);
        this.hoverBox.toBack();
    }

    public void updateHoverBox(String mainText, String subText, String countryId) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();
        this.hoverBox.update(mainText, subText, this.skinFlags.getDrawable(countryId));
        this.hoverBox.setPosition(x + (float) this.gameContext.getCursorManager().getWidth(),
            y - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverBox.setVisible(true);
        this.hoverBox.toBack();
    }

    @Override
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
            this.inputHandler.handleInput();
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
