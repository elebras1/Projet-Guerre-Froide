package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.IntSet;
import com.populaire.projetguerrefroide.entity.Government;
import com.populaire.projetguerrefroide.entity.Ideology;

import java.util.*;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class World {
    private final List<Country> countries;
    private final IntObjectMap<Province> provinces;
    private LandProvince selectedProvince;
    private Country selectedCountry;
    private Pixmap provincesColorPixmap;
    private Texture provincesColorTexture;
    private Texture countriesColorTexture;
    private Texture waterTexture;
    private Texture colorMapWaterTexture;
    private Texture provincesColorStripesTexture;
    private Texture terrainTexture;
    private Texture stripesTexture;
    private Texture colormapTexture;
    private Texture overlayTileTexture;
    private Texture bordersTexture;
    private Texture defaultTexture;
    private TextureArray terrainSheetArray;
    private ShaderProgram mapShader;
    private ShaderProgram fontShader;

    public World(List<Country> countries, IntObjectMap<Province> provinces) {
        this.countries = countries;
        this.provinces = provinces;
        this.createCountriesColorTexture();
        this.createProvincesColorStripesTexture();
        this.provincesColorPixmap = new Pixmap(Gdx.files.internal("map/provinces.bmp"));
        this.provincesColorTexture = new Texture(this.provincesColorPixmap);
        this.waterTexture = new Texture("map/terrain/sea_normal.png");
        this.waterTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.colorMapWaterTexture = new Texture("map/terrain/colormap_water.png");
        this.colorMapWaterTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.colormapTexture = new Texture("map/terrain/colormap.png");
        this.terrainTexture = new Texture("map/terrain.bmp");
        this.stripesTexture = new Texture("map/terrain/stripes.png");
        this.overlayTileTexture = new Texture("map/terrain/map_overlay_tile.png");
        this.overlayTileTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.defaultTexture = new Texture(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGB888);
        this.createBordersTexture();
        for(Country country : this.countries) {
            country.createLabels();
        }

        String[] terrainTexturePaths = new String[64];
        String pathBase = "map/terrain/textures/";
        for(int i = 0; i < 64; i++) {
            terrainTexturePaths[i] = pathBase + "text_" + i + ".png";
        }
        this.terrainSheetArray = new TextureArray(terrainTexturePaths);

        String vertexMapShader = Gdx.files.internal("shaders/map_v.glsl").readString();
        String fragmentMapShader = Gdx.files.internal("shaders/map_f.glsl").readString();
        this.mapShader = new ShaderProgram(vertexMapShader, fragmentMapShader);
        String vertexFontShader = Gdx.files.internal("shaders/font_v.glsl").readString();
        String fragmentFontShader = Gdx.files.internal("shaders/font_f.glsl").readString();
        this.fontShader = new ShaderProgram(vertexFontShader, fragmentFontShader);
        if (!fontShader.isCompiled()) {
            Gdx.app.error("Shader", "Compilation failed: " + fontShader.getLog());
        }
        ShaderProgram.pedantic = false;
    }

    public LandProvince getProvince(short x, short y) {
        short adjustedX = x;
        if (x < 0) {
            adjustedX += WORLD_WIDTH;
        } else if (x > WORLD_WIDTH) {
            adjustedX -= WORLD_WIDTH;
        }

        int provinceColor = this.provincesColorPixmap.getPixel(adjustedX, y);
        Province province = this.provinces.get(provinceColor);

        if(province instanceof LandProvince landProvince) {
            return landProvince;
        }

        return null;
    }

    public void selectProvince(short x, short y) {
        this.selectedProvince = this.getProvince(x, y);
        if(this.selectedProvince != null) {
            this.selectedCountry = this.selectedProvince.getCountryOwner();
        } else {
            this.selectedCountry = null;
        }
    }

    public Country getSelectedCountry() {
        return this.selectedCountry;
    }

    public void createCountriesColorTexture() {
        Pixmap pixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        for(Province province : provinces.values()) {
            if(province instanceof LandProvince landProvince) {
                for(IntSet.IntSetIterator iterator = landProvince.getPixels().iterator(); iterator.hasNext();) {
                    int pixelInt = iterator.nextInt();
                    int pixelX = (pixelInt >> 16);
                    int pixelY = (pixelInt & 0xFFFF);
                    pixmap.drawPixel(pixelX, pixelY, landProvince.getCountryOwner().getColor());
                }
            }
        }

        this.countriesColorTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void createProvincesColorStripesTexture() {
        Pixmap pixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGBA8888);
        for(Province province : this.provinces.values()) {
            if(province instanceof LandProvince landProvince && !landProvince.getCountryOwner().equals(landProvince.getCountryController())) {
                for(IntSet.IntSetIterator iterator = landProvince.getPixels().iterator(); iterator.hasNext();) {
                    int pixelInt = iterator.nextInt();
                    short pixelX = (short) (pixelInt >> 16);
                    short pixelY = (short) (pixelInt & 0xFFFF);
                    pixmap.drawPixel(pixelX, pixelY, landProvince.getCountryController().getColor());
                }
            }
        }

        this.provincesColorStripesTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void createBordersTexture() {
        Pixmap pixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGBA8888);
        short redBorderCountry = 255;
        short greenBorderRegion = 255;
        short blueBorderProvince = 255;
        short alphaBorder = 255;

        for(Country country : this.countries) {
            IntSet countryPixels = country.getPixels();
            IntSet regionsPixelsBorder = country.getRegionsPixelsBorder();
            IntList provincesPixelsBorder = country.getProvincesPixelsBorder();
            for(int i = 0; i < provincesPixelsBorder.size(); i++) {
                int pixelInt = provincesPixelsBorder.get(i);
                int pixelX = (pixelInt >> 16);
                int pixelY = (pixelInt & 0xFFFF);

                if(country.isPixelBorder((short) pixelX, (short) pixelY, countryPixels)) {
                    pixmap.drawPixel(pixelX, pixelY, redBorderCountry << 24 | greenBorderRegion << 16 | blueBorderProvince << 8 | alphaBorder);
                } else if(regionsPixelsBorder.contains(pixelInt)) {
                    pixmap.drawPixel(pixelX, pixelY, greenBorderRegion << 16 | blueBorderProvince << 8 | alphaBorder);
                } else {
                    pixmap.drawPixel(pixelX, pixelY, blueBorderProvince << 8 | alphaBorder);
                }
            }
        }

        this.bordersTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void render(SpriteBatch batch, OrthographicCamera cam, float time) {
        this.mapShader.bind();
        this.provincesColorTexture.bind(0);
        this.countriesColorTexture.bind(1);
        this.colorMapWaterTexture.bind(2);
        this.waterTexture.bind(3);
        this.terrainTexture.bind(4);
        this.terrainSheetArray.bind(5);
        this.colormapTexture.bind(6);
        this.provincesColorStripesTexture.bind(7);
        this.stripesTexture.bind(8);
        this.overlayTileTexture.bind(9);
        this.bordersTexture.bind(10);
        this.defaultTexture.bind(11);

        this.mapShader.setUniformi("u_textureProvinces", 0);
        this.mapShader.setUniformi("u_textureCountries", 1);
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
        batch.draw(this.countriesColorTexture, -WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(this.countriesColorTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(this.countriesColorTexture, WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT);
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
        this.provincesColorTexture.dispose();
        this.countriesColorTexture.dispose();
        this.provincesColorStripesTexture.dispose();
        this.terrainTexture.dispose();
        this.stripesTexture.dispose();
        this.overlayTileTexture.dispose();
        this.colormapTexture.dispose();
        this.defaultTexture.dispose();
        this.terrainSheetArray.dispose();
    }
}
