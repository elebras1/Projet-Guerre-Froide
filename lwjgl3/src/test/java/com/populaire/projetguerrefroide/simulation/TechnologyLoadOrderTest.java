package com.populaire.projetguerrefroide.simulation;

import com.populaire.projetguerrefroide.TestGdxFiles;
import com.populaire.projetguerrefroide.component.CountryEducationPolicy;
import com.populaire.projetguerrefroide.component.CountryEducationPolicyView;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.loader.WorldLoader;
import com.populaire.projetguerrefroide.system.EconomyPipelines;
import io.github.elebras1.flecs.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TechnologyLoadOrderTest {

    @Test
    public void technologiesOfTheStockApplyWhenSystemsAreBuiltBeforeLoadingLikeInTheGame() {
        TestGdxFiles.install();
        World ecsWorld = new World();
        ComponentRegistry.register(ecsWorld);
        SimulationContext simulationContext = new SimulationContext(ecsWorld);
        EconomyPipelines economyPipelines = new EconomyPipelines(simulationContext);
        new WorldLoader(new WorldDaoImpl()).createWorld(simulationContext);
        economyPipelines.initialize();

        assertEquals(169, simulationContext.getEcsConstants().technologyCount());
        CountryEducationPolicyView usa = ecsWorld.obtainEntityView(ecsWorld.lookup("USA")).getView(CountryEducationPolicy.class);
        assertEquals(0.06f, usa.researchEfficiency(), 1e-5f);
        ecsWorld.close();
    }
}
