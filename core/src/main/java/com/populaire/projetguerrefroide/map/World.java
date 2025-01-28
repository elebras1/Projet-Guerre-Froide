package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.IntSet;

import java.util.*;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class World {
    private final List<Country> countries;
    private final IntObjectMap<LandProvince> provinces;
    private final IntObjectMap<WaterProvince> waterProvinces;
    private final AsyncExecutor asyncExecutor;
    private LandProvince selectedProvince;
    private Pixmap provincesPixmap;
    private Pixmap mapModePixmap;
    private Texture provincesTexture;
    private Texture mapModeTexture;
    private Texture bordersTexture;
    private Texture waterTexture;
    private Texture colorMapWaterTexture;
    private Texture provincesStripesTexture;
    private Texture terrainTexture;
    private Texture stripesTexture;
    private Texture colorMapTexture;
    private Texture overlayTileTexture;
    private Texture defaultTexture;
    private TextureArray terrainSheetArray;
    private ShaderProgram mapShader;
    private ShaderProgram fontShader;

    public World(List<Country> countries, IntObjectMap<LandProvince> provinces, IntObjectMap<WaterProvince> waterProvinces, AsyncExecutor asyncExecutor) {
        this.countries = countries;
        this.provinces = provinces;
        this.waterProvinces = waterProvinces;
        this.asyncExecutor = asyncExecutor;
        this.mapModePixmap = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        this.mapModePixmap.setBlending(Pixmap.Blending.None);
        this.mapModePixmap.setColor(0, 0, 0, 0);
        this.mapModePixmap.fill();
        this.updatePixmapCountriesColor();
        Pixmap bordersPixmap = this.createBordersPixmap();
        this.provincesPixmap = new Pixmap(Gdx.files.internal("map/provinces.bmp"));
        Pixmap provincesColorStripesPixmap = this.createProvincesColorStripesPixmap();
        String[] terrainTexturePaths = this.createTerrainTexturePaths();

        for(Country country : this.countries) {
            country.createLabels();
        }
        this.mapModeTexture = new Texture(this.mapModePixmap);
        this.mapModeTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.provincesStripesTexture = new Texture(provincesColorStripesPixmap);
        this.provincesStripesTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.bordersTexture = new Texture(bordersPixmap);
        this.bordersTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
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

        String vertexMapShader = Gdx.files.internal("shaders/map_v.glsl").readString();
        String fragmentMapShader = Gdx.files.internal("shaders/map_f.glsl").readString();
        this.mapShader = new ShaderProgram(vertexMapShader, fragmentMapShader);
        String vertexFontShader = Gdx.files.internal("shaders/font_v.glsl").readString();
        String fragmentFontShader = Gdx.files.internal("shaders/font_f.glsl").readString();
        this.fontShader = new ShaderProgram(vertexFontShader, fragmentFontShader);
        ShaderProgram.pedantic = false;
    }

    public LandProvince getProvince(short x, short y) {
        short adjustedX = x;
        if (x < 0) {
            adjustedX += WORLD_WIDTH;
        } else if (x > WORLD_WIDTH) {
            adjustedX -= WORLD_WIDTH;
        }

        int provinceColor = this.provincesPixmap.getPixel(adjustedX, y);

        return this.provinces.get(provinceColor);
    }

    public void selectProvince(short x, short y) {
        this.selectedProvince = this.getProvince(x, y);
    }

    public LandProvince getSelectedProvince() {
        return this.selectedProvince;
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

    public void updatePixmapTerrainColor() {
        int whiteColor = 0xFFFFFFFF;
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            this.mapModePixmap.drawPixel(red, green, whiteColor);
        }
    }

    public void updatePixmapResourcesColor() {
        int whiteColor = 0xFFFFFFFF;
        for(LandProvince province : this.provinces.values()) {
            int color = province.getColor();
            short red = (short) ((color >> 24) & 0xFF);
            short green = (short) ((color >> 16) & 0xFF);
            if(province.getGood() != null) {
                this.mapModePixmap.drawPixel(red, green, province.getGood().getColor());
            } else {
                this.mapModePixmap.drawPixel(red, green, whiteColor);
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

    public Pixmap createBordersPixmap() {
        Pixmap pixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGBA8888);
        short redBorderCountry = 255;
        short greenBorderRegion = 255;
        short blueBorderProvince = 255;
        short alphaBorder = 255;

        for(Country country : this.countries) {
            IntSet countryPixelsBorder = country.getPixelsBorder();
            IntSet regionsPixelsBorder = country.getRegionsPixelsBorder();
            IntList provincesPixelsBorder = country.getProvincesPixelsBorder();
            for(int i = 0; i < provincesPixelsBorder.size(); i++) {
                int pixelInt = provincesPixelsBorder.get(i);
                int pixelX = (pixelInt >> 16);
                int pixelY = (pixelInt & 0xFFFF);

                if(countryPixelsBorder.contains(pixelInt)) {
                    pixmap.drawPixel(pixelX, pixelY, redBorderCountry << 24 | greenBorderRegion << 16 | blueBorderProvince << 8 | alphaBorder);
                } else if(regionsPixelsBorder.contains(pixelInt)) {
                    pixmap.drawPixel(pixelX, pixelY, greenBorderRegion << 16 | blueBorderProvince << 8 | alphaBorder);
                } else {
                    pixmap.drawPixel(pixelX, pixelY, blueBorderProvince << 8 | alphaBorder);
                }
            }
        }

        return pixmap;
    }

    public void changeMapMode(String mapMode) {
        switch(mapMode) {
            case "mapmode_political":
                this.updatePixmapCountriesColor();
                break;
            case "mapmode_strength":
                this.updatePixmapIdeologiesColor();
                break;
            case "mapmode_diplomatic":
                this.updatePixmapCulturesColor();
                break;
            case "mapmode_intel":
                this.updatePixmapReligionsColor();
                break;
            case "mapmode_terrain":
                this.updatePixmapTerrainColor();
                break;
            case "mapmode_resources":
                this.updatePixmapResourcesColor();
                break;
        }

        this.mapModeTexture.dispose();
        this.mapModeTexture = new Texture(this.mapModePixmap);
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
        this.bordersTexture.bind(10);
        this.defaultTexture.bind(11);

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
        batch.draw(this.mapModeTexture, -WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(this.mapModeTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(this.mapModeTexture, WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT);
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
    }
}
