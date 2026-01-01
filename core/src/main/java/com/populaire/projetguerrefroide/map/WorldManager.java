package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Disposable;
import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
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
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.component.Color;
import com.populaire.projetguerrefroide.dao.impl.MapDaoImpl;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.building.EmployeeStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;
import com.populaire.projetguerrefroide.economy.good.GoodStore;
import com.populaire.projetguerrefroide.economy.population.PopulationTypeStore;
import com.populaire.projetguerrefroide.entity.RawMeshMulti;
import com.populaire.projetguerrefroide.util.*;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.*;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class WorldManager implements WorldContext, Disposable {
    private final MapDaoImpl mapDao;
    private final List<Country> countries;
    private final IntLongMap provinces;
    private final ProvinceStore provinceStore;
    private final RegionStore regionStore;
    private final BuildingStore buildingStore;
    private final GoodStore goodStore;
    private final EmployeeStore employeeStore;
    private final Borders borders;
    private final ProductionTypeStore productionTypeStore;
    private final PopulationTypeStore populationTypeStore;
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
    private final WebGPUUniformBuffer uniformBufferWorld;
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
    private final int uniformBufferSizeWorld;
    private long selectedProvinceId;
    private final Vector4 selectedProvinceColor;
    private long playerCountryId;
    private MapMode mapMode;
    private final GameContext gameContext;

    public WorldManager(List<Country> countries, IntLongMap provinces, ProvinceStore provinceStore, RegionStore regionStore, BuildingStore buildingStore, GoodStore goodStore, ProductionTypeStore productionTypeStore, EmployeeStore employeeStore, PopulationTypeStore populationTypeStore, Borders borders, GameContext gameContext) {
        this.mapDao = new MapDaoImpl();
        this.countries = countries;
        this.provinces = provinces;
        this.provinceStore = provinceStore;
        this.regionStore = regionStore;
        this.buildingStore = buildingStore;
        this.goodStore = goodStore;
        this.productionTypeStore = productionTypeStore;
        this.employeeStore = employeeStore;
        this.populationTypeStore = populationTypeStore;
        this.borders = borders;
        this.gameContext = gameContext;
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
        this.selectedProvinceColor = new Vector4();

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
        this.terrainSheetArray = new WgTextureArray(true, false, terrainTextureFiles);
        this.mapElementsTextureAtlas = new WgTextureAtlas(Gdx.files.internal("map/elements/map_elements.atlas"));
        this.mapElementsTextureAtlas.getTextures().first().setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
        this.fontMapLabelRegion = gameContext.getLabelStylePool().getLabelStyle("kart_60").font.getRegion();
        this.fontMapLabelRegion.getTexture().setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);
        this.uniformBufferSizeWorld = (16 + 4 + 4) * Float.BYTES;
        this.uniformBufferWorld = new WebGPUUniformBuffer(this.uniformBufferSizeWorld, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        VertexAttributes vertexAttributesProvinces = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.meshProvinces = this.generateMeshProvinces(vertexAttributesProvinces);
        this.binderProvinces = this.createBinderProvinces();
        this.pipelineProvinces = this.createPipelineProvinces(vertexAttributesProvinces, WgslUtils.getShaderSource("map.wgsl"));
        VertexAttributes vertexAttributesMapLabels = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.meshMapLabels = this.generateMeshMapLabels(vertexAttributesMapLabels, gameContext.getLocalisation(), gameContext.getLabelStylePool());
        this.binderMapLabels = this.createBinderMapLabels();
        this.pipelineMapLabels = this.createPipelineMapLabels(vertexAttributesMapLabels, WgslUtils.getShaderSource("font.wgsl"));
        VertexAttributes vertexAttributesBuildings = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.meshBuildings = this.generateMeshBuildings(vertexAttributesBuildings);
        this.binderBuildings = this.createBinderBuildings();
        this.pipelineBuildings = this.createPipelineBuildings(vertexAttributesBuildings, WgslUtils.getShaderSource("element.wgsl"));
        VertexAttributes vertexAttributesResources = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.Normal, 2, "center"));
        this.meshResources = this.generateMeshResources(vertexAttributesResources);
        this.binderResources = this.createBinderResources();
        this.pipelineResources = this.createPipelineResources(vertexAttributesResources, WgslUtils.getShaderSource("element_scale.wgsl"));
        VertexAttributes vertexAttributesRivers = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.Normal, 1, "width"));
        this.meshRivers = this.generateMeshRivers(vertexAttributesRivers);
        this.binderRivers = this.createBinderRivers();
        this.pipelineRivers = this.createPipelineRivers(vertexAttributesRivers, WgslUtils.getShaderSource("river.wgsl"));
        this.bindStaticTextures();

    }

    @Override
    public ProvinceStore getProvinceStore() {
        return this.provinceStore;
    }

    @Override
    public RegionStore getRegionStore() {
        return this.regionStore;
    }

    @Override
    public BuildingStore getBuildingStore() {
        return this.buildingStore;
    }

    @Override
    public ProductionTypeStore getProductionTypeStore() {
        return this.productionTypeStore;
    }

    @Override
    public GoodStore getGoodStore() {
        return this.goodStore;
    }

    @Override
    public EmployeeStore getEmployeeStore() {
        return this.employeeStore;
    }

    @Override
    public long getPlayerCountryId() {
        return this.playerCountryId;
    }

    @Override
    public List<Country> getCountries() {
        return this.countries;
    }

    public long getProvince(int x, int y) {
        x = (x + WORLD_WIDTH) % WORLD_WIDTH;

        int provinceColor = this.provincesPixmap.getPixel(x, y);
        int provinceColorRGB = (provinceColor & 0xFFFFFF00) | 255;

        return this.provinces.get(provinceColorRGB);
    }

    public boolean selectProvince(int x, int y) {
        World ecsWorld = this.gameContext.getEcsWorld();
        this.selectedProvinceId = this.getProvince(x, y);
        if(this.selectedProvinceId != 0) {
            Entity selectedProvinceEntity = ecsWorld.obtainEntity(this.selectedProvinceId);
            int provinceNameId = Integer.parseInt(selectedProvinceEntity.getName());
            int provinceIndex = this.provinceStore.getIndexById().get(provinceNameId);
            int color = this.provinceStore.getColors().get(provinceIndex);
            float r = ((color >> 24) & 0xFF) / 255f;
            float g = ((color >> 16) & 0xFF) / 255f;
            float b = ((color >> 8) & 0xFF) / 255f;
            float a = (color & 0xFF) / 255f;
            this.binderProvinces.setUniform("colorProvinceSelected", this.selectedProvinceColor.set(r, g, b, a));
        } else {
            this.binderProvinces.setUniform("colorProvinceSelected", this.selectedProvinceColor.set(0f, 0f, 0f, 0f));
        }
        this.uniformBufferWorld.flush();

        return this.selectedProvinceId != 0;
    }

    public long getSelectedProvinceId() {
        return this.selectedProvinceId;
    }

    public boolean setCountryPlayer() {
        World ecsWorld = this.gameContext.getEcsWorld();
        long ownedBy = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
        if(this.selectedProvinceId != 0) {
            Entity selectedProvinceEntity = ecsWorld.obtainEntity(this.selectedProvinceId);
            this.playerCountryId = selectedProvinceEntity.target(ownedBy);
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

    public int getPopulationAmountOfProvince(long provinceEntityId) {
        Entity provinceEntity = this.gameContext.getEcsWorld().obtainEntity(provinceEntityId);
        int provinceId = Integer.parseInt(provinceEntity.getName());
        int provinceIndex = this.provinceStore.getIndexById().get(provinceId);
        return this.provinceStore.getPopulationAmount(provinceIndex);
    }

    public int getPopulationAmountOfCountry(long countryEntityId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);

        int population = 0;

        try (Query provinceQuery = ecsWorld.query().with(landProvinceTagId).with(ownedById, countryEntityId).build()) {
            for (long provinceEntityId : provinceQuery.entities()) {
                Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                int provinceId = Integer.parseInt(provinceEntity.getName());
                int provinceIndex = this.provinceStore.getIndexById().get(provinceId);
                population += this.provinceStore.getPopulationAmount(provinceIndex);
            }
        }

        return population;
    }


    public String getColonizerId(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        long isColonyOfId = ecsWorld.lookup(EcsConstants.EcsIsColonyOf);
        Entity country = ecsWorld.obtainEntity(countryId);
        long countryColonizerId = country.target(isColonyOfId);
        if(countryColonizerId != 0) {
            Entity colony = ecsWorld.obtainEntity(countryColonizerId);
            return colony.getName();
        }

        return null;
    }

    public String getResourceGoodName(long provinceEntityId) {
        Entity provinceEntity = this.gameContext.getEcsWorld().obtainEntity(provinceEntityId);
        int provinceId = Integer.parseInt(provinceEntity.getName());
        int provinceIndex = this.provinceStore.getIndexById().get(provinceId);
        int resourceGoodId = this.provinceStore.getResourceGoodIds().get(provinceIndex);
        if(resourceGoodId != -1) {
            return this.goodStore.getNames().get(resourceGoodId);
        }
        return null;
    }

    public int getAmountAdults(long provinceEntityId) {
        Entity provinceEntity = this.gameContext.getEcsWorld().obtainEntity(provinceEntityId);
        int provinceId = Integer.parseInt(provinceEntity.getName());
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
        World ecsWorld = this.gameContext.getEcsWorld();
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
        long isColonyOfId = ecsWorld.lookup(EcsConstants.EcsIsColonyOf);
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 1; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);

            long provinceEntityId = this.provinces.get(color);
            Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);

            long countryEntityId = provinceEntity.target(ownedById);
            Entity countryEntity = ecsWorld.obtainEntity(countryEntityId);
            Color countryColor = countryEntity.get(Color.class);

            long countryColonizerId = countryEntity.target(isColonyOfId);
            if(countryColonizerId != 0) {
                Entity colonyEntity = ecsWorld.obtainEntity(countryColonizerId);
                countryColor = colonyEntity.get(Color.class);
            }

            this.mapModePixmap.drawPixel(red, green, countryColor.value());

        }
    }

    private void updatePixmapIdeologiesColor(World ecsWorld) {
        long alignedWithId = ecsWorld.lookup(EcsConstants.EcsAlignedWith);
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            long provinceEntityId = this.provinces.get(color);
            if(provinceEntityId != -1) {
                Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                long countryOwnerId = provinceEntity.target(ownedById);
                Entity countryOwnerEntity = ecsWorld.obtainEntity(countryOwnerId);
                long ideologyId = countryOwnerEntity.target(alignedWithId);
                Entity ideologyEntity = ecsWorld.obtainEntity(ideologyId);
                Ideology ideology = ideologyEntity.get(Ideology.class);
                this.mapModePixmap.drawPixel(red, green, ideology.color());
            }
        }
    }

    private void updatePixmapCulturesColor(World ecsWorld) {
        IntList provinceColors = this.provinceStore.getColors();
        IntList provinceCultureValues = this.provinceStore.getCultureValues();
        IntList provinceCultureStarts = this.provinceStore.getCultureStarts();
        IntList provinceCultureCounts = this.provinceStore.getCultureCounts();
        LongList provinceCultureIds = this.provinceStore.getCultureIds();

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
                long biggestCultureId = provinceCultureIds.get(biggestCultureIndex);
                this.mapModePixmap.drawPixel(red, green, Objects.requireNonNull(ecsWorld.obtainEntity(biggestCultureId).get(Color.class)).value());
            }
        }
    }

    private void updatePixmapReligionsColor(World ecsWorld) {
        IntList provinceColors = this.provinceStore.getColors();
        IntList provinceReligionValues = this.provinceStore.getReligionValues();
        IntList provinceReligionStarts = this.provinceStore.getReligionStarts();
        IntList provinceReligionCounts = this.provinceStore.getReligionCounts();
        LongList provinceReligionIds = this.provinceStore.getReligionIds();
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
                long biggestReligionId = provinceReligionIds.get(biggestReligionIndex);
                this.mapModePixmap.drawPixel(red, green, Objects.requireNonNull(ecsWorld.obtainEntity(biggestReligionId).get(Color.class)).value());
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
                this.mapModePixmap.drawPixel(red, green, this.goodStore.getColors().get(provinceResourceGoodId));
            } else {
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getWhiteRGBA());
            }
        }
    }

    private void updatePixmapRegionColor() {
        World ecsWorld = this.gameContext.getEcsWorld();
        long locatedInRegionId = ecsWorld.lookup(EcsConstants.EcsLocatedInRegion);
        long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);
        try(Query query = ecsWorld.query().with(landProvinceTagId).with(Color.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long landProvinceId = iter.entity(i);
                    Entity landProvinceEntity = ecsWorld.obtainEntity(landProvinceId);
                    long regionId = landProvinceEntity.target(locatedInRegionId);
                    Entity regionEntity = ecsWorld.obtainEntity(regionId);
                    int color = iter.fieldInt(Color.class, 1, "value", i);
                    int red = (color >> 24) & 0xFF;
                    int green = (color >> 16) & 0xFF;
                    this.mapModePixmap.drawPixel(red, green, ColorGenerator.getDeterministicRGBA(regionEntity.getName()));
                }
            });
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

    private void updatePixmapTerrain2Color(World ecsWorld) {
        long hasTerrainId = ecsWorld.lookup(EcsConstants.EcsHasTerrain);
        IntList provinceColors = this.provinceStore.getColors();
        for(int provinceId = 0; provinceId < this.provinceStore.getColors().size(); provinceId++) {
            int color = provinceColors.get(provinceId);
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            long provinceEntityId = this.provinces.get(color);
            if(provinceEntityId != -1) {
                Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                long terrainId = provinceEntity.target(hasTerrainId);
                Entity terrainEntity = ecsWorld.obtainEntity(terrainId);
                Terrain terrain = terrainEntity.get(Terrain.class);
                this.mapModePixmap.drawPixel(red, green, terrain.color());
            }
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
        World ecsWorld = this.gameContext.getEcsWorld();
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
        long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);

        Entity playerCountryEntity = ecsWorld.obtainEntity(this.playerCountryId);

        try(Query query = ecsWorld.query().with(landProvinceTagId).with(Color.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long landProvinceId = iter.entity(i);
                    Entity landProvinceEntity = ecsWorld.obtainEntity(landProvinceId);
                    int color = iter.fieldInt(Color.class, 1, "value", i);
                    int red = (color >> 24) & 0xFF;
                    int green = (color >> 16) & 0xFF;

                    long countryOwnerId = landProvinceEntity.target(ownedById);
                    if(this.playerCountryId == countryOwnerId) {
                        this.mapModePixmap.drawPixel(red, green, ColorGenerator.getLightBlueRGBA());
                    } else if (countryOwnerId != 0) {
                        DiplomaticRelation relation = playerCountryEntity.get(DiplomaticRelation.class, countryOwnerId);
                        if (relation != null) {
                            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getRedToGreenGradientRGBA(relation.value(), 200));
                        } else {
                            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getGreyRGBA());
                        }
                    } else {
                        this.mapModePixmap.drawPixel(red, green, ColorGenerator.getGreyRGBA());
                    }
                }
            });
        }
    }

    private Pixmap createProvincesColorStripesPixmap() {
        Pixmap pixmap = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        World ecsWorld = this.gameContext.getEcsWorld();
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
        long controlledById = ecsWorld.lookup(EcsConstants.EcsControlledBy);
        long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);
        try(Query query = ecsWorld.query().with(landProvinceTagId).with(Color.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long landProvinceId = iter.entity(i);
                    Entity landProvinceEntity = ecsWorld.obtainEntity(landProvinceId);
                    long countryOwnerId = landProvinceEntity.target(ownedById);
                    long countryControllerId = landProvinceEntity.target(controlledById);
                    if(countryOwnerId == countryControllerId) {
                        continue;
                    }
                    Entity countryEntity = ecsWorld.obtainEntity(countryControllerId);
                    int color = iter.fieldInt(Color.class, 1, "value", i);
                    int red = (color >> 24) & 0xFF;
                    int green = (color >> 16) & 0xFF;
                    int countryColor = countryEntity.get(Color.class).value();
                    pixmap.drawPixel(red, green, countryColor);
                }
            });
        }

        return pixmap;
    }

    private void updateBordersProvincesPixmap() {
        World ecsWorld = this.gameContext.getEcsWorld();
        long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
        long locatedInRegionId = ecsWorld.lookup(EcsConstants.EcsLocatedInRegion);
        int[] xyBorders = this.borders.getPixels();
        try(Query landProvinceQuery = ecsWorld.query().with(landProvinceTagId).build()) {
            landProvinceQuery.each(landProvinceId -> {
                Entity landProvince = ecsWorld.obtainEntity(landProvinceId);
                Border border = landProvince.get(Border.class);
                for(int i = border.startIndex(); i <= border.endIndex(); i = i + 2) {
                    int x = xyBorders[i];
                    int y = xyBorders[i + 1];

                    int color = this.provincesPixmap.getPixel(x, y);
                    int red = (color >> 24) & 0xFF;
                    int green = (color >> 16) & 0xFF;
                    int blue = (color >> 8) & 0xFF;

                    long countryId = landProvince.target(ownedById);
                    long regionId = landProvince.target(locatedInRegionId);
                    color = (red << 24) | (green << 16) | (blue << 8) | this.getBorderType(ecsWorld, x, y, countryId, regionId, ownedById, locatedInRegionId);
                    this.provincesPixmap.drawPixel(x, y, color);
                }
            });
        }
    }

    public void changeMapMode(String mapMode) {
        World ecsWorld = this.gameContext.getEcsWorld();
        switch(mapMode) {
            case "mapmode_political":
                this.updatePixmapCountriesColor();
                this.mapMode = MapMode.POLITICAL;
                break;
            case "mapmode_strength":
                this.updatePixmapIdeologiesColor(ecsWorld);
                this.mapMode = MapMode.IDEOLOGICAL;
                break;
            case "mapmode_diplomatic":
                this.updatePixmapCulturesColor(ecsWorld);
                this.mapMode = MapMode.CULTURAL;
                break;
            case "mapmode_intel":
                this.updatePixmapReligionsColor(ecsWorld);
                this.mapMode = MapMode.RELIGIOUS;
                break;
            case "mapmode_terrain":
                this.updatePixmapTerrainColor();
                this.mapMode = MapMode.TERRAIN;
                break;
            case "mapmode_terrain_2":
                this.updatePixmapTerrain2Color(ecsWorld);
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
        this.binderProvinces.setUniform("showTerrain", this.mapMode == MapMode.TERRAIN ? 1 : 0);
        this.uniformBufferWorld.flush();
    }

    private short getBorderType(World ecsWorld, int x, int y, long countryId, long regionId, long countryRelationId, long regionRelationId) {
        long provinceRightId = this.getProvince((x + 1), y);
        Entity provinceRight = ecsWorld.obtainEntity(provinceRightId);
        long provinceLeftId = this.getProvince((x - 1), y);
        Entity provinceLeft = ecsWorld.obtainEntity(provinceLeftId);
        long provinceUpId = this.getProvince(x, (y + 1));
        Entity provinceUp = ecsWorld.obtainEntity(provinceUpId);
        long provinceDownId = this.getProvince(x, (y - 1));
        Entity provinceDown = ecsWorld.obtainEntity(provinceDownId);

        // 0: water, nothing or province border, 153: country border, 77: region border
        if(provinceRightId == 0 || provinceLeftId == 0 || provinceUpId == 0 || provinceDownId == 0) {
            return 0;
        } else if (provinceRight.target(countryRelationId) != countryId || provinceLeft.target(countryRelationId) != countryId || provinceUp.target(countryRelationId) != countryId || provinceDown.target(countryRelationId) != countryId) {
            return 153;
        } else if (provinceRight.target(regionRelationId) != regionId || provinceLeft.target(regionRelationId) != regionId || provinceUp.target(regionRelationId) != regionId || provinceDown.target(regionRelationId) != regionId) {
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
        World ecsWorld = this.gameContext.getEcsWorld();
        long countryTagId = ecsWorld.lookup(EcsConstants.EcsCountryTag);
        MapLabel mapLabel = new MapLabel(labelStylePool.getLabelStyle("kart_60").font);
        FloatList vertices = new FloatList();
        ShortList indices = new ShortList();
        try(Query query = ecsWorld.query().with(countryTagId).build()) {
            query.each(countryId -> {
                Entity country = ecsWorld.obtainEntity(countryId);
                String countryNameId = country.getName();
                this.getLabelsData(
                    ecsWorld,
                    countryId,
                    countryNameId,
                    LocalisationUtils.getCountryNameLocalisation(localisation, countryNameId, this.getColonizerId(country.id())),
                    mapLabel,
                    vertices,
                    indices,
                    this.borders
                );
            });
        }

        WgMesh mesh = new WgMesh(false, vertices.size() / 4, indices.size(), vertexAttributes);
        mesh.setVertices(vertices.toArray());
        mesh.setIndices(indices.toArray());

        return mesh;
    }

    private void getLabelsData(World ecsWorld, long countryId, String countryNameId, String countryName, MapLabel mapLabel, FloatList vertices, ShortList indices, Borders borders) {
        LongSet visitedProvinces = new LongSet();
        long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);
        com.github.elebras1.flecs.collection.LongList provinceIds;
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);

        try(Query query = ecsWorld.query().with(ownedById, countryId).with(landProvinceTagId).build()) {
            provinceIds = query.entities();
        }

        for (int i = 0; i < provinceIds.size(); i++) {
            long provinceId = provinceIds.get(i);
            if (!visitedProvinces.contains(provinceId)) {
                LongList connectedProvinces = new LongList();
                this.getConnectedProvinces(ecsWorld, countryNameId, provinceId, visitedProvinces, connectedProvinces);
                if(connectedProvinces.size() > 5 || (connectedProvinces.size() == provinceIds.size() && !connectedProvinces.isEmpty())) {
                    IntList positionsProvinces = new IntList();
                    IntList pixelsBorderProvinces = new IntList();
                    for(int j = 0; j < connectedProvinces.size(); j++) {
                        long connectedProvinceId = connectedProvinces.get(j);
                        Entity connectedProvince = ecsWorld.obtainEntity(connectedProvinceId);
                        Border border = connectedProvince.get(Border.class);
                        long positionEntityId = ecsWorld.lookup("province_" + connectedProvince.getName() + "_pos_default");
                        Entity positionEntity = ecsWorld.obtainEntity(positionEntityId);
                        Position position = positionEntity.get(Position.class);
                        positionsProvinces.add(position.x());
                        positionsProvinces.add(position.y());
                        pixelsBorderProvinces.addAll(Arrays.copyOfRange(borders.getPixels(), border.startIndex(), border.endIndex()));
                    }
                    mapLabel.generateData(countryName, pixelsBorderProvinces, positionsProvinces, vertices, indices);
                }
            }
        }
    }

    private void getConnectedProvinces(World ecsWorld, String countryNameId, long startProvinceId, LongSet visitedProvinceIds, LongList connectedProvinceIds) {
        long adjacentToId = ecsWorld.lookup(EcsConstants.EcsAdjacentTo);
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
        long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);

        LongList toProcess = new LongList();
        toProcess.add(startProvinceId);

        visitedProvinceIds.add(startProvinceId);
        connectedProvinceIds.add(startProvinceId);

        while (!toProcess.isEmpty()) {
            long currentId = toProcess.pop();

            try (Query query = ecsWorld.query().with(adjacentToId, currentId).with(landProvinceTagId).build()) {
                query.each(adjacentId -> {
                    Entity adjacentIdx = ecsWorld.obtainEntity(adjacentId);
                    long ownerEntityId = adjacentIdx.target(ownedById);

                    if (ownerEntityId != 0 && ecsWorld.obtainEntity(ownerEntityId).getName().equals(countryNameId)) {
                        if (!visitedProvinceIds.contains(adjacentId)) {
                            visitedProvinceIds.add(adjacentId);
                            connectedProvinceIds.add(adjacentId);
                            toProcess.add(adjacentId);
                        }
                    }
                });
            }
        }
    }

    private WgMesh generateMeshBuildings(VertexAttributes vertexAttributes) {
        World ecsWorld = this.gameContext.getEcsWorld();
        long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);
        long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
        long countryTagId = ecsWorld.lookup(EcsConstants.EcsCountryTag);
        long hasCapitalId = ecsWorld.lookup(EcsConstants.EcsHasCapital);

        MutableInt numBuildings = new MutableInt(0);

        IntList provinceBuildingIds = this.provinceStore.getBuildingIds();
        IntList provinceBuildingStarts = this.provinceStore.getBuildingStarts();
        IntList provinceBuildingCounts = this.provinceStore.getBuildingCounts();

        BooleanList buildingOnMap = this.buildingStore.getOnMap();
        List<String> buildingNames = this.buildingStore.getNames();

        try (Query countryQuery = ecsWorld.query().with(countryTagId).build()) {
            countryQuery.each(countryEntityId -> {
                Entity countryEntity = ecsWorld.obtainEntity(countryEntityId);
                long capitalTarget = countryEntity.target(hasCapitalId);
                final boolean[] hadProvince = {false};

                try (Query provinceQuery = ecsWorld.query()
                    .with(landProvinceTagId)
                    .with(ownedById, countryEntityId)
                    .build()) {

                    provinceQuery.each(provinceEntityId -> {
                        hadProvince[0] = true;

                        Entity province = ecsWorld.obtainEntity(provinceEntityId);
                        int provinceNameId = Integer.parseInt(province.getName());
                        int provinceIndex = provinceStore.getIndexById().get(provinceNameId);
                        int start = provinceBuildingStarts.get(provinceIndex);
                        int end = start + provinceBuildingCounts.get(provinceIndex);

                        for (int bi = start; bi < end; bi++) {
                            int buildingId = provinceBuildingIds.get(bi);
                            if (buildingOnMap.get(buildingId)) {
                                numBuildings.increment();
                            }
                        }
                    });
                }

                if (capitalTarget != 0 && hadProvince[0]) {
                    numBuildings.increment();
                }
            });
        }

        int nb = numBuildings.getValue();
        float[] vertices = new float[nb * 4 * 4];
        short[] indices = new short[nb * 6];

        final MutableInt vertexIndex = new MutableInt(0);
        final MutableInt indexIndex = new MutableInt(0);
        final MutableInt vertexOffset = new MutableInt(0);

        short width = 6;
        short height = 6;

        TextureRegion capitalRegion = this.mapElementsTextureAtlas.findRegion("building_capital");

        try (Query countryQuery = ecsWorld.query().with(countryTagId).build()) {
            countryQuery.each(countryEntityId -> {
                Entity countryEntity = ecsWorld.obtainEntity(countryEntityId);
                long capitalTarget = countryEntity.target(hasCapitalId);
                final boolean[] hadProvince = {false};

                try (Query provinceQuery = ecsWorld.query()
                    .with(landProvinceTagId)
                    .with(ownedById, countryEntityId)
                    .build()) {

                    provinceQuery.each(provinceEntityId -> {
                        hadProvince[0] = true;

                        Entity province = ecsWorld.obtainEntity(provinceEntityId);
                        int provinceNameId = Integer.parseInt(province.getName());
                        int provinceIndex = provinceStore.getIndexById().get(provinceNameId);
                        int start = provinceBuildingStarts.get(provinceIndex);
                        int end = start + provinceBuildingCounts.get(provinceIndex);

                        for (int bi = start; bi < end; bi++) {
                            int buildingId = provinceBuildingIds.get(bi);
                            if (!buildingOnMap.get(buildingId)) {
                                continue;
                            }

                            String buildingName = buildingNames.get(buildingId);
                            TextureRegion buildingRegion = this.mapElementsTextureAtlas.findRegion("building_" + buildingName + "_empty");

                            long buildingPositionEntityId = ecsWorld.lookup("province_" + provinceNameId + "_pos_" + buildingName);
                            Entity buildingPositionEntity = ecsWorld.obtainEntity(buildingPositionEntityId);
                            Position pos = buildingPositionEntity.get(Position.class);
                            int bx = pos.x();
                            int by = pos.y();

                            this.addVerticesIndicesBuilding(vertices, indices, vertexIndex.getValue(), indexIndex.getValue(), (short) vertexOffset.getValue(), bx, by, width, height, buildingRegion);

                            vertexIndex.increment(16);
                            indexIndex.increment(6);
                            vertexOffset.increment(4);
                        }
                    });
                }

                if (capitalTarget != 0 && hadProvince[0]) {
                    Entity capitalProvinceEntity = ecsWorld.obtainEntity(capitalTarget);
                    long positionEntityId = ecsWorld.lookup("province_" + capitalProvinceEntity.getName() + "_pos_default");
                    Entity positionEntity = ecsWorld.obtainEntity(positionEntityId);
                    Position position = positionEntity.get(Position.class);
                    int cx = position.x();
                    int cy = position.y();

                    this.addVerticesIndicesBuilding(vertices, indices, vertexIndex.getValue(), indexIndex.getValue(), (short) vertexOffset.getValue(), cx, cy, width, height, capitalRegion);

                    vertexIndex.increment(16);
                    indexIndex.increment(6);
                    vertexOffset.increment(4);
                }
            });
        }

        WgMesh mesh = new WgMesh(true, vertices.length / 4, indices.length, vertexAttributes);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    private void addVerticesIndicesBuilding(float[] vertices, short[] indices, int vertexIndex, int indexIndex, short vertexOffset, int x, int y, int width, int height, TextureRegion region) {
        int x1 = x - (width / 2);
        int y1 = y - (height / 2);
        int x2 = x1 + width;
        int y2 = y1 + height;

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
        World ecsWorld = this.gameContext.getEcsWorld();
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
            TextureRegion resourceRegion = this.mapElementsTextureAtlas.findRegion("resource_" + this.goodStore.getNames().get(provinceResourceGoodId));

            long positionEntityId = ecsWorld.lookup("province_" + this.provinceStore.getIds().get(provinceId) + "_pos_default");
            Entity positionEntity = ecsWorld.obtainEntity(positionEntityId);
            Position position = positionEntity.get(Position.class);
            int cx = position.x();
            int cy = position.y();

            int x = cx - (width / 2);
            int y = cy - (height / 2);

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

        WgMeshMulti mesh = new WgMeshMulti(true, rawMesh.vertices().length / 5, 0, vertexAttributes);
        mesh.setVertices(rawMesh.vertices());
        mesh.setIndirectCommands(rawMesh.starts(), rawMesh.counts());

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
        offset += Float.BYTES;
        binder.defineUniform("colorProvinceSelected", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferWorld, 0, this.uniformBufferSizeWorld);

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
        offset += Float.BYTES;
        binder.defineUniform("time", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("showTerrain", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("colorProvinceSelected", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferWorld, 0, this.uniformBufferSizeWorld);

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
        offset += Float.BYTES;
        binder.defineUniform("zoom", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("time", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("showTerrain", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("colorProvinceSelected", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferWorld, 0, this.uniformBufferSizeWorld);

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
        offset += Float.BYTES;
        binder.defineUniform("time", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("showTerrain", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("colorProvinceSelected", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferWorld, 0, this.uniformBufferSizeWorld);

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
        binder.defineUniform("zoom", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("time", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("showTerrain", 0, 0, offset);
        offset += Float.BYTES;
        binder.defineUniform("colorProvinceSelected", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBufferWorld, 0, this.uniformBufferSizeWorld);

        return binder;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutProvinces() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout provinces");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeWorld, false);
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
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeWorld, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutBuildings() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout buildings");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeWorld, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutResources() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout resources");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeWorld, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUBindGroupLayout createBindGroupLayoutRivers() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout rivers");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSizeWorld, false);
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
        this.binderProvinces.setUniform("colorProvinceSelected", this.selectedProvinceColor.set(0f, 0f, 0f, 0f));

        this.binderMapLabels.setTexture("texture", ((WgTexture) this.fontMapLabelRegion.getTexture()).getTextureView());
        this.binderMapLabels.setSampler("textureSampler", ((WgTexture) this.fontMapLabelRegion.getTexture()).getSampler());
        this.binderMapLabels.setUniform("worldWidth", (float) WORLD_WIDTH);

        this.binderBuildings.setTexture("texture", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getTextureView());
        this.binderBuildings.setSampler("textureSampler", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getSampler());
        this.binderBuildings.setUniform("worldWidth", (float) WORLD_WIDTH);

        this.binderResources.setTexture("texture", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getTextureView());
        this.binderResources.setSampler("textureSampler", ((WgTexture) this.mapElementsTextureAtlas.getTextures().first()).getSampler());
        this.binderResources.setUniform("worldWidth", (float) WORLD_WIDTH);

        this.binderRivers.setTexture("texture", this.riverBodyTexture.getTextureView());
        this.binderRivers.setSampler("textureSampler", this.riverBodyTexture.getSampler());
        this.binderRivers.setTexture("textureColorMapWater", this.colorMapWaterTexture.getTextureView());
        this.binderRivers.setSampler("textureColorMapWaterSampler", this.colorMapWaterTexture.getSampler());
        this.binderRivers.setUniform("worldWidth", (float) WORLD_WIDTH);
        this.uniformBufferWorld.flush();
    }

    public void render(WgProjection projection, OrthographicCamera cam, float time) {
        this.binderProvinces.setUniform("projTrans", projection.getCombinedMatrix());
        this.binderProvinces.setUniform("zoom", cam.zoom);
        this.binderProvinces.setUniform("time", time);
        this.binderMapLabels.setUniform("projTrans", projection.getCombinedMatrix());
        this.binderMapLabels.setUniform("zoom", cam.zoom);
        this.binderBuildings.setUniform("projTrans", projection.getCombinedMatrix());
        this.binderResources.setUniform("projTrans", projection.getCombinedMatrix());
        this.binderResources.setUniform("zoom", cam.zoom);
        this.binderRivers.setUniform("projTrans", projection.getCombinedMatrix());
        this.binderRivers.setUniform("zoom", cam.zoom);
        this.binderRivers.setUniform("time", time);
        this.uniformBufferWorld.flush();

        this.renderMeshProvinces();
        this.renderMeshRivers();
        this.renderMeshMapLabels();
        if(cam.zoom <= 0.8f) {
            this.renderMeshBuildings();
        }
        if(this.mapMode == MapMode.RESOURCES && cam.zoom <= 0.8f) {
            this.renderMeshResources();
        }
    }

    private void renderMeshProvinces() {
        WebGPURenderPass pass = RenderPassBuilder.create("Provinces pass");
        pass.setPipeline(this.pipelineProvinces);
        this.binderProvinces.bindGroup(pass, 0);

        this.meshProvinces.render(pass, GL20.GL_TRIANGLES, 0, this.meshProvinces.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshMapLabels() {
        WebGPURenderPass pass = RenderPassBuilder.create("Map labels pass");
        pass.setPipeline(this.pipelineMapLabels);
        this.binderMapLabels.bindGroup(pass, 0);

        this.meshMapLabels.render(pass, GL20.GL_TRIANGLES, 0, this.meshMapLabels.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshBuildings() {
        WebGPURenderPass pass = RenderPassBuilder.create("Buildings pass");
        pass.setPipeline(this.pipelineBuildings);
        this.binderBuildings.bindGroup(pass, 0);

        this.meshBuildings.render(pass, GL20.GL_TRIANGLES, 0, this.meshBuildings.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshResources() {
        WebGPURenderPass pass = RenderPassBuilder.create("Resources pass");
        pass.setPipeline(this.pipelineResources);
        this.binderResources.bindGroup(pass, 0);

        this.meshResources.render(pass, GL20.GL_TRIANGLES, 0, this.meshResources.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshRivers() {
        WebGPURenderPass pass = RenderPassBuilder.create("Rivers pass");
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
        this.uniformBufferWorld.dispose();
        this.pipelineProvinces.dispose();
        this.pipelineMapLabels.dispose();
        this.pipelineBuildings.dispose();
        this.pipelineResources.dispose();
    }
}
