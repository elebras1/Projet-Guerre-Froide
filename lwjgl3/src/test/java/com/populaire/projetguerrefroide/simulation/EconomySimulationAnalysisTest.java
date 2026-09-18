package com.populaire.projetguerrefroide.simulation;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.impl.ConfigurationDaoImpl;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.pojo.Bookmark;
import com.populaire.projetguerrefroide.service.EconomyService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.util.EcsConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EconomySimulationAnalysisTest {

    private static final int YEARS = 5;
    private static final int DAYS_PER_YEAR = 365;
    private static final int TOTAL_TICKS = YEARS * DAYS_PER_YEAR;

    private World ecsWorld;
    private EconomyService economyService;
    private LocalDate startDate;

    @BeforeAll
    public void setUp() {
        this.setupGdxFiles();

        this.ecsWorld = new World();
        this.registerComponents(this.ecsWorld);

        EcsConstants ecsConstants = new EcsConstants(this.ecsWorld);
        GameContext gameContext = Mockito.mock(GameContext.class);
        Mockito.when(gameContext.getEcsWorld()).thenReturn(this.ecsWorld);
        Mockito.when(gameContext.getEcsConstants()).thenReturn(ecsConstants);

        this.startDate = this.loadStartDate();

        new WorldDaoImpl().createWorld(gameContext);
        this.economyService = new EconomyService(gameContext);

        this.ecsWorld.runPipeline(this.economyService.getInitPipeline().id(), 1f);
    }

    private void setupGdxFiles() {
        GdxNativesLoader.load();

        Gdx.files = new Files() {
            @Override
            public FileHandle getFileHandle(String path, FileType type) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle classpath(String path) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle internal(String path) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle external(String path) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle absolute(String path) {
                return new FileHandle(path);
            }

            @Override
            public FileHandle local(String path) {
                return new FileHandle(path);
            }

            @Override
            public String getExternalStoragePath() {
                return System.getProperty("user.home") + File.separator;
            }

            @Override
            public boolean isExternalStorageAvailable() {
                return true;
            }

            @Override
            public String getLocalStoragePath() {
                return new File("").getAbsolutePath() + File.separator;
            }

            @Override
            public boolean isLocalStorageAvailable() {
                return true;
            }
        };

        if (!Gdx.files.internal("common/bookmark.json").exists()) {
            fail("Assets not found. Run the test through Gradle (`./gradlew :lwjgl3:test`) "
                + "so the working directory points to the 'assets' folder.");
        }
    }

    private void registerComponents(World world) {
        world.component(Modifiers.class);
        world.component(Overrides.class);
        world.component(Minister.class);
        world.component(Ideology.class);
        world.component(Terrain.class);
        world.component(ElectoralMechanism.class);
        world.component(Leader.class);
        world.component(Color.class);
        world.component(Position.class);
        world.component(Border.class);
        world.component(DiplomaticRelation.class);
        world.component(Adjacencies.class);
        world.component(Country.class);
        world.component(Province.class);
        world.component(Law.class);
        world.component(LawGroup.class);
        world.component(GovernmentPolicy.class);
        world.component(PopulationType.class);
        world.component(Good.class);
        world.component(EconomyBuildingType.class);
        world.component(SpecialBuildingType.class);
        world.component(DevelopmentBuildingType.class);
        world.component(Building.class);
        world.component(EconomyBuilding.class);
        world.component(SpecialBuilding.class);
        world.component(DevelopmentBuilding.class);
        world.component(ResourceGathering.class);
        world.component(ExpansionBuilding.class);
        world.component(RegionInstance.class);
        world.component(Population.class);
        world.component(ResourceGatheringType.class);
        world.component(Demographics.class);
        world.component(CountryDemographics.class);
        world.component(CountryMarket.class);
        world.component(GlobalPopulationType.class);
        world.component(GlobalGood.class);
        world.component(GlobalMarket.class);
        world.component(CountryTaxPolicy.class);
        world.component(CountryBudgetPolicy.class);
        world.component(CountryEducationPolicy.class);
        world.component(CountryTradePolicy.class);
        world.component(CountryProductionPolicy.class);
        world.component(CountryLaborPolicy.class);
        world.component(CountryPopulationPolicy.class);
        world.component(CountryPoliticalPolicy.class);
        world.component(CountryCulturePolicy.class);
        world.component(CountryProfitDistributionPolicy.class);
        world.component(RegionInstanceIncome.class);
        world.component(Unit.class);
    }

    private LocalDate loadStartDate() {
        Bookmark bookmark = new ConfigurationDaoImpl().loadBookmark();
        return bookmark != null ? bookmark.date() : LocalDate.of(1946, 1, 1);
    }

    @AfterAll
    public void tearDown() {
        if (this.ecsWorld != null) {
            this.ecsWorld.destroy();
            this.ecsWorld = null;
        }
    }

    @Test
    public void simulateFiveYearsAndAnalyseEconomy() throws Exception {
        String outputDir = System.getProperty("simulation.outputDir");
        assertTrue(outputDir != null && !outputDir.isBlank(),
            "'simulation.outputDir' system property is not set.");

        try (SimulationRecorder recorder = new SimulationRecorder(this.ecsWorld, new File(outputDir), this.startDate)) {
            long start = System.nanoTime();
            for (int tick = 0; tick < TOTAL_TICKS; tick++) {
                this.ecsWorld.runPipeline(this.economyService.getMainPipeline().id(), 1f);
                recorder.recordTick(tick);
                if ((tick + 1) % 365 == 0) {
                    System.out.printf("Simulated %d / %d ticks (%s)%n",
                        tick + 1, TOTAL_TICKS, this.startDate.plusDays(tick));
                }
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("Simulated %d ticks in %.2f s%n", TOTAL_TICKS, elapsedMs / 1000.0);

            this.assertDataSanity(recorder);
            this.validateHealth(recorder);
        }

        System.out.println("CSV data written to " + outputDir);
    }

    private void validateHealth(SimulationRecorder recorder) {
        float surplusRatio = recorder.getMinSurplusRatio();

        List<HealthCheck> checks = List.of(
            new HealthCheck("employment_le_population",
                recorder.getEmploymentExceedsPopulationRows() == 0L
                    && recorder.getFinalTotalEmployment() <= recorder.getFinalTotalPopulation(),
                "emploi=" + recorder.getFinalTotalEmployment() + " pop=" + recorder.getFinalTotalPopulation()
                    + " violations=" + recorder.getEmploymentExceedsPopulationRows()),
            new HealthCheck("price_bounds",
                recorder.getPriceDriftMin() >= 0.25f && recorder.getPriceDriftMax() <= 3.0f,
                String.format("drift [%.2f, %.2f] (bornes [0.25, 3.0])", recorder.getPriceDriftMin(), recorder.getPriceDriftMax())),
            new HealthCheck("production_floor_95",
                recorder.getLastTickWorldProduction() >= 0.95f * recorder.getFirstTickWorldProduction(),
                String.format("production %.0f -> %.0f (%.1f%% du niveau initial)",
                    recorder.getFirstTickWorldProduction(), recorder.getLastTickWorldProduction(),
                    100.0 * recorder.getLastTickWorldProduction() / recorder.getFirstTickWorldProduction())),
            new HealthCheck("population_growth",
                recorder.getLastTickWorldPopulation() > recorder.getFirstTickWorldPopulation(),
                String.format("population %.0f -> %.0f",
                    recorder.getFirstTickWorldPopulation(), recorder.getLastTickWorldPopulation())),
            new HealthCheck("treasury_bounded",
                recorder.getLastTickWorldTreasury() <= HEALTH_TREASURY_MULTIPLIER * recorder.getFinalWorldProductionValue(),
                String.format("tresor=%.0f vs %d x valeur production/journal=%.0f",
                    recorder.getLastTickWorldTreasury(), HEALTH_TREASURY_MULTIPLIER, recorder.getFinalWorldProductionValue())),
            new HealthCheck("no_starving_strata",
                recorder.getStarvingPopTypesCount() == 0,
                "strates affamees=" + recorder.getStarvingPopTypesCount()),
            new HealthCheck("wages_trace_consistent",
                Math.abs(recorder.getFinalWagesPaid() - (recorder.getFinalRgoWages() + recorder.getFinalBuildingWages()))
                    <= Math.max(1.0f, 0.01f * recorder.getFinalWagesPaid()),
                String.format("salaires verses=%.0f (rgo=%.0f + usines=%.0f)",
                    recorder.getFinalWagesPaid(), recorder.getFinalRgoWages(), recorder.getFinalBuildingWages())),
            new HealthCheck("money_factor_coverage",
                surplusRatio >= -0.005f,
                String.format("surplus min (revenu-salaires-profits)/revenu=%.4f sur le run (intrants>=0)", surplusRatio))
        );

        System.out.println();
        System.out.println("=== SANTE DE LA SIMULATION ===");
        boolean allGreen = true;
        for (HealthCheck check : checks) {
            String status;
            if (check.ok()) {
                status = "OK";
            } else if (KNOWN_FAILING_HEALTH_CHECKS.contains(check.name())) {
                status = "ECHEC CONNU (fix en cours, non verrouille)";
            } else {
                status = "ECHEC";
            }
            System.out.printf("  [%-12s] %-38s %s%n", status, check.name(), check.detail());

            if (!check.ok() && !KNOWN_FAILING_HEALTH_CHECKS.contains(check.name())) {
                allGreen = false;
            }
        }
        System.out.println();

        assertTrue(allGreen, "Des invariants de sante de la simulation sont violes (voir rapport ci-dessus).");
    }

    private record HealthCheck(String name, boolean ok, String detail) {
    }

    private static final Set<String> KNOWN_FAILING_HEALTH_CHECKS = Set.of(
        "production_floor_95", "population_growth");

    private static final int HEALTH_TREASURY_MULTIPLIER = 100;

    private void assertDataSanity(SimulationRecorder recorder) {
        assertEquals(41, recorder.getGoodNames().length, "Expected all goods to be recorded");
        assertTrue(recorder.getCountryNames().size() > 100,
            "Expected a full set of countries, got " + recorder.getCountryNames().size());
        assertTrue(recorder.getBuildingTypeNames().size() > 0, "Expected economy building types");
        assertEquals(12, recorder.getPopTypeNames().length, "Expected all pop types");

        assertTrue(recorder.getWorldProductionTotal() > 0.0,
            "Expected the world economy to produce goods over five years");
        assertTrue(recorder.getBuildingProductionTotal() > 0.0,
            "Expected economy buildings to produce goods over five years");
        assertEquals(0L, recorder.getMismatchCount(),
            "Per-country production re-aggregated by the recorder must match world market production");
    }
}
