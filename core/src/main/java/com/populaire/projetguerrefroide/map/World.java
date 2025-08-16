package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.*;
import com.github.xpenatan.webgpu.*;
import com.monstrous.gdx.webgpu.application.WebGPUContext;
import com.monstrous.gdx.webgpu.application.WgGraphics;
import com.monstrous.gdx.webgpu.graphics.Binder;
import com.monstrous.gdx.webgpu.graphics.WgMesh;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.graphics.WgTextureArray;
import com.monstrous.gdx.webgpu.graphics.g2d.WgTextureAtlas;
import com.monstrous.gdx.webgpu.wrappers.*;
import com.populaire.projetguerrefroide.adapter.graphics.WGProjection;
import com.populaire.projetguerrefroide.dao.impl.MapDaoImpl;
import com.populaire.projetguerrefroide.economy.Economy;
import com.populaire.projetguerrefroide.entity.ModifierStore;
import com.populaire.projetguerrefroide.entity.Terrain;
import com.populaire.projetguerrefroide.national.NationalIdeas;
import com.populaire.projetguerrefroide.politics.AllianceType;
import com.populaire.projetguerrefroide.politics.Politics;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.util.ColorGenerator;
import com.populaire.projetguerrefroide.util.LocalisationUtils;
import com.populaire.projetguerrefroide.util.WebGPUHelper;
import com.populaire.projetguerrefroide.util.WgslUtils;

