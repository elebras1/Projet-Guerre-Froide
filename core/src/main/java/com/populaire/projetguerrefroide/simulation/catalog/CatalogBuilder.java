package com.populaire.projetguerrefroide.simulation.catalog;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntLongMap;
import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.loader.WorldData;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.catalog.NamedTable;
import com.populaire.projetguerrefroide.protocol.catalog.ProvinceTable;
import com.populaire.projetguerrefroide.util.ColorUtils;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;

import java.util.Arrays;

public final class CatalogBuilder {
    private static final String POSITION_INFIX = "_pos_";
    private static final String DEFAULT_POSITION = "default";
    private final World ecsWorld;
    private final EcsConstants ecsConstants;

    public CatalogBuilder(SimulationContext simulationContext) {
        this.ecsWorld = simulationContext.getEcsWorld();
        this.ecsConstants = simulationContext.getEcsConstants();
    }

    public Catalog build(WorldData worldData) {
        NamedTable regions = this.buildTagged(this.ecsConstants.regionTag(), true);
        NamedTable countries = this.buildColored(this.ecsWorld.query().with(Country.class).build());
        NamedTable goods = this.buildColored(this.ecsWorld.query().with(Good.class).build());
        NamedTable terrains = this.buildTerrains();
        NamedTable cultures = this.buildTagged(this.ecsConstants.cultureTag(), false);
        NamedTable religions = this.buildTagged(this.ecsConstants.religionTag(), false);
        IntList buildingTypeKinds = new IntList();
        NamedTable buildingTypes = this.buildBuildingTypes(buildingTypeKinds);
        NamedTable governments = this.buildGovernments();
        ObjectList<String> ministerImages = new ObjectList<>();
        NamedTable ministers = this.buildMinisters(ministerImages);
        ProvinceTable provinces = this.buildProvinces(regions, terrains, goods, buildingTypes);
        return new Catalog(provinces, regions, countries, goods, buildingTypes, buildingTypeKinds.toArray(), terrains, cultures, religions, governments, ministers, ministerImages.toArray(new String[0]), worldData.borders().pixels());
    }

    private NamedTable buildTagged(long tag, boolean deterministicColor) {
        NamedTable.Builder builder = new NamedTable.Builder();
        Query query = this.ecsWorld.query().with(tag).build();
        query.each(entityId -> {
            EntityView entity = this.ecsWorld.obtainEntityView(entityId);
            String name = entity.name();
            int color;
            if (deterministicColor) {
                color = ColorUtils.getDeterministicRGBA(name);
            } else {
                color = entity.has(Color.class) ? entity.<ColorMutView>getMutView(Color.class).value() : 0;
            }
            builder.add(entityId, name, color);
        });
        query.destruct();
        return builder.build();
    }

    private NamedTable buildColored(Query query) {
        NamedTable.Builder builder = new NamedTable.Builder();
        query.each(entityId -> {
            EntityView entity = this.ecsWorld.obtainEntityView(entityId);
            String name = entity.name();
            int color = entity.has(Color.class) ? entity.<ColorMutView>getMutView(Color.class).value() : 0;
            builder.add(entityId, name, color);
        });
        query.destruct();
        return builder.build();
    }

    private NamedTable buildTerrains() {
        NamedTable.Builder builder = new NamedTable.Builder();
        Query query = this.ecsWorld.query().with(Terrain.class).build();
        query.each(entityId -> {
            EntityView entity = this.ecsWorld.obtainEntityView(entityId);
            String name = entity.name();
            int color = entity.<TerrainMutView>getMutView(Terrain.class).color();
            builder.add(entityId, name, color);
        });
        query.destruct();
        return builder.build();
    }

    private NamedTable buildBuildingTypes(IntList kinds) {
        NamedTable.Builder builder = new NamedTable.Builder();
        this.addBuildingTypes(builder, kinds, EconomyBuildingType.class, Catalog.ECONOMY_BUILDING);
        this.addBuildingTypes(builder, kinds, SpecialBuildingType.class, Catalog.SPECIAL_BUILDING);
        this.addBuildingTypes(builder, kinds, DevelopmentBuildingType.class, Catalog.DEVELOPMENT_BUILDING);
        return builder.build();
    }

    private void addBuildingTypes(NamedTable.Builder builder, IntList kinds, Class<?> typeComponent, int kind) {
        Query query = this.ecsWorld.query().with(typeComponent).with(Flecs.Prefab).build();
        query.each(entityId -> {
            builder.add(entityId, this.ecsWorld.obtainEntityView(entityId).name(), 0);
            kinds.add(kind);
        });
        query.destruct();
    }

    private NamedTable buildGovernments() {
        NamedTable.Builder builder = new NamedTable.Builder();
        LongIntMap seen = new LongIntMap();
        Query query = this.ecsWorld.query().with(Country.class).build();
        query.each(countryId -> {
            long governmentId = this.ecsWorld.obtainEntityView(countryId).<CountryMutView>getMutView(Country.class).governmentId();
            if (governmentId != 0 && !seen.containsKey(governmentId)) {
                seen.put(governmentId, builder.size());
                builder.add(governmentId, this.ecsWorld.obtainEntityView(governmentId).name(), 0);
            }
        });
        query.destruct();
        return builder.build();
    }

