package com.populaire.projetguerrefroide.repository;

import com.badlogic.gdx.utils.Disposable;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class QueryRepository implements Disposable {
    private final Query queryProvincesWithColor;
    private final Query queryProvincesWithResourceGathering;
    private final Query queryProvincesWithColorAndResourceGathering;
    private final Query queryProvinces;
    private final Query queryBuildings;
    private final Query queryCountries;

    public QueryRepository(World ecsWorld) {
        this.queryProvincesWithColor = ecsWorld.query()
            .with(Province.class)
            .with(Color.class)
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

        this.queryProvinces = ecsWorld.query()
            .with(Province.class)
            .build();

        this.queryBuildings = ecsWorld.query()
            .with(Building.class)
            .build();

        this.queryCountries = ecsWorld.query()
            .with(Country.class)
            .build();
    }

    public Query getProvincesWithColor() {
        return this.queryProvincesWithColor;
    }

    public Query getProvincesWithResourceGathering() {
        return this.queryProvincesWithResourceGathering;
    }

    public Query getProvincesWithColorAndResourceGathering() {
        return this.queryProvincesWithColorAndResourceGathering;
    }

    public Query getProvinces() {
        return this.queryProvinces;
    }

    public Query getBuildings() {
        return this.queryBuildings;
    }

    public Query getCountries() {
        return this.queryCountries;
    }

    @Override
    public void dispose() {
        this.queryProvincesWithColor.destroy();
        this.queryProvincesWithResourceGathering.destroy();
        this.queryProvincesWithColor.destroy();
        this.queryProvinces.destroy();
        this.queryBuildings.destroy();
        this.queryCountries.destroy();
    }
}
