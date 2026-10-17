package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.readPopulationTypes;

public class CountryBankSystem {

    public CountryBankSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryBankSystem")
            .kind(phaseId)
            .with(CountryBank.class)
            .with(CountryDemographics.class)
            .multiThreaded()
            .iter(this::spend);
    }

    private void spend(Iter iter) {
        int[] incomeTypes = new int[POP_TYPE_COUNT];
        boolean[] assistedTypes = new boolean[POP_TYPE_COUNT];
        readPopulationTypes(iter.world(), incomeTypes, assistedTypes);

        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 0);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryBankMutView countryBank = countryBankField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);

            double assistedUnits = 0.0;
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                if (assistedTypes[p]) {
                    assistedUnits += countryDemographics.totalByPopType(p) / NEEDS_SCALING_FACTOR;
                }
            }

            double deposits = Math.max(0.0, countryBank.deposits());
            double transfers = assistedUnits > 0.0 ? Math.min(deposits, Math.max(0.0, BANK_MINIMUM_SPENDING_RATE * (deposits + countryBank.loansToday()) - countryBank.loansToday())) : 0.0;
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                countryBank.transferRatesByPopType(p, assistedTypes[p] && transfers > 0.0 ? (float) (transfers / assistedUnits) : 0f);
            }
            countryBank.transfersBudget((float) transfers);
        }
    }
}
