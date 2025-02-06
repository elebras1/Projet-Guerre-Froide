package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.IntSet;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.economy.building.Building;
import com.populaire.projetguerrefroide.util.ColorGenerator;

import java.util.*;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class World {
    private final List<Country> countries;
    private final IntObjectMap<LandProvince> provinces;
    private final IntObjectMap<WaterProvince> waterProvinces;
    private final AsyncExecutor asyncExecutor;
    private LandProvince selectedProvince;
    private Country countryPlayer;
    private Pixmap provincesPixmap;
    private Pixmap mapModePixmap;
    private Texture provincesTexture;
    private Texture mapModeTexture;
    private Texture waterTexture;
    private Texture colorMapWaterTexture;
    private Texture provincesStripesTexture;
    private Texture terrainTexture;
    private Texture stripesTexture;
    private Texture colorMapTexture;
    private Texture overlayTileTexture;
    private Texture defaultTexture;
    private TextureArray terrainSheetArray;
    private TextureAtlas mapElementsTextureAtlas;
    private ShaderProgram mapShader;
    private ShaderProgram fontShader;
    private ShaderProgram elementShader;
    private ShaderProgram elementScaleShader;
    private Mesh meshBuildings;
    private Mesh meshResources;
    private MapMode mapMode;

    public World(List<Country> countries, IntObjectMap<LandProvince> provinces, IntObjectMap<WaterProvince> waterProvinces, AsyncExecutor asyncExecutor) {
        this.countries = countries;
        this.provinces = provinces;
        this.waterProvinces = waterProvinces;
        this.asyncExecutor = asyncExecutor;
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
            country.createLabels();
        }
        this.mapModeTexture = new Texture(this.mapModePixmap);
        this.mapModeTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.provincesStripesTexture = new Texture(provincesColorStripesPixmap);
        this.provincesStripesTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.provincesTexture = new Texture(this.provincesPixmap);
        this.provincesTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.waterTexture = new Texture("map/terrain/sea_normal.png");
        this.waterTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.colorMapWaterTexture = new Texture("map/terrain/colormap_water.png");
        this.colorMapWaterTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.colorMapTexture = new Texture("map/terrain/colormap.png");
        this.terrainTexture = new Texture("map/terrain.bmp");
        this.stripesTexture = new Texture("map/terrain/stripes.png");
        this.overlayTileTexture = new Texture("map/terrain/map_overlay_tile.png");
        this.overlayTileTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.defaultTexture = new Texture(0, 0, Pixmap.Format.RGB565);
        this.terrainSheetArray = new TextureArray(terrainTexturePaths);
        this.mapElementsTextureAtlas = new TextureAtlas("map/elements/map_elements.atlas");
        this.mapElementsTextureAtlas.getTextures().first().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.meshBuildings = this.generateMeshBuildings();
        this.meshResources = this.generateMeshResources();

        String vertexMapShader = Gdx.files.internal("shaders/map_v.glsl").readString();
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
        ShaderProgram.pedantic = false;
    }

    public LandProvince getProvince(short x, short y) {
        x = (short) ((x + WORLD_WIDTH) % WORLD_WIDTH);

        int provinceColor = this.provincesPixmap.getPixel(x, y);
        int provinceColorRGB = (provinceColor & 0xFFFFFF00) | 255;

        return this.provinces.get(provinceColorRGB);
    }

    public void selectProvince(short x, short y) {
        this.selectedProvince = this.getProvince(x, y);
    }

    public LandProvince getSelectedProvince() {
        return this.selectedProvince;
    }

    public boolean setCountryPlayer() {
        if(this.selectedProvince != null) {
            this.countryPlayer = this.selectedProvince.getCountryOwner();
            return true;
        }

        return false;
    }

    public short getNumberOfProvinces() {
        return (short) this.provinces.size();
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
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, province.getCountryOwner().getColor());
        }
    }

    public void updatePixmapIdeologiesColor() {
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, province.getCountryOwner().getIdeology().getColor());
        }
    }

    public void updatePixmapCulturesColor() {
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, province.getCountryOwner().getCulture().getColor());
        }
    }

    public void updatePixmapReligionsColor() {
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, province.getCountryOwner().getReligion().getColor());
        }
    }

    public void updatePixmapResourcesColor() {
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            if(province.getGood() != null) {
                this.mapModePixmap.drawPixel(red, green, province.getGood().getColor());
            } else {
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getWhiteRGBA());
            }
        }
    }

    public void updatePixmapRegionColor() {
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getDeterministicRGBA(province.getRegion().getId()));
        }
    }

    public void updatePixmapTerrainColor() {
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getWhiteRGBA());
        }
    }

    public void updatePixmapTerrain2Color() {
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, province.getTerrain().getColor());
        }
    }

    public void updatePixmapPopulationColor() {
        int maxPopulation = 0;
        for (LandProvince province : this.provinces.values()) {
            maxPopulation = Math.max(maxPopulation, province.getPopulation().getSize());
        }

        for (LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            int pop = province.getPopulation().getSize();
            float ratio = (maxPopulation > 0) ? (float) pop / maxPopulation : 0f;
            this.mapModePixmap.drawPixel(red, green, ColorGenerator.getMagmaColorRGBA(ratio));
        }
    }


    public void updatePixmapRelationsColor() {
        ObjectIntMap<Country> relations = this.countryPlayer.getRelations();

        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);

            if(province.getCountryOwner().equals(this.countryPlayer)) {
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
                int color = province.getColor();
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
        this.mapModeTexture = new Texture(this.mapModePixmap);
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
        } else if (!provinceRight.getRegion().equals(region) || !provinceLeft.getRegion().equals(region) || !provinceUp.getRegion().equals(region) || !provinceDown.getRegion().equals(region)) {
            return 77;
        } else {
            return 0;
        }
    }

    public Mesh generateMeshBuildings() {
        int numBuildings = 0;

        for (Country country : this.countries) {
            if (country.getCapital() != null && !country.getProvinces().isEmpty()) {
                numBuildings++;
            }
            for (LandProvince province : country.getProvinces()) {
                for (Building building : province.getBuildings().keySet()) {
                    if (building.isOnMap()) {
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

                addVerticesIndicesBuilding(vertices, indices, vertexIndex, indexIndex, vertexOffset, cx, cy, width, height, capitalRegion);

                vertexIndex += 16;
                indexIndex += 6;
                vertexOffset += 4;
            }

            for (LandProvince province : country.getProvinces()) {
                for (Building building : province.getBuildings().keySet()) {
                    if (!building.isOnMap()) {
                        continue;
                    }

                    TextureRegion buildingRegion = this.mapElementsTextureAtlas.findRegion("building_" + building.getName() + "_empty");

                    int buildingPosition = province.getPosition(building.getName());
                    short bx = (short) (buildingPosition >> 16);
                    short by = (short) (buildingPosition & 0xFFFF);

                    addVerticesIndicesBuilding(vertices, indices, vertexIndex, indexIndex, vertexOffset, bx, by, width, height, buildingRegion);

                    vertexIndex += 16;
                    indexIndex += 6;
                    vertexOffset += 4;
                }
            }
        }

        Mesh mesh = new Mesh(true, vertices.length / 4, indices.length,
            new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));

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

    public Mesh generateMeshResources() {
        int numProvinces = 0;
        for(LandProvince province : this.provinces.values()) {
            if(province.getGood() != null) {
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

        for (LandProvince province : this.provinces.values()) {
            if(province.getGood() == null) {
                continue;
            }
            TextureRegion resourceRegion = this.mapElementsTextureAtlas.findRegion("resource_" + province.getGood().getName());

            int ressourcePosition = province.getPosition("default");
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

        Mesh mesh = new Mesh(true, vertices.length / 6, indices.length,
            new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
            new VertexAttribute(VertexAttributes.Usage.Generic, 2, "a_center"));

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
    }

    public void render(SpriteBatch batch, OrthographicCamera cam, float time) {
        this.mapShader.bind();
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
        this.defaultTexture.bind(10);

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
        this.mapShader.setUniformi("u_textureBorders", 10);
        this.mapShader.setUniformf("u_zoom", cam.zoom);
        this.mapShader.setUniformf("u_time", time);
        this.mapShader.setUniformi("u_showTerrain", this.mapMode == MapMode.TERRAIN ? 1 : 0);
        if(this.selectedProvince != null) {
            int color = this.selectedProvince.getColor();
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

        this.fontShader.bind();
        this.fontShader.setUniformf("u_zoom", cam.zoom);
        batch.setShader(this.fontShader);
        for(Country country : this.countries) {
            for(MapLabel label : country.getLabels()) {
                label.render(batch);
            }
        }
        batch.setShader(null);
        batch.end();
        if(cam.zoom <= 0.8f) {
            this.renderMeshBuildings(cam);
        }
        if(this.mapMode == MapMode.RESOURCES && cam.zoom <= 0.8f) {
            this.renderMeshResources(cam);
        }
    }

    private void renderMeshBuildings(OrthographicCamera cam) {
        this.elementShader.bind();
        this.mapElementsTextureAtlas.getTextures().first().bind(0);
        this.elementShader.setUniformi("u_texture", 0);
        this.elementShader.setUniformMatrix("u_projTrans", cam.combined);
        this.elementShader.setUniformi("u_worldWidth", WORLD_WIDTH);
        this.meshBuildings.bind(this.elementShader);
        Gdx.gl.glEnable(GL32.GL_BLEND);
        Gdx.gl.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl32.glDrawElementsInstanced(GL32.GL_TRIANGLES, this.meshBuildings.getNumIndices(), GL32.GL_UNSIGNED_SHORT, 0, 3);
        this.meshBuildings.unbind(this.elementShader);
    }

    private void renderMeshResources(OrthographicCamera cam) {
        this.elementScaleShader.bind();
        this.mapElementsTextureAtlas.getTextures().first().bind(0);
        this.elementScaleShader.setUniformi("u_texture", 0);
        this.elementScaleShader.setUniformMatrix("u_projTrans", cam.combined);
        this.elementScaleShader.setUniformf("u_zoom", cam.zoom);
        this.elementScaleShader.setUniformi("u_worldWidth", WORLD_WIDTH);
        this.meshResources.bind(this.elementScaleShader);
        Gdx.gl.glEnable(GL32.GL_BLEND);
        Gdx.gl.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl32.glDrawElementsInstanced(GL32.GL_TRIANGLES, this.meshResources.getNumIndices(), GL32.GL_UNSIGNED_SHORT, 0, 3);
        this.meshResources.unbind(this.elementScaleShader);
    }

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
        this.defaultTexture.dispose();
        this.terrainSheetArray.dispose();
        this.mapModePixmap.dispose();
        this.provincesPixmap.dispose();
        this.mapShader.dispose();
        this.fontShader.dispose();
        this.elementScaleShader.dispose();
        this.meshResources.dispose();
        this.meshBuildings.dispose();
        this.mapElementsTextureAtlas.dispose();
    }
}
