package com.populaire.projetguerrefroide.system.technology;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PolicyModifierTest {

    @Test
    public void technologyModifierNamesResolveOrFail() {
        assertEquals(PolicyModifier.HEALTH, PolicyModifier.forTechnology("public_health"));
        assertEquals(PolicyModifier.RESEARCH_EFFICIENCY_RATE, PolicyModifier.forTechnology("research_efficiency"));
        assertNull(PolicyModifier.forTechnology("air_capacity"));
        assertNull(PolicyModifier.forTechnology("espionage_bonus"));
        assertThrows(IllegalArgumentException.class, () -> PolicyModifier.forTechnology("public_helth"));
    }
}
