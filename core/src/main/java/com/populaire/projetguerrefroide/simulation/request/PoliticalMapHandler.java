package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.Country;
import com.populaire.projetguerrefroide.component.CountryView;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ProvinceView;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.PoliticalMap;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Iter;

public final class PoliticalMapHandler extends RequestHandler<PoliticalMap> {
    private final FlecsSystem provincesSystem;
    private final FlecsSystem countriesSystem;
    private PoliticalMap response;

    public PoliticalMapHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
        this.provincesSystem = this.ecsWorld.system("PoliticalMapProvincesRequest")
            .kind(0)
            .with(Province.class)
            .iter(this::collectProvinces);
        this.countriesSystem = this.ecsWorld.system("PoliticalMapCountriesRequest")
            .kind(0)
            .with(Country.class)
            .iter(this::collectCountries);
    }

    @Override
    protected boolean fill(RequestParams params, PoliticalMap response) {
        response.clear(this.catalog.provinces().size(), this.catalog.countries().size());
        this.response = response;
        this.provincesSystem.run();
        this.countriesSystem.run();
        this.response = null;
        return true;
    }

    private void collectProvinces(Iter iter) {
        Field<Province> provinceField = iter.field(Province.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            int provinceIndex = this.catalog.provinces().indexOf(iter.entity(i));
            if (provinceIndex < 0) {
                continue;
            }
            ProvinceView province = provinceField.getMutView(i);
            this.response.setProvince(provinceIndex, this.countryIndex(province.ownerId()), this.countryIndex(province.controllerId()));
        }
    }

    private void collectCountries(Iter iter) {
        Field<Country> countryField = iter.field(Country.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            long countryId = iter.entity(i);
            int countryIndex = this.countryIndex(countryId);
            if (countryIndex < 0) {
                continue;
            }
            CountryView country = countryField.getMutView(i);
            long capitalId = country.capitalId();
            int capitalIndex = capitalId == 0 ? -1 : this.catalog.provinces().indexOf(capitalId);
            this.response.setCountry(countryIndex, this.colonizerIndex(countryId), capitalIndex);
        }
    }
}
