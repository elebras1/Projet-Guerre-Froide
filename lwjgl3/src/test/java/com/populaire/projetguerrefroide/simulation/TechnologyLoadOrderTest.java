package com.populaire.projetguerrefroide.simulation;

import com.populaire.projetguerrefroide.TestGdxFiles;
import com.populaire.projetguerrefroide.component.CountryEducationPolicy;
import com.populaire.projetguerrefroide.component.CountryEducationPolicyView;
import com.populaire.projetguerrefroide.component.CountryProductionTechnology;
import com.populaire.projetguerrefroide.component.CountryProductionTechnologyView;
import com.populaire.projetguerrefroide.component.EconomyBuildingType;
import com.populaire.projetguerrefroide.component.EconomyBuildingTypeView;
import com.populaire.projetguerrefroide.component.GlobalGood;
import com.populaire.projetguerrefroide.component.GlobalGoodView;
import com.populaire.projetguerrefroide.component.Technology;
import com.populaire.projetguerrefroide.component.TechnologyView;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.loader.WorldLoader;
import com.populaire.projetguerrefroide.system.EconomyPipelines;
import com.populaire.projetguerrefroide.system.technology.TechnologyEffectUtils;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        EcsConstants ecsConstants = simulationContext.getEcsConstants();

        assertEquals(169, ecsConstants.technologyCount());
        EntityView usa = ecsWorld.obtainEntityView(ecsWorld.lookup("USA"));
        CountryEducationPolicyView usaEducation = usa.getView(CountryEducationPolicy.class);
        assertEquals(0.06f, usaEducation.researchEfficiency(), 1e-5f);

        CountryProductionTechnologyView usaProduction = usa.getView(CountryProductionTechnology.class);
        assertEquals(0.10f, usaProduction.factoryProductivities(typeIndex(ecsWorld, "inorganic_chemicals_factory")), 1e-5f);
        assertTrue(usaProduction.advancedRecipes(typeIndex(ecsWorld, "aluminium_factory")));
        int oil = goodIndex(ecsWorld, simulationContext, "oil");
        assertEquals(0f, usaProduction.rgoProductivities(oil), 1e-6f);

        int rotaryDrilling = ecsWorld.obtainEntityView(ecsWorld.lookup("rotary_drilling")).<TechnologyView>getView(Technology.class).index();
        TechnologyEffectUtils.applyTechnology(ecsConstants, usa, rotaryDrilling);
        assertEquals(0.10f, usa.<CountryProductionTechnologyView>getView(CountryProductionTechnology.class).rgoProductivities(oil), 1e-5f);
        assertEquals(0.06f, usa.<CountryEducationPolicyView>getView(CountryEducationPolicy.class).researchEfficiency(), 1e-5f);
        ecsWorld.close();
    }

    private static int typeIndex(World ecsWorld, String buildingType) {
        return ecsWorld.obtainEntityView(ecsWorld.lookup(buildingType)).<EconomyBuildingTypeView>getView(EconomyBuildingType.class).typeIndex();
    }

    private static int goodIndex(World ecsWorld, SimulationContext simulationContext, String good) {
        long goodId = ecsWorld.lookup(good);
        GlobalGoodView goods = ecsWorld.obtainEntityView(simulationContext.getEcsConstants().globalGood()).getView(GlobalGood.class);
        for (int g = 0; g < Constants.GOOD_COUNT; g++) {
            if (goods.goodIds(g) == goodId) {
                return g;
            }
        }
        throw new IllegalArgumentException(good);
    }
}
