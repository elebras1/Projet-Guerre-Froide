package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ProvinceView;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.CountryOverview;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Iter;

public final class CountryOverviewHandler extends RequestHandler<CountryOverview> {
    private final FlecsSystem populationSystem;
    private long countryId;
    private int population;

    public CountryOverviewHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
        this.populationSystem = this.ecsWorld.system("CountryOverviewPopulationRequest")
            .kind(0)
            .with(Province.class)
            .iter(this::sumPopulation);
    }

    @Override
    protected boolean fill(RequestParams params, CountryOverview response) {
        this.countryId = this.state.playerCountryId();
        if (this.countryId == 0) {
            response.set(-1, 0, 0, 0, 0, 0, 0, 0f, 0, 0, 0, 0);
            return false;
        }
        this.population = 0;
        this.populationSystem.run();
        response.set(this.countryIndex(this.countryId), this.population, 0, 0, 0, 0, 0, 0f, 0, 0, 0, 0);
        return true;
    }

    private void sumPopulation(Iter iter) {
        Field<Province> provinceField = iter.field(Province.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() == this.countryId) {
                this.population += province.childrenAmount() + province.adultsAmount() + province.seniorsAmount();
            }
        }
    }
}
