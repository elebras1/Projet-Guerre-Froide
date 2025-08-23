package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.*;
import com.github.xpenatan.webgpu.*;
import com.monstrous.gdx.webgpu.graphics.Binder;
import com.monstrous.gdx.webgpu.graphics.WgMesh;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.graphics.WgTextureArray;
import com.monstrous.gdx.webgpu.graphics.g2d.WgTextureAtlas;
import com.monstrous.gdx.webgpu.wrappers.*;
import com.populaire.projetguerrefroide.adapter.graphics.WgMeshMulti;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.dao.impl.MapDaoImpl;
import com.populaire.projetguerrefroide.economy.Economy;
import com.populaire.projetguerrefroide.entity.ModifierStore;
import com.populaire.projetguerrefroide.entity.RawMeshMulti;
import com.populaire.projetguerrefroide.entity.Terrain;
import com.populaire.projetguerrefroide.national.NationalIdeas;
import com.populaire.projetguerrefroide.politics.AllianceType;
import com.populaire.projetguerrefroide.politics.Politics;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.util.ColorGenerator;
import com.populaire.projetguerrefroide.util.LocalisationUtils;
import com.populaire.projetguerrefroide.util.WebGPUHelper;
import com.populaire.projetguerrefroide.util.WgslUtils;

import java.util.*;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class World implements Disposable {
    private final MapDaoImpl mapDao;
    private final List<Country> countries;
    private final IntObjectMap<LandProvince> provinces;
    private final IntObjectMap<WaterProvince> waterProvinces;
    private final ProvinceStore provinceStore;
    private final RegionStore regionStore;
    private final ModifierStore modifierStore;
    private final Economy economy;
    private final Politics politics;
    private final NationalIdeas nationalIdeas;
    private final Map<String, Terrain> terrains;
    private final Pixmap provincesPixmap;
    private final Pixmap mapModePixmap;
    private final Pixmap provincesColorStripesPixmap;
    private WgTexture mapModeTexture;
    private final WgTexture provincesTexture;
    private final WgTexture waterTexture;
    private final WgTexture colorMapWaterTexture;
    private final WgTexture provincesStripesTexture;
    private final WgTexture terrainTexture;
    private final WgTexture stripesTexture;
    private final WgTexture colorMapTexture;
    private final WgTexture overlayTileTexture;
    private final WgTexture riverBodyTexture;
    private final WgTextureArray terrainSheetArray;
    private final WgTextureAtlas mapElementsTextureAtlas;
    private final TextureRegion fontMapLabelRegion;
    private final WgMesh meshProvinces;
    private final WgMesh meshMapLabels;
    private final WgMesh meshBuildings;
    private final WgMesh meshResources;
    private final WgMeshMulti meshRivers;
    private final WebGPUUniformBuffer uniformBufferProvinces;
    private final WebGPUUniformBuffer uniformBufferMapLabels;
    private final WebGPUUniformBuffer uniformBufferBuildings;
    private final WebGPUUniformBuffer uniformBufferResources;
    private final WebGPUUniformBuffer uniformBufferRivers;
    private final Binder binderProvinces;
    private final Binder binderMapLabels;
    private final Binder binderBuildings;
    private final Binder binderResources;
    private final Binder binderRivers;
    private final WebGPUPipeline pipelineProvinces;
    private final WebGPUPipeline pipelineMapLabels;
    private final WebGPUPipeline pipelineBuildings;
    private final WebGPUPipeline pipelineResources;
    private final WebGPUPipeline pipelineRivers;
    private final int uniformBufferSizeProvinces;
    private final int uniformBufferSizeMapLabels;
    private final int uniformBufferSizeBuildings;
    private final int uniformBufferSizeResources;
    private  final int uniformBufferSizeRivers;
    private LandProvince selectedProvince;
    private Country countryPlayer;
    private MapMode mapMode;

    public World(List<Country> countries, IntObjectMap<LandProvince> provinces, IntObjectMap<WaterProvince> waterProvinces, ProvinceStore provinceStore, RegionStore regionStore, ModifierStore modifierStore, Economy economy, Politics politics, NationalIdeas nationalIdeas, Map<String, Terrain> terrains, GameContext gameContext) {
        this.mapDao = new MapDaoImpl();
        this.countries = countries;
        this.provinces = provinces;
        this.waterProvinces = waterProvinces;
        this.provinceStore = provinceStore;
        this.regionStore = regionStore;
        this.modifierStore = modifierStore;
        this.economy = economy;
        this.politics = politics;
        this.nationalIdeas = nationalIdeas;
        this.terrains = terrains;
        this.mapModePixmap = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        this.mapModePixmap.setColor(0, 0, 0, 0);
        this.mapModePixmap.fill();
        Pixmap tempProvincesPixmap = new Pixmap(Gdx.files.internal("map/provinces.bmp"));
        this.provincesPixmap = new Pixmap(tempProvincesPixmap.getWidth(), tempProvincesPixmap.getHeight(), Pixmap.Format.RGBA8888);
        this.provincesPixmap.setBlending(Pixmap.Blending.None);
        this.provincesPixmap.drawPixmap(tempProvincesPixmap, 0, 0);
        tempProvincesPixmap.dispose();
        this.updatePixmapCountriesColor();
        this.updateBordersProvincesPixmap();
        this.provincesColorStripesPixmap = this.createProvincesColorStripesPixmap();
        FileHandle[] terrainTextureFiles = this.createTerrainTextureFiles();
        this.mapMode = MapMode.POLITICAL;

        PixmapTextureData mapModeTextureData = new PixmapTextureData(this.mapModePixmap, Pixmap.Format.RGBA8888, false, false);
        this.mapModeTexture = new WgTexture(mapModeTextureData, "mapModeTexture", false);
        this.mapModeTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        PixmapTextureData provincesColorStripesTextureData = new PixmapTextureData(provincesColorStripesPixmap, Pixmap.Format.RGBA8888, false, false);
        this.provincesStripesTexture = new WgTexture(provincesColorStripesTextureData, "provincesStripesTexture", false);
        this.provincesStripesTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        PixmapTextureData provincesTextureData = new PixmapTextureData(this.provincesPixmap, Pixmap.Format.RGBA8888, false, false);
        this.provincesTexture = new WgTexture(provincesTextureData, "provincesTexture", false);
        this.provincesTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        this.waterTexture = new WgTexture("map/terrain/sea_normal.png");
        this.waterTexture.setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);
        this.waterTexture.setWrap(WgTexture.TextureWrap.Repeat, WgTexture.TextureWrap.Repeat);
        Pixmap colorMapWaterPixmap = new Pixmap(Gdx.files.internal("map/terrain/colormap_water.png"));
        this.colorMapWaterTexture = new WgTexture(colorMapWaterPixmap, "colorMapWaterTexture", false);
        colorMapWaterPixmap.dispose();
        this.colorMapWaterTexture.setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);
        this.colorMapTexture = new WgTexture("map/terrain/colormap.png");
        this.colorMapTexture.setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);
        Pixmap terrainPixmap = new Pixmap(Gdx.files.internal("map/terrain.bmp"));
        PixmapTextureData terrainTextureData = new PixmapTextureData(terrainPixmap, Pixmap.Format.RGBA8888, false, false);
        this.terrainTexture = new WgTexture(terrainTextureData, "terrainTexture", false);
        terrainPixmap.dispose();
        this.stripesTexture = new WgTexture("map/terrain/stripes.png");
        this.overlayTileTexture = new WgTexture("map/terrain/map_overlay_tile.png");
        this.overlayTileTexture.setWrap(WgTexture.TextureWrap.Repeat, WgTexture.TextureWrap.Repeat);
        this.riverBodyTexture = new WgTexture("map/terrain/river.png");
        this.riverBodyTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.terrainSheetArray = new WgTextureArray(terrainTextureFiles);
        this.mapElementsTextureAtlas = new WgTextureAtlas(Gdx.files.internal("map/elements/map_elements.atlas"));
        this.mapElementsTextureAtlas.getTextures().first().setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
        this.fontMapLabelRegion = gameContext.getLabelStylePool().getLabelStyle("kart_60").font.getRegion();
        this.fontMapLabelRegion.getTexture().setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);
        VertexAttributes vertexAttributesProvinces = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.meshProvinces = this.generateMeshProvinces(vertexAttributesProvinces);
        this.uniformBufferSizeProvinces = (16 + 4 + 4) * Float.BYTES;
        this.uniformBufferProvinces = new WebGPUUniformBuffer(this.uniformBufferSizeProvinces, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        this.binderProvinces = this.createBinderProvinces();
        this.pipelineProvinces = this.createPipelineProvinces(vertexAttributesProvinces, WgslUtils.getShaderSource("map.wgsl"));
        VertexAttributes vertexAttributesMapLabels = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.meshMapLabels = this.generateMeshMapLabels(vertexAttributesMapLabels, gameContext.getLocalisation(), gameContext.getLabelStylePool());
        this.uniformBufferSizeMapLabels = (16 + 4) * Float.BYTES;
        this.uniformBufferMapLabels = new WebGPUUniformBuffer(this.uniformBufferSizeMapLabels, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        this.binderMapLabels = this.createBinderMapLabels();
        this.pipelineMapLabels = this.createPipelineMapLabels(vertexAttributesMapLabels, WgslUtils.getShaderSource("font.wgsl"));
        VertexAttributes vertexAttributesBuildings = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.meshBuildings = this.generateMeshBuildings(vertexAttributesBuildings);
        this.uniformBufferSizeBuildings = (16 + 4) * Float.BYTES;
        this.uniformBufferBuildings = new WebGPUUniformBuffer(this.uniformBufferSizeBuildings, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        this.binderBuildings = this.createBinderBuildings();
        this.pipelineBuildings = this.createPipelineBuildings(vertexAttributesBuildings, WgslUtils.getShaderSource("element.wgsl"));
        VertexAttributes vertexAttributesResources = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.Normal, 2, "center"));
        this.meshResources = this.generateMeshResources(vertexAttributesResources);
        this.uniformBufferSizeResources = (16 + 4) * Float.BYTES;
        this.uniformBufferResources = new WebGPUUniformBuffer(this.uniformBufferSizeResources, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        this.binderResources = this.createBinderResources();
        this.pipelineResources = this.createPipelineResources(vertexAttributesResources, WgslUtils.getShaderSource("element_scale.wgsl"));
        VertexAttributes vertexAttributesRivers = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.Normal, 1, "width"));
        this.meshRivers = this.generateMeshRivers(vertexAttributesRivers);
        this.uniformBufferSizeRivers = (16 + 4 + 4) * Float.BYTES;
        this.uniformBufferRivers = new WebGPUUniformBuffer(this.uniformBufferSizeRivers, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        this.binderRivers = this.createBinderRivers();
        this.pipelineRivers = this.createPipelineRivers(vertexAttributesRivers, WgslUtils.getShaderSource("river.wgsl"));
        this.bindStaticTextures();

    }

    public ProvinceStore getProvinceStore() {
        return this.provinceStore;
    }

    public RegionStore getRegionStore() {
        return this.regionStore;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public Politics getPolitics() {
        return this.politics;
    }

    public NationalIdeas getNationalIdeas() {
        return  this.nationalIdeas;
    }

    public LandProvince getProvince(short x, short y) {
        x = (short) ((x + WORLD_WIDTH) % WORLD_WIDTH);

        int provinceColor = this.provincesPixmap.getPixel(x, y);
        int provinceColorRGB = (provinceColor & 0xFFFFFF00) | 255;

        return this.provinces.get(provinceColorRGB);
    }

    public boolean selectProvince(short x, short y) {
        this.selectedProvince = this.getProvince(x, y);
        return this.selectedProvince != null;
    }

    public LandProvince getSelectedProvince() {
        return this.selectedProvince;
    }

    public Country getCountryPlayer() {
        return this.countryPlayer;
    }

    public boolean setCountryPlayer() {
        if(this.selectedProvince != null) {
            this.countryPlayer = this.selectedProvince.getCountryOwner();
            return true;
        }

        return false;
    }

    public MapMode getMapMode() {
        return this.mapMode;
    }

    public short getNumberOfProvinces() {
        return (short) this.provinces.size();
    }

    public int getPopulationAmount(Province province) {
        int provinceId = province.getId();
        int provinceIndex = this.provinceStore.getIndexById().get(provinceId);
        return this.provinceStore.getPopulationAmount(provinceIndex);
    }

    public int getPopulationAmount(Country country) {
        return this.economy.getPopulationAmount(this.provinceStore, country);
    }

    public String getColonizerId(Country country) {
        if(country.getAlliances() == null) {
            return null;
        }

        for(Map.Entry<Country, AllianceType> alliances : country.getAlliances().entrySet()) {
            if(alliances.getValue() == AllianceType.COLONY) {
                return alliances.getKey().getId();
            }
        }

        return null;
    }

    public String getResourceGoodName(Province province) {
        int provinceId = province.getId();
        int provinceIndex = this.provinceStore.getIndexById().get(provinceId);
        int resourceGoodId = this.provinceStore.getResourceGoodIds().get(provinceIndex);
        if(resourceGoodId != -1) {
            return this.economy.getGoodStore().getNames().get(resourceGoodId);
        }
        return null;
    }

    public int getAmountAdults(Province province) {
        int provinceId = province.getId();
        int provinceIndex = this.provinceStore.getIndexById().get(provinceId);
        return this.provinceStore.getAmountAdults().get(provinceIndex);
    }

    private FileHandle[] createTerrainTextureFiles() {
        FileHandle[] terrainTexturePaths = new FileHandle[64];
        String pathBase = "map/terrain/textures/";
        for(int i = 0; i < 64; i++) {
            terrainTexturePaths[i] = Gdx.files.internal(pathBase + "text_" + i + ".png");
        }

        return terrainTexturePaths;
    }

    private void updatePixmapCountriesColor() {
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);

            Country country = Objects.requireNonNull(this.provinces.get(color)).getCountryOwner();
            int countryColor = country.getColor();
            if(country.getAlliances() != null) {
                for(Map.Entry<Country, AllianceType> alliance : country.getAlliances().entrySet()) {
                    if(alliance.getValue() == AllianceType.COLONY) {
                        countryColor = alliance.getKey().getColor();
                        break;
                    }
                }
            }
            this.mapModePixmap.drawPixel(red, green, countryColor);

        }
    }

    private void updatePixmapIdeologiesColor() {
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, Objects.requireNonNull(this.provinces.get(color)).getCountryOwner().getIdeology().getColor());
        }
    }

    private void updatePixmapCulturesColor() {
        IntList provinceColors = this.provinceStore.getColors();
        IntList provinceCultureValues = this.provinceStore.getCultureValues();
        IntList provinceCultureStarts = this.provinceStore.getCultureStarts();
        IntList provinceCultureCounts = this.provinceStore.getCultureCounts();
        IntList provinceCultureIds = this.provinceStore.getCultureIds();

        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            int provinceCultureStart = provinceCultureStarts.get(provinceId);
            int biggestCultureIndex = -1;
            for(int cultureIndex = provinceCultureStart; cultureIndex < provinceCultureStart + provinceCultureCounts.get(provinceId); cultureIndex++) {
                if(biggestCultureIndex == -1 || provinceCultureValues.get(cultureIndex) > provinceCultureValues.get(biggestCultureIndex)) {
                    biggestCultureIndex = cultureIndex;
                }
            }
            if(biggestCultureIndex != -1) {
                int biggestCultureId = provinceCultureIds.get(biggestCultureIndex);
                this.mapModePixmap.drawPixel(red, green, this.nationalIdeas.getCultureStore().getColors().get(biggestCultureId));
            }
        }
    }

    private void updatePixmapReligionsColor() {
        IntList provinceColors = this.provinceStore.getColors();
        IntList provinceReligionValues = this.provinceStore.getReligionValues();
        IntList provinceReligionStarts = this.provinceStore.getReligionStarts();
        IntList provinceReligionCounts = this.provinceStore.getReligionCounts();
        IntList provinceReligionIds = this.provinceStore.getReligionIds();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            int provinceReligionStart = provinceReligionStarts.get(provinceId);
            int biggestReligionIndex = -1;
            for(int religionIndex = provinceReligionStart; religionIndex < provinceReligionStart + provinceReligionCounts.get(provinceId); religionIndex++) {
                if(biggestReligionIndex == -1 || provinceReligionValues.get(religionIndex) > provinceReligionValues.get(biggestReligionIndex)) {
                    biggestReligionIndex = religionIndex;
                }
            }
            if(biggestReligionIndex != -1) {
                int biggestReligionId = provinceReligionIds.get(biggestReligionIndex);
                this.mapModePixmap.drawPixel(red, green, this.nationalIdeas.getReligionStore().getColors().get(biggestReligionId));
            }
        }
    }

    private void updatePixmapResourcesColor() {
        IntList provinceColors = this.provinceStore.getColors();
        IntList provinceResourceGoodIds = this.provinceStore.getResourceGoodIds();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            int provinceResourceGoodId = provinceResourceGoodIds.get(provinceId);
            if(provinceResourceGoodId != -1) {
                this.mapModePixmap.drawPixel(red, green, this.economy.getGoodStore().getColors().get(provinceResourceGoodId));
            } else {
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getWhiteRGBA());
            }
        }
    }

    private void updatePixmapRegionColor() {
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getDeterministicRGBA(Objects.requireNonNull(this.provinces.get(color)).getRegion().getId()));
        }
    }

    private void updatePixmapTerrainColor() {
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getWhiteRGBA());
        }
    }

    public void updatePixmapTerrain2Color() {
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, Objects.requireNonNull(this.provinces.get(color)).getTerrain().getColor());
        }
    }

    private void updatePixmapPopulationColor() {
        int maxPopulation = 0;
        for (int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            maxPopulation = Math.max(maxPopulation, this.provinceStore.getPopulationAmount(provinceId));
        }

        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            int pop = this.provinceStore.getPopulationAmount(provinceId);
            float ratio = (maxPopulation > 0) ? (float) pop / maxPopulation : 0f;
            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getMagmaColorRGBA(ratio));
        }
    }


    private void updatePixmapRelationsColor() {
        ObjectIntMap<Country> relations = this.countryPlayer.getRelations();

        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);

            LandProvince province = this.provinces.get(color);
            if(Objects.requireNonNull(province).getCountryOwner().equals(this.countryPlayer)) {
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getLightBlueRGBA());
            } else if(relations != null && relations.containsKey(province.getCountryOwner())) {
                int relationValue = relations.get(province.getCountryOwner());
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getRedToGreenGradientRGBA(relationValue, 200));
            } else {
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getGreyRGBA());
            }
        }
    }

    private Pixmap createProvincesColorStripesPixmap() {
        Pixmap pixmap = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        for(LandProvince province : this.provinces.values()) {
            if(!province.getCountryOwner().equals(province.getCountryController())) {
                int provinceIndex = this.provinceStore.getIndexById().get(province.getId());
                int color = this.provinceStore.getColors().get(provinceIndex);
                short red = (short) ((color >> 24) & 0xFF);
                short green = (short) ((color >> 16) & 0xFF);
                pixmap.drawPixel(red, green, province.getCountryController().getColor());
            }
        }

        return pixmap;
    }

    private void updateBordersProvincesPixmap() {
        for(LandProvince province : this.provinces.values()) {
            IntSet provincesBorderPixels = province.getBorderPixels();
            for(IntSet.IntSetIterator iterator = provincesBorderPixels.iterator(); iterator.hasNext;) {
                int borderPixel = iterator.next();
                short x = (short) (borderPixel >> 16);
                short y = (short) (borderPixel & 0xFFFF);

                int color = this.provincesPixmap.getPixel(x, y);
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;
                int blue = (color >> 8) & 0xFF;

                color = (red << 24) | (green << 16) | (blue << 8) | this.getBorderType(x, y, province.getCountryOwner(), province.getRegion());
                this.provincesPixmap.drawPixel(x, y, color);
            }
        }
    }

    public void changeMapMode(String mapMode) {
        switch(mapMode) {
            case "mapmode_political":
                this.updatePixmapCountriesColor();
                this.mapMode = MapMode.POLITICAL;
                break;
            case "mapmode_strength":
                this.updatePixmapIdeologiesColor();
                this.mapMode = MapMode.IDEOLOGICAL;
                break;
            case "mapmode_diplomatic":
                this.updatePixmapCulturesColor();
                this.mapMode = MapMode.CULTURAL;
                break;
            case "mapmode_intel":
                this.updatePixmapReligionsColor();
                this.mapMode = MapMode.RELIGIOUS;
                break;
            case "mapmode_terrain":
                this.updatePixmapTerrainColor();
                this.mapMode = MapMode.TERRAIN;
                break;
            case "mapmode_terrain_2":
                this.updatePixmapTerrain2Color();
                this.mapMode = MapMode.TERRAIN_2;
                break;
            case "mapmode_resources":
                this.updatePixmapResourcesColor();
                this.mapMode = MapMode.RESOURCES;
                break;
            case "mapmode_region":
                this.updatePixmapRegionColor();
                this.mapMode = MapMode.REGION;
                break;
            case "mapmode_theatre":
                this.updatePixmapRelationsColor();
                this.mapMode = MapMode.RELATIONS;
                break;
            case "mapmode_weather":
                this.updatePixmapPopulationColor();
                this.mapMode = MapMode.POPULATION;
                break;
        }

        this.mapModeTexture.dispose();
        PixmapTextureData mapModeTextureData = new PixmapTextureData(this.mapModePixmap, Pixmap.Format.RGBA8888, false, false);
        this.mapModeTexture = new WgTexture(mapModeTextureData, "mapModeTexture", false);
        this.mapModeTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        this.binderProvinces.setTexture("textureMapMode", this.mapModeTexture.getTextureView());
        this.binderProvinces.setSampler("textureMapModeSampler", this.mapModeTexture.getSampler());
        this.uniformBufferProvinces.flush();
    }

    private short getBorderType(short x, short y, Country country, Region region) {
        LandProvince provinceRight = this.getProvince((short) (x + 1), y);
        LandProvince provinceLeft = this.getProvince((short) (x - 1), y);
        LandProvince provinceUp = this.getProvince(x, (short) (y + 1));
        LandProvince provinceDown = this.getProvince(x, (short) (y - 1));

        // 0: water, nothing or province border, 153: country border, 77: region border
        if(provinceRight == null || provinceLeft == null || provinceUp == null || provinceDown == null) {
            return 0;
        } else if (!provinceRight.getCountryOwner().equals(country) || !provinceLeft.getCountryOwner().equals(country) || !provinceUp.getCountryOwner().equals(country) || !provinceDown.getCountryOwner().equals(country)) {
            return 153;
        } else if (!region.equals(provinceRight.getRegion()) || !region.equals(provinceLeft.getRegion()) || !region.equals(provinceUp.getRegion()) || !region.equals(provinceDown.getRegion())) {
            return 77;
        } else {
            return 0;
        }
    }

    private WgMesh generateMeshProvinces(VertexAttributes vertexAttributes) {
        float[] vertices = new float[] {
            0, 0, 0, 0,
            WORLD_WIDTH, 0, 1, 0,
            WORLD_WIDTH, WORLD_HEIGHT, 1, 1,
            0, WORLD_HEIGHT, 0, 1
        };

        short[] indices = new short[] {
            0, 1, 2,
            2, 3, 0
        };

        WgMesh mesh = new WgMesh(true, vertices.length / 4, indices.length, vertexAttributes);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    private WgMesh generateMeshMapLabels(VertexAttributes vertexAttributes, Map<String, String> localisation, LabelStylePool labelStylePool) {
        MapLabel mapLabel = new MapLabel(labelStylePool.getLabelStyle("kart_60").font);
        FloatList vertices = new FloatList();
        ShortList indices = new ShortList();

        for(Country country : this.countries) {
            country.getLabelsData(LocalisationUtils.getCountryNameLocalisation(localisation, country.getId(), this.getColonizerId(country)), mapLabel, vertices, indices);
        }

        WgMesh mesh = new WgMesh(false, vertices.size() / 4, indices.size(), vertexAttributes);
        mesh.setVertices(vertices.toArray());
        mesh.setIndices(indices.toArray());

        return mesh;
    }

    private WgMesh generateMeshBuildings(VertexAttributes vertexAttributes) {
        int numBuildings = 0;

        IntList provinceBuildingIds = this.provinceStore.getBuildingIds();
        IntList provinceBuildingStarts = this.provinceStore.getBuildingStarts();
        IntList provinceBuildingCounts = this.provinceStore.getBuildingCounts();

        BooleanList buildingOnMap = this.economy.getBuildingStore().getOnMap();
        List<String> buildingNames = this.economy.getBuildingStore().getNames();

        for (Country country : this.countries) {
            if (country.getCapital() != null && !country.getProvinces().isEmpty()) {
                numBuildings++;
            }
            for (LandProvince province : country.getProvinces()) {
                int provinceId = province.getId();
                int provinceIndex = this.provinceStore.getIndexById().get(provinceId);
                int provinceBuildingStart = provinceBuildingStarts.get(provinceIndex);
                int provinceBuildingEnd = provinceBuildingStart + provinceBuildingCounts.get(provinceIndex);
                for (int buildingIndex = provinceBuildingStart; buildingIndex < provinceBuildingEnd; buildingIndex++) {
                    int buildingId = provinceBuildingIds.get(buildingIndex);
                    if (buildingOnMap.get(buildingId)) {
                        numBuildings++;
                    }
                }
            }
        }

        float[] vertices = new float[numBuildings * 4 * 4];
        short[] indices = new short[numBuildings * 6];

        int vertexIndex = 0;
        int indexIndex = 0;
        short vertexOffset = 0;

        short width = 6;
        short height = 6;

        TextureRegion capitalRegion = this.mapElementsTextureAtlas.findRegion("building_capital");
        for (Country country : this.countries) {
            if (country.getCapital() != null && !country.getProvinces().isEmpty()) {
                int buildingPosition = country.getCapital().getPosition("default");
                short cx = (short) (buildingPosition >> 16);
                short cy = (short) (buildingPosition & 0xFFFF);

                this.addVerticesIndicesBuilding(vertices, indices, vertexIndex, indexIndex, vertexOffset, cx, cy, width, height, capitalRegion);

                vertexIndex += 16;
                indexIndex += 6;
                vertexOffset += 4;
            }

            for (LandProvince province : country.getProvinces()) {
                int provinceId = province.getId();
                int provinceIndex = this.provinceStore.getIndexById().get(provinceId);
                int provinceBuildingStart = provinceBuildingStarts.get(provinceIndex);
                int provinceBuildingEnd = provinceBuildingStart + provinceBuildingCounts.get(provinceIndex);
                for (int buildingIndex = provinceBuildingStart; buildingIndex < provinceBuildingEnd; buildingIndex++) {
                    int buildingId = provinceBuildingIds.get(buildingIndex);
                    if (!buildingOnMap.get(buildingId)) {
                        continue;
                    }

                    TextureRegion buildingRegion = this.mapElementsTextureAtlas.findRegion("building_" + buildingNames.get(buildingId) + "_empty");

                    int buildingPosition = province.getPosition(buildingNames.get(buildingId));
                    short bx = (short) (buildingPosition >> 16);
                    short by = (short) (buildingPosition & 0xFFFF);

                    this.addVerticesIndicesBuilding(vertices, indices, vertexIndex, indexIndex, vertexOffset, bx, by, width, height, buildingRegion);

                    vertexIndex += 16;
                    indexIndex += 6;
                    vertexOffset += 4;
                }
            }
        }

        WgMesh mesh = new WgMesh(true, vertices.length / 4, indices.length, vertexAttributes);

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    private void addVerticesIndicesBuilding(float[] vertices, short[] indices, int vertexIndex, int indexIndex, short vertexOffset, short x, short y, short width, short height, TextureRegion region) {
        short x1 = (short) (x - (width / 2));
        short y1 = (short) (y - (height / 2));
        short x2 = (short) (x1 + width);
        short y2 = (short) (y1 + height);

        float u1 = region.getU();
        float v1 = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();

        vertices[vertexIndex++] = x1;
        vertices[vertexIndex++] = y1;
        vertices[vertexIndex++] = u1;
        vertices[vertexIndex++] = v1;

        vertices[vertexIndex++] = x2;
        vertices[vertexIndex++] = y1;
        vertices[vertexIndex++] = u2;
        vertices[vertexIndex++] = v1;

        vertices[vertexIndex++] = x2;
        vertices[vertexIndex++] = y2;
        vertices[vertexIndex++] = u2;
        vertices[vertexIndex++] = v2;

        vertices[vertexIndex++] = x1;
        vertices[vertexIndex++] = y2;
        vertices[vertexIndex++] = u1;
        vertices[vertexIndex] = v2;

        indices[indexIndex++] = vertexOffset;
        indices[indexIndex++] = (short) (vertexOffset + 1);
        indices[indexIndex++] = (short) (vertexOffset + 2);
        indices[indexIndex++] = (short) (vertexOffset + 2);
        indices[indexIndex++] = (short) (vertexOffset + 3);
        indices[indexIndex] = vertexOffset;
    }

    private WgMesh generateMeshResources(VertexAttributes vertexAttributes) {
        int numProvinces = 0;
        IntList provinceResourceGoodIds = this.provinceStore.getResourceGoodIds();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            if(provinceResourceGoodIds.get(provinceId) != -1) {
                numProvinces++;
            }
        }

        float[] vertices = new float[numProvinces * 4 * 6];
        short[] indices = new short[numProvinces * 6];

        int vertexIndex = 0;
        int indexIndex = 0;
        short vertexOffset = 0;

        short width = 13;
        short height = 10;

        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int provinceResourceGoodId = provinceResourceGoodIds.get(provinceId);
            if(provinceResourceGoodId == -1) {
                continue;
            }
            TextureRegion resourceRegion = this.mapElementsTextureAtlas.findRegion("resource_" + this.economy.getGoodStore().getNames().get(provinceResourceGoodId));

            int ressourcePosition = Objects.requireNonNull(this.provinces.get(this.provinceStore.getColors().get(provinceId))).getPosition("default");
            short cx = (short) (ressourcePosition >> 16);
            short cy = (short) (ressourcePosition & 0xFFFF);

            short x = (short) (cx - (width / 2));
            short y = (short) (cy - (height / 2));

            float u1 = resourceRegion.getU();
            float v1 = resourceRegion.getV2();
            float u2 = resourceRegion.getU2();
            float v2 = resourceRegion.getV();

            vertices[vertexIndex++] = x;
            vertices[vertexIndex++] = y;
            vertices[vertexIndex++] = u1;
            vertices[vertexIndex++] = v1;
            vertices[vertexIndex++] = cx;
            vertices[vertexIndex++] = cy;

            vertices[vertexIndex++] = x + width;
            vertices[vertexIndex++] = y;
            vertices[vertexIndex++] = u2;
            vertices[vertexIndex++] = v1;
            vertices[vertexIndex++] = cx;
            vertices[vertexIndex++] = cy;

            vertices[vertexIndex++] = x + width;
            vertices[vertexIndex++] = y + height;
            vertices[vertexIndex++] = u2;
            vertices[vertexIndex++] = v2;
            vertices[vertexIndex++] = cx;
            vertices[vertexIndex++] = cy;

            vertices[vertexIndex++] = x;
            vertices[vertexIndex++] = y + height;
            vertices[vertexIndex++] = u1;
            vertices[vertexIndex++] = v2;
            vertices[vertexIndex++] = cx;
            vertices[vertexIndex++] = cy;

            indices[indexIndex++] = vertexOffset;
            indices[indexIndex++] = (short) (vertexOffset + 1);
            indices[indexIndex++] = (short) (vertexOffset + 2);

            indices[indexIndex++] = (short) (vertexOffset + 2);
            indices[indexIndex++] = (short) (vertexOffset + 3);
            indices[indexIndex++] = vertexOffset;

            vertexOffset += 4;
        }

        WgMesh mesh = new WgMesh(true, vertices.length / 6, indices.length, false, vertexAttributes);

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    private WgMeshMulti generateMeshRivers(VertexAttributes vertexAttributes) {
        RawMeshMulti rawMesh = this.mapDao.readRiversMeshJson();

        WgMeshMulti mesh = new WgMeshMulti(true, rawMesh.getVertices().length / 5, 0, vertexAttributes);
        mesh.setVertices(rawMesh.getVertices());
        mesh.setIndirectCommands(rawMesh.getStarts(), rawMesh.getCounts());

        return mesh;
    }

    private Binder createBinderProvinces() {
        Binder binder = new Binder();
        binder.defineGroup(0, this.createBindGroupLayoutProvinces());

        binder.defineBinding("uniforms", 0, 0);
        binder.defineBinding("textureProvinces", 0, 1);
        binder.defineBinding("textureProvincesSampler", 0, 2);
        binder.defineBinding("textureMapMode", 0, 3);
        binder.defineBinding("textureMapModeSampler", 0, 4);
        binder.defineBinding("textureColorMapWater", 0, 5);
        binder.defineBinding("textureColorMapWaterSampler", 0, 6);
        binder.defineBinding("textureWaterNormal", 0, 7);
        binder.defineBinding("textureWaterNormalSampler", 0, 8);
        binder.defineBinding("textureTerrain", 0, 9);
        binder.defineBinding("textureTerrainSampler", 0, 10);
        binder.defineBinding("textureTerrainsheet", 0, 11);
        binder.defineBinding("textureTerrainsheetSampler", 0, 12);
        binder.defineBinding("textureColormap", 0, 13);
        binder.defineBinding("textureColormapSampler", 0, 14);
        binder.defineBinding("textureProvincesStripes", 0, 15);
        binder.defineBinding("textureProvincesStripesSampler", 0, 16);
        binder.defineBinding("textureStripes", 0, 17);
        binder.defineBinding("textureStripesSampler", 0, 18);
        binder.defineBinding("textureOverlayTile", 0, 19);
        binder.defineBinding("textureOverlayTileSampler", 0, 20);

        int offset = 0;
        binder.defineUniform("projTrans", 0, 0, offset);
        offset += 16 * Float.BYTES;
        binder.defineUniform("worldWidth", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("zoom", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("time", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("showTerrain", 0, 0, offset);
        offset += Integer.BYTES;
        binder.defineUniform("colorProvinceSelected", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferProvinces, 0, this.uniformBufferSizeProvinces);

        return binder;
    }

    private Binder createBinderMapLabels() {
        Binder binder = new Binder();
        binder.defineGroup(0, this.createBindGroupLayoutMapLabels());

        binder.defineBinding("uniforms", 0, 0);
        binder.defineBinding("texture", 0, 1);
        binder.defineBinding("textureSampler", 0, 2);

        int offset = 0;
        binder.defineUniform("projTrans", 0, 0, offset);
        offset += 16 * Float.BYTES;
        binder.defineUniform("worldWidth", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("zoom", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferMapLabels, 0, this.uniformBufferSizeMapLabels);

        return binder;
    }

    private Binder createBinderBuildings() {
        Binder binder = new Binder();
        binder.defineGroup(0, this.createBindGroupLayoutBuildings());

        binder.defineBinding("uniforms", 0, 0);
        binder.defineBinding("texture", 0, 1);
        binder.defineBinding("textureSampler", 0, 2);

        int offset = 0;
        binder.defineUniform("projTrans", 0, 0, offset);
        offset += 16 * Float.BYTES;
        binder.defineUniform("worldWidth", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferBuildings, 0, this.uniformBufferSizeBuildings);

        return binder;
    }

    private Binder createBinderResources() {
        Binder binder = new Binder();
        binder.defineGroup(0, this.createBindGroupLayoutResources());

        binder.defineBinding("uniforms", 0, 0);
        binder.defineBinding("texture", 0, 1);
        binder.defineBinding("textureSampler", 0, 2);

        int offset = 0;
        binder.defineUniform("projTrans", 0, 0, offset);
        offset += 16 * Float.BYTES;
        binder.defineUniform("worldWidth", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("zoom", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferResources, 0, this.uniformBufferSizeResources);

        return binder;
    }

    private Binder createBinderRivers() {
        Binder binder = new Binder();
        binder.defineGroup(0, this.createBindGroupLayoutRivers());

        binder.defineBinding("uniforms", 0, 0);
        binder.defineBinding("texture", 0, 1);
        binder.defineBinding("textureSampler", 0, 2);
        binder.defineBinding("textureColorMapWater", 0, 3);
        binder.defineBinding("textureColorMapWaterSampler", 0, 4);

        int offset = 0;
        binder.defineUniform("projTrans", 0, 0, offset);
        offset += 16 * Float.BYTES;
        binder.defineUniform("worldWidth", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("time", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("zoom", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferRivers, 0, this.uniformBufferSizeRivers);

        return binder;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutProvinces() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout provinces");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeProvinces, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(3, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(4, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(5, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(6, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(7, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(8, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(9, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(10, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(11, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2DArray, false);
        layout.addSampler(12, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(13, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(14, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(15, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(16, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(17, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(18, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(19, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(20, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutMapLabels() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout map labels");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeMapLabels, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutBuildings() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout buildings");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeBuildings, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutResources() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout resources");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeResources, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutRivers() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout rivers");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeRivers, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(3, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(4, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUPipeline createPipelineProvinces(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification(vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline";
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderProvinces.getPipelineLayout("pipeline layout provinces"), pipelineSpec);
    }

    private WebGPUPipeline createPipelineMapLabels(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification(vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline map labels";
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderMapLabels.getPipelineLayout("pipeline layout map labels"), pipelineSpec);
    }

    private WebGPUPipeline createPipelineBuildings(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification(vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline";
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderBuildings.getPipelineLayout("pipeline layout buildings"), pipelineSpec);
    }

    private WebGPUPipeline createPipelineResources(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification(vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline resources";
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderResources.getPipelineLayout("pipeline layout resources"), pipelineSpec);
    }

    private WebGPUPipeline createPipelineRivers(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification(vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline rivers";
        pipelineSpec.topology = WGPUPrimitiveTopology.TriangleStrip;
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderRivers.getPipelineLayout("pipeline layout rivers"), pipelineSpec);
    }

    private void bindStaticTextures() {
        this.binderProvinces.setTexture("textureProvinces", this.provincesTexture.getTextureView());
        this.binderProvinces.setSampler("textureProvincesSampler", this.provincesTexture.getSampler());
        this.binderProvinces.setTexture("textureMapMode", this.mapModeTexture.getTextureView());
        this.binderProvinces.setSampler("textureMapModeSampler", this.mapModeTexture.getSampler());
        this.binderProvinces.setTexture("textureColorMapWater", this.colorMapWaterTexture.getTextureView());
        this.binderProvinces.setSampler("textureColorMapWaterSampler", this.colorMapWaterTexture.getSampler());
        this.binderProvinces.setTexture("textureWaterNormal", this.waterTexture.getTextureView());
        this.binderProvinces.setSampler("textureWaterNormalSampler", this.waterTexture.getSampler());
        this.binderProvinces.setTexture("textureTerrain", this.terrainTexture.getTextureView());
        this.binderProvinces.setSampler("textureTerrainSampler", this.terrainTexture.getSampler());
        this.binderProvinces.setTexture("textureTerrainsheet", this.terrainSheetArray.getTextureView());
        this.binderProvinces.setSampler("textureTerrainsheetSampler", this.terrainSheetArray.getSampler());
        this.binderProvinces.setTexture("textureColormap", this.colorMapTexture.getTextureView());
        this.binderProvinces.setSampler("textureColormapSampler", this.colorMapTexture.getSampler());
        this.binderProvinces.setTexture("textureProvincesStripes", this.provincesStripesTexture.getTextureView());
        this.binderProvinces.setSampler("textureProvincesStripesSampler", this.provincesStripesTexture.getSampler());
        this.binderProvinces.setTexture("textureStripes", this.stripesTexture.getTextureView());
        this.binderProvinces.setSampler("textureStripesSampler", this.stripesTexture.getSampler());
        this.binderProvinces.setTexture("textureOverlayTile", this.overlayTileTexture.getTextureView());
        this.binderProvinces.setSampler("textureOverlayTileSampler", this.overlayTileTexture.getSampler());
        this.binderProvinces.setUniform("worldWidth", (float) WORLD_WIDTH);
        this.uniformBufferProvinces.flush();

        this.binderMapLabels.setTexture("texture", ((WgTexture) this.fontMapLabelRegion.getTexture()).getTextureView());
        this.binderMapLabels.setSampler("textureSampler", ((WgTexture) this.fontMapLabelRegion.getTexture()).getSampler());
        this.binderMapLabels.setUniform("worldWidth", (float) WORLD_WIDTH);
        this.uniformBufferMapLabels.flush();

        this.binderBuildings.setTexture("texture", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getTextureView());
        this.binderBuildings.setSampler("textureSampler", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getSampler());
        this.binderBuildings.setUniform("worldWidth", (float) WORLD_WIDTH);
        this.uniformBufferBuildings.flush();

        this.binderResources.setTexture("texture", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getTextureView());
        this.binderResources.setSampler("textureSampler", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getSampler());
        this.binderResources.setUniform("worldWidth", (float) WORLD_WIDTH);
        this.uniformBufferResources.flush();

        this.binderRivers.setTexture("texture", this.riverBodyTexture.getTextureView());
        this.binderRivers.setSampler("textureSampler", this.riverBodyTexture.getSampler());
        this.binderRivers.setTexture("textureColorMapWater", this.colorMapWaterTexture.getTextureView());
        this.binderRivers.setSampler("textureColorMapWaterSampler", this.colorMapWaterTexture.getSampler());
        this.binderRivers.setUniform("worldWidth", (float) WORLD_WIDTH);
        this.uniformBufferRivers.flush();
    }

    public void render(WgProjection projection, OrthographicCamera cam, float time) {
        this.renderMeshProvinces(projection.getCombinedMatrix(), cam.zoom, time);
        this.renderMeshRivers(projection.getCombinedMatrix(), cam.zoom, time);
        this.renderMeshMapLabels(projection.getCombinedMatrix(), cam.zoom);
        if(cam.zoom <= 0.8f) {
            this.renderMeshBuildings(projection.getCombinedMatrix());
        }
        if(this.mapMode == MapMode.RESOURCES && cam.zoom <= 0.8f) {
            this.renderMeshResources(projection.getCombinedMatrix(), cam.zoom);
        }
    }

    private void renderMeshProvinces(Matrix4 projectionViewTransform, float zoom, float time) {
        this.binderProvinces.setUniform("projTrans", projectionViewTransform);
        this.binderProvinces.setUniform("zoom", zoom);
        this.binderProvinces.setUniform("time", time);
        this.binderProvinces.setUniform("showTerrain", this.mapMode == MapMode.TERRAIN ? 1 : 0);
        if(this.selectedProvince != null) {
            int provinceIndex = this.provinceStore.getIndexById().get(this.selectedProvince.getId());
            int color = this.provinceStore.getColors().get(provinceIndex);
            float r = ((color >> 24) & 0xFF) / 255f;
            float g = ((color >> 16) & 0xFF) / 255f;
            float b = ((color >> 8) & 0xFF) / 255f;
            float a = (color & 0xFF) / 255f;
            this.binderProvinces.setUniform("colorProvinceSelected", new Vector4(r, g, b, a));
        } else {
            this.binderProvinces.setUniform("colorProvinceSelected", new Vector4(0f, 0f, 0f, 0f));
        }
        this.uniformBufferProvinces.flush();

        Rectangle view = WebGPUHelper.getViewport();

        WebGPURenderPass pass = RenderPassBuilder.create("Provinces pass");
        pass.setViewport(view.x, view.y, view.width, view.height, 0, 1);
        pass.setPipeline(this.pipelineProvinces);
        this.binderProvinces.bindGroup(pass, 0);

        this.meshProvinces.render(pass, GL20.GL_TRIANGLES, 0, this.meshProvinces.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshMapLabels(Matrix4 projectionViewTransform, float zoom) {
        this.binderMapLabels.setUniform("projTrans", projectionViewTransform);
        this.binderMapLabels.setUniform("zoom", zoom);
        this.uniformBufferMapLabels.flush();

        Rectangle view = WebGPUHelper.getViewport();

        WebGPURenderPass pass = RenderPassBuilder.create("Map labels pass");
        pass.setViewport(view.x, view.y, view.width, view.height, 0, 1);
        pass.setPipeline(this.pipelineMapLabels);
        this.binderMapLabels.bindGroup(pass, 0);

        this.meshMapLabels.render(pass, GL20.GL_TRIANGLES, 0, this.meshMapLabels.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshBuildings(Matrix4 projectionViewTransform) {
        this.binderBuildings.setUniform("projTrans", projectionViewTransform);
        this.uniformBufferBuildings.flush();

        Rectangle view = WebGPUHelper.getViewport();

        WebGPURenderPass pass = RenderPassBuilder.create("Buildings pass");
        pass.setViewport(view.x, view.y, view.width, view.height, 0, 1);
        pass.setPipeline(this.pipelineBuildings);
        this.binderBuildings.bindGroup(pass, 0);

        this.meshBuildings.render(pass, GL20.GL_TRIANGLES, 0, this.meshBuildings.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshResources(Matrix4 projectionViewTransform, float zoom) {
        this.binderResources.setUniform("projTrans", projectionViewTransform);
        this.binderResources.setUniform("zoom", zoom);
        this.uniformBufferResources.flush();

        Rectangle view = WebGPUHelper.getViewport();

        WebGPURenderPass pass = RenderPassBuilder.create("Resources pass");
        pass.setViewport(view.x, view.y, view.width, view.height, 0, 1);
        pass.setPipeline(this.pipelineResources);
        this.binderResources.bindGroup(pass, 0);

        this.meshResources.render(pass, GL20.GL_TRIANGLES, 0, this.meshResources.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshRivers(Matrix4 projectionViewTransform, float zoom, float time) {
        this.binderRivers.setUniform("projTrans", projectionViewTransform);
        this.binderRivers.setUniform("zoom", zoom);
        this.binderRivers.setUniform("time", time);
        this.uniformBufferRivers.flush();

        Rectangle view = WebGPUHelper.getViewport();

        WebGPURenderPass pass = RenderPassBuilder.create("Rivers pass");
        pass.setViewport(view.x, view.y, view.width, view.height, 0, 1);
        pass.setPipeline(this.pipelineRivers);
        this.binderRivers.bindGroup(pass, 0);

        this.meshRivers.render(pass, GL20.GL_TRIANGLE_STRIP, this.meshRivers.getNumIndices(), 0, 3, 0);

        pass.end();
    }

    @Override
    public void dispose() {
        this.waterTexture.dispose();
        this.colorMapWaterTexture.dispose();
        this.provincesTexture.dispose();
        this.mapModeTexture.dispose();
        this.provincesStripesTexture.dispose();
        this.terrainTexture.dispose();
        this.stripesTexture.dispose();
        this.overlayTileTexture.dispose();
        this.colorMapTexture.dispose();
        this.terrainSheetArray.dispose();
        this.mapModePixmap.dispose();
        this.provincesPixmap.dispose();
        this.provincesColorStripesPixmap.dispose();
        this.mapElementsTextureAtlas.dispose();
        this.meshProvinces.dispose();
        this.meshMapLabels.dispose();
        this.meshBuildings.dispose();
        this.meshResources.dispose();
        this.binderProvinces.dispose();
        this.binderMapLabels.dispose();
        this.binderBuildings.dispose();
        this.binderResources.dispose();
        this.uniformBufferProvinces.dispose();
        this.uniformBufferMapLabels.dispose();
        this.uniformBufferBuildings.dispose();
        this.uniformBufferResources.dispose();
        this.pipelineProvinces.dispose();
        this.pipelineMapLabels.dispose();
        this.pipelineBuildings.dispose();
        this.pipelineResources.dispose();
    }
}
