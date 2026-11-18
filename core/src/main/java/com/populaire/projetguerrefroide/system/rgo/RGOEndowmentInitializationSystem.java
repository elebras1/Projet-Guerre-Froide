package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeShare;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeWorkers;

public class RGOEndowmentInitializationSystem {
    private final EcsConstants ecsConstants;
    private final double[] effectiveLevels = new double[GOOD_COUNT];
    private final double[] potentialOutputs = new double[GOOD_COUNT];

    public RGOEndowmentInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("RGOEndowmentTotalsSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .with(Province.class)
            .with(ProvinceInfrastructure.class)
            .iter(this::accumulate);
        ecsWorld.system("RGOEndowmentInitializationSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .iter(this::assign);
    }

    private void accumulate(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        ResourceGatheringTypeView rgoType = iter.field(ResourceGatheringType.class, 2).getView(0);
        Field<Province> provinceField = iter.field(Province.class, 3);
        Field<ProvinceInfrastructure> infrastructureField = iter.field(ProvinceInfrastructure.class, 4);
        float alpha = rgoType.endowmentShare();
        if (alpha <= 0f) {
            return;
        }
        Context context = new Context(rgoType);
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringView rgo = rgoField.getMutView(i);
            if (rgo.potential() <= 0f) {
                continue;
            }
            context.load(iter, provinceField.getMutView(i), rgo.goodIndex());
            double l = context.levels(demographicsField.getMutView(i));
            if (l <= 0.0) {
                continue;
            }
            double m = context.multiplier(rgo, infrastructureField.getMutView(i));
            int good = rgo.goodIndex();
            this.effectiveLevels[good] += m * l;
            this.potentialOutputs[good] += m * Math.pow(rgo.potential(), alpha) * Math.pow(l, 1.0 - alpha);
        }
    }

    private void assign(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        ResourceGatheringTypeView rgoType = iter.field(ResourceGatheringType.class, 2).getView(0);
        int workforce = Math.max(1, rgoType.workforce());
        float alpha = rgoType.endowmentShare();
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView rgo = rgoField.getMutView(i);
            int good = rgo.goodIndex();
            if (alpha <= 0f || rgo.potential() <= 0f || this.potentialOutputs[good] <= 0.0) {
                DemographicsView demographics = demographicsField.getMutView(i);
                int workerAmount = demographics.totalByPopType(rgoType.workerPopTypeIndex());
                rgo.endowment(((workerAmount + workforce - 1) / workforce) * 1.5f);
                rgo.endowmentShare(0f);
                continue;
            }
            double scale = Math.pow(this.effectiveLevels[good] / this.potentialOutputs[good], 1.0 / alpha);
            rgo.endowment((float) (rgo.potential() * scale));
            rgo.endowmentShare(alpha);
        }
    }

    private final class Context {
        private final ResourceGatheringTypeView rgoType;
        private final float workerBaseShare;
        private long countryId;
        private float workforceShare;
        private float modifiers;

        private Context(ResourceGatheringTypeView rgoType) {
            this.rgoType = rgoType;
            this.workerBaseShare = ecsConstants.baseWorkforceShares()[rgoType.workerPopTypeIndex()];
        }

        private void load(Iter iter, ProvinceView province, int good) {
            if (province.ownerId() == this.countryId) {
                return;
            }
            this.countryId = province.ownerId();
            var country = iter.stage().obtainEntityView(this.countryId);
            CountryPopulationPolicyView populationPolicy = country.getView(CountryPopulationPolicy.class);
            this.workforceShare = activeShare(this.workerBaseShare, populationPolicy.workforceShareModifier());
            float outputModifier = country.<CountryProductionPolicyView>getView(CountryProductionPolicy.class).rgoOutputModifier();
            float productivity = country.<CountryProductionTechnologyView>getView(CountryProductionTechnology.class).rgoProductivities(good);
            this.modifiers = 1f + outputModifier + productivity;
        }

        private double levels(DemographicsView demographics) {
            int active = activeWorkers(demographics.totalByPopType(this.rgoType.workerPopTypeIndex()), this.workforceShare);
            return active / ((double) Math.max(1, this.rgoType.workforce()) * this.rgoType.workerPopTypeRatio());
        }

        private double multiplier(ResourceGatheringView rgo, ProvinceInfrastructureView infrastructure) {
            float modeFactor = rgo.mode() > 0 ? 1f + (ecsConstants.rgoModeOutputs()[rgo.mode()] - 1f) * rgo.modeServed() : 1f;
            return (double) rgo.efficiency() * this.modifiers * this.rgoType.workerEffectMultiplier() * (1f + infrastructure.productionBonus()) * modeFactor;
        }
    }
}
