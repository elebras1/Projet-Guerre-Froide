package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.ConstructionUtils.finished;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerIndex;

public class ConstructionClosureSystem {

    private final EcsConstants ecsConstants;

    public ConstructionClosureSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ConstructionClosureSystem")
            .kind(phaseId)
            .with(ConstructionSite.class)
            .iter(this::close);
    }

    private void close(Iter iter) {
        Field<ConstructionSite> constructionSiteField = iter.field(ConstructionSite.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ConstructionSiteView site = constructionSiteField.getMutView(i);
            if (!finished(site)) {
                continue;
            }

            EntityView country = iter.world().obtainEntityView(site.countryId());
            CountryInvestmentMutView countryInvestment = country.getMutView(CountryInvestment.class);
            int owner = ownerIndex(site.ownerTagId(), this.ecsConstants);
            countryInvestment.committed(owner, Math.max(0.0, countryInvestment.committed(owner) - site.committedCost()));
            countryInvestment.sitesInProgress(countryInvestment.sitesInProgress() - 1);
            countryInvestment.sitesCompleted(countryInvestment.sitesCompleted() + 1);
            iter.world().obtainEntityView(iter.entity(i)).destruct();
        }
    }
}
