package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeShare;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeWorkers;

/**
 * RGO endowment at loading (annex 10 of the economy plan, § 10.21): land for farmers, deposit or forest for labourers,
 * in levels of {@code workforce}. Production is {@code value x M x endowment^alpha x levels^(1 - alpha)}.
 * <p>
 * The province field {@code good_potential} is the natural production potential of the RGO, reachable after 2000 (its
 * share of the world potential of its good). The endowment is proportional to it, {@code endowment = k x potential},
 * with k such that the 1946 world production of the good is the one of the former rule (value x sum of M x levels):
 * {@code k = (sum(M x L) / sum(M x potential^alpha x L^(1 - alpha)))^(1 / alpha)}. {@code value} cancels out, so the
 * supply calibration still scales the good through {@code value}; the 1946 production of each RGO then follows its
 * potential, its 1946 workers and its owner's technologies, and nothing caps it later.
 * <p>
 * An RGO without potential keeps the former rule: endowment = 1.5 x levels of its worker type, alpha = 0 (production
 * proportional to its workers, capped at the endowment).
 * <p>
 * Runs after the law, technology, mechanization and infrastructure initializations, before the employment one.
 */
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

    /** Owner data read once per country, and the 1946 terms of the production formula. */
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

        /**
         * Levels of workers in 1946: active workers / (workforce x worker ratio). A mechanized RGO employs fewer of them, each
         * doing the work of 1 + k x level (annex 12, decision 192): the effective levels are still the active workers.
         */
        private double levels(DemographicsView demographics) {
            int active = activeWorkers(demographics.totalByPopType(this.rgoType.workerPopTypeIndex()), this.workforceShare);
            return active / ((double) Math.max(1, this.rgoType.workforce()) * this.rgoType.workerPopTypeRatio());
        }

        /** M of the production formula in 1946 (no slaves employed yet). */
        private double multiplier(ResourceGatheringView rgo, ProvinceInfrastructureView infrastructure) {
            float modeFactor = rgo.mode() > 0 ? 1f + (ecsConstants.rgoModeOutputs()[rgo.mode()] - 1f) * rgo.modeServed() : 1f;
            return (double) rgo.efficiency() * this.modifiers * this.rgoType.workerEffectMultiplier() * (1f + infrastructure.productionBonus()) * modeFactor;
        }
    }
}
