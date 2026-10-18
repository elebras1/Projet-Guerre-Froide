package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.STATE_OWNER;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerIndex;

public class ConstructionPurchaseSystem {

    private final EcsConstants ecsConstants;

    public ConstructionPurchaseSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ConstructionPurchaseSystem")
            .kind(phaseId)
            .with(ConstructionSite.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryInvestmentMutView countryInvestment = null;

        Field<ConstructionSite> constructionSiteField = iter.field(ConstructionSite.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ConstructionSiteMutView site = constructionSiteField.getMutView(i);

            if (site.countryId() != countryId) {
                countryId = site.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryInvestment = country.getMutView(CountryInvestment.class);
            }

            double paid = 0.0;
            for (int k = 0; k < site.goodIndexesLength(); k++) {
                int goodIndex = site.goodIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                float demanded = site.demandedGoods(k);
                float demand = countryMarket.goodDemandAmounts(goodIndex);
                if (demanded <= 0f || demand <= 0f) {
                    continue;
                }
                float delivered = demanded * countryMarket.goodConsumedAmounts(goodIndex) / demand;
                double cost = (double) delivered * countryMarket.goodPrices(goodIndex);
                countryMarket.goodRevenues(goodIndex, countryMarket.goodRevenues(goodIndex) + cost);
                site.goodsLeft(k, Math.max(0f, site.goodsLeft(k) - delivered));
                paid += cost;
            }

            if (site.daysLeft() > 0) {
                site.daysLeft(site.daysLeft() - 1);
            }

            int owner = ownerIndex(site.ownerTagId(), this.ecsConstants);
            if (owner == STATE_OWNER) {
                countryMarket.treasury(countryMarket.treasury() - paid);
            } else {
                countryInvestment.funds(owner, countryInvestment.funds(owner) - paid);
            }
            double released = Math.min(paid, site.committedCost());
            site.committedCost(site.committedCost() - released);
            countryInvestment.committed(owner, Math.max(0.0, countryInvestment.committed(owner) - released));
            countryInvestment.spentToday(owner, countryInvestment.spentToday(owner) + paid);
        }
    }
}
