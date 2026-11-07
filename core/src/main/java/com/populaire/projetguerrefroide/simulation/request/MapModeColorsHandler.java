package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.protocol.request.MapMode;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.MapModeColors;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import com.populaire.projetguerrefroide.util.ColorUtils;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Iter;

public final class MapModeColorsHandler extends RequestHandler<MapModeColors> {
    private static final MapMode[] MAP_MODES = MapMode.values();
    private final FlecsSystem provincesSystem;
    private final FlecsSystem resourcesSystem;
    private final FlecsSystem populationSystem;
    private MapModeColors response;
    private MapMode mode;
    private int maxPopulation;

    public MapModeColorsHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
        this.provincesSystem = this.ecsWorld.system("MapModeColorsRequest")
            .kind(0)
            .with(Province.class)
            .with(Color.class)
            .iter(this::colorProvinces);
        this.resourcesSystem = this.ecsWorld.system("MapModeResourcesRequest")
            .kind(0)
            .with(Province.class)
            .with(Color.class)
            .with(ResourceGathering.class)
            .iter(this::colorResources);
        this.populationSystem = this.ecsWorld.system("MapModePopulationRequest")
            .kind(0)
            .with(Province.class)
            .iter(this::findMaxPopulation);
    }

    @Override
    protected boolean fill(RequestParams params, MapModeColors response) {
        int option = params.option();
        if (option < 0 || option >= MAP_MODES.length) {
            response.clear(option);
            return false;
        }
        response.clear(option);
        this.response = response;
        this.mode = MAP_MODES[option];
        if (this.mode == MapMode.POPULATION) {
            this.maxPopulation = 0;
            this.populationSystem.run();
        }
        this.provincesSystem.run();
        if (this.mode == MapMode.RESOURCES) {
            this.resourcesSystem.run();
        }
        this.response = null;
        return true;
    }

    private void colorProvinces(Iter iter) {
        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<Color> colorField = iter.field(Color.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            int provinceColor = colorField.<ColorMutView>getMutView(i).value();
            switch (this.mode) {
                case POLITICAL -> this.response.set(provinceColor, this.countryColor(province.ownerId()));
                case IDEOLOGICAL -> this.colorIdeology(provinceColor, province.ownerId());
                case CULTURAL -> this.colorBiggestCulture(provinceColor, province);
                case RELIGIOUS -> this.colorBiggestReligion(provinceColor, province);
                case TERRAIN, RESOURCES -> this.response.set(provinceColor, ColorUtils.getWhiteRGBA());
                case TERRAIN_2 -> this.response.set(provinceColor, this.ecsWorld.obtainEntityView(province.terrainId()).<TerrainMutView>getMutView(Terrain.class).color());
                case REGION -> this.colorRegion(provinceColor, province.regionId());
                case RELATIONS -> this.colorRelation(provinceColor, province.ownerId());
                case POPULATION -> this.colorPopulation(provinceColor, province);
            }
        }
    }

    private int countryColor(long countryId) {
        EntityView country = this.ecsWorld.obtainEntityView(countryId);
        long colonizerId = country.target(this.ecsConstants.isColonyOf());
        if (colonizerId != 0) {
            return this.ecsWorld.obtainEntityView(colonizerId).<ColorMutView>getMutView(Color.class).value();
        }
        return country.<ColorMutView>getMutView(Color.class).value();
    }

    private void colorIdeology(int provinceColor, long ownerId) {
        long ideologyId = this.ecsWorld.obtainEntityView(ownerId).<CountryMutView>getMutView(Country.class).ideologyId();
        this.response.set(provinceColor, this.ecsWorld.obtainEntityView(ideologyId).<IdeologyMutView>getMutView(Ideology.class).color());
    }

    private void colorBiggestCulture(int provinceColor, ProvinceView province) {
        int biggestIndex = -1;
        int biggestAmount = 0;
        for (int j = 0; j < province.cultureIdsLength(); j++) {
            if (province.cultureIds(j) != 0 && province.cultureAmounts(j) > biggestAmount) {
                biggestAmount = province.cultureAmounts(j);
                biggestIndex = j;
            }
        }
        if (biggestIndex != -1) {
            int cultureIndex = this.catalog.cultures().indexOf(province.cultureIds(biggestIndex));
            if (cultureIndex >= 0) {
                this.response.set(provinceColor, this.catalog.cultures().color(cultureIndex));
            }
        }
    }

    private void colorBiggestReligion(int provinceColor, ProvinceView province) {
        int biggestIndex = -1;
        int biggestAmount = 0;
        for (int j = 0; j < province.religionIdsLength(); j++) {
            if (province.religionIds(j) != 0 && province.religionAmounts(j) > biggestAmount) {
                biggestAmount = province.religionAmounts(j);
                biggestIndex = j;
            }
        }
        if (biggestIndex != -1) {
            int religionIndex = this.catalog.religions().indexOf(province.religionIds(biggestIndex));
            if (religionIndex >= 0) {
                this.response.set(provinceColor, this.catalog.religions().color(religionIndex));
            }
        }
    }

    private void colorRegion(int provinceColor, long regionId) {
        int regionIndex = this.catalog.regions().indexOf(regionId);
        if (regionIndex >= 0) {
            this.response.set(provinceColor, this.catalog.regions().color(regionIndex));
        }
    }

    private void colorRelation(int provinceColor, long ownerId) {
        long playerCountryId = this.state.playerCountryId();
        if (playerCountryId != 0 && playerCountryId == ownerId) {
            this.response.set(provinceColor, ColorUtils.getLightBlueRGBA());
            return;
        }
        if (ownerId == 0 || playerCountryId == 0) {
            this.response.set(provinceColor, ColorUtils.getGreyRGBA());
            return;
        }
        DiplomaticRelationView relation = this.ecsWorld.obtainEntityView(playerCountryId).getMutView(DiplomaticRelation.class, ownerId);
        if (relation != null) {
            this.response.set(provinceColor, ColorUtils.getRedToGreenGradientRGBA(relation.value(), 200));
        } else {
            this.response.set(provinceColor, ColorUtils.getGreyRGBA());
        }
    }

    private void colorPopulation(int provinceColor, ProvinceView province) {
        int population = province.childrenAmount() + province.adultsAmount() + province.seniorsAmount();
        float ratio = this.maxPopulation > 0 ? (float) population / this.maxPopulation : 0f;
        this.response.set(provinceColor, ColorUtils.getMagmaColorRGBA(ratio));
    }

    private void findMaxPopulation(Iter iter) {
        Field<Province> provinceField = iter.field(Province.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            int population = province.childrenAmount() + province.adultsAmount() + province.seniorsAmount();
            if (population > this.maxPopulation) {
                this.maxPopulation = population;
            }
        }
    }

    private void colorResources(Iter iter) {
        Field<Color> colorField = iter.field(Color.class, 1);
        Field<ResourceGathering> resourceField = iter.field(ResourceGathering.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            long goodId = resourceField.<ResourceGatheringMutView>getMutView(i).goodId();
            if (goodId == -1 || goodId == 0) {
                continue;
            }
            int goodIndex = this.catalog.goods().indexOf(goodId);
            if (goodIndex >= 0) {
                this.response.set(colorField.<ColorMutView>getMutView(i).value(), this.catalog.goods().color(goodIndex));
            }
        }
    }
}
