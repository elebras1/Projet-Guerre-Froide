package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;
import com.populaire.projetguerrefroide.component.WorldMarket;
import com.populaire.projetguerrefroide.component.WorldMarketView;

public class CountryMarketResolveSystem {

    public CountryMarketResolveSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryMarketResolveSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::resolve);
    }

    private void resolve(Iter iter) {
        EntityView worldMarket = iter.world().obtainEntityView(iter.world().lookup("world_market"));
        WorldMarketView worldMarketData = worldMarket.getMutView(WorldMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            for (int g = 0; g < countryMarket.effectiveGoodPricesLength(); g++) {
                float domesticSupply = countryMarket.domesticMarketPool(g);
                float globalSupply = worldMarketData.globalMarketPool(g);
                float stockSupply = countryMarket.stockpiles(g);
                float totalSupply = domesticSupply + globalSupply + stockSupply;

                float demand = countryMarket.realDemand(g);
                float satisfaction;
                float priceAdjustment;

                if (totalSupply > 0) {
                    satisfaction = Math.min(1.0f, totalSupply / Math.max(0.001f, demand));
                    priceAdjustment = (demand - totalSupply) /Math.max(0.001f, totalSupply);
                } else {
                    satisfaction = 0.0f;
                    priceAdjustment = 1.0f;
                }

                countryMarket.demandSatisfaction(g, satisfaction);

                float inertia = 0.05f;
                float oldPrice = countryMarket.effectiveGoodPrices(g);
                float baseFactor = 1.0f + priceAdjustment;
                float newPrice = oldPrice * (1.0f + inertia * (baseFactor - 1.0f));

                countryMarket.effectiveGoodPrices(g, Math.max(0.001f, newPrice));
            }
        }
    }
}
