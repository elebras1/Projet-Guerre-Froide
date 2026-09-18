package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryMarketResolveSystem {

    public CountryMarketResolveSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryMarketResolveSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryTradePolicy.class)
            .iter(this::resolve);
    }

    private void resolve(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            CountryTradePolicyView countryTradePolicy = countryTradePolicyField.getMutView(i);

            float tariffRate = countryTradePolicy.tariffRate();

            for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                boolean drawingOnStockpile = countryMarket.goodDrawingOnStockpiles(g);

                float domesticSupply = countryMarket.goodAmountsPool(g);
                float stockSupply = drawingOnStockpile ? countryMarket.goodStockpiles(g) : 0f;
                float domesticAvailable = domesticSupply + stockSupply;

                float demand = countryMarket.goodDemandAmounts(g);
                float imported = countryMarket.goodImportNeeds(g) * globalMarketData.goodTradeRatios(g);

                float remaining = demand;
                float consumedDomestic = Math.min(domesticSupply, remaining);
                remaining -= consumedDomestic;
                float consumedStock = Math.min(stockSupply, remaining);
                remaining -= consumedStock;
                float consumedImport = Math.min(imported, remaining);

                float satisfaction = Math.min(1.0f, (consumedDomestic + consumedStock + consumedImport) / (demand + 0.001f));
                float oldSatisfaction = countryMarket.goodDemandSatisfactionRatios(g);
                countryMarket.goodDemandSatisfactionRatios(g, oldSatisfaction * 0.95f + satisfaction * 0.05f);

                float basePrice = globalMarketData.goodPrices(g);
                float importedFraction = Math.min(1.0f, consumedImport / (demand + 0.001f));
                float referencePrice = basePrice * (1f + tariffRate * importedFraction);
                float scarcity = Math.max(0f, (demand - consumedDomestic - consumedStock - consumedImport) / (demand + 0.001f));
                float targetPrice = Math.max(0.001f, referencePrice * (1f + scarcity));
                float localPrice = countryMarket.goodPrices(g) * 0.95f + targetPrice * 0.05f;
                countryMarket.goodPrices(g, Math.max(0.001f, localPrice));

                countryMarket.goodAmountsPool(g, domesticSupply - consumedDomestic);
                countryMarket.goodStockpiles(g, stockSupply - consumedStock);
                countryMarket.goodImportedAmounts(g, consumedImport);
                globalMarketData.goodAmountsPool(g, globalMarketData.goodAmountsPool(g) - consumedImport);

                float deficit = countryMarket.goodStockpileDailyDeficits(g);
                if (deficit > 0 && !drawingOnStockpile) {
                    float purchased = deficit * satisfaction * countryMarket.spendingRatio();
                    float cost = purchased * countryMarket.goodPrices(g);
                    countryMarket.goodStockpiles(g, countryMarket.goodStockpiles(g) + purchased);
                    countryMarket.treasury(countryMarket.treasury() - cost);
                    countryMarket.salesRevenue(countryMarket.salesRevenue() + cost);
                }
            }
        }
    }
}
