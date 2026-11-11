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
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ProvinceView;
import com.populaire.projetguerrefroide.component.ResourceGathering;
import com.populaire.projetguerrefroide.component.ResourceGatheringView;
import com.populaire.projetguerrefroide.component.Technology;
import com.populaire.projetguerrefroide.component.TechnologyView;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.loader.WorldLoader;
import com.populaire.projetguerrefroide.system.EconomyPipelines;
import com.populaire.projetguerrefroide.system.technology.TechnologyEffectUtils;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Query;
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

        assertEquals(0.10f, usaProduction.factoryProductivities(typeIndex(ecsWorld, "refinery")), 1e-5f);
        assertEquals(0.10f, usaProduction.rgoProductivities(oil), 1e-5f);
        int grain = goodIndex(ecsWorld, simulationContext, "grain");
        assertEquals(1, usaProduction.mechanizationCaps(grain));

        long usaId = ecsWorld.lookup("USA");
        long rajId = ecsWorld.lookup("RAJ");
        int[] usaGrain = new int[3];
        int[] rajGrain = new int[1];
        Query rgoQuery = ecsWorld.query().with(Province.class).with(ResourceGathering.class).build();
        rgoQuery.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 1);
            for (int i = 0; i < iter.count(); i++) {
                ProvinceView province = provinceField.getMutView(i);
                ResourceGatheringView rgo = rgoField.getMutView(i);
                if (rgo.goodIndex() != grain) {
                    continue;
                }
                if (province.ownerId() == usaId) {
                    usaGrain[0]++;
                    usaGrain[1] += rgo.mechanization() == 1 && Math.abs(rgo.efficiency() - 1.25f) < 1e-5f ? 1 : 0;
                    usaGrain[2] += rgo.production() <= 0f || rgo.upkeepValue() > 0f ? 1 : 0;
                } else if (province.ownerId() == rajId) {
                    rajGrain[0] += rgo.mechanization();
                }
            }
        });
        rgoQuery.destruct();
        assertTrue(usaGrain[0] > 0);
        assertEquals(usaGrain[0], usaGrain[1]);
        assertEquals(usaGrain[0], usaGrain[2]);
        assertEquals(0, rajGrain[0]);

        int deepDrilling = ecsWorld.obtainEntityView(ecsWorld.lookup("deep_drilling")).<TechnologyView>getView(Technology.class).index();
        TechnologyEffectUtils.applyTechnology(ecsConstants, usa, deepDrilling);
        assertEquals(0.15f, usa.<CountryProductionTechnologyView>getView(CountryProductionTechnology.class).rgoProductivities(oil), 1e-5f);
        assertEquals(2, usa.<CountryProductionTechnologyView>getView(CountryProductionTechnology.class).mechanizationCaps(oil));
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