    private NamedTable buildMinisters(ObjectList<String> images) {
        NamedTable.Builder builder = new NamedTable.Builder();
        Query query = this.ecsWorld.query().with(Minister.class).build();
        query.each(ministerId -> {
            Minister minister = this.ecsWorld.obtainEntity(ministerId).get(Minister.class);
            builder.add(ministerId, minister.name(), 0);
            images.add(minister.imageFileName());
        });
        query.destruct();
        return builder.build();
    }

    private ProvinceTable buildProvinces(NamedTable regions, NamedTable terrains, NamedTable goods, NamedTable buildingTypes) {
        NamedTable.Builder builder = new NamedTable.Builder();
        IntList regionIndexes = new IntList();
        IntList terrainIndexes = new IntList();
        IntList resourceGoodIndexes = new IntList();
        IntList borderStarts = new IntList();
        IntList borderEnds = new IntList();
        Query query = this.ecsWorld.query().with(Province.class).with(Color.class).build();
        query.each(provinceId -> {
            EntityView province = this.ecsWorld.obtainEntityView(provinceId);
            String name = province.name();
            int color = province.<ColorMutView>getMutView(Color.class).value();
            ProvinceView provinceData = province.getMutView(Province.class);
            regionIndexes.add(regions.indexOf(provinceData.regionId()));
            terrainIndexes.add(terrains.indexOf(provinceData.terrainId()));
            int resourceGoodIndex = -1;
            if (province.has(ResourceGathering.class)) {
                long goodId = province.<ResourceGatheringMutView>getMutView(ResourceGathering.class).goodId();
                if (goodId != -1 && goodId != 0) {
                    resourceGoodIndex = goods.indexOf(goodId);
                }
            }
            resourceGoodIndexes.add(resourceGoodIndex);
            if (province.has(Border.class)) {
                BorderView border = province.getMutView(Border.class);
                borderStarts.add(border.startIndex());
                borderEnds.add(border.endIndex());
            } else {
                borderStarts.add(0);
                borderEnds.add(0);
            }
            builder.add(provinceId, name, color);
        });
        query.destruct();
        NamedTable names = builder.build();
        int provinceCount = names.size();

        int[] adjacencyStarts = new int[provinceCount + 1];
        IntList adjacencies = new IntList();
        for (int index = 0; index < provinceCount; index++) {
            adjacencyStarts[index] = adjacencies.size();
            EntityView province = this.ecsWorld.obtainEntityView(names.entityId(index));
            if (!province.has(Adjacencies.class)) {
                continue;
            }
            AdjacenciesView adjacenciesView = province.getMutView(Adjacencies.class);
            for (int i = 0; i < adjacenciesView.provinceIdsLength(); i++) {
                long neighborId = adjacenciesView.provinceIds(i);
                if (neighborId == 0) {
                    continue;
                }
                int neighborIndex = names.indexOf(neighborId);
                if (neighborIndex >= 0) {
                    adjacencies.add(neighborIndex);
                }
            }
        }
        adjacencyStarts[provinceCount] = adjacencies.size();

        int[] positionsX = new int[provinceCount];
        int[] positionsY = new int[provinceCount];
        Arrays.fill(positionsX, -1);
        Arrays.fill(positionsY, -1);
        ObjectIntMap<String> buildingTypeByName = new ObjectIntMap<>();
        buildingTypeByName.setDefaultValue(-1);
        for (int index = 0; index < buildingTypes.size(); index++) {
            buildingTypeByName.put(buildingTypes.nameId(index), index);
        }
        int buildingTypeCount = buildingTypes.size();
        IntLongMap buildingPositions = new IntLongMap();
        Query positionQuery = this.ecsWorld.query().with(Position.class).build();
        positionQuery.each(positionId -> {
            EntityView positionEntity = this.ecsWorld.obtainEntityView(positionId);
            String name = positionEntity.name();
            PositionView position = positionEntity.getMutView(Position.class);
            int provinceIndex = names.indexOf(position.provinceId());
            if (name == null || provinceIndex < 0) {
                return;
            }
            int infix = name.indexOf(POSITION_INFIX);
            if (infix < 0) {
                return;
            }
            String suffix = name.substring(infix + POSITION_INFIX.length());
            if (suffix.equals(DEFAULT_POSITION)) {
                positionsX[provinceIndex] = position.x();
                positionsY[provinceIndex] = position.y();
                return;
            }
            int buildingTypeIndex = buildingTypeByName.get(suffix);
            if (buildingTypeIndex >= 0) {
                buildingPositions.put(ProvinceTable.buildingPositionKey(provinceIndex, buildingTypeIndex, buildingTypeCount), ProvinceTable.packPosition(position.x(), position.y()));
            }
        });
        positionQuery.destruct();

        return new ProvinceTable(names, regionIndexes.toArray(), terrainIndexes.toArray(), resourceGoodIndexes.toArray(), positionsX, positionsY, borderStarts.toArray(), borderEnds.toArray(), adjacencyStarts, adjacencies.toArray(), buildingPositions, buildingTypeCount);
    }
}
