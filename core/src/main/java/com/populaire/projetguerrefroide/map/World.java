package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.populaire.projetguerrefroide.utils.DataManager;
import com.populaire.projetguerrefroide.utils.Logging;

import java.util.*;
import java.util.logging.Logger;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class World {
    private final DataManager dataManager;
    private final List<Country> countries;
    private final Map<Color, Province> provinces;
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
    private static final Logger LOGGER = Logging.getLogger(World.class.getName());

    public World() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        LOGGER.info("Max memory: " + maxMemory / 1024 / 1024 + "MB");
        long startTime = System.currentTimeMillis();
        long startTimeDataManager = System.currentTimeMillis();
        this.dataManager = new DataManager();
        this.countries = this.dataManager.loadCountries();
        this.provinces = this.dataManager.loadProvinces();
        long endTimeDataManager = System.currentTimeMillis();
        LOGGER.info("DataManager loaded in " + (endTimeDataManager - startTimeDataManager) + "ms");
        long startTimeTextures = System.currentTimeMillis();
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

        String[] terrainTexturePaths = new String[64];
        String pathBase = "map/terrain/textures/";
        for(int i = 0; i < 64; i++) {
            terrainTexturePaths[i] = pathBase + "text_" + i + ".png";
        }
        this.terrainSheetArray = new TextureArray(terrainTexturePaths);
        long endTimeTextures = System.currentTimeMillis();
        LOGGER.info("Textures loaded in " + (endTimeTextures - startTimeTextures) + "ms");

        String vertexShader = Gdx.files.internal("shaders/map_v.glsl").readString();
        String fragmentShader = Gdx.files.internal("shaders/map_f.glsl").readString();
        this.mapShader = new ShaderProgram(vertexShader, fragmentShader);
        ShaderProgram.pedantic = false;

        long endTime = System.currentTimeMillis();
        LOGGER.info("World loaded in " + (endTime - startTime) + "ms");
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public LandProvince getProvinceByPixel(short x, short y) {
        short adjustedX = x;
        if (x < 0) {
            adjustedX += WORLD_WIDTH;
        } else if (x > WORLD_WIDTH) {
            adjustedX -= WORLD_WIDTH;
        }

        Color provinceColor = new Color();
        Color.rgba8888ToColor(provinceColor, this.provincesColorPixmap.getPixel(adjustedX, y));

        Province province = this.provinces.get(provinceColor);

        if(province instanceof LandProvince) {
            return (LandProvince) province;
        }

        return null;
    }


    public void selectProvince(short x, short y) {
        this.selectedProvince = this.getProvinceByPixel(x, y);
        if(this.selectedProvince != null) {
            this.selectedCountry = this.selectedProvince.getCountryOwner();
        } else {
            this.selectedCountry = null;
        }
    }

    public Country getSelectedCountry() {
        return this.selectedCountry;
    }

    private List<WaterProvince> getWaterProvinces() {
        List<WaterProvince> waterProvinces = new ArrayList<>();
        for(Province province : this.provinces.values()) {
            if(province instanceof WaterProvince) {
                waterProvinces.add((WaterProvince) province);
            }
        }

        return waterProvinces;
    }

    public void createCountriesColorTexture() {
        Pixmap pixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        for(Province province : provinces.values()) {
            if(province instanceof LandProvince) {
                for(Pixel pixel : ((LandProvince) province).getPixels()) {
                    pixmap.drawPixel(pixel.getX(), pixel.getY(), Color.rgba8888(((LandProvince) province).getCountryOwner().getColor()));
                }
            }
        }

        for (Country country : this.countries) {
            country.createLabels();
            for (MapLabel label : country.getLabels()) {
                Pixel centroid = label.getCentroid();
                Pixel[] farthestPoints = label.getFarthestPoints();
                pixmap.setColor(Color.GREEN);
                pixmap.drawCircle(farthestPoints[0].getX(), farthestPoints[0].getY(), 10);
                pixmap.drawCircle(farthestPoints[1].getX(), farthestPoints[1].getY(), 10);

                pixmap.setColor(Color.RED);
                pixmap.drawCircle(centroid.getX(), centroid.getY(), 10);

                pixmap.setColor(Color.BLUE);
                pixmap.drawLine(farthestPoints[0].getX(), farthestPoints[0].getY(), centroid.getX(), centroid.getY());
                pixmap.drawLine(farthestPoints[1].getX(), farthestPoints[1].getY(), centroid.getX(), centroid.getY());
            }
        }

        this.countriesColorTexture = new Texture(pixmap);
        pixmap.dispose();
    }



    public void createProvincesColorStripesTexture() {
        Pixmap pixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGBA8888);
        for(Province province : this.provinces.values()) {
            if(province instanceof LandProvince && !((LandProvince) province).getCountryOwner().equals(((LandProvince) province).getCountryController())) {
                for(Pixel pixel : ((LandProvince) province).getPixels()) {
                    pixmap.drawPixel(pixel.getX(), pixel.getY(), Color.rgba8888(((LandProvince) province).getCountryController().getColor()));
                }
            }
        }

        this.provincesColorStripesTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void createBordersTexture() {
        Pixmap pixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGBA8888);
        for(Country country : this.countries) {
            for(Pixel pixel : country.getProvincesPixelsBorder()) {
                pixmap.drawPixel(pixel.getX(), pixel.getY(), Color.rgba8888(Color.BLACK));
            }
        }

        this.bordersTexture = new Texture(pixmap);
        pixmap.dispose();
    }


    public void render(SpriteBatch batch, OrthographicCamera cam, float time) {
        /*this.mapShader.bind();
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
            this.mapShader.setUniformf("u_colorProvinceSelected", this.selectedProvince.getColor());
        } else {
            this.mapShader.setUniformf("u_colorProvinceSelected", new Color(0, 0, 0, 0));
        }
        this.mapShader.setUniformMatrix("u_projTrans", cam.combined);

        batch.setShader(this.mapShader);*/
        batch.begin();
        batch.draw(this.countriesColorTexture, -WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(this.countriesColorTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(this.countriesColorTexture, WORLD_WIDTH, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.end();
        batch.setShader(null);

        Gdx.gl.glActiveTexture(GL32.GL_TEXTURE0);
        Gdx.gl.glBindTexture(GL32.GL_TEXTURE_2D, 0);
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
