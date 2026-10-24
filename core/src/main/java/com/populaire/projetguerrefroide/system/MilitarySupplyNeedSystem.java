package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.MilitaryUtils;

import static com.populaire.projetguerrefroide.util.Constants.DAYS_PER_YEAR;

public class MilitarySupplyNeedSystem {

    public MilitarySupplyNeedSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("MilitarySupplyNeedSystem")
            .kind(phaseId)
            .with(UnitInstance.class)
            .with(AirWing.class).optional()
            .iter(this::accumulate);
    }

    private void accumulate(Iter iter) {
        World world = iter.world();
        boolean airWings = iter.isSet(1);
        Field<UnitInstance> unitInstanceField = iter.field(UnitInstance.class, 0);
        Field<AirWing> airWingField = airWings ? iter.field(AirWing.class, 1) : null;

        long countryId = 0;
        CountryMilitaryMutView countryMilitary = null;
        float fillRatio = 1f;
        for (int i = 0; i < iter.count(); i++) {
            UnitInstanceView unitInstance = unitInstanceField.getMutView(i);
            long unitCountryId = MilitaryUtils.countryId(world, unitInstance, airWings ? airWingField.getMutView(i) : null);
            if (unitCountryId != countryId) {
                countryId = unitCountryId;
                countryMilitary = world.obtainEntityView(countryId).getMutView(CountryMilitary.class);
                fillRatio = MilitaryUtils.fillRatio(countryMilitary);
            }

            UnitView unit = world.obtainEntityView(unitInstance.typeId()).getView(Unit.class);
            float scale = unit.supplyConsumption() * unitInstance.strength() * fillRatio;
            for (int k = 0; k < unit.supplyCostGoodIndexesLength(); k++) {
                int goodIndex = unit.supplyCostGoodIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                countryMilitary.supplyNeeds(goodIndex, countryMilitary.supplyNeeds(goodIndex) + unit.supplyCostAmounts(k) * scale / DAYS_PER_YEAR);
            }
        }
    }
}
