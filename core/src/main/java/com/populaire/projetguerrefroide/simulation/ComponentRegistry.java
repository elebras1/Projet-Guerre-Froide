package com.populaire.projetguerrefroide.simulation;

import com.populaire.projetguerrefroide.component.*;
import io.github.elebras1.flecs.World;

public final class ComponentRegistry {
    private ComponentRegistry() {
    }

    public static void register(World ecsWorld) {
        ecsWorld.component(Modifiers.class);
        ecsWorld.component(Overrides.class);
        ecsWorld.component(Minister.class);
        ecsWorld.component(Ideology.class);
        ecsWorld.component(Terrain.class);
        ecsWorld.component(ElectoralMechanism.class);
        ecsWorld.component(Leader.class);
        ecsWorld.component(Color.class);
        ecsWorld.component(Position.class);
        ecsWorld.component(Border.class);
        ecsWorld.component(DiplomaticRelation.class);
        ecsWorld.component(Adjacencies.class);
        ecsWorld.component(Country.class);
        ecsWorld.component(Province.class);
        ecsWorld.component(Law.class);
        ecsWorld.component(LawGroup.class);
        ecsWorld.component(GovernmentPolicy.class);
        ecsWorld.component(PopulationType.class);
        ecsWorld.component(StatePostType.class);
        ecsWorld.component(GlobalStateConsumption.class);
        ecsWorld.component(CountryStateConsumption.class);
        ecsWorld.component(CountryPublicWorks.class);
        ecsWorld.component(ArtisanCraft.class);
        ecsWorld.component(GlobalArtisanCrafts.class);
        ecsWorld.component(RegionArtisans.class);
        ecsWorld.component(CountryMaintenance.class);
        ecsWorld.component(CountryStrategicStock.class);
        ecsWorld.component(CountryCentralBank.class);
        ecsWorld.component(CountryMilitary.class);
        ecsWorld.component(ProvinceInfrastructure.class);
        ecsWorld.component(Good.class);
        ecsWorld.component(EconomyBuildingType.class);
        ecsWorld.component(SpecialBuildingType.class);
        ecsWorld.component(GlobalStrategicStorage.class);
        ecsWorld.component(GlobalMilitaryFacilities.class);
        ecsWorld.component(DevelopmentBuildingType.class);
        ecsWorld.component(Building.class);
        ecsWorld.component(EconomyBuilding.class);
        ecsWorld.component(SpecialBuilding.class);
        ecsWorld.component(DevelopmentBuilding.class);
        ecsWorld.component(ResourceGathering.class);
        ecsWorld.component(ConstructionSite.class);
        ecsWorld.component(CountryInvestment.class);
        ecsWorld.component(RegionInstance.class);
        ecsWorld.component(Population.class);
        ecsWorld.component(PopulationTransfer.class);
        ecsWorld.component(EconomyCalendar.class);
        ecsWorld.component(CountryBudget.class);
        ecsWorld.component(CountryBank.class);
        ecsWorld.component(ResourceGatheringType.class);
        ecsWorld.component(Demographics.class);
        ecsWorld.component(RegionDemographics.class);
        ecsWorld.component(CountryDemographics.class);
        ecsWorld.component(CountryMarket.class);
        ecsWorld.component(GlobalPopulationType.class);
        ecsWorld.component(GlobalGood.class);
        ecsWorld.component(GlobalMarket.class);
        ecsWorld.component(CountryTaxPolicy.class);
        ecsWorld.component(CountryBudgetPolicy.class);
        ecsWorld.component(CountryEducationPolicy.class);
        ecsWorld.component(CountryTradePolicy.class);
        ecsWorld.component(CountryProductionPolicy.class);
        ecsWorld.component(CountryLaborPolicy.class);
        ecsWorld.component(CountryPopulationPolicy.class);
        ecsWorld.component(CountryPoliticalPolicy.class);
        ecsWorld.component(CountryCulturePolicy.class);
        ecsWorld.component(CountryProfitDistributionPolicy.class);
        ecsWorld.component(RegionInstanceIncome.class);
        ecsWorld.component(RegionSpecialBuildings.class);
        ecsWorld.component(Unit.class);
        ecsWorld.component(Formation.class);
        ecsWorld.component(UnitInstance.class);
        ecsWorld.component(AirWing.class);
    }
}
