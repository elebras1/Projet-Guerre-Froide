package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record GlobalMilitaryFacilities(long navalBaseTypeId, float navalCapacityPerLevel, long airBaseTypeId, float airCapacityPerLevel,
                                       long academyTypeId, int academyStaffPopTypeIndex) {
}
