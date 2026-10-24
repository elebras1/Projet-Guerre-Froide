package com.populaire.projetguerrefroide.system;

import com.github.tommyettinger.ds.LongFloatMap;
import com.github.tommyettinger.ds.LongList;
import com.github.tommyettinger.ds.LongObjectMap;
import com.github.tommyettinger.ds.ObjectList;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.MilitaryUtils;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;
import com.populaire.projetguerrefroide.util.StateBudgetUtils;

import java.util.Comparator;

import static com.populaire.projetguerrefroide.util.Constants.DAYS_PER_YEAR;
import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.MILITARY_INCOME_TYPE;

public class ArmyDisbandSystem {

    private record UnitCost(long unitId, double cost) {
    }

    private final Query unitQuery;

    public ArmyDisbandSystem(World ecsWorld, long phaseId) {
        this.unitQuery = ecsWorld.query()
            .with(UnitInstance.class)
            .with(AirWing.class).optional()
            .build();
        ecsWorld.system("ArmyDisbandSystem")
            .kind(phaseId)
            .with(CountryBudget.class)
            .run(this::disband);
    }

    private void disband(Iter iter) {
        World world = iter.world();
        boolean decisionDay = PopulationMobilityUtils.isMobilityDay(world);
        LongFloatMap gaps = new LongFloatMap();
        while (iter.next()) {
            if (!decisionDay) {
                continue;
            }
            Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryBudgetView countryBudget = countryBudgetField.getMutView(i);
                double gap = countryBudget.reservedNeeds() - countryBudget.revenueTrend();
                if (countryBudget.spendingRatio() < 1f && gap > 0.0) {
                    gaps.put(iter.entity(i), (float) gap);
                }
            }
        }
        if (gaps.isEmpty()) {
            return;
        }

        LongObjectMap<ObjectList<UnitCost>> unitsByCountry = new LongObjectMap<>();
        this.unitQuery.iter(unitIter -> {
            boolean airWings = unitIter.isSet(1);
            Field<UnitInstance> unitInstanceField = unitIter.field(UnitInstance.class, 0);
            Field<AirWing> airWingField = airWings ? unitIter.field(AirWing.class, 1) : null;
            for (int i = 0; i < unitIter.count(); i++) {
                UnitInstanceView unitInstance = unitInstanceField.getMutView(i);
                long countryId = MilitaryUtils.countryId(world, unitInstance, airWings ? airWingField.getMutView(i) : null);
                if (!gaps.containsKey(countryId)) {
                    continue;
                }
                ObjectList<UnitCost> units = unitsByCountry.get(countryId);
                if (units == null) {
                    units = new ObjectList<>();
                    unitsByCountry.put(countryId, units);
                }
                units.add(new UnitCost(unitIter.entity(i), dailyCost(world, countryId, unitInstance)));
            }
        });

        LongList disbanded = new LongList();
        for (LongObjectMap.Entry<ObjectList<UnitCost>> entry : unitsByCountry) {
            ObjectList<UnitCost> units = entry.value;
            units.sort(Comparator.comparingDouble((UnitCost unitCost) -> unitCost.cost()).reversed());
            double removed = 0.0;
            float gap = gaps.get(entry.key);
            for (int u = 0; u < units.size() && removed < gap; u++) {
                disbanded.add(units.get(u).unitId());
                removed += units.get(u).cost();
            }
        }
        for (int u = 0; u < disbanded.size(); u++) {
            world.obtainEntityView(disbanded.get(u)).destruct();
        }
    }

    private static double dailyCost(World world, long countryId, UnitInstanceView unitInstance) {
        EntityView country = world.obtainEntityView(countryId);
        CountryMarketView countryMarket = country.getView(CountryMarket.class);
        float fillRatio = MilitaryUtils.fillRatio(country.getView(CountryMilitary.class));
        UnitView unit = world.obtainEntityView(unitInstance.typeId()).getView(Unit.class);

        double cost = 0.0;
        float scale = unit.supplyConsumption() * unitInstance.strength() * fillRatio / DAYS_PER_YEAR;
        for (int k = 0; k < unit.supplyCostGoodIndexesLength(); k++) {
            int goodIndex = unit.supplyCostGoodIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            cost += (double) unit.supplyCostAmounts(k) * scale * countryMarket.goodBudgetPrices(goodIndex);
        }

        float wageSlider = StateBudgetUtils.wageSlider(MILITARY_INCOME_TYPE, country.getView(CountryBudgetPolicy.class), country.getView(CountryEducationPolicy.class));
        for (int k = 0; k < unit.crewPopTypeIndexesLength(); k++) {
            int popTypeIndex = unit.crewPopTypeIndexes(k);
            if (popTypeIndex < 0) {
                break;
            }
            cost += (double) unit.crewAmounts(k) * fillRatio / NEEDS_SCALING_FACTOR * StateBudgetUtils.wageRate(countryMarket, popTypeIndex, wageSlider);
        }
        return cost;
    }
}
