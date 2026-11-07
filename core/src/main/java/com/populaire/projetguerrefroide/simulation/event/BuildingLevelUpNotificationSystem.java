package com.populaire.projetguerrefroide.simulation.event;

import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.protocol.event.EventKind;
import com.populaire.projetguerrefroide.protocol.event.EventRing;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

public class BuildingLevelUpNotificationSystem {

    private final EventRing events;
    private final long leveledUpTag;

    public BuildingLevelUpNotificationSystem(World ecsWorld, EcsConstants ecsConstants, EventRing events) {
        this.events = events;
        this.leveledUpTag = ecsConstants.leveledUp();
        ecsWorld.system("BuildingLevelUpNotificationSystem")
            .kind(Flecs.OnUpdate)
            .with(this.leveledUpTag)
            .iter(this::notify);
    }

    private void notify(Iter iter) {
        for (int i = 0; i < iter.count(); i++) {
            long buildingId = iter.entity(i);
            this.events.publish(EventKind.BUILDING_LEVEL_UP, buildingId, 0L, 0.0);
            iter.world().obtainEntityView(buildingId).remove(this.leveledUpTag);
        }
    }
}
