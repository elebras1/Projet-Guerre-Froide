package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.STATE_OWNER;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerIndex;

public class ConstructionDemandSystem {

    private final EcsConstants ecsConstants;

    public ConstructionDemandSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ConstructionDemandSystem")
            .kind(phaseId)
            .with(ConstructionSite.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryInvestmentMutView countryInvestment = null;
        CountryBudgetView countryBudget = null;

        Field<ConstructionSite> constructionSiteField = iter.field(ConstructionSite.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ConstructionSiteMutView site = constructionSiteField.getMutView(i);

            if (site.countryId() != countryId) {
                countryId = site.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryInvestment = country.getMutView(CountryInvestment.class);
                countryBudget = country.getMutView(CountryBudget.class);
            }

            int owner = ownerIndex(site.ownerTagId(), this.ecsConstants);
            double cost = 0.0;
            for (int k = 0; k < site.goodIndexesLength(); k++) {
                int goodIndex = site.goodIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                cost += (double) Math.min(site.goodsLeft(k), site.dailyGoods(k)) * countryMarket.goodBudgetPrices(goodIndex);
            }

            double payerBalance = owner == STATE_OWNER ? countryMarket.treasury() - countryBudget.reservedNeeds() : countryInvestment.funds(owner);
            double available = Math.max(0.0, payerBalance - countryInvestment.plannedToday(owner));
            float ratio = cost > available ? (float) (available / cost) : 1f;
            countryInvestment.plannedToday(owner, countryInvestment.plannedToday(owner) + cost * ratio);

            for (int k = 0; k < site.goodIndexesLength(); k++) {
                int goodIndex = site.goodIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                float demanded = Math.min(site.goodsLeft(k), site.dailyGoods(k)) * ratio;
                site.demandedGoods(k, demanded);
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + demanded);
            }
        }
    }
}
