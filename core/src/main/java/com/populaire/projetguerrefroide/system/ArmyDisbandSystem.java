package com.populaire.projetguerrefroide.system;

import com.github.tommyettinger.ds.LongFloatMap;
import com.github.tommyettinger.ds.LongList;
import com.github.tommyettinger.ds.ObjectList;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.callback.IterCallback;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.util.MilitaryUtils;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;
import com.populaire.projetguerrefroide.util.StateBudgetUtils;
import com.populaire.projetguerrefroide.util.collection.LongObjectSlotMap;
import com.populaire.projetguerrefroide.util.collection.ObjectPool;

import java.util.Comparator;

import static com.populaire.projetguerrefroide.simulation.Constants.DAYS_PER_YEAR;
import static com.populaire.projetguerrefroide.simulation.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.MILITARY_INCOME_TYPE;

public class ArmyDisbandSystem {

    private static final Comparator<UnitCost> UNIT_COST_ORDER = Comparator.comparingDouble((UnitCost unitCost) -> unitCost.cost()).reversed();

    private static final class UnitCost {
        private long unitId;
        private double cost;

        private UnitCost set(long unitId, double cost) {
            this.unitId = unitId;
            this.cost = cost;
            return this;
        }

        private long unitId() {
            return this.unitId;
        }

        private double cost() {
            return this.cost;
        }
    }

    private final EcsConstants ecsConstants;
    private final IterCallback readUnitReader = this::readUnit;
    private World world;
    private final LongFloatMap gaps = new LongFloatMap();
    private final LongList disbanded = new LongList();
    private final ObjectPool<UnitCost> unitCostPool = new ObjectPool<>(UnitCost::new, unitCost -> {
    });
    private final ObjectPool<ObjectList<UnitCost>> unitListPool = new ObjectPool<>(ObjectList::new, ObjectList::clear);
    private LongObjectSlotMap<ObjectList<UnitCost>> unitsByCountry;
    private final Query unitQuery;

    public ArmyDisbandSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
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
        boolean decisionDay = PopulationMobilityUtils.isMobilityDay(world, this.ecsConstants);
        LongFloatMap gaps = this.gaps;
        gaps.clear();
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

        this.unitCostPool.releaseAll();
        this.unitListPool.releaseAll();
        LongObjectSlotMap<ObjectList<UnitCost>> unitsByCountry = new LongObjectSlotMap<>();
        this.unitsByCountry = unitsByCountry;
        this.world = world;
        this.unitQuery.iter(this.readUnitReader);

        LongList disbanded = this.disbanded;
        disbanded.clear();
        for (int slot = 0; slot < unitsByCountry.slotCount(); slot++) {
            long countryId = unitsByCountry.slotKey(slot);
            if (countryId == 0) {
                continue;
            }
            ObjectList<UnitCost> units = unitsByCountry.slotValue(slot);
            units.sort(UNIT_COST_ORDER);
            double removed = 0.0;
            float gap = gaps.get(countryId);
            for (int u = 0; u < units.size() && removed < gap; u++) {
                disbanded.add(units.get(u).unitId());
                removed += units.get(u).cost();
            }
        }
        for (int u = 0; u < disbanded.size(); u++) {
            world.obtainEntityView(disbanded.get(u)).destruct();
        }
    }

    private static double dailyCost(World world, EcsConstants ecsConstants, long countryId, UnitInstanceView unitInstance) {
        EntityView country = world.obtainEntityView(countryId);
        CountryMarketView countryMarket = country.getView(CountryMarket.class);
        CountryMilitaryView countryMilitary = country.getView(CountryMilitary.class);
        float fillRatio = MilitaryUtils.fillRatio(countryMilitary);
        UnitView unit = world.obtainEntityView(unitInstance.typeId()).getView(Unit.class);

        double cost = 0.0;
        float coverage = MilitaryUtils.baseCoverage(countryMilitary, unit.forceTypeTagId(), ecsConstants);
        float scale = unit.supplyConsumption() * unitInstance.strength() * fillRatio * coverage / DAYS_PER_YEAR;
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

    private void readUnit(Iter unitIter) {
        World world = this.world;
        LongFloatMap gaps = this.gaps;
        LongObjectSlotMap<ObjectList<UnitCost>> unitsByCountry = this.unitsByCountry;
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
                units = this.unitListPool.obtain();
                unitsByCountry.put(countryId, units);
            }
            units.add(this.unitCostPool.obtain().set(unitIter.entity(i), dailyCost(world, this.ecsConstants, countryId, unitInstance)));
        }
    }
}
