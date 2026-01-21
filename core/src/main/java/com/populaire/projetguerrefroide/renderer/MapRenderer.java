package com.populaire.projetguerrefroide.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Disposable;
import com.github.elebras1.flecs.*;
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
import com.populaire.projetguerrefroide.dao.MapDao;
import com.populaire.projetguerrefroide.pojo.Borders;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.pojo.MapTextures;
import com.populaire.projetguerrefroide.pojo.RawMeshMulti;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.service.CountryService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.service.MapLabelService;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.LocalisationUtils;
import com.populaire.projetguerrefroide.util.MutableInt;
import com.populaire.projetguerrefroide.util.WgslUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class MapRenderer implements Disposable {
    private final CountryService countryService;
    private final GameContext gameContext;
    private final MapDao mapDao;
    private final QueryRepository queryRepository;
    private WgTexture mapModeTexture;
    private WgTexture provincesTexture;
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
    private final Vector4 selectedProvinceColor;

    public MapRenderer(CountryService countryService, GameContext gameContext, MapDao mapDao, QueryRepository queryRepository, MapTextures mapTextures, Borders borders) {
        this.countryService = countryService;
        this.gameContext = gameContext;
        this.mapDao = mapDao;
        this.queryRepository = queryRepository;

        this.mapModeTexture = mapTextures.mapModeTexture();
        this.provincesStripesTexture = mapTextures.provincesStripesTexture();
        this.provincesTexture = mapTextures.provincesTexture();
        this.waterTexture = mapTextures.waterTexture();
        this.colorMapWaterTexture = mapTextures.colorMapWaterTexture();
        this.colorMapTexture = mapTextures.colorMapTexture();
        this.terrainTexture = mapTextures.terrainTexture();
        this.stripesTexture = mapTextures.stripesTexture();
        this.overlayTileTexture = mapTextures.overlayTileTexture();
        this.riverBodyTexture = mapTextures.riverBodyTexture();
        this.terrainSheetArray = new WgTextureArray(true, false, mapTextures.terrainSheetFiles());

        this.mapElementsTextureAtlas = new WgTextureAtlas(Gdx.files.internal("map/elements/map_elements.atlas"));
        this.mapElementsTextureAtlas.getTextures().first().setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
        this.fontMapLabelRegion = gameContext.getLabelStylePool().getLabelStyle("kart_60").font.getRegion();
        this.fontMapLabelRegion.getTexture().setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);
        this.selectedProvinceColor = new Vector4();
        this.uniformBufferSizeWorld = (16 + 4 + 4) * Float.BYTES;
        this.uniformBufferWorld = new WebGPUUniformBuffer(this.uniformBufferSizeWorld, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        VertexAttributes vertexAttributesProvinces = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.meshProvinces = this.generateMeshProvinces(vertexAttributesProvinces);
        this.binderProvinces = this.createBinderProvinces();
        this.pipelineProvinces = this.createPipelineProvinces(vertexAttributesProvinces, WgslUtils.getShaderSource("map.wgsl"));
        VertexAttributes vertexAttributesMapLabels = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.meshMapLabels = this.generateMeshMapLabels(vertexAttributesMapLabels, gameContext.getLocalisation(), gameContext.getLabelStylePool(), borders);
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

    private WgMesh generateMeshMapLabels(VertexAttributes vertexAttributes, Map<String, String> localisation, LabelStylePool labelStylePool, Borders borders) {
        World ecsWorld = this.gameContext.getEcsWorld();
        MapLabelService mapLabelService = new MapLabelService(labelStylePool.getLabelStyle("kart_60").font);
        FloatList vertices = new FloatList();
        ShortList indices = new ShortList();
        Query query = this.queryRepository.getCountries();
        query.each(countryId -> {
            Entity country = ecsWorld.obtainEntity(countryId);
            String countryNameId = country.getName();
            this.getLabelsData(
                ecsWorld,
                countryId,
                LocalisationUtils.getCountryNameLocalisation(localisation, countryNameId, this.countryService.getColonizerId(country.id())),
                mapLabelService,
                vertices,
                indices,
                borders
            );
        });

        WgMesh mesh = new WgMesh(false, vertices.size() / 4, indices.size(), vertexAttributes);
        mesh.setVertices(vertices.toArray());
        mesh.setIndices(indices.toArray());

        return mesh;
    }

    private void getLabelsData(World ecsWorld, long countryId, String countryName, MapLabelService mapLabelService, FloatList vertices, ShortList indices, Borders borders) {
        LongSet visitedProvinces = new LongSet();
        LongList provinceIds = getLongList(countryId);

        for (int i = 0; i < provinceIds.size(); i++) {
            long provinceId = provinceIds.get(i);
            if (!visitedProvinces.contains(provinceId)) {
                LongList connectedProvinces = new LongList();
                this.getConnectedProvinces(ecsWorld, countryId, provinceId, visitedProvinces, connectedProvinces);
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
                    mapLabelService.generateData(countryName, pixelsBorderProvinces, positionsProvinces, vertices, indices);
                }
            }
        }
    }

    private LongList getLongList(long countryId) {
        LongList provinceIds = new LongList();

        Query query = this.queryRepository.getProvinces();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                if(provinceView.ownerId() == countryId) {
                    long provinceEntityId = iter.entity(i);
                    provinceIds.add(provinceEntityId);
                }
            }
        });
        return provinceIds;
    }

    private void getConnectedProvinces(World ecsWorld, long countryId, long startProvinceId, LongSet visitedProvinceIds, LongList connectedProvinceIds) {
        LongList toProcess = new LongList();
        toProcess.add(startProvinceId);

        visitedProvinceIds.add(startProvinceId);
        connectedProvinceIds.add(startProvinceId);

        while (!toProcess.isEmpty()) {
            long currentId = toProcess.pop();
            Entity currentEntity = ecsWorld.obtainEntity(currentId);
            Adjacencies adjacencies = currentEntity.get(Adjacencies.class);

            if (adjacencies == null || adjacencies.provinceIds() == null) {
                continue;
            }

            for (int i = 0; i < adjacencies.provinceIds().length; i++) {
                long neighborId = adjacencies.provinceIds()[i];
                if (neighborId == 0 || visitedProvinceIds.contains(neighborId)) {
                    continue;
                }
                Entity neighborEntity = ecsWorld.obtainEntity(neighborId);
                if (!neighborEntity.has(Province.class)) {
                    continue;
                }
                Province neighborProvinceData = neighborEntity.get(Province.class);
                long ownerEntityId = neighborProvinceData.ownerId();
                if (ownerEntityId != 0 && ownerEntityId == countryId) {
                    visitedProvinceIds.add(neighborId);
                    connectedProvinceIds.add(neighborId);
                    toProcess.add(neighborId);
                }
            }
        }
    }

    private WgMesh generateMeshBuildings(VertexAttributes vertexAttributes) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();

        MutableInt numBuildings = new MutableInt(0);

        Set<Long> countriesWithProvinces = new HashSet<>();
        LongObjectMap<LongList> buildingsByProvince = new LongObjectMap<>();

        Query provinceQuery = this.queryRepository.getProvinces();
        provinceQuery.each(provinceEntityId -> {
            EntityView provinceView = ecsWorld.obtainEntityView(provinceEntityId);
            ProvinceView provinceDataView = provinceView.getMutView(Province.class);
            countriesWithProvinces.add(provinceDataView.ownerId());
            buildingsByProvince.put(provinceEntityId, new LongList());
        });

        Query buildingQuery = this.queryRepository.getBuildings();
        buildingQuery.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                BuildingView buildingView = buildingField.getMutView(i);
                long buildingEntityId = iter.entity(i);
                EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingView.typeId());
                if (buildingTypeView.has(ecsConstants.onMap())) {
                    LongList buildings = buildingsByProvince.get(buildingView.parentId());
                    if (buildings != null) {
                        buildings.add(buildingEntityId);
                        numBuildings.increment();
                    }
                }
            }
        });

        Query countryQuery = this.queryRepository.getCountries();
        countryQuery.each(countryEntityId -> {
            EntityView countryView = ecsWorld.obtainEntityView(countryEntityId);
            CountryView countryDataView = countryView.getMutView(Country.class);

            if (countryDataView.capitalId() != 0 && countriesWithProvinces.contains(countryEntityId)) {
                numBuildings.increment();
            }
        });

        int nb = numBuildings.getValue();
        float[] vertices = new float[nb * 4 * 4];
        short[] indices = new short[nb * 6];

        final MutableInt vertexIndex = new MutableInt(0);
        final MutableInt indexIndex = new MutableInt(0);
        final MutableInt vertexOffset = new MutableInt(0);

        int width = 6;
        int height = 6;

        TextureRegion capitalRegion = this.mapElementsTextureAtlas.findRegion("building_capital");

        Query provinceBuildingQuery = this.queryRepository.getProvinces();
        provinceBuildingQuery.each(provinceEntityId -> {
            Entity province = ecsWorld.obtainEntity(provinceEntityId);
            String provinceNameId = province.getName();
            LongList buildings = buildingsByProvince.get(provinceEntityId);
            if (buildings == null) {
                return;
            }

            for (int bi = 0; bi < buildings.size(); bi++) {
                long buildingEntityId = buildings.get(bi);
                Entity building = ecsWorld.obtainEntity(buildingEntityId);
                Building buildingData = building.get(Building.class);
                Entity buildingType = ecsWorld.obtainEntity(buildingData.typeId());
                String buildingName = buildingType.getName();

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

        Query countryCapitalQuery = this.queryRepository.getCountries();
        countryCapitalQuery.each(countryEntityId -> {
            Entity countryEntity = ecsWorld.obtainEntity(countryEntityId);
            Country countryData = countryEntity.get(Country.class);

            if (countryData.capitalId() != 0 && countriesWithProvinces.contains(countryEntityId)) {
                Entity capitalProvinceEntity = ecsWorld.obtainEntity(countryData.capitalId());
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
        MutableInt numProvinces = new MutableInt(0);
        Query queryCount = this.queryRepository.getProvincesWithResourceGathering();
        queryCount.iter(iter -> {
            for(int i = 0; i < iter.count(); i++) {
                long provinceEntityId = iter.entity(i);
                Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                ResourceGathering resourceGathering = provinceEntity.get(ResourceGathering.class);
                if(resourceGathering.goodId() != -1) {
                    numProvinces.increment();
                }
            }
        });

        float[] vertices = new float[numProvinces.getValue() * 4 * 6];
        short[] indices = new short[numProvinces.getValue() * 6];

        final MutableInt vertexIndex = new MutableInt(0);
        final MutableInt indexIndex = new MutableInt(0);
        final MutableInt vertexOffset = new MutableInt(0);

        int width = 13;
        int height = 10;

        Query query = this.queryRepository.getProvincesWithResourceGathering();
        query.iter(iter -> {
            for(int i = 0; i < iter.count(); i++) {
                long provinceEntityId = iter.entity(i);
                Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                ResourceGathering resourceGathering = provinceEntity.get(ResourceGathering.class);
                long provinceResourceGoodId = resourceGathering.goodId();
                if(provinceResourceGoodId == -1) {
                    continue;
                }
                TextureRegion resourceRegion = this.mapElementsTextureAtlas.findRegion("resource_" + ecsWorld.obtainEntity(provinceResourceGoodId).getName());

                long positionEntityId = ecsWorld.lookup("province_" + provinceEntity.getName() + "_pos_default");
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

                int vIdx = vertexIndex.getValue();
                vertices[vIdx++] = x;
                vertices[vIdx++] = y;
                vertices[vIdx++] = u1;
                vertices[vIdx++] = v1;
                vertices[vIdx++] = cx;
                vertices[vIdx++] = cy;

                vertices[vIdx++] = x + width;
                vertices[vIdx++] = y;
                vertices[vIdx++] = u2;
                vertices[vIdx++] = v1;
                vertices[vIdx++] = cx;
                vertices[vIdx++] = cy;

                vertices[vIdx++] = x + width;
                vertices[vIdx++] = y + height;
                vertices[vIdx++] = u2;
                vertices[vIdx++] = v2;
                vertices[vIdx++] = cx;
                vertices[vIdx++] = cy;

                vertices[vIdx++] = x;
                vertices[vIdx++] = y + height;
                vertices[vIdx++] = u1;
                vertices[vIdx++] = v2;
                vertices[vIdx++] = cx;
                vertices[vIdx++] = cy;
                vertexIndex.setValue(vIdx);

                int iIdx = indexIndex.getValue();
                short vOff = (short) vertexOffset.getValue();
                indices[iIdx++] = vOff;
                indices[iIdx++] = (short) (vOff + 1);
                indices[iIdx++] = (short) (vOff + 2);

                indices[iIdx++] = (short) (vOff + 2);
                indices[iIdx++] = (short) (vOff + 3);
                indices[iIdx++] = vOff;
                indexIndex.setValue(iIdx);

                vertexOffset.increment(4);
            }
        });

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

    public void updateSelectedProvince(float r, float g, float b, float a) {
        this.binderProvinces.setUniform("colorProvinceSelected", this.selectedProvinceColor.set(r, g, b, a));
        this.uniformBufferWorld.flush();
    }

    public void updateMapMode(MapMode mapMode, Pixmap mapModePixmap) {
        this.mapModeTexture.dispose();
        PixmapTextureData mapModeTextureData = new PixmapTextureData(mapModePixmap, Pixmap.Format.RGBA8888, false, false);
        this.mapModeTexture = new WgTexture(mapModeTextureData, "mapModeTexture", false);
        this.mapModeTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        this.binderProvinces.setTexture("textureMapMode", this.mapModeTexture.getTextureView());
        this.binderProvinces.setSampler("textureMapModeSampler", this.mapModeTexture.getSampler());
        this.binderProvinces.setUniform("showTerrain", mapMode == MapMode.TERRAIN ? 1 : 0);
        this.uniformBufferWorld.flush();
    }

    public void updateProvincesTexture(Pixmap provincesPixmap) {
        this.provincesTexture.dispose();
        PixmapTextureData provincesTextureData = new PixmapTextureData(provincesPixmap, Pixmap.Format.RGBA8888, false, false);
        this.provincesTexture = new WgTexture(provincesTextureData, "provincesTexture", false);
        this.provincesTexture.setFilter(WgTexture.TextureFilter.Nearest, WgTexture.TextureFilter.Nearest);
        this.binderProvinces.setTexture("textureProvinces", this.provincesTexture.getTextureView());
        this.binderProvinces.setSampler("textureProvincesSampler", this.provincesTexture.getSampler());
    }

    public void render(WgProjection projection, OrthographicCamera cam, float time, MapMode mapMode) {
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
        if(mapMode == MapMode.RESOURCES && cam.zoom <= 0.8f) {
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
