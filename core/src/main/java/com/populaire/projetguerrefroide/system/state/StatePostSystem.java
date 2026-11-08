package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.system.population.EmploymentUtils;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

public class StatePostSystem {
    private final EcsConstants ecsConstants;

    public StatePostSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("StatePostSystem")
            .kind(phaseId)
            .with(RegionInstance.class)
            .with(RegionInstanceIncome.class)
            .with(RegionDemographics.class)
            .with(RegionSpecialBuildings.class)
            .iter(this::post);
    }

    private void post(Iter iter) {
        World world = iter.stage();
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(this.ecsConstants.globalPopulationType()).getView(GlobalPopulationType.class);
        int postIndex = globalPopulationType.statePostPopTypeIndex();
        if (postIndex < 0) {
            return;
        }
        StatePostTypeView statePostType = world.obtainEntityView(globalPopulationType.popTypeIds(postIndex)).getView(StatePostType.class);
        float managementRatio = statePostType.managementRatio();
        float administrationRatio = statePostType.administrationRatio();
        float taxCollectionFloor = statePostType.taxCollectionFloor();
        float managementOutputBonus = statePostType.managementOutputBonus();
        float postBaseShare = globalPopulationType.workforceShareByPopType(postIndex);

        long countryId = 0;
        float administrationSlider = 0f;
        float efficiency = 1f;
        float workforceShare = 0f;

        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        Field<RegionDemographics> regionDemographicsField = iter.field(RegionDemographics.class, 2);
        Field<RegionSpecialBuildings> regionSpecialBuildingsField = iter.field(RegionSpecialBuildings.class, 3);
        for (int i = 0; i < iter.count(); i++) {
            RegionInstanceMutView regionInstance = regionInstanceField.getMutView(i);
            RegionInstanceIncomeView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);
            RegionDemographicsView regionDemographics = regionDemographicsField.getMutView(i);
            RegionSpecialBuildingsMutView regionSpecialBuildings = regionSpecialBuildingsField.getMutView(i);

            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                EntityView country = world.obtainEntityView(countryId);
                CountryBudgetPolicyView budgetPolicy = country.getView(CountryBudgetPolicy.class);
                CountryPoliticalPolicyView politicalPolicy = country.getView(CountryPoliticalPolicy.class);
                CountryPopulationPolicyView populationPolicy = country.getView(CountryPopulationPolicy.class);
                workforceShare = EmploymentUtils.activeShare(postBaseShare, populationPolicy.workforceShareModifier());
                administrationSlider = StateBudgetUtils.slider(budgetPolicy.administrationSpendingRate());
                efficiency = Math.max(0f, 1f + politicalPolicy.administrativeEfficiency());
            }

            float managementPosts = managementRatio * regionInstanceIncome.stateBuildingWorkers();
            float statePosts = managementPosts + administrationRatio * regionDemographics.totalAdults() * administrationSlider;
            float buildingPosts = regionSpecialBuildings.staffPosts();
            float posts = statePosts + buildingPosts;
            int holders = regionDemographics.totalByPopType(postIndex);
            float activeHolders = holders * workforceShare;
            float employed = Math.min(activeHolders, posts);
            float fill = statePosts > 0f ? Math.min(activeHolders, statePosts) / statePosts : 0f;
            regionSpecialBuildings.staffFill(buildingPosts > 0f ? Math.clamp((activeHolders - statePosts) / buildingPosts, 0f, 1f) : 0f);

            regionInstance.statePostsByPopType(postIndex, posts);
            regionInstance.workerPopTypeEmploymentRatios(postIndex, holders > 0 ? employed / holders : 0f);
            regionInstance.vacanciesByPopType(postIndex, Math.max(0f, posts - activeHolders));
            regionInstance.applicantsByPopType(postIndex, 0f);
            regionInstance.taxCollectionShare(Math.min(1f, taxCollectionFloor + (1f - taxCollectionFloor) * fill * efficiency));
            regionInstance.managementOutputBonus(managementPosts > 0f ? managementOutputBonus * fill * efficiency : 0f);
        }
    }
}
