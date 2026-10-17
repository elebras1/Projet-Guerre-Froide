package com.populaire.projetguerrefroide.loader;

import com.badlogic.gdx.graphics.Pixmap;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.component.Demographics;
import com.populaire.projetguerrefroide.dao.csv.ProvinceDefinitionCsv;
import com.populaire.projetguerrefroide.dao.json.*;
import com.populaire.projetguerrefroide.pojo.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import java.util.List;
import java.util.Map;

import static com.populaire.projetguerrefroide.util.Constants.*;

public final class MapLoader {
    private final WorldLoadingContext context;
    private final World ecsWorld;
    private final EcsConstants ecsConstants;

    public MapLoader(WorldLoadingContext context) {
        this.context = context;
        this.ecsWorld = context.ecsWorld();
        this.ecsConstants = context.ecsConstants();
    }

    public void loadTerrains(Map<String, TerrainJson> terrains) {
        try {
            for(var terrainEntry : terrains.entrySet()) {
                TerrainJson terrain = terrainEntry.getValue();
                byte movementCost = (byte) terrain.movementCost();
                byte humidity = (byte) terrain.humidity();
                byte temperature = (byte) terrain.temperature();
                byte precipitation = (byte) terrain.precipitation();
                int color = this.context.parseColor(terrain.color());

                long entityTerrainId = this.ecsWorld.entity(terrainEntry.getKey());
                EntityView entityTerrain = this.ecsWorld.obtainEntityView(entityTerrainId);
                entityTerrain.set(new Terrain(movementCost, temperature, humidity, precipitation, color));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public ProvinceBuildings loadProvinces(Map<String, ProvinceJson> provinces, Map<String, PopulationTemplateJson> populationTemplatesJson) {
        IntObjectMap<LongIntOrderedMap> regionBuildingsByProvince = new IntObjectMap<>(396, 1f);
        IntObjectMap<LongIntMap> regionBuildingModes = new IntObjectMap<>(396, 1f);
        Map<String, PopulationTemplate> populationTemplates = new ObjectObjectMap<>();
        for(var populationTemplateEntry : populationTemplatesJson.entrySet()) {
            double[] ratios = populationTemplateEntry.getValue().value();
            populationTemplates.put(populationTemplateEntry.getKey(), new PopulationTemplate((float) ratios[0], (float) ratios[1], (float) ratios[2]));
        }
        try {
            for (Map.Entry<String, ProvinceJson> provinceEntry : provinces.entrySet()) {
                int provinceId = Integer.parseInt(provinceEntry.getKey());
                this.loadProvince(provinceEntry.getValue(), provinceId, regionBuildingsByProvince, regionBuildingModes, populationTemplates);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return new ProvinceBuildings(regionBuildingsByProvince, regionBuildingModes);
    }

    private void loadProvince(ProvinceJson provinceJson, int provinceNameId, IntObjectMap<LongIntOrderedMap> regionBuildingsByProvince, IntObjectMap<LongIntMap> regionBuildingModes, Map<String, PopulationTemplate> populationTemplates) {
        try {

            long provinceId = this.ecsWorld.entity(String.valueOf(provinceNameId));
            Entity province = this.ecsWorld.obtainEntity(provinceId);

            long[] coreIds = new long[MAX_PROVINCE_CORES];
            int i = 0;
            for(String countryCore : provinceJson.addCore()) {
                coreIds[i] = this.ecsWorld.lookup(countryCore);
                i++;
            }

            long countryOwnerId = this.ecsWorld.lookup(provinceJson.owner());
            long countryControllerId = this.ecsWorld.lookup(provinceJson.controller());
            long terrainId = this.ecsWorld.lookup(provinceJson.terrain());

            PopulationTotalJson population = provinceJson.populationTotal();
            int amount = (int) population.amount();
            PopulationTemplate populationTemplate = populationTemplates.get(String.valueOf(population.template()));
            int childrenAmount = (int) (amount * populationTemplate.childrenRatio());
            int adultsAmount = (int) (amount * populationTemplate.adultsRatio());
            int seniorsAmount = (int) (amount * populationTemplate.seniorsRatio());

            this.parsePopulation(countryOwnerId, provinceId, population.populations(), adultsAmount);
            Pair<long[], int[]> cultures = this.parseDistribution(population.cultures(), adultsAmount, CULTURE_COUNT);
            Pair<long[], int[]> religions = this.parseDistribution(population.religions(), adultsAmount, RELIGION_COUNT);

            if(provinceJson.economyBuildings() != null) {
                LongIntOrderedMap buildings = new LongIntOrderedMap();
                LongIntMap buildingModes = new LongIntMap();
                for(ProvinceBuildingJson buildingJson : provinceJson.economyBuildings()) {
                    long buildingTypeId = this.ecsWorld.lookup(buildingJson.name());
                    buildings.put(buildingTypeId, (int) buildingJson.size());
                    if("advanced".equals(buildingJson.inputMode())) {
                        buildingModes.put(buildingTypeId, 1);
                    }
                }
                regionBuildingsByProvince.put(provinceNameId, buildings);
                regionBuildingModes.put(provinceNameId, buildingModes);
            }

            if(provinceJson.good() != null) {
                String goodNameId = provinceJson.good();
                long goodId = this.ecsWorld.lookup(goodNameId);
                EntityView goodEntity = this.ecsWorld.obtainEntityView(goodId);
                Good good = goodEntity.get(Good.class);
                float goodAmount = good.value();
                long rgoTypeId = this.ecsWorld.lookup("rgo_" + goodNameId);
                province.set(new ResourceGathering(rgoTypeId, goodId, this.context.getGoodIndex(goodId), goodAmount, 0, 0f, 0f, 0, 0, 0f, 0f));
            }

            if(provinceJson.buildings() != null) {
                for(ProvinceBuildingJson buildingJson : provinceJson.buildings()) {
                    long buildingTypeId = this.ecsWorld.lookup(buildingJson.name());
                    EntityView building = this.ecsWorld.obtainEntityView(this.ecsWorld.entity());
                    building.isA(buildingTypeId);
                    building.set(new Building(provinceId, countryOwnerId, (int) buildingJson.size()));
                    building.set(new DevelopmentBuilding());
                }
            }

            province.set(new Province(coreIds, countryOwnerId, countryControllerId, terrainId, 0, 0, 0, childrenAmount, adultsAmount, seniorsAmount, cultures.first(), cultures.second(), religions.first(), religions.second()));
            province.set(new Demographics(0, 0, 0, 0f, 0f, 0f, 0f, 0f, 0f, new int[POP_TYPE_COUNT], new int[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], 0, 0, 0));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void parsePopulation(long countryId, long provinceId, Map<String, Double> distribution, int adultsAmount) {
        for(var distributionEntry : distribution.entrySet()) {
            float percentage = distributionEntry.getValue().floatValue();
            int amount = (int) (adultsAmount * percentage);
            long typeId = this.ecsWorld.lookup(distributionEntry.getKey());
            long popId = this.ecsWorld.entity();
            EntityView pop = this.ecsWorld.obtainEntityView(popId);
            pop.isA(typeId);
            pop.set(new Population(countryId, provinceId, 0, amount, 0, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
        }
    }

    private Pair<long[], int[]> parseDistribution(Map<String, Double> distribution, int adultsAmount, int length) {
        long[] ids = new long[length];
        int[] amounts = new int[length];
        int i = 0;
        for(var distributionEntry : distribution.entrySet()) {
            float percentage = distributionEntry.getValue().floatValue();
            ids[i] = this.ecsWorld.lookup(distributionEntry.getKey());
            amounts[i] = (int) (adultsAmount * percentage);
            i++;
        }

        return new Pair<>(ids, amounts);
    }

    public void loadRegions(Map<String, int[]> regions, ProvinceBuildings provinceBuildings) {
        IntObjectMap<LongIntOrderedMap> regionBuildingsByProvince = provinceBuildings.buildings();
        IntObjectMap<LongIntMap> regionBuildingModes = provinceBuildings.modes();
        try {
            for(var regionEntry : regions.entrySet()) {
                String regionId = regionEntry.getKey();
                long regionEntityId = this.ecsWorld.entity(regionId);
                EntityView region = this.ecsWorld.obtainEntityView(regionEntityId);
                region.add(this.ecsConstants.regionTag());
                for(int provinceId : regionEntry.getValue()) {
                    long provinceEntityId = this.ecsWorld.lookup(String.valueOf(provinceId));
                    EntityView province = provinceEntityId != 0 ? this.ecsWorld.obtainEntityView(provinceEntityId) : null;
                    if(provinceEntityId != 0 && province.has(Province.class)) {
                        ProvinceMutView provinceData = province.getMutView(Province.class);
                        long regionInstanceId = this.ecsWorld.entity("region_instance_" + regionId + "_" + provinceData.ownerId());
                        EntityView regionInstance = this.ecsWorld.obtainEntityView(regionInstanceId);
                        if(!regionInstance.has(RegionInstance.class)) {
                            regionInstance.set(new RegionInstance(regionEntityId, provinceData.ownerId(), new float[POP_TYPE_COUNT]));
                            regionInstance.set(new RegionInstanceIncome(new float[POP_TYPE_COUNT], new int[POP_TYPE_COUNT], new int[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], 0f, 0f, 0f, 0f, 0f, 0f));
                            regionInstance.set(new RegionDemographics(0, 0, 0, 0f, 0f, 0f, 0f, 0f, 0f, new int[POP_TYPE_COUNT], new int[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], 0, 0, 0));
                        }
                        provinceData.regionId(regionEntityId).regionInstanceId(regionInstanceId);
                        LongIntOrderedMap regionBuildingIds = regionBuildingsByProvince.get(provinceId);
                        if(regionBuildingIds != null) {
                            LongIntMap regionBuildingModesById = regionBuildingModes.get(provinceId);
                            for(var buildingEntry : regionBuildingIds) {
                                long buildingTypeId = buildingEntry.key;
                                int size = buildingEntry.value;
                                EntityView building = this.ecsWorld.obtainEntityView(this.ecsWorld.entity());
                                EntityView buildingType = this.ecsWorld.obtainEntityView(buildingTypeId);
                                building.isA(buildingTypeId);
                                building.set(new Building(regionInstanceId, provinceData.ownerId(), size));
                                if(buildingType.has(EconomyBuildingType.class)) {
                                    EconomyBuildingTypeView buildingTypeData = buildingType.getMutView(EconomyBuildingType.class);
                                    boolean advancedRecipe = regionBuildingModesById != null && regionBuildingModesById.get(buildingTypeId) == 1 && buildingTypeData.advancedGoodInputIndexes(0) >= 0;
                                    int[] activeInputGoodIndexes = new int[MAX_GOODS];
                                    float[] activeInputAmounts = new float[MAX_GOODS];
                                    for(int g = 0; g < MAX_GOODS; g++) {
                                        activeInputGoodIndexes[g] = advancedRecipe ? buildingTypeData.advancedGoodInputIndexes(g) : buildingTypeData.goodInputIndexes(g);
                                        activeInputAmounts[g] = advancedRecipe ? buildingTypeData.advancedGoodInputAmounts(g) : buildingTypeData.goodInputAmounts(g);
                                    }
                                    building.set(new EconomyBuilding(0, 0f, 1f, 0f, 0d, 1f, 0, 0, 0f, 0f, new float[MAX_GOODS], activeInputGoodIndexes, activeInputAmounts, advancedRecipe));
                                } else if (buildingType.has(SpecialBuildingType.class)) {
                                    building.set(new SpecialBuilding());
                                }
                            }
                        }
                    } else {
                        provinceEntityId = this.ecsWorld.entity(String.valueOf(provinceId));
                        province = this.ecsWorld.obtainEntityView(provinceEntityId);
                        province.add(this.ecsConstants.seaProvinceTag());
                    }
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public IntLongMap loadProvinceDefinitions(List<ProvinceDefinitionCsv> definitions) {
        IntLongMap provinces = new IntLongMap(15000, 1f);
        try {
            for (ProvinceDefinitionCsv definition : definitions) {
                long provinceEntityId = this.ecsWorld.lookup(String.valueOf(definition.id()));
                EntityView provinceEntity = this.ecsWorld.obtainEntityView(provinceEntityId);
                int alpha = 255;

                int color =  (definition.red() << 24) | (definition.green() << 16) | (definition.blue() << 8) | alpha;
                provinceEntity.set(new Color(color));
                if (provinceEntity.has(Province.class)) {
                    provinces.put(color, provinceEntityId);
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return provinces;
    }

    public Borders loadBorders(Pixmap provincesPixmap, IntLongMap provinces) {

        IntList xyValues = new IntList();
        LongObjectOrderedMap<IntList> temporaryGroups = new LongObjectOrderedMap<>();

        int height = provincesPixmap.getHeight();
        int width = provincesPixmap.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = provincesPixmap.getPixel(x, y);
                long provinceEntityId = provinces.get(color);

                if (provinceEntityId != 0 && isBorderPixel(provincesPixmap, x, y, color, width, height)) {
                    IntList group = temporaryGroups.get(provinceEntityId);
                    if (group == null) {
                        group = new IntList();
                        temporaryGroups.put(provinceEntityId, group);
                    }
                    group.add(x);
                    group.add(y);
                }
            }
        }

        for (LongObjectMap.Entry<IntList> entry : temporaryGroups.entrySet()) {
            IntList coords = entry.value;
            int startIndex = xyValues.size();
            xyValues.addAll(coords);
            int endIndex = xyValues.size();
            EntityView provinceEntity = this.ecsWorld.obtainEntityView(entry.key);
            provinceEntity.set(new Border(startIndex, endIndex));
        }
        provincesPixmap.dispose();
        return new Borders(xyValues.shrink());
    }

    private boolean isBorderPixel(Pixmap pixmap, int x, int y, int color, int w, int h) {
        if (x + 1 < w && pixmap.getPixel(x + 1, y) != color) {
            return true;
        }
        if (x > 0 && pixmap.getPixel(x - 1, y) != color) {
            return true;
        }
        if (y + 1 < h && pixmap.getPixel(x, y + 1) != color) {
            return true;
        }
        return y > 0 && pixmap.getPixel(x, y - 1) != color;
    }

    public void loadContinents(Map<String, int[]> continents) {
        try {
            for(var continentEntry : continents.entrySet()) {
                long continentId = this.ecsWorld.entity(continentEntry.getKey());
                for(int provinceId : continentEntry.getValue()) {
                    long provinceEntityId = this.ecsWorld.lookup(String.valueOf(provinceId));
                    EntityView province = this.ecsWorld.obtainEntityView(provinceEntityId);
                    ProvinceMutView provinceData = province.getMutView(Province.class);
                    provinceData.continentId(continentId);
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadAdjacencies(Map<String, int[]> adjacencies) {
        try {
            for(var provinceEntry : adjacencies.entrySet()) {
                long provinceEntityId = this.ecsWorld.lookup(String.valueOf(Integer.parseInt(provinceEntry.getKey())));
                EntityView provinceEntity = this.ecsWorld.obtainEntityView(provinceEntityId);
                long[] adjacencyIds = new long[MAX_ADJACENCIES];
                int i = 0;
                for(int adjacencyId : provinceEntry.getValue()) {
                    adjacencyIds[i] = this.ecsWorld.lookup(String.valueOf(adjacencyId));
                    i++;
                }
                provinceEntity.set(new Adjacencies(adjacencyIds));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadPositions(Map<String, Map<String, PositionJson>> positions) {
        try {
            for(var provinceEntry : positions.entrySet()) {
                int provinceId = Integer.parseInt(provinceEntry.getKey());
                long provinceEntityId = this.ecsWorld.lookup(String.valueOf(provinceId));
                for(var positionEntry : provinceEntry.getValue().entrySet()) {
                    PositionJson position = positionEntry.getValue();
                    long positionEntityId = this.ecsWorld.entity("province_" + provinceId + "_pos_" + positionEntry.getKey());
                    EntityView positionEntity = this.ecsWorld.obtainEntityView(positionEntityId);
                    positionEntity.set(new Position((int) position.x(), (int) position.y(), provinceEntityId));
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
