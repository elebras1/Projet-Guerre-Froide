package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class EconomyCalendarSystem {

    public EconomyCalendarSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyCalendarSystem")
            .kind(phaseId)
            .with(EconomyCalendar.class)
            .iter(this::advance);
    }

    private void advance(Iter iter) {
        Field<EconomyCalendar> calendarField = iter.field(EconomyCalendar.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            EconomyCalendarMutView calendar = calendarField.getMutView(i);
            calendar.day(calendar.day() + 1);
        }
    }
}
