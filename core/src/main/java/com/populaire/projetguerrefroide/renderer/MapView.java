package com.populaire.projetguerrefroide.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.dao.impl.MapDaoImpl;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.pojo.MapTextures;
import com.populaire.projetguerrefroide.protocol.SimulationChannel;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.catalog.ProvinceTable;
import com.populaire.projetguerrefroide.protocol.request.Request;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.request.Response;
import com.populaire.projetguerrefroide.protocol.response.MapBuildings;
import com.populaire.projetguerrefroide.protocol.response.MapModeColors;
import com.populaire.projetguerrefroide.protocol.response.PoliticalMap;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.UiState;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public final class MapView implements Disposable {
    private static final int PROVINCE_DATA_SIZE = 65536 * 8;
    private static final MapMode[] MAP_MODES = MapMode.values();
    private final Catalog catalog;
    private final UiCatalog uiCatalog;
    private final UiContext uiContext;
    private final UiState uiState;
    private final Pixmap provincesPixmap;
    private final Pixmap mapModePixmap;
    private final Pixmap provincesColorStripesPixmap;
    private final IntBuffer mapModeColors;
    private final IntBuffer stripesColors;
    private final ByteBuffer provincesData;
    private final BordersCompute bordersCompute;
    private final RequestWatch<PoliticalMap> politicalMapWatch;
    private final RequestWatch<MapModeColors> mapModeWatch;
    private final RequestWatch<MapBuildings> mapBuildingsWatch;
    private final MapBuildings appliedBuildings;
    private MapTextures mapTextures;
    private MapRenderer mapRenderer;
    private MapMode appliedMapMode;
    private boolean politicalMapApplied;
    private boolean buildingsApplied;
    private boolean bordersRequested;
    private boolean bordersDirty;
    private boolean mapModeApplied;

    public MapView(SimulationChannel channel, UiCatalog uiCatalog, UiContext uiContext, UiState uiState) {
        this.catalog = uiCatalog.catalog();
        this.uiCatalog = uiCatalog;
        this.uiContext = uiContext;
        this.uiState = uiState;
        this.mapModePixmap = this.createEmptyPixmap();
        this.provincesColorStripesPixmap = this.createEmptyPixmap();
        this.mapModeColors = this.mapModePixmap.getPixels().duplicate().order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        this.stripesColors = this.provincesColorStripesPixmap.getPixels().duplicate().order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        this.provincesPixmap = this.createProvincesPixmap();
        this.bordersCompute = new BordersCompute(this.provincesPixmap.getPixels());
        this.provincesData = BufferUtils.newByteBuffer(PROVINCE_DATA_SIZE);
        this.provincesData.order(ByteOrder.LITTLE_ENDIAN);
        this.appliedBuildings = new MapBuildings();
        this.politicalMapWatch = new RequestWatch<>(new Request<>(RequestKind.POLITICAL_MAP, new PoliticalMap(), new PoliticalMap()), channel.requests(), channel.header(),
            () -> true, params -> {
            }, RequestWatch.Staleness.OWNERSHIP, this::applyPoliticalMap);
        this.mapBuildingsWatch = new RequestWatch<>(new Request<>(RequestKind.MAP_BUILDINGS, new MapBuildings(), new MapBuildings()), channel.requests(), channel.header(),
            () -> true, params -> {
            }, RequestWatch.Staleness.OWNERSHIP_OR_MONTHLY, this::applyMapBuildings);
        this.mapModeWatch = new RequestWatch<>(new Request<>(RequestKind.MAP_MODE_COLORS, new MapModeColors(), new MapModeColors()), channel.requests(), channel.header(),
            () -> true, params -> params.option(this.uiState.mapMode().ordinal()), MapView::mapModeStale, this::applyMapModeColors);
    }

    private static boolean mapModeStale(Response response, SimulationHeader header) {
        MapMode mode = MAP_MODES[response.params().option()];
        return switch (mode) {
            case TERRAIN, TERRAIN_2, RESOURCES, REGION -> false;
            case POLITICAL, RELATIONS -> RequestWatch.Staleness.OWNERSHIP.stale(response, header);
            case IDEOLOGICAL, CULTURAL, RELIGIOUS, POPULATION -> RequestWatch.Staleness.MONTHLY.stale(response, header);
        };
    }

    private Pixmap createEmptyPixmap() {
        Pixmap pixmap = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        return pixmap;
    }

    private Pixmap createProvincesPixmap() {
        Pixmap tempProvincesPixmap = new Pixmap(Gdx.files.internal("map/provinces.bmp"));
        Pixmap pixmap = new Pixmap(tempProvincesPixmap.getWidth(), tempProvincesPixmap.getHeight(), Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.drawPixmap(tempProvincesPixmap, 0, 0);
        tempProvincesPixmap.dispose();
        return pixmap;
    }

    public void update() {
        this.politicalMapWatch.update();
        this.mapBuildingsWatch.update();
        this.mapModeWatch.update();
        if (this.mapRenderer == null && this.politicalMapApplied && this.buildingsApplied) {
            this.createRenderer();
        }
    }

    public boolean isReady() {
        return this.mapRenderer != null && this.mapModeApplied;
    }

    public PoliticalMap politicalMap() {
        return this.politicalMapWatch.response();
    }

    private void createRenderer() {
        PoliticalMap politicalMap = this.politicalMapWatch.response();
        this.mapTextures = this.createMapTextures();
        this.mapRenderer = new MapRenderer(this.catalog, this.uiContext, new MapDaoImpl(), this.mapTextures, politicalMap, this.appliedBuildings, this.uiCatalog::countryName);
        this.bordersRequested = false;
        this.updateBorders(politicalMap);
        if (this.mapModeApplied) {
            this.mapRenderer.updateMapMode(this.appliedMapMode, this.mapModePixmap);
        }
    }

    private void applyPoliticalMap(PoliticalMap politicalMap) {
        boolean namesChanged = this.uiCatalog.applyPoliticalMap(politicalMap);
        this.stripesColors.clear();
        for (int i = 0; i < MapModeColors.SIZE; i++) {
            this.stripesColors.put(i, 0);
        }
        ProvinceTable provinces = this.catalog.provinces();
        for (int provinceIndex = 0; provinceIndex < provinces.size(); provinceIndex++) {
            int owner = politicalMap.owner(provinceIndex);
            int controller = politicalMap.controller(provinceIndex);
            if (owner == controller || controller < 0) {
                continue;
            }
            int color = provinces.color(provinceIndex);
            int red = (color >> 24) & 0xFF;
            int green = (color >> 16) & 0xFF;
            this.stripesColors.put(green * 256 + red, this.catalog.countries().color(controller));
        }
        if (this.mapRenderer != null) {
            this.mapRenderer.updateProvincesStripesTexture(this.provincesColorStripesPixmap);
            this.mapRenderer.rebuildLabels(politicalMap, this.uiCatalog::countryName);
            this.mapRenderer.rebuildBuildings(politicalMap, this.appliedBuildings);
            this.updateBorders(politicalMap);
        }
        this.politicalMapApplied = true;
    }

    private void applyMapBuildings(MapBuildings mapBuildings) {
        if (this.buildingsApplied && this.appliedBuildings.sameContent(mapBuildings)) {
            return;
        }
        this.appliedBuildings.copyFrom(mapBuildings);
        this.buildingsApplied = true;
        if (this.mapRenderer != null && this.politicalMapWatch.response() != null) {
            this.mapRenderer.rebuildBuildings(this.politicalMapWatch.response(), this.appliedBuildings);
        }
    }

    private void applyMapModeColors(MapModeColors colors) {
        this.mapModeColors.clear();
        this.mapModeColors.put(colors.colors());
        this.appliedMapMode = MAP_MODES[colors.mode()];
        this.mapModeApplied = true;
        if (this.mapRenderer != null) {
            this.mapRenderer.updateMapMode(this.appliedMapMode, this.mapModePixmap);
        }
    }

    private void updateBorders(PoliticalMap politicalMap) {
        if (this.bordersRequested) {
            this.bordersDirty = true;
            return;
        }
        this.bordersDirty = false;
        ProvinceTable provinces = this.catalog.provinces();
        this.provincesData.clear();
        for (int i = 0; i < PROVINCE_DATA_SIZE; i++) {
            this.provincesData.put(i, (byte) 0);
        }
        for (int provinceIndex = 0; provinceIndex < provinces.size(); provinceIndex++) {
            int color = provinces.color(provinceIndex);
            int red = (color >> 24) & 0xFF;
            int green = (color >> 16) & 0xFF;
            int key = red + green * 256;
            this.provincesData.putInt(key * 8, politicalMap.owner(provinceIndex) + 1);
            this.provincesData.putInt(key * 8 + 4, provinces.regionIndex(provinceIndex) + 1);
        }
        this.provincesData.position(0);
        this.provincesData.limit(PROVINCE_DATA_SIZE);
        this.bordersRequested = true;
        this.bordersCompute.updateAndRunAsync(this.provincesData, resultBuffer -> {
            ByteBuffer provincesBuffer = this.provincesPixmap.getPixels();
            provincesBuffer.clear();
            provincesBuffer.put(resultBuffer);
            provincesBuffer.flip();
            this.mapRenderer.updateProvincesTexture(this.provincesPixmap);
            this.bordersRequested = false;
            if (this.bordersDirty && this.politicalMapWatch.response() != null) {
                this.updateBorders(this.politicalMapWatch.response());
            }
        });
    }

    private MapTextures createMapTextures() {
        PixmapTextureData mapModeTextureData = new PixmapTextureData(this.mapModePixmap, Pixmap.Format.RGBA8888, false, false);
        WgTexture mapModeTexture = new WgTexture(mapModeTextureData, "mapModeTexture", false);
        mapModeTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);

        PixmapTextureData provincesColorStripesTextureData = new PixmapTextureData(this.provincesColorStripesPixmap, Pixmap.Format.RGBA8888, false, false);
        WgTexture provincesStripesTexture = new WgTexture(provincesColorStripesTextureData, "provincesStripesTexture", false);
        provincesStripesTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);

        PixmapTextureData provincesTextureData = new PixmapTextureData(this.provincesPixmap, Pixmap.Format.RGBA8888, false, false);
        WgTexture provincesTexture = new WgTexture(provincesTextureData, "provincesTexture", false);
        provincesTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);

        WgTexture waterTexture = new WgTexture("map/terrain/sea_normal.png");
        waterTexture.setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);
        waterTexture.setWrap(WgTexture.TextureWrap.Repeat, WgTexture.TextureWrap.Repeat);

        Pixmap colorMapWaterPixmap = new Pixmap(Gdx.files.internal("map/terrain/colormap_water.png"));
        WgTexture colorMapWaterTexture = new WgTexture(colorMapWaterPixmap, "colorMapWaterTexture", false);
        colorMapWaterPixmap.dispose();
        colorMapWaterTexture.setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);

        WgTexture colorMapTexture = new WgTexture("map/terrain/colormap.png");
        colorMapTexture.setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);

        Pixmap terrainPixmap = new Pixmap(Gdx.files.internal("map/terrain.bmp"));
        PixmapTextureData terrainTextureData = new PixmapTextureData(terrainPixmap, Pixmap.Format.RGBA8888, false, false);
        WgTexture terrainTexture = new WgTexture(terrainTextureData, "terrainTexture", false);
        terrainPixmap.dispose();

        WgTexture stripesTexture = new WgTexture("map/terrain/stripes.png");

        WgTexture overlayTileTexture = new WgTexture("map/terrain/map_overlay_tile.png");
        overlayTileTexture.setWrap(WgTexture.TextureWrap.Repeat, WgTexture.TextureWrap.Repeat);

        WgTexture riverBodyTexture = new WgTexture("map/terrain/river.png");
        riverBodyTexture.setWrap(WgTexture.TextureWrap.Repeat, WgTexture.TextureWrap.Repeat);

        FileHandle[] terrainSheetFiles = this.createTerrainTextureFiles();

        return new MapTextures(mapModeTexture, provincesTexture, waterTexture, colorMapWaterTexture, provincesStripesTexture, terrainTexture, stripesTexture, colorMapTexture, overlayTileTexture, riverBodyTexture, terrainSheetFiles);
    }

    private FileHandle[] createTerrainTextureFiles() {
        FileHandle[] terrainTexturePaths = new FileHandle[64];
        String pathBase = "map/terrain/textures/";
        for (int i = 0; i < 64; i++) {
            terrainTexturePaths[i] = Gdx.files.internal(pathBase + "text_" + i + ".png");
        }
        return terrainTexturePaths;
    }

    public int provinceIndexAt(int x, int y) {
        x = (x + WORLD_WIDTH) % WORLD_WIDTH;
        if (y < 0 || y >= this.provincesPixmap.getHeight()) {
            return -1;
        }
        int provinceColor = this.provincesPixmap.getPixel(x, y);
        int provinceColorRGB = (provinceColor & 0xFFFFFF00) | 255;
        return this.catalog.provinces().indexOfColor(provinceColorRGB);
    }

    public void highlightProvince(int provinceIndex) {
        if (this.mapRenderer == null) {
            return;
        }
        if (provinceIndex < 0) {
            this.mapRenderer.updateSelectedProvince(0f, 0f, 0f, 0f);
            return;
        }
        int color = this.catalog.provinces().color(provinceIndex);
        float r = ((color >> 24) & 0xFF) / 255f;
        float g = ((color >> 16) & 0xFF) / 255f;
        float b = ((color >> 8) & 0xFF) / 255f;
        float a = (color & 0xFF) / 255f;
        this.mapRenderer.updateSelectedProvince(r, g, b, a);
    }

    public int capitalOf(int countryIndex) {
        PoliticalMap politicalMap = this.politicalMapWatch.response();
        return politicalMap == null || countryIndex < 0 ? -1 : politicalMap.capital(countryIndex);
    }

    public int ownerOf(int provinceIndex) {
        PoliticalMap politicalMap = this.politicalMapWatch.response();
        return politicalMap == null || provinceIndex < 0 ? -1 : politicalMap.owner(provinceIndex);
    }

    public MapMode appliedMapMode() {
        return this.appliedMapMode;
    }

    public void render(WgProjection projection, OrthographicCamera cam, float time) {
        if (this.mapRenderer == null) {
            return;
        }
        this.mapRenderer.render(projection, cam, time, this.appliedMapMode == null ? MapMode.POLITICAL : this.appliedMapMode);
    }

    @Override
    public void dispose() {
        if (this.mapRenderer != null) {
            this.mapRenderer.dispose();
        }
        this.mapModePixmap.dispose();
        this.provincesPixmap.dispose();
        this.provincesColorStripesPixmap.dispose();
    }
}
