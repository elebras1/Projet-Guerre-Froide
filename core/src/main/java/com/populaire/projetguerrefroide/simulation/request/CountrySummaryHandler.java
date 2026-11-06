package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.Country;
import com.populaire.projetguerrefroide.component.CountryMutView;
import com.populaire.projetguerrefroide.component.CountryView;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ProvinceView;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.CountrySummary;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Iter;

public final class CountrySummaryHandler extends RequestHandler<CountrySummary> {
    private final FlecsSystem populationSystem;
    private final long[] allyRelations;
    private long countryId;
    private int population;

    public CountrySummaryHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
        this.populationSystem = this.ecsWorld.system("CountrySummaryPopulationRequest")
            .kind(0)
            .with(Province.class)
            .iter(this::sumPopulation);
        this.allyRelations = new long[] {
            this.ecsConstants.alliedWith(),
            this.ecsConstants.guarantees(),
            this.ecsConstants.isGuaranteedBy(),
            this.ecsConstants.isPuppetMasterOf(),
            this.ecsConstants.isPuppetOf(),
            this.ecsConstants.colonizes()
        };
    }

    @Override
    protected boolean fill(RequestParams params, CountrySummary response) {
        response.clearAllies();
        int countryIndex = params.index();
        if (countryIndex < 0 || countryIndex >= this.catalog.countries().size()) {
            response.set(-1, 0, -1, -1, -1);
            return false;
        }
        this.countryId = this.catalog.countries().entityId(countryIndex);
        this.population = 0;
        this.populationSystem.run();

        EntityView country = this.ecsWorld.obtainEntityView(this.countryId);
        CountryView countryData = country.getMutView(Country.class);
        long governmentId = countryData.governmentId();
        long headOfStateId = countryData.headOfStateId();
        long colonizerId = country.target(this.ecsConstants.isColonyOf());
        if (colonizerId != 0) {
            headOfStateId = this.ecsWorld.obtainEntityView(colonizerId).<CountryMutView>getMutView(Country.class).headOfStateId();
        }
        int governmentIndex = governmentId == 0 ? -1 : this.catalog.governments().indexOf(governmentId);
        int headOfStateIndex = headOfStateId == 0 ? -1 : this.catalog.ministers().indexOf(headOfStateId);
        response.set(countryIndex, this.population, governmentIndex, headOfStateIndex, this.countryIndex(colonizerId));

        for (long relation : this.allyRelations) {
            int position = 0;
            long alliedId = this.ecsWorld.obtainEntityView(this.countryId).target(relation, position);
            while (alliedId != 0) {
                int alliedIndex = this.countryIndex(alliedId);
                if (alliedIndex >= 0) {
                    response.addAlly(alliedIndex);
                }
                position++;
                alliedId = this.ecsWorld.obtainEntityView(this.countryId).target(relation, position);
            }
        }
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
