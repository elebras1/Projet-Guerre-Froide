package com.populaire.projetguerrefroide.system.military;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.ORGANISATION_ADJUSTMENT_DAYS;

public class MilitaryOrganisationSystem {

    private final EcsConstants ecsConstants;

    public MilitaryOrganisationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("MilitaryOrganisationSystem")
            .kind(phaseId)
            .with(UnitInstance.class)
            .with(AirWing.class).optional()
            .iter(this::adjust);
    }

    private void adjust(Iter iter) {
        World world = iter.stage();
        boolean airWings = iter.isSet(1);
        Field<UnitInstance> unitInstanceField = iter.field(UnitInstance.class, 0);
        Field<AirWing> airWingField = airWings ? iter.field(AirWing.class, 1) : null;

        EntityView globalFacilities = world.obtainEntityView(this.ecsConstants.globalMilitaryFacilities());
        int academyStaffIndex = globalFacilities.has(GlobalMilitaryFacilities.class)
            ? globalFacilities.<GlobalMilitaryFacilitiesView>getView(GlobalMilitaryFacilities.class).academyStaffPopTypeIndex() : -1;

        long countryId = 0;
        CountryMilitaryView countryMilitary = null;
        float trainedShare = 1f;
        for (int i = 0; i < iter.count(); i++) {
            UnitInstanceMutView unitInstance = unitInstanceField.getMutView(i);
            long unitCountryId = MilitaryUtils.countryId(world, unitInstance, airWings ? airWingField.getMutView(i) : null);
            if (unitCountryId != countryId) {
                countryId = unitCountryId;
                countryMilitary = world.obtainEntityView(countryId).getView(CountryMilitary.class);
                trainedShare = MilitaryUtils.trainedOrganisationShare(MilitaryUtils.trainingCoverage(countryMilitary, academyStaffIndex));
            }

            UnitView unit = world.obtainEntityView(unitInstance.typeId()).getView(Unit.class);
            float coverage = MilitaryUtils.baseCoverage(countryMilitary, unit.forceTypeTagId(), this.ecsConstants);
            float target = unit.defaultOrganisation() * Math.clamp(countryMilitary.supplySatisfaction(), 0f, 1f) * coverage * trainedShare;
            float organisation = unitInstance.organisation();
            unitInstance.organisation(organisation + (target - organisation) / ORGANISATION_ADJUSTMENT_DAYS);
        }
    }
}
