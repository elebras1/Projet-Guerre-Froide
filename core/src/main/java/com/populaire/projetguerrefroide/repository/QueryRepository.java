package com.populaire.projetguerrefroide.repository;

import com.badlogic.gdx.utils.Disposable;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class QueryRepository implements Disposable {
    private final Query queryProvincesWithColor;
    private final Query queryProvincesWithGeoHierarchy;
    private final Query queryProvincesWithResourceGathering;
    private final Query queryProvincesWithColorAndResourceGathering;
    private final Query queryProvincesWithColorAndGeoHierarchy;
    private final Query queryProvincesWithColorAndCultureDistribution;
    private final Query queryProvincesWithColorAndReligionDistribution;
    private final Query queryProvincesWithBorderAndGeoHierarchy;
    private final Query queryProvincesAll;
    private final Query queryBuildingsAll;
    private final Query queryCountries;

    public QueryRepository(World ecsWorld, EcsConstants ecsConstants) {
        this.queryProvincesWithColor = ecsWorld.query()
            .with(Province.class)
            .with(Color.class)
            .build();

        this.queryProvincesWithGeoHierarchy = ecsWorld.query()
            .with(Province.class)
            .with(GeoHierarchy.class)
            .build();

        this.queryProvincesWithResourceGathering = ecsWorld.query()
            .with(Province.class)
            .with(ResourceGathering.class)
            .build();

        this.queryProvincesWithColorAndResourceGathering = ecsWorld.query()
            .with(Province.class)
            .with(Color.class)
            .with(ResourceGathering.class)
            .build();

        this.queryProvincesWithColorAndGeoHierarchy = ecsWorld.query()
            .with(Province.class)
            .with(Color.class)
            .with(GeoHierarchy.class)
            .build();

        this.queryProvincesWithColorAndCultureDistribution = ecsWorld.query()
            .with(Province.class)
            .with(Color.class)
            .with(CultureDistribution.class)
            .build();

        this.queryProvincesWithColorAndReligionDistribution = ecsWorld.query()
            .with(Province.class)
            .with(Color.class)
            .with(ReligionDistribution.class)
            .build();

        this.queryProvincesWithBorderAndGeoHierarchy = ecsWorld.query()
            .with(Province.class)
            .with(Border.class)
            .with(GeoHierarchy.class)
            .build();

        this.queryProvincesAll = ecsWorld.query()
            .with(Province.class)
            .build();

        this.queryBuildingsAll = ecsWorld.query()
            .with(Building.class)
            .build();

        this.queryCountries = ecsWorld.query()
            .with(Country.class)
            .build();
    }

    public Query getProvincesWithColor() {
        return this.queryProvincesWithColor;
    }

    public Query getProvincesWithGeoHierarchy() {
        return this.queryProvincesWithGeoHierarchy;
    }

    public Query getProvincesWithResourceGathering() {
        return this.queryProvincesWithResourceGathering;
    }

    public Query getProvincesWithColorAndResourceGathering() {
        return this.queryProvincesWithColorAndResourceGathering;
    }

    public Query getProvincesWithColorAndGeoHierarchy() {
        return this.queryProvincesWithColorAndGeoHierarchy;
    }

    public Query getProvincesWithColorAndCultureDistribution() {
        return this.queryProvincesWithColorAndCultureDistribution;
    }

    public Query getProvincesWithColorAndReligionDistribution() {
        return this.queryProvincesWithColorAndReligionDistribution;
    }

    public Query getProvincesWithBorderAndGeoHierarchy() {
        return this.queryProvincesWithBorderAndGeoHierarchy;
    }

    public Query getProvinces() {
        return this.queryProvincesAll;
    }

    public Query getBuildings() {
        return this.queryBuildingsAll;
    }

    public Query getCountries() {
        return this.queryCountries;
    }

    @Override
    public void dispose() {
        this.queryProvincesWithColor.close();
        this.queryProvincesWithGeoHierarchy.close();
        this.queryProvincesWithResourceGathering.close();
        this.queryProvincesWithColorAndGeoHierarchy.close();
        this.queryProvincesWithColorAndCultureDistribution.close();
        this.queryProvincesWithColorAndReligionDistribution.close();
        this.queryProvincesWithBorderAndGeoHierarchy.close();
        this.queryProvincesAll.close();
        this.queryBuildingsAll.close();
        this.queryCountries.close();
    }
}
