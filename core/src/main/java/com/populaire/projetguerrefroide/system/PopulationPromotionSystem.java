package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

public class PopulationPromotionSystem {

    private final EcsConstants ecsConstants;

    public PopulationPromotionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("PopulationPromotionSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .iter(this::promote);
    }

    private void promote(Iter iter) {
        World world = iter.world();
        if (!PopulationMobilityUtils.isMobilityDay(world)) {
            return;
        }

        PopulationTypeView populationType = iter.field(PopulationType.class, 1).getView(0);
        float[] promotionWeights = PopulationMobilityUtils.normalizedWeights(populationType, true);
        float[] demotionWeights = PopulationMobilityUtils.normalizedWeights(populationType, false);
        if (!PopulationMobilityUtils.hasWeights(promotionWeights, demotionWeights)) {
            return;
        }
        boolean[] employeeTypes = PopulationMobilityUtils.popTypesWith(world, this.ecsConstants.employeeTag());
        boolean[] ruralTypes = PopulationMobilityUtils.popTypesWith(world, this.ecsConstants.ruralTag());
        float[] vacancyShares = new float[POP_TYPE_COUNT];
        float[] openShares = new float[POP_TYPE_COUNT];

        long countryId = 0;
        float classRigidity = 0f;
        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);
            int amount = population.amount();
            if (amount <= 0) {
                continue;
            }
            if (population.countryId() != countryId) {
                countryId = population.countryId();
                CountryPoliticalPolicyView politicalPolicy = world.obtainEntityView(countryId).getView(CountryPoliticalPolicy.class);
                classRigidity = politicalPolicy.classRigidity();
            }

            EntityView province = world.obtainEntityView(population.provinceId());
            ResourceGatheringView resourceGathering = null;
            int rgoWorkerIndex = -1;
            if (province.has(ResourceGathering.class)) {
                resourceGathering = province.getView(ResourceGathering.class);
                ResourceGatheringTypeView rgoType = world.obtainEntityView(province.target(this.ecsConstants.rgoType(), 0)).getView(ResourceGatheringType.class);
                rgoWorkerIndex = rgoType.workerPopTypeIndex();
            }
            PopulationMobilityUtils.shares(population, classRigidity, promotionWeights, demotionWeights, employeeTypes, ruralTypes,
                rgoWorkerIndex, province.getView(Demographics.class), vacancyShares, openShares);

            PopulationTransferMutView transfer = province.getMutView(PopulationTransfer.class);
            RegionInstanceView regionInstance = world.obtainEntityView(population.regionInstanceId()).getView(RegionInstance.class);
            for (int b = 0; b < POP_TYPE_COUNT; b++) {
                float hiredRatio = 0f;
                if (vacancyShares[b] > 0f) {
                    float vacancies = employeeTypes[b] ? regionInstance.vacanciesByPopType(b) : resourceGathering.vacancies();
                    float applicants = employeeTypes[b] ? regionInstance.applicantsByPopType(b) : resourceGathering.applicants();
                    hiredRatio = applicants > 0f ? Math.min(1f, vacancies / applicants) : 0f;
                }
                int current = population.amount();
                int moving = Math.min((int) (amount * (vacancyShares[b] * hiredRatio + openShares[b])), current);
                if (moving <= 0) {
                    continue;
                }
                float movingSavings = population.savings() * moving / current;
                population.amount(current - moving);
                population.savings(population.savings() - movingSavings);
                transfer.arrivingAmounts(b, transfer.arrivingAmounts(b) + moving);
                transfer.arrivingSavings(b, transfer.arrivingSavings(b) + movingSavings);
            }
            population.employment(Math.min(population.employment(), population.amount()));
        }
    }
}
