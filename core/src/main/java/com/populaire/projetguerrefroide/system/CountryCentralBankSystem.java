package com.populaire.projetguerrefroide.system;

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
            .with(CountryBank.class)
            .with(CountryInvestment.class)
            .with(CountryStrategicStock.class)
            .iter(this::regulate);
    }

    private void regulate(Iter iter) {
        World world = iter.world();
        float[] baseCosts = this.ecsConstants.goodCosts();
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(this.ecsConstants.globalPopulationType()).getView(GlobalPopulationType.class);

        Field<CountryCentralBank> countryCentralBankField = iter.field(CountryCentralBank.class, 0);
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 1);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 2);
        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 3);
        Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 4);
        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 5);
        for (int i = 0; i < iter.count(); i++) {
            CountryCentralBankMutView countryCentralBank = countryCentralBankField.getMutView(i);
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            CountryBankMutView countryBank = countryBankField.getMutView(i);
            CountryInvestmentView countryInvestment = countryInvestmentField.getMutView(i);
            CountryStrategicStockView countryStrategicStock = countryStrategicStockField.getMutView(i);

            double realOutput = 0.0;
            for (int g = 0; g < baseCosts.length; g++) {
                realOutput += (double) (countryMarket.goodProducedAmounts(g) - countryStrategicStock.releasedAmounts(g)) * baseCosts[g];
            }

            if (countryCentralBank.targetPriceIndex() <= 0f) {
                initialize(world, globalPopulationType, countryCentralBank, countryMarket, countryDemographics, baseCosts);
                countryCentralBank.realOutputTrend(realOutput);
                continue;
            }

            double priceIndex = 0.0;
            for (int g = 0; g < baseCosts.length; g++) {
                float weight = countryCentralBank.priceIndexWeights(g);
                if (weight > 0f && countryCentralBank.basePrices(g) > 0f) {
                    priceIndex += weight * countryMarket.goodPrices(g) / countryCentralBank.basePrices(g);
                }
            }
            double targetPriceIndex = countryCentralBank.targetPriceIndex() * Math.pow(1.0 + CENTRAL_BANK_TARGET_INFLATION, 1.0 / DAYS_PER_YEAR);
            double realOutputTrend = countryCentralBank.realOutputTrend() > 0.0
                ? countryCentralBank.realOutputTrend() + (realOutput - countryCentralBank.realOutputTrend()) / DAYS_PER_YEAR : realOutput;
            double realGrowth = realOutputTrend > 0.0 ? realOutput / realOutputTrend - 1.0 : 0.0;
            double gap = priceIndex > 0.0 ? Math.log(targetPriceIndex / priceIndex) : 0.0;
            double annualRate = Math.clamp(CENTRAL_BANK_TARGET_INFLATION + realGrowth + CENTRAL_BANK_GAP_GAIN * gap, -CENTRAL_BANK_MAX_RATE, CENTRAL_BANK_MAX_RATE);

            double moneyStock = countryDemographics.savings() + Math.max(0.0, countryMarket.treasury()) + Math.max(0.0, countryBank.reserves()) + countryBank.interestPool();
            for (int o = 0; o < OWNER_COUNT; o++) {
                moneyStock += Math.max(0.0, countryInvestment.funds(o));
            }
            double amount = moneyStock * annualRate / DAYS_PER_YEAR;
            if (amount < 0.0) {
                amount = -Math.min(-amount, Math.max(0.0, countryBank.reserves()));
            }
            countryBank.reserves(countryBank.reserves() + amount);
            countryCentralBank.createdMoney(countryCentralBank.createdMoney() + amount);
            countryCentralBank.priceIndex((float) priceIndex);
            countryCentralBank.targetPriceIndex((float) targetPriceIndex);
            countryCentralBank.realOutputTrend(realOutputTrend);
        }
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
        countryCentralBank.targetPriceIndex(1f);
    }
}
