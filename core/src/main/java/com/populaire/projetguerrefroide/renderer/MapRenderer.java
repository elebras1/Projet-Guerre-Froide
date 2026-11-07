package com.populaire.projetguerrefroide.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.populaire.projetguerrefroide.adapter.graphics.WgMeshMulti;
import com.github.tommyettinger.ds.*;
import com.github.xpenatan.webgpu.*;
import com.monstrous.gdx.webgpu.graphics.Binder;
import com.monstrous.gdx.webgpu.graphics.WgMesh;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.graphics.WgTextureArray;
import com.monstrous.gdx.webgpu.graphics.g2d.WgTextureAtlas;
import com.monstrous.gdx.webgpu.wrappers.*;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.dao.MapDao;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.pojo.MapTextures;
import com.populaire.projetguerrefroide.pojo.RawMeshMulti;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.catalog.ProvinceTable;
import com.populaire.projetguerrefroide.protocol.response.MapBuildings;
import com.populaire.projetguerrefroide.protocol.response.PoliticalMap;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.util.WgslUtils;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class MapRenderer implements Disposable {
    private final Catalog catalog;
    private final MapDao mapDao;
    private final MapLabelMeshBuilder mapLabelMeshBuilder;
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
    private WgMesh meshMapLabels;
    private WgMesh meshBuildings;
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
    private final VertexAttributes vertexAttributesMapLabels;
    private final VertexAttributes vertexAttributesBuildings;

    public MapRenderer(Catalog catalog, UiContext uiContext, MapDao mapDao, MapTextures mapTextures, PoliticalMap politicalMap, MapBuildings mapBuildings, IntFunction<String> countryNames) {
        this.catalog = catalog;
        this.mapDao = mapDao;
        this.mapLabelMeshBuilder = new MapLabelMeshBuilder(uiContext.getSkinFonts().get(FontsSkin.Styles.Label.KART_60, Label.LabelStyle.class).font);

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
        this.fontMapLabelRegion = uiContext.getSkinFonts().get(FontsSkin.Styles.Label.KART_60, Label.LabelStyle.class).font.getRegion();
        this.fontMapLabelRegion.getTexture().setFilter(WgTexture.TextureFilter.MipMapLinearLinear, WgTexture.TextureFilter.MipMapLinearLinear);
        this.selectedProvinceColor = new Vector4();
        this.uniformBufferSizeWorld = (16 + 4 + 4) * Float.BYTES;
        this.uniformBufferWorld = new WebGPUUniformBuffer(this.uniformBufferSizeWorld, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        VertexAttributes vertexAttributesProvinces = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, "position"), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texCoord"));
        this.meshProvinces = this.generateMeshProvinces(vertexAttributesProvinces);
        this.binderProvinces = this.createBinderProvinces();
        this.pipelineProvinces = this.createPipelineProvinces(vertexAttributesProvinces, WgslUtils.getShaderSource("map.wgsl"));
        this.vertexAttributesMapLabels = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, "position"), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texCoord"));
        this.meshMapLabels = this.generateMeshMapLabels(this.vertexAttributesMapLabels, politicalMap, countryNames);
        this.binderMapLabels = this.createBinderMapLabels();
        this.pipelineMapLabels = this.createPipelineMapLabels(this.vertexAttributesMapLabels, WgslUtils.getShaderSource("font.wgsl"));
        this.vertexAttributesBuildings = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, "position"), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texCoord"));
        this.meshBuildings = this.generateMeshBuildings(this.vertexAttributesBuildings, politicalMap, mapBuildings);
        this.binderBuildings = this.createBinderBuildings();
        this.pipelineBuildings = this.createPipelineBuildings(this.vertexAttributesBuildings, WgslUtils.getShaderSource("element.wgsl"));
        VertexAttributes vertexAttributesResources = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, "position"), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texCoord"), new VertexAttribute(VertexAttributes.Usage.Generic, 2, "center"));
        this.meshResources = this.generateMeshResources(vertexAttributesResources);
        this.binderResources = this.createBinderResources();
        this.pipelineResources = this.createPipelineResources(vertexAttributesResources, WgslUtils.getShaderSource("element_scale.wgsl"));
        VertexAttributes vertexAttributesRivers = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, "position"), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texCoord"), new VertexAttribute(VertexAttributes.Usage.Generic, 1, "width"));
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

    private WgMesh generateMeshMapLabels(VertexAttributes vertexAttributes, PoliticalMap politicalMap, IntFunction<String> countryNames) {
        ProvinceTable provinces = this.catalog.provinces();
        int provinceCount = provinces.size();
        int[] borderPixels = this.catalog.borderPixels();
        FloatList vertices = new FloatList();
        ShortList indices = new ShortList();
        IntList ownedProvinces = new IntList();
        IntList connectedProvinces = new IntList();
        IntList toProcess = new IntList();
        IntList positionsProvinces = new IntList();
        IntList pixelsBorderProvinces = new IntList();
        boolean[] visited = new boolean[provinceCount];
        for (int countryIndex = 0; countryIndex < this.catalog.countries().size(); countryIndex++) {
            if (!politicalMap.hasProvinces(countryIndex)) {
                continue;
            }
            ownedProvinces.clear();
            for (int provinceIndex = 0; provinceIndex < provinceCount; provinceIndex++) {
                if (politicalMap.owner(provinceIndex) == countryIndex) {
                    ownedProvinces.add(provinceIndex);
                }
            }
            String countryName = countryNames.apply(countryIndex);
            for (int i = 0; i < ownedProvinces.size(); i++) {
                int provinceIndex = ownedProvinces.get(i);
                if (visited[provinceIndex]) {
                    continue;
                }
                connectedProvinces.clear();
                this.collectConnectedProvinces(politicalMap, countryIndex, provinceIndex, visited, connectedProvinces, toProcess);
                if (connectedProvinces.size() > 5 || (connectedProvinces.size() == ownedProvinces.size() && !connectedProvinces.isEmpty())) {
                    positionsProvinces.clear();
                    pixelsBorderProvinces.clear();
                    for (int j = 0; j < connectedProvinces.size(); j++) {
                        int connectedProvince = connectedProvinces.get(j);
                        positionsProvinces.add(provinces.positionX(connectedProvince));
                        positionsProvinces.add(provinces.positionY(connectedProvince));
                        for (int pixel = provinces.borderStart(connectedProvince); pixel < provinces.borderEnd(connectedProvince); pixel++) {
                            pixelsBorderProvinces.add(borderPixels[pixel]);
                        }
                    }
                    this.mapLabelMeshBuilder.generateData(countryName, pixelsBorderProvinces, positionsProvinces, vertices, indices);
                }
            }
        }

        WgMesh mesh = new WgMesh(false, Math.max(vertices.size() / 4, 4), Math.max(indices.size(), 6), vertexAttributes);
        mesh.setVertices(vertices.toArray());
        mesh.setIndices(indices.toArray());

        return mesh;
    }

    private void collectConnectedProvinces(PoliticalMap politicalMap, int countryIndex, int startProvince, boolean[] visited, IntList connectedProvinces, IntList toProcess) {
        ProvinceTable provinces = this.catalog.provinces();
        toProcess.clear();
        toProcess.add(startProvince);
        visited[startProvince] = true;
        connectedProvinces.add(startProvince);

        while (!toProcess.isEmpty()) {
            int current = toProcess.pop();
            for (int position = provinces.adjacencyStart(current); position < provinces.adjacencyEnd(current); position++) {
                int neighbor = provinces.adjacency(position);
                if (visited[neighbor] || politicalMap.owner(neighbor) != countryIndex) {
                    continue;
                }
                visited[neighbor] = true;
                connectedProvinces.add(neighbor);
                toProcess.add(neighbor);
            }
        }
    }

    private WgMesh generateMeshBuildings(VertexAttributes vertexAttributes, PoliticalMap politicalMap, MapBuildings mapBuildings) {
        ProvinceTable provinces = this.catalog.provinces();
        int markers = 0;
        for (int i = 0; i < mapBuildings.count(); i++) {
            if (provinces.buildingPosition(mapBuildings.province(i), mapBuildings.buildingType(i)) != ProvinceTable.NO_POSITION) {
                markers++;
            }
        }
        for (int countryIndex = 0; countryIndex < this.catalog.countries().size(); countryIndex++) {
            if (politicalMap.capital(countryIndex) >= 0 && politicalMap.hasProvinces(countryIndex)) {
                markers++;
            }
        }

        float[] vertices = new float[Math.max(markers, 1) * 4 * 4];
        short[] indices = new short[Math.max(markers, 1) * 6];
        int vertexIndex = 0;
        int indexIndex = 0;
        short vertexOffset = 0;
        int width = 6;
        int height = 6;

        for (int i = 0; i < mapBuildings.count(); i++) {
            long position = provinces.buildingPosition(mapBuildings.province(i), mapBuildings.buildingType(i));
            if (position == ProvinceTable.NO_POSITION) {
                continue;
            }
            String buildingName = this.catalog.buildingTypes().nameId(mapBuildings.buildingType(i));
            TextureRegion buildingRegion = this.mapElementsTextureAtlas.findRegion("building_" + buildingName + "_empty");
            this.addVerticesIndicesBuilding(vertices, indices, vertexIndex, indexIndex, vertexOffset, ProvinceTable.unpackX(position), ProvinceTable.unpackY(position), width, height, buildingRegion);
            vertexIndex += 16;
            indexIndex += 6;
            vertexOffset += 4;
        }

        TextureRegion capitalRegion = this.mapElementsTextureAtlas.findRegion("building_capital");
        for (int countryIndex = 0; countryIndex < this.catalog.countries().size(); countryIndex++) {
            int capital = politicalMap.capital(countryIndex);
            if (capital < 0 || !politicalMap.hasProvinces(countryIndex)) {
                continue;
            }
            this.addVerticesIndicesBuilding(vertices, indices, vertexIndex, indexIndex, vertexOffset, provinces.positionX(capital), provinces.positionY(capital), width, height, capitalRegion);
            vertexIndex += 16;
            indexIndex += 6;
            vertexOffset += 4;
        }

        WgMesh mesh = new WgMesh(true, vertices.length / 4, indices.length, vertexAttributes);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    public void rebuildLabels(PoliticalMap politicalMap, IntFunction<String> countryNames) {
        WgMesh previous = this.meshMapLabels;
        this.meshMapLabels = this.generateMeshMapLabels(this.vertexAttributesMapLabels, politicalMap, countryNames);
        previous.dispose();
    }

    public void rebuildBuildings(PoliticalMap politicalMap, MapBuildings mapBuildings) {
        WgMesh previous = this.meshBuildings;
        this.meshBuildings = this.generateMeshBuildings(this.vertexAttributesBuildings, politicalMap, mapBuildings);
        previous.dispose();
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
        ProvinceTable provinces = this.catalog.provinces();
        int resourceProvinces = 0;
        for (int provinceIndex = 0; provinceIndex < provinces.size(); provinceIndex++) {
            if (provinces.resourceGoodIndex(provinceIndex) >= 0) {
                resourceProvinces++;
            }
        }

        float[] vertices = new float[Math.max(resourceProvinces, 1) * 4 * 6];
        short[] indices = new short[Math.max(resourceProvinces, 1) * 6];
        int vIdx = 0;
        int iIdx = 0;
        short vOff = 0;
        int width = 13;
        int height = 10;

        for (int provinceIndex = 0; provinceIndex < provinces.size(); provinceIndex++) {
            int goodIndex = provinces.resourceGoodIndex(provinceIndex);
            if (goodIndex < 0) {
                continue;
            }
            TextureRegion resourceRegion = this.mapElementsTextureAtlas.findRegion("resource_" + this.catalog.goods().nameId(goodIndex));
            int cx = provinces.positionX(provinceIndex);
            int cy = provinces.positionY(provinceIndex);
            int x = cx - (width / 2);
            int y = cy - (height / 2);

            float u1 = resourceRegion.getU();
            float v1 = resourceRegion.getV2();
            float u2 = resourceRegion.getU2();
            float v2 = resourceRegion.getV();

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

            indices[iIdx++] = vOff;
            indices[iIdx++] = (short) (vOff + 1);
            indices[iIdx++] = (short) (vOff + 2);
            indices[iIdx++] = (short) (vOff + 2);
            indices[iIdx++] = (short) (vOff + 3);
            indices[iIdx++] = vOff;
            vOff += 4;
        }

        WgMesh mesh = new WgMesh(true, vertices.length / 6, indices.length, false, vertexAttributes);

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    private WgMeshMulti generateMeshRivers(VertexAttributes vertexAttributes) {
        RawMeshMulti rawMesh = this.mapDao.readRiversMesh();

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
        PipelineSpecification pipelineSpec = new PipelineSpecification("pipeline specification provinces", vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline";
        pipelineSpec.vertexLayout.setVertexAttributeLocation("position", 0);
        pipelineSpec.vertexLayout.setVertexAttributeLocation("texCoord", 1);
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderProvinces.getPipelineLayout("pipeline layout provinces"), pipelineSpec);
    }

    private WebGPUPipeline createPipelineMapLabels(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification("pipeline specification map labels", vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline map labels";
        pipelineSpec.vertexLayout.setVertexAttributeLocation("position", 0);
        pipelineSpec.vertexLayout.setVertexAttributeLocation("texCoord", 1);
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderMapLabels.getPipelineLayout("pipeline layout map labels"), pipelineSpec);
    }

    private WebGPUPipeline createPipelineBuildings(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification("pipeline specification buildings", vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline";
        pipelineSpec.vertexLayout.setVertexAttributeLocation("position", 0);
        pipelineSpec.vertexLayout.setVertexAttributeLocation("texCoord", 1);
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderBuildings.getPipelineLayout("pipeline layout buildings"), pipelineSpec);
    }

    private WebGPUPipeline createPipelineResources(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification("pipeline specification resources", vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline resources";
        pipelineSpec.vertexLayout.setVertexAttributeLocation("position", 0);
        pipelineSpec.vertexLayout.setVertexAttributeLocation("texCoord", 1);
        pipelineSpec.vertexLayout.setVertexAttributeLocation("center", 2);
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binderResources.getPipelineLayout("pipeline layout resources"), pipelineSpec);
    }

    private WebGPUPipeline createPipelineRivers(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification("pipeline specification rivers", vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline rivers";
        pipelineSpec.vertexLayout.setVertexAttributeLocation("position", 0);
        pipelineSpec.vertexLayout.setVertexAttributeLocation("texCoord", 1);
        pipelineSpec.vertexLayout.setVertexAttributeLocation("width", 2);
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
        this.uploadPixmap(this.mapModeTexture, mapModePixmap);
        this.binderProvinces.setUniform("showTerrain", mapMode == MapMode.TERRAIN ? 1 : 0);
        this.uniformBufferWorld.flush();
    }

    public void updateProvincesTexture(Pixmap provincesPixmap) {
        this.uploadPixmap(this.provincesTexture, provincesPixmap);
    }

    public void updateProvincesStripesTexture(Pixmap provincesStripesPixmap) {
        this.uploadPixmap(this.provincesStripesTexture, provincesStripesPixmap);
    }

    private void uploadPixmap(WgTexture texture, Pixmap pixmap) {
        ByteBuffer pixels = pixmap.getPixels();
        pixels.position(0);
        texture.load(pixels, 4, pixmap.getWidth(), pixmap.getHeight());
        pixels.position(0);
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

        this.meshProvinces.render(pass, 0, this.meshProvinces.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshMapLabels() {
        WebGPURenderPass pass = RenderPassBuilder.create("Map labels pass");
        pass.setPipeline(this.pipelineMapLabels);
        this.binderMapLabels.bindGroup(pass, 0);

        this.meshMapLabels.render(pass, 0, this.meshMapLabels.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshBuildings() {
        WebGPURenderPass pass = RenderPassBuilder.create("Buildings pass");
        pass.setPipeline(this.pipelineBuildings);
        this.binderBuildings.bindGroup(pass, 0);

        this.meshBuildings.render(pass, 0, this.meshBuildings.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshResources() {
        WebGPURenderPass pass = RenderPassBuilder.create("Resources pass");
        pass.setPipeline(this.pipelineResources);
        this.binderResources.bindGroup(pass, 0);

        this.meshResources.render(pass, 0, this.meshResources.getNumIndices(), 3, 0);

        pass.end();
    }

    private void renderMeshRivers() {
        WebGPURenderPass pass = RenderPassBuilder.create("Rivers pass");
        pass.setPipeline(this.pipelineRivers);
        this.binderRivers.bindGroup(pass, 0);

        this.meshRivers.render(pass, this.meshRivers.getNumIndices(), 0, 3, 0);

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
        this.meshRivers.dispose();
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
