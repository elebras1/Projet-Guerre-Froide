package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryMarketResolveSystem {

    public CountryMarketResolveSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryMarketResolveSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryEffectPolicy.class)
            .iter(this::resolve);
    }

    private void resolve(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryEffectPolicy> countryEffectPolicyField = iter.field(CountryEffectPolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            CountryEffectPolicyView countryEffectPolicy = countryEffectPolicyField.getMutView(i);

            float tariffRate = countryEffectPolicy.tariffRate();

            for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                boolean drawingOnStockpile = countryMarket.goodDrawingOnStockpiles(g);

                float domesticSupply = countryMarket.goodAmountsPool(g);
                float globalSupply = globalMarketData.goodAmountsPool(g);
                float stockSupply = drawingOnStockpile ? countryMarket.goodStockpiles(g) : 0f;
                float totalSupply = domesticSupply + globalSupply + stockSupply;

                float demand = countryMarket.goodDemandAmounts(g);
                float satisfaction = Math.min(1.0f, totalSupply / (demand + 0.001f));
                float oldSatisfaction = countryMarket.goodDemandSatisfactionRatios(g);
                float lissedSatisfaction = oldSatisfaction * 0.95f + satisfaction * 0.05f;
                countryMarket.goodDemandSatisfactionRatios(g, lissedSatisfaction);

                float basePrice = globalMarketData.goodPrices(g);

                float domesticAvailable = domesticSupply + stockSupply;
                float domesticFraction = Math.min(1.0f, domesticAvailable / (demand + 0.001f));

                float effectivePrice;
                if (tariffRate < 0) {
                    float globalFraction = Math.min(1.0f, globalSupply / (demand + 0.001f));
                    effectivePrice = basePrice * (globalFraction + (1 - globalFraction) * (1 + tariffRate));
                } else {
                    effectivePrice = basePrice * (domesticFraction + (1 - domesticFraction) * (1 + tariffRate));
                }
                countryMarket.goodPrices(g, Math.max(0.001f, effectivePrice));

                float remaining = demand * satisfaction;
                float consumedDomestic, consumedGlobal, consumedStock;

                if (tariffRate >= 0) {
                    consumedDomestic = Math.min(domesticSupply, remaining);
                    remaining -= consumedDomestic;
                    consumedStock = Math.min(stockSupply, remaining);
                    remaining -= consumedStock;
                    consumedGlobal = Math.min(globalSupply, remaining);
                } else {
                    consumedGlobal = Math.min(globalSupply, remaining);
                    remaining -= consumedGlobal;
                    consumedDomestic = Math.min(domesticSupply, remaining);
                    remaining -= consumedDomestic;
                    consumedStock = Math.min(stockSupply, remaining);
                }

                countryMarket.goodAmountsPool(g, domesticSupply - consumedDomestic);
                countryMarket.goodStockpiles(g, stockSupply - consumedStock);
                globalMarketData.goodAmountsPool(g, globalSupply - consumedGlobal);

                float deficit = countryMarket.goodStockpileDailyDeficits(g);
                if (deficit > 0 && !drawingOnStockpile) {
                    float purchased = deficit * satisfaction * countryMarket.spendingRatio();
                    float cost = purchased * countryMarket.goodPrices(g);
                    countryMarket.goodStockpiles(g, countryMarket.goodStockpiles(g) + purchased);
                    countryMarket.treasury(countryMarket.treasury() - cost);
                }
            }
        }
    }
}
