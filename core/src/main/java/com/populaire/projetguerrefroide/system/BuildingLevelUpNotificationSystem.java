package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.command.CommandBus;
import com.populaire.projetguerrefroide.command.request.BuildingLevelUpCommand;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class BuildingLevelUpNotificationSystem {

    private final CommandBus commandBus;
    private final long leveledUpTag;

    public BuildingLevelUpNotificationSystem(World ecsWorld, EcsConstants ecsConstants, CommandBus commandBus) {
        this.commandBus = commandBus;
        this.leveledUpTag = ecsConstants.leveledUp();
        ecsWorld.system("BuildingLevelUpNotificationSystem")
            .kind(Flecs.OnUpdate)
            .with(this.leveledUpTag)
            .iter(this::notify);
    }

    private void notify(Iter iter) {
        for (int i = 0; i < iter.count(); i++) {
            long buildingId = iter.entity(i);
            this.commandBus.dispatch(new BuildingLevelUpCommand(buildingId));
            iter.world().obtainEntityView(buildingId).remove(this.leveledUpTag);
        }
    }
}
