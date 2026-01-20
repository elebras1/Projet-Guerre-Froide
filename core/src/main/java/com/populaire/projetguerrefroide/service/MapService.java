package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Disposable;
import com.github.elebras1.flecs.*;
import com.github.tommyettinger.ds.*;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.component.Color;
import com.populaire.projetguerrefroide.dao.MapDao;
import com.populaire.projetguerrefroide.pojo.Borders;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.pojo.MapTextures;
import com.populaire.projetguerrefroide.renderer.MapRenderer;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.screen.WorldContext;
import com.populaire.projetguerrefroide.util.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import java.util.*;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class MapService implements WorldContext, Disposable {
    private final GameContext gameContext;
    private final QueryRepository queryRepository;
    private final MapDao mapDao;
    private final CountryService countryService;
    private final IntLongMap provinces;
    private final Borders borders;
    private final Pixmap provincesPixmap;
    private final Pixmap mapModePixmap;
    private final Pixmap provincesColorStripesPixmap;
    private long selectedProvinceId;
    private long playerCountryId;
    private MapMode mapMode;
    private final MapRenderer mapRenderer;

    public MapService(GameContext gameContext, QueryRepository queryRepository, MapDao mapDao, CountryService countryService, IntLongMap provinces, Borders borders) {
        this.gameContext = gameContext;
        this.queryRepository = queryRepository;
        this.mapDao = mapDao;
        this.countryService = countryService;
        this.provinces = provinces;
        this.borders = borders;
        this.mapMode = MapMode.POLITICAL;
        this.mapModePixmap = this.createMapModePixmap();
        this.provincesPixmap = this.createProvincesPixmap();
        this.updatePixmapCountriesColor();
        this.updateBordersProvincesPixmap();
        this.provincesColorStripesPixmap = this.createProvincesColorStripesPixmap();
        MapTextures mapTextures = this.createMapTextures();
        this.mapRenderer = new MapRenderer(this.countryService, this.gameContext, this.mapDao, this.queryRepository, mapTextures, this.borders);
    }

    private Pixmap createMapModePixmap() {
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
        for(int i = 0; i < 64; i++) {
            terrainTexturePaths[i] = Gdx.files.internal(pathBase + "text_" + i + ".png");
        }
        return terrainTexturePaths;
    }

    @Override
    public long getPlayerCountryId() {
        return this.playerCountryId;
    }

    public long getLandProvinceId(int x, int y) {
        x = (x + WORLD_WIDTH) % WORLD_WIDTH;

        int provinceColor = this.provincesPixmap.getPixel(x, y);
        int provinceColorRGB = (provinceColor & 0xFFFFFF00) | 255;

        return this.provinces.get(provinceColorRGB);
    }

    public boolean selectProvince(int x, int y) {
        World ecsWorld = this.gameContext.getEcsWorld();
        long selectedProvinceId = this.getLandProvinceId(x, y);
        if(selectedProvinceId != 0) {
            this.selectedProvinceId = selectedProvinceId;
            Entity selectedProvinceEntity = ecsWorld.obtainEntity(this.selectedProvinceId);
            int color = selectedProvinceEntity.get(Color.class).value();
            float r = ((color >> 24) & 0xFF) / 255f;
            float g = ((color >> 16) & 0xFF) / 255f;
            float b = ((color >> 8) & 0xFF) / 255f;
            float a = (color & 0xFF) / 255f;
            this.mapRenderer.updateSelectedProvince(r, g, b, a);
        } else {
            this.selectedProvinceId = -1;
            this.mapRenderer.updateSelectedProvince(0f, 0f, 0f, 0f);
        }

        return this.selectedProvinceId != -1;
    }

    public long getSelectedProvinceId() {
        return this.selectedProvinceId;
    }

    public boolean setCountryPlayer() {
        World ecsWorld = this.gameContext.getEcsWorld();
        if(this.selectedProvinceId != 0) {
            Entity selectedProvinceEntity = ecsWorld.obtainEntity(this.selectedProvinceId);
            Province selectedProvinceData = selectedProvinceEntity.get(Province.class);
            this.playerCountryId = selectedProvinceData.ownerId();
            return true;
        }

        return false;
    }

    public MapMode getMapMode() {
        return this.mapMode;
    }

    public int getNumberOfProvinces() {
        return this.provinces.size();
    }

    public int getPopulationAmountOfProvince(long provinceEntityId) {
        Entity provinceEntity = this.gameContext.getEcsWorld().obtainEntity(provinceEntityId);
        Province provinceData = provinceEntity.get(Province.class);
        return provinceData.amountChildren() + provinceData.amountAdults() + provinceData.amountSeniors();
    }

    public int getPopulationAmountOfCountry(long countryEntityId) {
        MutableInt population = new MutableInt(0);
        Query query = this.queryRepository.getProvinces();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                if(provinceView.ownerId() == countryEntityId) {
                    population.increment(provinceView.amountChildren() + provinceView.amountAdults() + provinceView.amountSeniors());
                }
            }
        });

        return population.getValue();
    }

    public String getResourceGoodName(long provinceEntityId) {
        Entity provinceEntity = this.gameContext.getEcsWorld().obtainEntity(provinceEntityId);
        ResourceGathering resourceGathering = provinceEntity.get(ResourceGathering.class);
        if(resourceGathering != null) {
            return this.gameContext.getEcsWorld().obtainEntity(resourceGathering.goodId()).getName();
        }
        return null;
    }

    private void updatePixmapCountriesColor() {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        Query query = this.queryRepository.getProvincesWithColor();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;

                EntityView countryView = ecsWorld.obtainEntityView(provinceView.ownerId());
                ColorView countryColor = countryView.getMutView(Color.class);

                long countryColonizerId = countryView.target(ecsConstants.isColonyOf());
                if(countryColonizerId != 0) {
                    EntityView colonyView = ecsWorld.obtainEntityView(countryColonizerId);
                    countryColor = colonyView.getMutView(Color.class);
                }

                this.mapModePixmap.drawPixel(red, green, countryColor.value());
            }
        });
    }

    private void updatePixmapIdeologiesColor(World ecsWorld) {
        Query query = this.queryRepository.getProvincesWithColor();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;

                EntityView countryOwnerView = ecsWorld.obtainEntityView(provinceView.ownerId());
                CountryView countryOwnerDataView = countryOwnerView.getMutView(Country.class);
                EntityView ideologyView = ecsWorld.obtainEntityView(countryOwnerDataView.ideologyId());
                IdeologyView ideologyDataView = ideologyView.getMutView(Ideology.class);
                this.mapModePixmap.drawPixel(red, green, ideologyDataView.color());
            }
        });
    }

    private void updatePixmapCulturesColor(World ecsWorld) {
        Query query = this.queryRepository.getProvincesWithColorAndCultureDistribution();
        query.iter(iter -> {
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;

                long provinceId = iter.entity(i);
                EntityView provinceView = ecsWorld.obtainEntityView(provinceId);
                CultureDistributionView cultureDistributionView = provinceView.getMutView(CultureDistribution.class);

                int biggestCultureIndex = -1;
                int biggestCultureAmount = 0;
                for(int j = 0; j < cultureDistributionView.populationIdsLength(); j++) {
                    if(cultureDistributionView.populationIds(j) != 0 && cultureDistributionView.populationAmounts(j) > biggestCultureAmount) {
                        biggestCultureAmount = cultureDistributionView.populationAmounts(j);
                        biggestCultureIndex = j;
                    }
                }
                if(biggestCultureIndex != -1) {
                    long biggestCultureId = cultureDistributionView.populationIds(biggestCultureIndex);
                    this.mapModePixmap.drawPixel(red, green, Objects.requireNonNull(ecsWorld.obtainEntity(biggestCultureId).get(Color.class)).value());
                }
            }
        });
    }

    private void updatePixmapReligionsColor(World ecsWorld) {
        Query query = this.queryRepository.getProvincesWithColorAndReligionDistribution();
        query.iter(iter -> {
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;

                long provinceId = iter.entity(i);
                EntityView provinceView = ecsWorld.obtainEntityView(provinceId);
                ReligionDistributionView religionDistributionView = provinceView.getMutView(ReligionDistribution.class);

                int biggestReligionIndex = -1;
                int biggestReligionAmount = 0;
                for(int j = 0; j < religionDistributionView.populationIdsLength(); j++) {
                    if(religionDistributionView.populationIds(j) != 0 && religionDistributionView.populationAmounts(j) > biggestReligionAmount) {
                        biggestReligionAmount = religionDistributionView.populationAmounts(j);
                        biggestReligionIndex = j;
                    }
                }
                if(biggestReligionIndex != -1) {
                    long biggestReligionId = religionDistributionView.populationIds(biggestReligionIndex);
                    this.mapModePixmap.drawPixel(red, green, Objects.requireNonNull(ecsWorld.obtainEntity(biggestReligionId).get(Color.class)).value());
                }
            }
        });
    }

    private void updatePixmapResourcesColor() {
        World ecsWorld = this.gameContext.getEcsWorld();
        Query queryProvince = this.queryRepository.getProvincesWithColor();
        queryProvince.iter(iter -> {
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getWhiteRGBA());
            }
        });

        Query queryRgo = this.queryRepository.getProvincesWithColorAndResourceGathering();
        queryRgo.iter(iter -> {
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;

                long provinceId = iter.entity(i);
                EntityView provinceView = ecsWorld.obtainEntityView(provinceId);
                ResourceGatheringView resourceGatheringView = provinceView.getMutView(ResourceGathering.class);
                long resourceGoodId = resourceGatheringView.goodId();
                if(resourceGoodId != -1) {
                    Color goodColor = ecsWorld.obtainEntity(resourceGoodId).get(Color.class);
                    this.mapModePixmap.drawPixel(red, green, goodColor.value());
                }
            }
        });
    }

    private void updatePixmapRegionColor() {
        World ecsWorld = this.gameContext.getEcsWorld();
        Query query = this.queryRepository.getProvincesWithColorAndGeoHierarchy();
        query.iter(iter -> {
            Field<Color> colorField = iter.field(Color.class, 1);
            Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 2);
            for(int i = 0; i < iter.count(); i++) {
                ColorView colorView = colorField.getMutView(i);
                GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);
                EntityView regionView = ecsWorld.obtainEntityView(geoHierarchyView.regionId());
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getDeterministicRGBA(regionView.getName()));
            }
        });
    }

    private void updatePixmapTerrainColor() {
        Query query = this.queryRepository.getProvincesWithColor();
        query.iter(iter -> {
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getWhiteRGBA());
            }
        });
    }

    private void updatePixmapTerrain2Color(World ecsWorld) {
        Query query = this.queryRepository.getProvincesWithColor();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;

                EntityView terrainView = ecsWorld.obtainEntityView(provinceView.terrainId());
                TerrainView terrainDataView = terrainView.getMutView(Terrain.class);
                this.mapModePixmap.drawPixel(red, green, terrainDataView.color());
            }
        });
    }

    private void updatePixmapPopulationColor() {
        MutableInt maxPopulation = new MutableInt(0);

        Query query = this.queryRepository.getProvinces();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                int pop = provinceView.amountChildren() + provinceView.amountAdults() + provinceView.amountSeniors();
                if(pop > maxPopulation.getValue()) {
                    maxPopulation.setValue(pop);
                }
            }
        });

        Query queryProvincesColor = this.queryRepository.getProvincesWithColor();
        queryProvincesColor.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;

                int pop = provinceView.amountChildren() + provinceView.amountAdults() + provinceView.amountSeniors();
                float ratio = (maxPopulation.getValue() > 0) ? (float) pop / maxPopulation.getValue() : 0f;
                this.mapModePixmap.drawPixel(red, green, ColorGenerator.getMagmaColorRGBA(ratio));
            }
        });
    }


    private void updatePixmapRelationsColor() {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity playerCountryEntity = ecsWorld.obtainEntity(this.playerCountryId);

        Query query = this.queryRepository.getProvincesWithColor();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                ColorView colorView = colorField.getMutView(i);
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;

                if(this.playerCountryId == provinceView.ownerId()) {
                    this.mapModePixmap.drawPixel(red, green, ColorGenerator.getLightBlueRGBA());
                } else if (provinceView.ownerId() != 0) {
                    DiplomaticRelationView relationView = playerCountryEntity.getMutView(DiplomaticRelation.class, provinceView.ownerId());
                    if (relationView != null) {
                        this.mapModePixmap.drawPixel(red, green, ColorGenerator.getRedToGreenGradientRGBA(relationView.value(), 200));
                    } else {
                        this.mapModePixmap.drawPixel(red, green, ColorGenerator.getGreyRGBA());
                    }
                } else {
                    this.mapModePixmap.drawPixel(red, green, ColorGenerator.getGreyRGBA());
                }
            }
        });
    }

    private Pixmap createProvincesColorStripesPixmap() {
        Pixmap pixmap = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        World ecsWorld = this.gameContext.getEcsWorld();
        Query query = this.queryRepository.getProvincesWithColor();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<Color> colorField = iter.field(Color.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                if(provinceView.ownerId() == provinceView.controllerId()) {
                    continue;
                }
                ColorView colorView = colorField.getMutView(i);
                EntityView countryView = ecsWorld.obtainEntityView(provinceView.controllerId());
                int color = colorView.value();
                int red = (color >> 24) & 0xFF;
                int green = (color >> 16) & 0xFF;
                ColorView countryColorView = countryView.getMutView(Color.class);
                pixmap.drawPixel(red, green, countryColorView.value());
            }
        });

        return pixmap;
    }

    private void updateBordersProvincesPixmap() {
        World ecsWorld = this.gameContext.getEcsWorld();
        int[] xyBorders = this.borders.getPixels();
        Query provinceQuery = this.queryRepository.getProvincesWithBorderAndGeoHierarchy();
        provinceQuery.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<Border> borderField = iter.field(Border.class, 1);
            Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 2);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                BorderView borderView = borderField.getMutView(i);
                GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);
                for(int j = borderView.startIndex(); j < borderView.endIndex(); j = j + 2) {
                    int x = xyBorders[j];
                    int y = xyBorders[j + 1];

                    int color = this.provincesPixmap.getPixel(x, y);
                    int red = (color >> 24) & 0xFF;
                    int green = (color >> 16) & 0xFF;
                    int blue = (color >> 8) & 0xFF;

                    color = (red << 24) | (green << 16) | (blue << 8) | this.getBorderType(ecsWorld, x, y, provinceView.ownerId(), geoHierarchyView.regionId());
                    this.provincesPixmap.drawPixel(x, y, color);
                }
            }
        });
    }

    private int getBorderType(World ecsWorld, int x, int y, long countryId, long regionId) {
        long provinceRightId = this.getLandProvinceId((x + 1), y);
        if(provinceRightId == 0) {
            return 0; // water, nothing or province border
        }
        Entity provinceRight = ecsWorld.obtainEntity(provinceRightId);
        Province provinceRightData = provinceRight.get(Province.class);
        long provinceLeftId = this.getLandProvinceId((x - 1), y);
        if(provinceLeftId == 0) {
            return 0; // water, nothing or province border
        }
        Entity provinceLeft = ecsWorld.obtainEntity(provinceLeftId);
        Province provinceLeftData = provinceLeft.get(Province.class);
        long provinceUpId = this.getLandProvinceId(x, (y + 1));
        if(provinceUpId == 0) {
            return 0; // water, nothing or province border
        }
        Entity provinceUp = ecsWorld.obtainEntity(provinceUpId);
        Province provinceUpData = provinceUp.get(Province.class);
        long provinceDownId = this.getLandProvinceId(x, (y - 1));
        if(provinceDownId == 0) {
            return 0; // water, nothing or province border
        }
        Entity provinceDown = ecsWorld.obtainEntity(provinceDownId);
        Province provinceDownData = provinceDown.get(Province.class);

        if (provinceRightData.ownerId() != countryId || provinceLeftData.ownerId() != countryId || provinceUpData.ownerId() != countryId || provinceDownData.ownerId() != countryId) {
            return 153; // country border
        } else if (provinceRight.get(GeoHierarchy.class).regionId() != regionId || provinceLeft.get(GeoHierarchy.class).regionId() != regionId || provinceUp.get(GeoHierarchy.class).regionId() != regionId || provinceDown.get(GeoHierarchy.class).regionId() != regionId) {
            return 77; // region border
        } else {
            return 0; // water, nothing or province border
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

        this.mapRenderer.updateMapMode(this.mapMode, this.mapModePixmap);
    }

    public void render(WgProjection projection, OrthographicCamera cam, float time) {
        this.mapRenderer.render(projection, cam, time, this.mapMode);
    }

    @Override
    public void dispose() {
        this.mapModePixmap.dispose();
        this.provincesPixmap.dispose();
        this.provincesColorStripesPixmap.dispose();
    }
}
