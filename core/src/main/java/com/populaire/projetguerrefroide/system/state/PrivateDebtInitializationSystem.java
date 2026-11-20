package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryBank;
import com.populaire.projetguerrefroide.component.CountryBankMutView;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class PrivateDebtInitializationSystem {

    public PrivateDebtInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PrivateDebtInitializationSystem")
            .kind(phaseId)
            .with(CountryBank.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryBankMutView countryBank = countryBankField.getMutView(i);
            for (int o = 0; o < OWNER_COUNT; o++) {
                if (o == STATE_OWNER) {
                    continue;
                }
                double loans = INITIAL_PRIVATE_DEBT_YEARS * DAYS_PER_YEAR * Math.max(0.0, countryBank.ownerIncome(o));
                countryBank.centralBankAdvances(Math.max(0.0, countryBank.centralBankAdvances() + loans - countryBank.loans(o)));
                countryBank.loans(o, loans);
                countryBank.debtServiceDue(o, loans * countryBank.lendingRate() / DAYS_PER_YEAR);
            }
        }
    }
}
