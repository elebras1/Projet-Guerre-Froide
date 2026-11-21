package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.market.MarketUtils;
import com.populaire.projetguerrefroide.system.population.PopulationMobilityUtils;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.callback.IterCallback;

import java.util.Arrays;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.simulation.Constants.RECIPE_SWITCH_MARGIN;

public class RGOModeSystem {
    private static final double OFFSET_STEP = 0.6180339887498949;

    private final EcsConstants ecsConstants;
    private final boolean initial;
    private final Query rgoQuery;
    private final IterCallback gainReader = this::readGains;
    private final IterCallback chooser = this::choose;
    private final double[] candidateGains = new double[GOOD_COUNT];
    private final double[] pendingGains = new double[GOOD_COUNT];
    private final double[] adoptionShares = new double[GOOD_COUNT];
    private final double[] diffusion = new double[GOOD_COUNT];
    private World world;

    public RGOModeSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId, boolean initial) {
        this.ecsConstants = ecsConstants;
        this.initial = initial;
        this.rgoQuery = ecsWorld.query().with(ResourceGathering.class).with(Province.class).build();
        ecsWorld.system(initial ? "RGOModeInitializationSystem" : "RGOModeSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .run(this::run);
    }

    private void run(Iter iter) {
        World world = iter.stage();
        this.world = world;
        while (iter.next()) {
        }
        if (this.ecsConstants.rgoModeCount() <= 1 || !this.initial && !PopulationMobilityUtils.isMobilityDay(world, this.ecsConstants)) {
            return;
        }
        Arrays.fill(this.candidateGains, 0.0);
        Arrays.fill(this.pendingGains, 0.0);
        Arrays.fill(this.adoptionShares, this.initial ? 1.0 : 0.0);
        if (!this.initial) {
            this.rgoQuery.iter(this.gainReader);
            GlobalMarketView globalMarket = world.obtainEntityView(this.ecsConstants.globalMarket()).getView(GlobalMarket.class);
            float[] decays = this.ecsConstants.goodStockDecays();
            int day = world.obtainEntityView(this.ecsConstants.economyCalendar()).<EconomyCalendarView>getView(EconomyCalendar.class).day();
            double offset = (day / 30 * OFFSET_STEP) % 1.0;
            for (int g = 0; g < GOOD_COUNT; g++) {
                if (this.candidateGains[g] <= 0.0) {
                    continue;
                }
                double shortfall = MarketUtils.trendClearingProduction(globalMarket, g, decays[g]) - globalMarket.goodProductionAmounts(g)
                    - globalMarket.goodPendingRgoOutputs(g) - this.pendingGains[g];
                this.adoptionShares[g] = Math.clamp(shortfall / this.candidateGains[g], 0.0, 1.0);
                this.diffusion[g] = offset;
            }
        }
        this.rgoQuery.iter(this.chooser);
    }

    private void readGains(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        Field<Province> provinceField = iter.field(Province.class, 1);
        float[] outputs = this.ecsConstants.rgoModeOutputs();
        long countryId = 0;
        CountryProductionTechnologyView production = null;
        CountryMarketView market = null;
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = this.world.obtainEntityView(countryId);
                production = country.getView(CountryProductionTechnology.class);
                market = country.getView(CountryMarket.class);
            }
            ResourceGatheringView rgo = rgoField.getMutView(i);
            int good = rgo.goodIndex();
            int current = this.availableCurrent(rgo, production);
            if (current > 0) {
                this.pendingGains[good] += (double) rgo.modeBaseProduction() * (outputs[current] - 1f) * (1f - rgo.modeServed());
            }
            int best = this.best(current, good, production, market);
            if (best != current && outputs[best] > outputs[current]) {
                this.candidateGains[good] += (double) rgo.modeBaseProduction() * (outputs[best] - outputs[current]);
            }
        }
    }

    private void choose(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        Field<Province> provinceField = iter.field(Province.class, 1);
        float[] outputs = this.ecsConstants.rgoModeOutputs();
        long countryId = 0;
        CountryProductionTechnologyView production = null;
        CountryMarketView market = null;
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = this.world.obtainEntityView(countryId);
                production = country.getView(CountryProductionTechnology.class);
                market = country.getView(CountryMarket.class);
            }
            ResourceGatheringMutView rgo = rgoField.getMutView(i);
            int good = rgo.goodIndex();
            int current = this.availableCurrent(rgo, production);
            int best = this.best(current, good, production, market);
            int chosen = current;
            if (best != current) {
                if (this.initial || outputs[best] <= outputs[current]) {
                    chosen = best;
                } else {
                    double gain = (double) rgo.modeBaseProduction() * (outputs[best] - outputs[current]);
                    this.diffusion[good] += this.adoptionShares[good] * gain;
                    if (gain > 0.0 && this.diffusion[good] >= gain) {
                        this.diffusion[good] -= gain;
                        chosen = best;
                    }
                }
            }
            if (chosen != rgo.mode()) {
                rgo.mode(chosen);
                if (this.initial) {
                    rgo.modeServed(1f);
                }
            }
        }
    }

    private int availableCurrent(ResourceGatheringView rgo, CountryProductionTechnologyView production) {
        int current = rgo.mode();
        return current > 0 && !production.rgoModes(current) ? 0 : current;
    }

    private int best(int current, int good, CountryProductionTechnologyView production, CountryMarketView market) {
        double currentValue = this.value(current, good, market);
        int best = current;
        double bestValue = currentValue;
        for (int m = 0; m < this.ecsConstants.rgoModeCount(); m++) {
            if (m == current || (m > 0 && (!production.rgoModes(m) || !this.ecsConstants.rgoModeGoods()[m][good]))) {
                continue;
            }
            double value = this.value(m, good, market);
            if (value > bestValue) {
                best = m;
                bestValue = value;
            }
        }
        if (!this.initial && best != current && bestValue <= currentValue * (1.0 + RECIPE_SWITCH_MARGIN)) {
            return current;
        }
        return best;
    }

    private double value(int mode, int good, CountryMarketView market) {
        float[] costs = this.ecsConstants.goodCosts();
        double price = this.initial ? costs[good] : market.goodProducerPrices(good);
        int[] inputs = this.ecsConstants.rgoModeInputGoods()[mode];
        float[] amounts = this.ecsConstants.rgoModeInputAmounts()[mode];
        double inputCost = 0.0;
        for (int k = 0; k < inputs.length; k++) {
            inputCost += amounts[k] * (this.initial ? costs[inputs[k]] : market.goodPrices(inputs[k]));
        }
        return this.ecsConstants.rgoModeOutputs()[mode] * (price - inputCost);
    }
}
