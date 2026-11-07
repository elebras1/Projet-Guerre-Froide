package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.util.EmploymentUtils;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;

import static com.populaire.projetguerrefroide.simulation.Constants.POP_TYPE_COUNT;
import static com.populaire.projetguerrefroide.util.ArrayUtils.zeroed;

public class PopulationApplicationSystem {

    private final EcsConstants ecsConstants;
    private final float[] promotionWeights = new float[POP_TYPE_COUNT];
    private final float[] demotionWeights = new float[POP_TYPE_COUNT];
    private final float[] vacancyShares = new float[POP_TYPE_COUNT];
    private final float[] openShares = new float[POP_TYPE_COUNT];
    private final float[] fallbackSources = new float[POP_TYPE_COUNT];
    private final float[] fallbackWeights = new float[POP_TYPE_COUNT];

    public PopulationApplicationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("PopulationApplicationSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .iter(this::apply);
    }

    private void apply(Iter iter) {
        World world = iter.world();
        if (!PopulationMobilityUtils.isMobilityDay(world, this.ecsConstants)) {
            return;
        }

        PopulationTypeView populationType = iter.field(PopulationType.class, 1).getView(0);
        float[] promotionWeights = PopulationMobilityUtils.normalizedWeights(populationType, true, this.promotionWeights);
        float[] demotionWeights = PopulationMobilityUtils.normalizedWeights(populationType, false, this.demotionWeights);
        if (!PopulationMobilityUtils.hasWeights(promotionWeights, demotionWeights)) {
            return;
        }
        boolean[] employeeTypes = this.ecsConstants.employeePopTypes();
        boolean[] ruralTypes = this.ecsConstants.ruralPopTypes();
        float[] vacancyShares = zeroed(this.vacancyShares);
        float[] openShares = zeroed(this.openShares);
        float[] fallbackSources = zeroed(this.fallbackSources);
        float[] fallbackWeights = zeroed(this.fallbackWeights);

        long countryId = 0;
        float classRigidity = 0f;
        float workforceShare = 0f;
        float baseWorkforceShare = this.ecsConstants.baseWorkforceShares()[populationType.index()];
        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);
            int amount = population.amount();
            if (amount <= 0) {
                continue;
            }
            if (population.countryId() != countryId) {
                countryId = population.countryId();
                EntityView country = world.obtainEntityView(countryId);
                CountryPoliticalPolicyView politicalPolicy = country.getView(CountryPoliticalPolicy.class);
                CountryPopulationPolicyView populationPolicy = country.getView(CountryPopulationPolicy.class);
                classRigidity = politicalPolicy.classRigidity();
                workforceShare = EmploymentUtils.activeShare(baseWorkforceShare, populationPolicy.workforceShareModifier());
            }

            EntityView province = world.obtainEntityView(population.provinceId());
            ResourceGatheringMutView resourceGathering = null;
            int rgoWorkerIndex = -1;
            if (province.has(ResourceGathering.class)) {
                resourceGathering = province.getMutView(ResourceGathering.class);
                ResourceGatheringTypeView rgoType = world.obtainEntityView(province.target(this.ecsConstants.rgoType(), 0)).getView(ResourceGatheringType.class);
                rgoWorkerIndex = rgoType.workerPopTypeIndex();
            }
            PopulationMobilityUtils.shares(population, classRigidity, workforceShare, promotionWeights, demotionWeights, employeeTypes, ruralTypes,
                rgoWorkerIndex, province.getView(Demographics.class), vacancyShares, openShares, fallbackSources, fallbackWeights);

            RegionInstanceMutView regionInstance = world.obtainEntityView(population.regionInstanceId()).getMutView(RegionInstance.class);
            for (int b = 0; b < POP_TYPE_COUNT; b++) {
                if (vacancyShares[b] <= 0f) {
                    continue;
                }
                float applicants = amount * vacancyShares[b];
                if (employeeTypes[b]) {
                    regionInstance.applicantsByPopType(b, regionInstance.applicantsByPopType(b) + applicants);
                } else {
                    resourceGathering.applicants(resourceGathering.applicants() + applicants);
                }
            }
        }
    }
}
