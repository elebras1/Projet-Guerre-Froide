package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class CountryCentralBankSystem {
    private final EcsConstants ecsConstants;

    public CountryCentralBankSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryCentralBankSystem")
            .kind(phaseId)
            .with(CountryCentralBank.class)
            .with(CountryMarket.class)
            .with(CountryDemographics.class)
            .iter(this::regulate);
    }

    private void regulate(Iter iter) {
        World world = iter.stage();
        float[] baseCosts = this.ecsConstants.goodCosts();
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(this.ecsConstants.globalPopulationType()).getView(GlobalPopulationType.class);

        Field<CountryCentralBank> countryCentralBankField = iter.field(CountryCentralBank.class, 0);
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 1);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            CountryCentralBankMutView countryCentralBank = countryCentralBankField.getMutView(i);
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);

            if (countryCentralBank.priceIndex() <= 0f) {
                initialize(world, globalPopulationType, countryCentralBank, countryMarket, countryDemographics, baseCosts);
                countryCentralBank.policyRate(taylorRate(0f));
                continue;
            }

            double priceIndex = 0.0;
            for (int g = 0; g < baseCosts.length; g++) {
                float weight = countryCentralBank.priceIndexWeights(g);
                if (weight > 0f && countryCentralBank.basePrices(g) > 0f) {
                    priceIndex += weight * countryMarket.goodPrices(g) / countryCentralBank.basePrices(g);
                }
            }
            float inflation = countryCentralBank.inflation();
            if (priceIndex > 0.0) {
                double dailyChange = Math.log(priceIndex / countryCentralBank.priceIndex());
                inflation += (float) ((DAYS_PER_YEAR * dailyChange - inflation) / INFLATION_TREND_DAYS);
                countryCentralBank.priceIndex((float) priceIndex);
            }
            countryCentralBank.inflation(inflation);
            countryCentralBank.policyRate(taylorRate(inflation));
        }
    }

    private static float taylorRate(float inflation) {
        float rate = CENTRAL_BANK_REAL_RATE + inflation + CENTRAL_BANK_INFLATION_GAIN * (inflation - CENTRAL_BANK_TARGET_INFLATION);
        return Math.max(0f, rate);
    }

    private static void initialize(World world, GlobalPopulationTypeView globalPopulationType, CountryCentralBankMutView countryCentralBank,
                                   CountryMarketView countryMarket, CountryDemographicsView countryDemographics, float[] baseCosts) {
        double[] weights = new double[baseCosts.length];
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            double units = countryDemographics.totalByPopType(p) / NEEDS_SCALING_FACTOR;
            if (units <= 0.0) {
                continue;
            }
            PopulationTypeView populationType = world.obtainEntityView(globalPopulationType.popTypeIds(p)).getView(PopulationType.class);
            for (int k = 0; k < populationType.lifeNeedsGoodIndexesLength(); k++) {
                int goodIndex = populationType.lifeNeedsGoodIndexes(k);
                if (goodIndex >= 0) {
                    weights[goodIndex] += units * populationType.lifeNeedsGoodAmounts(k) * baseCosts[goodIndex];
                }
            }
            for (int k = 0; k < populationType.everydayNeedsGoodIndexesLength(); k++) {
                int goodIndex = populationType.everydayNeedsGoodIndexes(k);
                if (goodIndex >= 0) {
                    weights[goodIndex] += 0.5 * units * populationType.everydayNeedsGoodAmounts(k) * baseCosts[goodIndex];
                }
            }
        }
        double total = 0.0;
        for (double weight : weights) {
            total += weight;
        }
        for (int g = 0; g < baseCosts.length; g++) {
            countryCentralBank.priceIndexWeights(g, total > 0.0 ? (float) (weights[g] / total) : 0f);
            float price = countryMarket.goodPrices(g);
            countryCentralBank.basePrices(g, price > 0f ? price : baseCosts[g]);
        }
        countryCentralBank.priceIndex(1f);
        countryCentralBank.inflation(0f);
    }
}
