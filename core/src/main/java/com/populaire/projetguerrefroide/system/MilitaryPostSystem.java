package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EmploymentUtils;
import com.populaire.projetguerrefroide.util.MilitaryUtils;
import com.populaire.projetguerrefroide.util.StateBudgetUtils;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.MILITARY_INCOME_TYPE;

public class MilitaryPostSystem {

    private final Query unitQuery;
    private final Query regionQuery;

    public MilitaryPostSystem(World ecsWorld, long phaseId) {
        this.unitQuery = ecsWorld.query()
            .with(UnitInstance.class)
            .with(AirWing.class).optional()
            .build();
        this.regionQuery = ecsWorld.query()
            .with(RegionInstance.class)
            .with(RegionDemographics.class)
            .build();
        ecsWorld.system("MilitaryPostSystem")
            .kind(phaseId)
            .with(CountryMilitary.class)
            .run(this::post);
    }

    private void post(Iter iter) {
        World world = iter.world();
        while (iter.next()) {
            Field<CountryMilitary> countryMilitaryField = iter.field(CountryMilitary.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMilitaryMutView countryMilitary = countryMilitaryField.getMutView(i);
                for (int p = 0; p < POP_TYPE_COUNT; p++) {
                    countryMilitary.posts(p, 0f);
                    countryMilitary.filledPosts(p, 0f);
                }
                countryMilitary.units(0);
            }
        }

        this.unitQuery.iter(unitIter -> {
            boolean airWings = unitIter.isSet(1);
            Field<UnitInstance> unitInstanceField = unitIter.field(UnitInstance.class, 0);
            Field<AirWing> airWingField = airWings ? unitIter.field(AirWing.class, 1) : null;
            long countryId = 0;
            CountryMilitaryMutView countryMilitary = null;
            for (int i = 0; i < unitIter.count(); i++) {
                UnitInstanceView unitInstance = unitInstanceField.getMutView(i);
                long unitCountryId = MilitaryUtils.countryId(world, unitInstance, airWings ? airWingField.getMutView(i) : null);
                if (unitCountryId != countryId) {
                    countryId = unitCountryId;
                    countryMilitary = world.obtainEntityView(countryId).getMutView(CountryMilitary.class);
                }
                UnitView unit = world.obtainEntityView(unitInstance.typeId()).getView(Unit.class);
                for (int k = 0; k < unit.crewPopTypeIndexesLength(); k++) {
                    int popTypeIndex = unit.crewPopTypeIndexes(k);
                    if (popTypeIndex < 0) {
                        break;
                    }
                    countryMilitary.posts(popTypeIndex, countryMilitary.posts(popTypeIndex) + unit.crewAmounts(k));
                }
                countryMilitary.units(countryMilitary.units() + 1);
            }
        });

        float[] baseWorkforceShares = EmploymentUtils.baseWorkforceShares(world);
        int[] incomeTypes = new int[POP_TYPE_COUNT];
        StateBudgetUtils.readPopulationTypes(world, incomeTypes, new boolean[POP_TYPE_COUNT]);
        this.regionQuery.iter(regionIter -> {
            Field<RegionInstance> regionInstanceField = regionIter.field(RegionInstance.class, 0);
            Field<RegionDemographics> regionDemographicsField = regionIter.field(RegionDemographics.class, 1);
            long countryId = 0;
            CountryMilitaryMutView countryMilitary = null;
            long countryAdults = 0;
            float workforceShareModifier = 0f;
            for (int i = 0; i < regionIter.count(); i++) {
                RegionInstanceMutView regionInstance = regionInstanceField.getMutView(i);
                RegionDemographicsView regionDemographics = regionDemographicsField.getMutView(i);
                if (regionInstance.ownerId() != countryId) {
                    countryId = regionInstance.ownerId();
                    EntityView country = world.obtainEntityView(countryId);
                    countryMilitary = country.getMutView(CountryMilitary.class);
                    CountryDemographicsView countryDemographics = country.getView(CountryDemographics.class);
                    CountryPopulationPolicyView populationPolicy = country.getView(CountryPopulationPolicy.class);
                    countryAdults = countryDemographics.totalAdults();
                    workforceShareModifier = populationPolicy.workforceShareModifier();
                }
                float adultShare = countryAdults > 0 ? (float) regionDemographics.totalAdults() / countryAdults : 0f;
                for (int p = 0; p < POP_TYPE_COUNT; p++) {
                    if (incomeTypes[p] != MILITARY_INCOME_TYPE) {
                        continue;
                    }
                    float posts = countryMilitary.posts(p) * adultShare;
                    int holders = regionDemographics.totalByPopType(p);
                    float activeHolders = holders * EmploymentUtils.activeShare(baseWorkforceShares[p], workforceShareModifier);
                    float employed = Math.min(activeHolders, posts);
                    regionInstance.statePostsByPopType(p, posts);
                    regionInstance.workerPopTypeEmploymentRatios(p, holders > 0 ? employed / holders : 0f);
                    regionInstance.vacanciesByPopType(p, Math.max(0f, posts - activeHolders));
                    regionInstance.applicantsByPopType(p, 0f);
                    countryMilitary.filledPosts(p, countryMilitary.filledPosts(p) + employed);
                }
            }
        });
    }
}
