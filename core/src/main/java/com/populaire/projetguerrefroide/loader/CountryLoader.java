package com.populaire.projetguerrefroide.loader;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.json.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import java.time.LocalDate;
import java.util.Map;

import static com.populaire.projetguerrefroide.util.Constants.*;

public final class CountryLoader {
    private final WorldLoadingContext context;
    private final World ecsWorld;
    private final EcsConstants ecsConstants;

    public CountryLoader(WorldLoadingContext context) {
        this.context = context;
        this.ecsWorld = context.ecsWorld();
        this.ecsConstants = context.ecsConstants();
    }

    public void loadCountries(Map<String, CountryJson> countries) {
        try {
            for (Map.Entry<String, CountryJson> countryEntry : countries.entrySet()) {
                this.loadCountry(countryEntry.getKey(), countryEntry.getValue());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadCountry(String countryId, CountryJson countryJson) {
        try {

            int color = this.context.parseColor(countryJson.color());
            long countryEntityId = this.ecsWorld.entity(countryId);
            EntityView countryEntity = this.ecsWorld.obtainEntityView(countryEntityId);
            countryEntity.add(this.ecsConstants.countryTag());
            countryEntity.set(new Color(color));

            if (countryJson.ministers() != null) {
                for(var ministerEntry : countryJson.ministers().entrySet()) {
                    MinisterJson minister = ministerEntry.getValue();
                    int startDate = (int) LocalDate.parse(minister.startDate(), this.context.dateFormatter()).toEpochDay();
                    int deathDate = (int) LocalDate.parse(minister.deathDate(), this.context.dateFormatter()).toEpochDay();

                    long ideologyEntityId = this.ecsWorld.entity(minister.ideology());
                    long typeEntityId = this.ecsWorld.lookup(minister.type());
                    long ministerEntityId = this.ecsWorld.entity(ministerEntry.getKey());
                    EntityView ministerEntity = this.ecsWorld.obtainEntityView(ministerEntityId);
                    ministerEntity.set(new Minister(minister.name(), minister.picture(), (float) minister.loyalty(), startDate, deathDate, countryEntityId, ideologyEntityId, typeEntityId));
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadRelations(RelationsJson relations) {
        try {
            for(RelationJson relation : relations.relation()) {
                EntityView country1 = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup(relation.country1()));
                EntityView country2 = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup(relation.country2()));
                int value = (int) relation.value();
                country1.set(new DiplomaticRelation(value), country2.id());
                country2.set(new DiplomaticRelation(value), country1.id());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadAlliances(AlliancesJson alliances) {
        try {
            for(AllianceJson alliance : alliances.alliances()) {
                EntityView country1 = this.ecsWorld.obtainEntityView(this.ecsWorld.entity(alliance.country1()));
                EntityView country2 = this.ecsWorld.obtainEntityView(this.ecsWorld.entity(alliance.country2()));
                country1.add(this.ecsConstants.getAllianceRelation(alliance.type(), true), country2.id());
                country2.add(this.ecsConstants.getAllianceRelation(alliance.type(), false), country1.id());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadCountriesHistory(Map<String, CountryHistoryJson> countriesHistory) {
        try {
            for (Map.Entry<String, CountryHistoryJson> countryHistory : countriesHistory.entrySet()) {
                this.loadCountryHistory(countryHistory.getKey(), countryHistory.getValue());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadCountryHistory(String countryNameId, CountryHistoryJson countryHistory) {
        try {
            EntityView country = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup(countryNameId));

            long capitalId = this.ecsWorld.lookup(String.valueOf(countryHistory.capital()));
            long governmentId = this.ecsWorld.lookup(countryHistory.government());
            long ideologyId = this.ecsWorld.lookup(countryHistory.ideology());
            long identityId = this.ecsWorld.lookup(countryHistory.nationalIdentity());
            long attitudeId = this.ecsWorld.lookup(countryHistory.nationalAttitude());
            long ministerHeadOfStateEntityId = 0;
            long ministerHeadOfGovernmentEntityId = 0;
            if(countryHistory.headOfState() != null && countryHistory.headOfGovernment() != null) {
                ministerHeadOfStateEntityId = this.ecsWorld.lookup(countryHistory.headOfState());
                ministerHeadOfGovernmentEntityId = this.ecsWorld.lookup(countryHistory.headOfGovernment());
            }

            long[] lawIds = new long[LAW_GROUP_COUNT];
            for(var lawEntry : countryHistory.laws().entrySet()) {
                long lawGroupId = this.ecsWorld.lookup(lawEntry.getKey());
                int lawGroupIndex = this.context.getLawGroupIndex(lawGroupId);
                lawIds[lawGroupIndex] = this.ecsWorld.lookup(lawEntry.getValue());
            }
            country.set(new Country(capitalId, governmentId, ideologyId, identityId, attitudeId, ministerHeadOfStateEntityId, ministerHeadOfGovernmentEntityId, lawIds));
            country.set(new CountryMarket(new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new double[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new double[POP_TYPE_COUNT], new double[POP_TYPE_COUNT], new double[POP_TYPE_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], 0d, 0f, new double[GOOD_COUNT], 0f, 0f, 0f));
            country.set(new CountryDemographics(0, 0, 0f, 0f, 0f, 0f, 0f, 0f, 0f, new long[POP_TYPE_COUNT], new long[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], new float[POP_TYPE_COUNT], 0, 0, 0));
            country.set(new CountryTaxPolicy(0f, 0f, 0f));
            country.set(new CountryBudgetPolicy(0f, 0f, 0f, 0f));
            country.set(new CountryEducationPolicy(0f, 0f));
            country.set(new CountryTradePolicy(0f));
            country.set(new CountryProfitDistributionPolicy(0.7f, 0.3f, 0.75f, 0.7f));
            country.set(new CountryLaborPolicy(0f, false));
            country.set(new CountryProductionPolicy(0f, 0f, 0f, 0f, 0.2f));
            country.set(new CountryPopulationPolicy(0f, 0f, 0f));
            country.set(new CountryPoliticalPolicy(0f, 0f, 0f, 0f, 0f, 0f));
            country.set(new CountryCulturePolicy(0f, 0f, 0f));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
