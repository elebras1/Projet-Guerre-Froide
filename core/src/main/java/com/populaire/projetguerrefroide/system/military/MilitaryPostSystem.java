package com.populaire.projetguerrefroide.system.military;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.callback.IterCallback;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.system.population.EmploymentUtils;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.POP_TYPE_COUNT;
import static com.populaire.projetguerrefroide.system.state.IncomeTypeUtils.MILITARY_INCOME_TYPE;

public class MilitaryPostSystem {

    private final Query unitQuery;
    private final Query regionQuery;
    private final Query specialQuery;
    private final Query countryQuery;
    private final EcsConstants ecsConstants;
    private final EntityCallback readCountryReader = this::readCountry;
    private int countryStaffIndex;
    private final IterCallback readRegionReader = this::readRegion;
    private final IterCallback readAcademyReader = this::readAcademy;
    private long academyId;
    private int academyStaffIndex;
    private final IterCallback readUnitReader = this::readUnit;
    private World world;

    public MilitaryPostSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.unitQuery = ecsWorld.query()
            .with(UnitInstance.class)
            .with(AirWing.class).optional()
            .build();
        this.regionQuery = ecsWorld.query()
            .with(RegionInstance.class)
            .with(RegionDemographics.class)
            .build();
        this.specialQuery = ecsWorld.query()
            .with(Building.class)
            .with(SpecialBuilding.class)
            .with(SpecialBuildingType.class)
            .build();
        this.countryQuery = ecsWorld.query().with(CountryMilitary.class).build();
        ecsWorld.system("MilitaryPostSystem")
            .kind(phaseId)
            .with(CountryMilitary.class)
            .run(this::post);
    }

    private void post(Iter iter) {
        World world = iter.world();
        this.world = world;
        while (iter.next()) {
            Field<CountryMilitary> countryMilitaryField = iter.field(CountryMilitary.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMilitaryMutView countryMilitary = countryMilitaryField.getMutView(i);
                for (int p = 0; p < POP_TYPE_COUNT; p++) {
                    countryMilitary.posts(p, 0f);
                    countryMilitary.filledPosts(p, 0f);
                }
                countryMilitary.units(0);
                countryMilitary.academyPosts(0f);
                countryMilitary.academyFilled(0f);
            }
        }

        this.unitQuery.iter(this.readUnitReader);

        EntityView globalFacilities = world.obtainEntityView(this.ecsConstants.globalMilitaryFacilities());
        long academyTypeId = 0;
        int academyStaffIndex = -1;
        if (globalFacilities.has(GlobalMilitaryFacilities.class)) {
            GlobalMilitaryFacilitiesView facilities = globalFacilities.getView(GlobalMilitaryFacilities.class);
            academyTypeId = facilities.academyTypeId();
            academyStaffIndex = facilities.academyStaffPopTypeIndex();
        }
        if (academyTypeId != 0 && academyStaffIndex >= 0) {
            this.academyId = academyTypeId;
            this.academyStaffIndex = academyStaffIndex;
            this.specialQuery.iter(this.readAcademyReader);
        }

        this.regionQuery.iter(this.readRegionReader);

        if (academyStaffIndex < 0) {
            return;
        }
        this.countryStaffIndex = academyStaffIndex;
        this.countryQuery.each(this.readCountryReader);
    }

    private void readUnit(Iter unitIter) {
        World world = this.world;
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
    }

    private void readAcademy(Iter specialIter) {
        World world = this.world;
        long academyId = this.academyId;
        int staffIndex = this.academyStaffIndex;
        if (specialIter.src(2) != academyId) {
            return;
        }
        SpecialBuildingTypeView buildingType = specialIter.field(SpecialBuildingType.class, 2).getView(0);
        Field<Building> buildingField = specialIter.field(Building.class, 0);
        for (int j = 0; j < specialIter.count(); j++) {
            BuildingView building = buildingField.getMutView(j);
            if (building.size() <= 0 || building.countryId() == 0) {
                continue;
            }
            CountryMilitaryMutView countryMilitary = world.obtainEntityView(building.countryId()).getMutView(CountryMilitary.class);
            float posts = building.size() * buildingType.staffPosts();
            countryMilitary.posts(staffIndex, countryMilitary.posts(staffIndex) + posts);
            countryMilitary.academyPosts(countryMilitary.academyPosts() + posts);
        }
    }

    private void readRegion(Iter regionIter) {
        World world = this.world;
        float[] baseWorkforceShares = this.ecsConstants.baseWorkforceShares();
        int[] incomeTypes = this.ecsConstants.incomeTypes();
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
    }

    private void readCountry(long countryId) {
        World world = this.world;
        int staffIndex = this.countryStaffIndex;
        CountryMilitaryMutView countryMilitary = world.obtainEntityView(countryId).getMutView(CountryMilitary.class);
        float academyPosts = countryMilitary.academyPosts();
        if (academyPosts <= 0f) {
            return;
        }
        float unitPosts = countryMilitary.posts(staffIndex) - academyPosts;
        countryMilitary.academyFilled(Math.clamp(countryMilitary.filledPosts(staffIndex) - unitPosts, 0f, academyPosts));
    }
}
