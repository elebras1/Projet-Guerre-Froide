package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.simulation.Constants.STATE_OWNER;

/**
 * The state's surplus deposited at the national bank (annex 13, decision 219, J4 of annex 8): after the day's decisions of
 * the state (budget, public works, investment), what the treasury keeps above its needs of the day, its target and its
 * commitments goes to the state's account at the bank, whose reserves the bank lends to the firms; {@link CountryBudgetSystem}
 * pays its interest and draws on it when the treasury falls below that floor.
 */
public class StateDepositSystem {

    public StateDepositSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StateDepositSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryBudget.class)
            .with(CountryBank.class)
            .with(CountryInvestment.class)
            .iter(this::deposit);
    }

    private void deposit(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 1);
        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 2);
        Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 3);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryBudgetView countryBudget = countryBudgetField.getMutView(i);
            CountryBankMutView countryBank = countryBankField.getMutView(i);
            CountryInvestmentView countryInvestment = countryInvestmentField.getMutView(i);
            double surplus = countryMarket.treasury() - countryBudget.reservedNeeds() - countryBudget.targetTreasury() - countryInvestment.committed(STATE_OWNER);
            if (surplus <= 0.0) {
                continue;
            }
            countryMarket.treasury(countryMarket.treasury() - surplus);
            countryBank.reserves(countryBank.reserves() + surplus);
            countryBank.stateDeposit(countryBank.stateDeposit() + surplus);
        }
    }
}
