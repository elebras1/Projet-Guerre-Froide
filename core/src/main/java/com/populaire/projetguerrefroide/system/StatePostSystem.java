package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.StateBudgetUtils;

public class StatePostSystem {

    public StatePostSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StatePostSystem")
            .kind(phaseId)
            .with(RegionInstance.class)
            .with(RegionInstanceIncome.class)
            .with(RegionDemographics.class)
            .iter(this::post);
    }

    private void post(Iter iter) {
        World world = iter.world();
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(world.lookup("global_population_type")).getView(GlobalPopulationType.class);
        int postIndex = globalPopulationType.statePostPopTypeIndex();
        if (postIndex < 0) {
            return;
        }
        StatePostTypeView statePostType = world.obtainEntityView(globalPopulationType.popTypeIds(postIndex)).getView(StatePostType.class);
        float managementRatio = statePostType.managementRatio();
        float administrationRatio = statePostType.administrationRatio();
        float taxCollectionFloor = statePostType.taxCollectionFloor();
        float managementOutputBonus = statePostType.managementOutputBonus();

        long countryId = 0;
        float administrationSlider = 0f;
        float efficiency = 1f;

        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        Field<RegionDemographics> regionDemographicsField = iter.field(RegionDemographics.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            RegionInstanceMutView regionInstance = regionInstanceField.getMutView(i);
            RegionInstanceIncomeView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);
            RegionDemographicsView regionDemographics = regionDemographicsField.getMutView(i);

            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                EntityView country = world.obtainEntityView(countryId);
                CountryBudgetPolicyView budgetPolicy = country.getView(CountryBudgetPolicy.class);
                CountryPoliticalPolicyView politicalPolicy = country.getView(CountryPoliticalPolicy.class);
                administrationSlider = StateBudgetUtils.slider(budgetPolicy.administrationSpendingRate());
                efficiency = Math.max(0f, 1f + politicalPolicy.administrativeEfficiency());
            }

            float managementPosts = managementRatio * regionInstanceIncome.stateBuildingWorkers();
            float posts = managementPosts + administrationRatio * regionDemographics.totalAdults() * administrationSlider;
            int holders = regionDemographics.totalByPopType(postIndex);
            float employed = Math.min(holders, posts);
            float fill = posts > 0f ? employed / posts : 0f;

            regionInstance.statePostsByPopType(postIndex, posts);
            regionInstance.workerPopTypeEmploymentRatios(postIndex, holders > 0 ? employed / holders : 0f);
            regionInstance.vacanciesByPopType(postIndex, Math.max(0f, posts - holders));
            regionInstance.applicantsByPopType(postIndex, 0f);
            regionInstance.taxCollectionShare(Math.min(1f, taxCollectionFloor + (1f - taxCollectionFloor) * fill * efficiency));
            regionInstance.managementOutputBonus(managementPosts > 0f ? managementOutputBonus * fill * efficiency : 0f);
        }
    }
}
