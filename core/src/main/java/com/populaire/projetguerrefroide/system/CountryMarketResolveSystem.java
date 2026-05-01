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
            .iter(this::resolve);
    }

    private void resolve(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                boolean drawingOnStockpile = countryMarket.goodDrawingOnStockpiles(g);

                float domesticSupply = countryMarket.goodAmountsPool(g);
                float globalSupply = globalMarketData.goodAmountsPool(g);
                float stockSupply = drawingOnStockpile ? countryMarket.goodStockpiles(g) : 0f;
                float totalSupply = domesticSupply + globalSupply + stockSupply;

                float demand = countryMarket.goodDemandAmounts(g);
                float satisfaction;
                float priceAdjustment;

                if (totalSupply > 0) {
                    satisfaction = Math.min(1.0f, totalSupply / Math.max(0.001f, demand));
                    priceAdjustment = (demand - totalSupply) /Math.max(0.001f, totalSupply);
                } else {
                    satisfaction = 0.0f;
                    priceAdjustment = 1.0f;
                }

                countryMarket.goodDemandSatisfactionRatios(g, satisfaction);

                float remaining = demand * satisfaction;
                float consumedDomestic = Math.min(domesticSupply, remaining);
                remaining -= consumedDomestic;
                float consumedStock = Math.min(stockSupply, remaining);
                remaining -= consumedStock;
                float consumedGlobal = Math.min(globalSupply, remaining);

                countryMarket.goodAmountsPool(g, domesticSupply - consumedDomestic);
                countryMarket.goodStockpiles(g, stockSupply - consumedStock);
                globalMarketData.goodAmountsPool(g, globalSupply - consumedGlobal);

                float deficit = countryMarket.goodStockpileDailyDeficits(g);
                if (deficit > 0 && !drawingOnStockpile) {
                    float purchased = deficit * satisfaction * countryMarket.spendingRatio();
                    float price = countryMarket.goodPrices(g);
                    float cost = purchased * price;
                    countryMarket.goodStockpiles(g, countryMarket.goodStockpiles(g) + purchased);
                    countryMarket.treasury(countryMarket.treasury() - cost);
                }

                float inertia = 0.05f;
                float oldPrice = countryMarket.goodPrices(g);
                float baseFactor = 1.0f + priceAdjustment;
                float newPrice = oldPrice * (1.0f + inertia * (baseFactor - 1.0f));

                countryMarket.goodPrices(g, Math.max(0.001f, newPrice));
            }
        }
    }
}