import java.util.*;

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
    private Texture mapModeTexture;
    private final Texture provincesTexture;
    private final Texture waterTexture;
    private final Texture colorMapWaterTexture;
    private final Texture provincesStripesTexture;
    private final Texture terrainTexture;
    private final Texture stripesTexture;
    private final Texture colorMapTexture;
    private final Texture overlayTileTexture;
    private final Texture riverBodyTexture;
    private final WgTextureArray terrainSheetArray;
    private final TextureAtlas mapElementsTextureAtlas;
    /*private final ShaderProgram mapShader;
    private final ShaderProgram fontShader;
    private final ShaderProgram elementShader;
    private final ShaderProgram elementScaleShader;
    private final ShaderProgram riverShader;*/
    private final WgMesh meshBuildings;
    private final WgMesh meshResources;
    //private final MeshMultiDrawIndirect meshRivers;
    private final WebGPUUniformBuffer uniformBufferBuildings;
    private final WebGPUUniformBuffer uniformBufferResources;
    private final Binder binderBuildings;
    private final Binder binderResources;
    private final WebGPUPipeline pipelineBuildings;
    private final WebGPUPipeline pipelineResources;
    private final int uniformBufferSizeBuildings;
    private final int uniformBufferSizeResources;
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
        this.mapModePixmap.setBlending(Pixmap.Blending.None);
        this.mapModePixmap.setColor(0, 0, 0, 0);
        this.mapModePixmap.fill();
        Pixmap tempPixmap = new Pixmap(Gdx.files.internal("map/provinces.bmp"));
        this.provincesPixmap = new Pixmap(tempPixmap.getWidth(), tempPixmap.getHeight(), Pixmap.Format.RGBA8888);
        this.provincesPixmap.drawPixmap(tempPixmap, 0, 0);
        this.provincesPixmap.setBlending(Pixmap.Blending.None);
        tempPixmap.dispose();
        this.updatePixmapCountriesColor();
        this.updateBordersProvincesPixmap();
        Pixmap provincesColorStripesPixmap = this.createProvincesColorStripesPixmap();
        String[] terrainTexturePaths = this.createTerrainTexturePaths();
        this.mapMode = MapMode.POLITICAL;

        for(Country country : this.countries) {
            country.createLabels(LocalisationUtils.getCountryNameLocalisation(gameContext.getLocalisation(), country.getId(), this.getColonizerId(country)), gameContext.getLabelStylePool());
        }
        this.mapModeTexture = new WgTexture(this.mapModePixmap);
        this.mapModeTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        this.provincesStripesTexture = new WgTexture(provincesColorStripesPixmap);
        this.provincesStripesTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        this.provincesTexture = new WgTexture(this.provincesPixmap);
        this.provincesTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        this.waterTexture = new WgTexture("map/terrain/sea_normal.png");
        this.waterTexture.setWrap(WgTexture.TextureWrap.Repeat, WgTexture.TextureWrap.Repeat);
        this.colorMapWaterTexture = new WgTexture("map/terrain/colormap_water.png");
        this.colorMapWaterTexture.setFilter(WgTexture.TextureFilter.Linear, WgTexture.TextureFilter.Linear);
        this.colorMapTexture = new WgTexture("map/terrain/colormap.png");
        this.terrainTexture = new WgTexture("map/terrain.bmp");
        this.stripesTexture = new WgTexture("map/terrain/stripes.png");
        this.overlayTileTexture = new WgTexture("map/terrain/map_overlay_tile.png");
        this.overlayTileTexture.setWrap(WgTexture.TextureWrap.Repeat, WgTexture.TextureWrap.Repeat);
        this.riverBodyTexture = new WgTexture("map/terrain/river.png");
        this.riverBodyTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.terrainSheetArray = new WgTextureArray(terrainTexturePaths);
        this.mapElementsTextureAtlas = new WgTextureAtlas(Gdx.files.internal("map/elements/map_elements.atlas"));
        this.mapElementsTextureAtlas.getTextures().first().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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
        this.bindStaticTextures();
        //this.meshRivers = this.generateMeshRivers();

        /*String vertexMapShader = Gdx.files.internal("shaders/map_v.glsl").readString();
        String fragmentMapShader = Gdx.files.internal("shaders/map_f.glsl").readString();
        this.mapShader = new ShaderProgram(vertexMapShader, fragmentMapShader);
        String vertexFontShader = Gdx.files.internal("shaders/font_v.glsl").readString();
        String fragmentFontShader = Gdx.files.internal("shaders/font_f.glsl").readString();
        this.fontShader = new ShaderProgram(vertexFontShader, fragmentFontShader);
        String vertexElementShader = Gdx.files.internal("shaders/element_v.glsl").readString();
        String fragmentElementShader = Gdx.files.internal("shaders/element_f.glsl").readString();
        this.elementShader = new ShaderProgram(vertexElementShader, fragmentElementShader);
        String vertexElementScaleShader = Gdx.files.internal("shaders/element_scale_v.glsl").readString();
        String fragmentElementScaleShader = Gdx.files.internal("shaders/element_scale_f.glsl").readString();
        this.elementScaleShader = new ShaderProgram(vertexElementScaleShader, fragmentElementScaleShader);
        String vertexRiverShader = Gdx.files.internal("shaders/river_v.glsl").readString();
        String fragmentRiverShader = Gdx.files.internal("shaders/river_f.glsl").readString();
        this.riverShader = new ShaderProgram(vertexRiverShader, fragmentRiverShader);
        ShaderProgram.pedantic = false;*/
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

    public String[] createTerrainTexturePaths() {
        String[] terrainTexturePaths = new String[64];
        String pathBase = "map/terrain/textures/";
        for(int i = 0; i < 64; i++) {
            terrainTexturePaths[i] = pathBase + "text_" + i + ".png";
        }

        return terrainTexturePaths;
    }

    public void updatePixmapCountriesColor() {
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

    public void updatePixmapIdeologiesColor() {
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, Objects.requireNonNull(this.provinces.get(color)).getCountryOwner().getIdeology().getColor());
        }
    }

    public void updatePixmapCulturesColor() {
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

    public void updatePixmapReligionsColor() {
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

    public void updatePixmapResourcesColor() {
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

    public void updatePixmapRegionColor() {
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getDeterministicRGBA(Objects.requireNonNull(this.provinces.get(color)).getRegion().getId()));
        }
    }

    public void updatePixmapTerrainColor() {
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

    public void updatePixmapPopulationColor() {
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


    public void updatePixmapRelationsColor() {
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

    public Pixmap createProvincesColorStripesPixmap() {
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

    public void updateBordersProvincesPixmap() {
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
        this.mapModeTexture = new WgTexture(this.mapModePixmap);
    }

    public short getBorderType(short x, short y, Country country, Region region) {
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

    public WgMesh generateMeshBuildings(VertexAttributes vertexAttributes) {
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

    public WgMesh generateMeshResources(VertexAttributes vertexAttributes) {
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

        WgMesh mesh = new WgMesh(true, vertices.length / 6, indices.length, vertexAttributes);


        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    /*public MeshMultiDrawIndirect generateMeshRivers() {
        RawMeshMultiDraw rawMesh = this.mapDao.readRiversMeshJson();

        MeshMultiDrawIndirect mesh = new MeshMultiDrawIndirect(true, rawMesh.getVertices().length / 5, 0,
            new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
            new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_width"));
        mesh.setVertices(rawMesh.getVertices());
        mesh.setIndirectCommands(rawMesh.getStarts(), rawMesh.getCounts());

        return mesh;
    }*/

    public Binder createBinderBuildings() {
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

    public Binder createBinderResources() {
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

    private void bindStaticTextures() {
        this.binderBuildings.setTexture("texture", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getTextureView());
        this.binderBuildings.setSampler("textureSampler", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getSampler());
        this.binderBuildings.setUniform("worldWidth", (float) WORLD_WIDTH);
        this.uniformBufferBuildings.flush();

        this.binderResources.setTexture("texture", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getTextureView());
        this.binderResources.setSampler("textureSampler", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getSampler());
        this.binderResources.setUniform("worldWidth", (float) WORLD_WIDTH);
        this.uniformBufferResources.flush();
    }

    public void render(WGProjection projection, OrthographicCamera cam, float time) {
        /*this.mapShader.bind();
        this.provincesTexture.bind(0);
        this.mapModeTexture.bind(1);
        this.colorMapWaterTexture.bind(2);
        this.waterTexture.bind(3);
        this.terrainTexture.bind(4);
        this.terrainSheetArray.bind(5);
        this.colorMapTexture.bind(6);
        this.provincesStripesTexture.bind(7);
        this.stripesTexture.bind(8);
        this.overlayTileTexture.bind(9);

        this.mapShader.setUniformi("u_textureProvinces", 0);
        this.mapShader.setUniformi("u_textureMapMode", 1);
        this.mapShader.setUniformi("u_textureColorMapWater", 2);
        this.mapShader.setUniformi("u_textureWaterNormal", 3);
        this.mapShader.setUniformi("u_textureTerrain", 4);
        this.mapShader.setUniformi("u_textureTerrainsheet", 5);
        this.mapShader.setUniformi("u_textureColormap", 6);
        this.mapShader.setUniformi("u_textureProvincesStripes", 7);
        this.mapShader.setUniformi("u_textureStripes", 8);
        this.mapShader.setUniformi("u_textureOverlayTile", 9);
        this.mapShader.setUniformf("u_zoom", cam.zoom);
        this.mapShader.setUniformf("u_time", time);
        this.mapShader.setUniformi("u_showTerrain", this.mapMode == MapMode.TERRAIN ? 1 : 0);
        if(this.selectedProvince != null) {
            int provinceIndex = this.provinceStore.getIndexById().get(this.selectedProvince.getId());
            int color = this.provinceStore.getColors().get(provinceIndex);
            float r = ((color >> 24) & 0xFF) / 255f;
            float g = ((color >> 16) & 0xFF) / 255f;
            float b = ((color >> 8) & 0xFF) / 255f;
            float a = (color & 0xFF) / 255f;
            this.mapShader.setUniformf("u_colorProvinceSelected", r, g, b, a);
        } else {
            this.mapShader.setUniformf("u_colorProvinceSelected", 0f, 0f, 0f, 0f);
        }
        this.mapShader.setUniformMatrix("u_projTrans", cam.combined);

        batch.setShader(this.mapShader);
        batch.begin();
        batch.draw(this.provincesTexture, -WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(this.provincesTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(this.provincesTexture, WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.setShader(null);

        //this.renderMeshRivers(cam, time);

        this.fontShader.bind();
        this.fontShader.setUniformf("u_zoom", cam.zoom);
        batch.setShader(this.fontShader);
        for(Country country : this.countries) {
            for(MapLabel label : country.getLabels()) {
                label.render(batch);
            }
        }
        batch.setShader(null);
        batch.end();*/
        if(cam.zoom <= 0.8f) {
            this.renderMeshBuildings(projection.getCombinedMatrix());
        }
        //if(this.mapMode == MapMode.RESOURCES && cam.zoom <= 0.8f) {
            this.renderMeshResources(projection.getCombinedMatrix(), cam.zoom);
        //}
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
        System.out.println("Zoom : " + zoom);
        System.out.println("Projection view transform : " + projectionViewTransform);
        System.out.println("Vertices : " + Arrays.toString(this.meshResources.getVertices(new float[30])));

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

    /*private void renderMeshRivers(OrthographicCamera cam, float time) {
        this.riverShader.bind();
        this.riverBodyTexture.bind(0);
        this.colorMapWaterTexture.bind(1);
        this.riverShader.setUniformi("u_texture", 0);
        this.riverShader.setUniformi("u_textureColorMapWater", 1);
        this.riverShader.setUniformf("u_time", time);
        this.riverShader.setUniformMatrix("u_projTrans", cam.combined);
        this.riverShader.setUniformi("u_worldWidth", WORLD_WIDTH);
        this.riverShader.setUniformf("u_zoom", cam.zoom);
        this.meshRivers.bind(this.riverShader);
        Gdx.gl.glEnable(GL32.GL_BLEND);
        Gdx.gl.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
        GL43.glMultiDrawArraysIndirect(GL43.GL_TRIANGLE_STRIP, 0, this.meshRivers.getCommandCount(), 0);
        this.meshRivers.unbind(this.riverShader);
    }*/

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
        this.mapElementsTextureAtlas.dispose();
        /*this.mapShader.dispose();
        this.fontShader.dispose();
        this.elementScaleShader.dispose();
        this.meshResources.dispose();
        this.meshBuildings.dispose();
        this.elementShader.dispose();
        this.riverShader.dispose();
        this.meshRivers.dispose();*/
    }
}
