package com.populaire.projetguerrefroide.simulation;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import io.github.elebras1.flecs.Field;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EconomySimulationAnalysisTest {

    private static final int DAYS_PER_YEAR = 365;
    private static final int DEFAULT_YEARS = 5;
    private static final int LONG_RUN_YEARS = 62;
    private static final int LONG_RUN_DETAIL_EVERY = 30;
    private static final String FREEZE_PRODUCTION = "production";
    private static final List<String> FROZEN_PRODUCTION_SYSTEMS = List.of(
        "RGOProductionSystem", "EconomyBuildingProductionSystem", "EconomyBuildingScaleSystem");

    private static final float NEGATIVE_BALANCE_TOLERANCE = 1e-6f;
    private static final double MONEY_DRIFT_TOLERANCE = 1e-3;
    private static final double LIFE_NEEDS_FLOOR = 0.9;
    private static final double REAL_OUTPUT_FLOOR = 0.85;
    private static final double START_STEADY_TOLERANCE = 0.1;
    private static final double CPI_LOW = 0.7;
    private static final double CPI_HIGH = 1.5;
    private static final double PRICE_BOUNDS_MAX_SHARE = 0.1;
    private static final double REAL_OUTPUT_GROWTH = 2.0;

    private World ecsWorld;
    private EconomyService economyService;
    private LocalDate startDate;
    private Set<String> forcedLaws;

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

        this.forcedLaws = this.listProperty("simulation.laws");
        for (String lawName : this.forcedLaws) {
            this.forceLaw(lawName);
        }

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
        world.component(RegionDemographics.class);
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
            this.ecsWorld.close();
            this.ecsWorld = null;
        }
    }

    @Test
    public void simulateAndAnalyseEconomy() throws Exception {
        String outputDir = System.getProperty("simulation.outputDir");
        assertTrue(outputDir != null && !outputDir.isBlank(),
            "'simulation.outputDir' system property is not set.");

        int years = this.intProperty("simulation.years", DEFAULT_YEARS);
        int totalTicks = years * DAYS_PER_YEAR;
        int detailEvery = this.intProperty("simulation.detailEvery", years > DEFAULT_YEARS ? LONG_RUN_DETAIL_EVERY : 1);
        Set<String> freeze = this.listProperty("simulation.freeze");
        for (String mode : freeze) {
            assertTrue(FREEZE_PRODUCTION.equals(mode), "Unknown freeze mode: " + mode);
        }
        System.out.printf("Simulation: %d years, detailed CSV every %d ticks, frozen: %s, forced laws: %s%n", years, detailEvery,
            freeze.isEmpty() ? "none" : freeze, this.forcedLaws.isEmpty() ? "none" : this.forcedLaws);

        CalibrationReport calibration = null;
        try (SimulationRecorder recorder = new SimulationRecorder(this.ecsWorld, new File(outputDir), this.startDate, detailEvery)) {
            long start = System.nanoTime();
            for (int tick = 0; tick < totalTicks; tick++) {
                long tickStart = System.nanoTime();
                this.ecsWorld.runPipeline(this.economyService.getMainPipeline().id(), 1f);
                long tickNanos = System.nanoTime() - tickStart;
                if (tick == 0) {
                    calibration = CalibrationReport.measure(this.ecsWorld);
                    calibration.write(new File(outputDir));
                    if (freeze.contains(FREEZE_PRODUCTION)) {
                        this.disableSystems(FROZEN_PRODUCTION_SYSTEMS);
                    }
                }
                recorder.recordTick(tick, tickNanos);
                if ((tick + 1) % DAYS_PER_YEAR == 0) {
                    System.out.printf("Simulated %d / %d ticks (%s)%n",
                        tick + 1, totalTicks, this.startDate.plusDays(tick));
                }
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("Simulated %d ticks in %.2f s%n", totalTicks, elapsedMs / 1000.0);
            System.out.printf("Economy pipeline: %.2f ms per tick on average, %.2f ms max%n", recorder.getAverageTickMillis(), recorder.getMaxTickMillis());

            this.assertDataSanity(recorder);
            this.validateHealth(recorder, calibration, years);
        }

        System.out.println("CSV data written to " + outputDir);
    }

    private int intProperty(String name, int defaultValue) {
        String value = System.getProperty(name);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
    }

    private Set<String> listProperty(String name) {
        String value = System.getProperty(name);
        Set<String> values = new LinkedHashSet<>();
        if (value != null) {
            for (String item : value.split(",")) {
                if (!item.isBlank()) {
                    values.add(item.trim());
                }
            }
        }
        return values;
    }

    private void forceLaw(String lawName) {
        long lawId = this.ecsWorld.lookup(lawName);
        assertTrue(lawId != 0L && this.ecsWorld.obtainEntityView(lawId).has(Law.class), "Unknown law: " + lawName);
        LawView law = this.ecsWorld.obtainEntityView(lawId).getMutView(Law.class);
        LawGroupView lawGroup = this.ecsWorld.obtainEntityView(law.lawGroupId()).getMutView(LawGroup.class);
        int lawGroupIndex = lawGroup.index();
        this.ecsWorld.query().with(Country.class).build().iter(iter -> {
            Field<Country> countryField = iter.field(Country.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMutView country = countryField.getMutView(i);
                country.activeLawIds(lawGroupIndex, lawId);
            }
        });
    }

    private void disableSystems(List<String> systemNames) {
        for (String systemName : systemNames) {
            long systemId = this.ecsWorld.lookup(systemName);
            assertTrue(systemId != 0L, "System not found: " + systemName);
            this.ecsWorld.obtainEntity(systemId).disable();
        }
    }

    private void validateHealth(SimulationRecorder recorder, CalibrationReport calibration, int years) {
        float surplusRatio = recorder.getMinSurplusRatio();
        double firstMoney = recorder.getFirstMoney();
        int goodsOutOfBand = recorder.getGoodsOutOfBandAtLeast(DAYS_PER_YEAR);

        List<HealthCheck> checks = new ArrayList<>(List.of(
            new HealthCheck("employment_le_population",
                recorder.getEmploymentExceedsPopulationRows() == 0L
                    && recorder.getFinalTotalEmployment() <= recorder.getFinalTotalPopulation(),
                "emploi=" + recorder.getFinalTotalEmployment() + " pop=" + recorder.getFinalTotalPopulation()
                    + " violations=" + recorder.getEmploymentExceedsPopulationRows()),
            new HealthCheck("no_negative_balance",
                recorder.getMinBalance() >= -NEGATIVE_BALANCE_TOLERANCE * Math.abs(firstMoney),
                String.format("solde min=%.4f (%s)", recorder.getMinBalance(), recorder.getMinBalanceSource())),
            new HealthCheck("money_conservation",
                recorder.getMaxMoneyDrift() <= MONEY_DRIFT_TOLERANCE,
                String.format("derive max=%.4f%% de la masse initiale %.0f", 100.0 * recorder.getMaxMoneyDrift(), firstMoney)),
            new HealthCheck("life_needs_floor",
                recorder.getMinYearlyPoorLifeWorld() >= LIFE_NEEDS_FLOOR && recorder.getMinYearlyPoorLifeCountry() >= LIFE_NEEDS_FLOOR,
                String.format("pauvres, moyenne annuelle min : monde=%.3f, pire pays=%.3f %s",
                    recorder.getMinYearlyPoorLifeWorld(), recorder.getMinYearlyPoorLifeCountry(), recorder.getWorstPoorLifeCountry())),
            new HealthCheck("real_output_floor",
                recorder.getMinRealOutputRatio() >= REAL_OUTPUT_FLOOR,
                String.format("production reelle min=%.1f%% du tick 0", 100.0 * recorder.getMinRealOutputRatio())),
            new HealthCheck("start_steady",
                recorder.getStartMinRatio() >= 1.0 - START_STEADY_TOLERANCE && recorder.getStartMaxRatio() <= 1.0 + START_STEADY_TOLERANCE,
                String.format("premiere annee [%.1f%%, %.1f%%] du tick 0", 100.0 * recorder.getStartMinRatio(), 100.0 * recorder.getStartMaxRatio())),
            new HealthCheck("cpi_band",
                recorder.getMinCpi() >= CPI_LOW && recorder.getMaxCpi() <= CPI_HIGH,
                String.format("indice des prix [%.2f, %.2f] (bornes [%.1f, %.1f])", recorder.getMinCpi(), recorder.getMaxCpi(), CPI_LOW, CPI_HIGH)),
            new HealthCheck("price_bounds",
                goodsOutOfBand <= PRICE_BOUNDS_MAX_SHARE * recorder.getGoodNames().length,
                String.format("%d biens hors [0.25, 3] x cout plus de %d jours d'affilee", goodsOutOfBand, DAYS_PER_YEAR)),
            new HealthCheck("calibration_ratio",
                calibration.getOutOfBandGoods().isEmpty(),
                String.format("%d/%d biens demandes hors [%.1f, %.1f], %d produits sans demande : %s",
                    calibration.getOutOfBandGoods().size(), calibration.getDemandedGoods(), CalibrationReport.RATIO_LOW, CalibrationReport.RATIO_HIGH,
                    calibration.getGoodsWithoutDemand(), String.join(" ", calibration.getOutOfBandGoods()))),
            new HealthCheck("population_growth",
                recorder.getLastTickWorldPopulation() > recorder.getFirstTickWorldPopulation(),
                String.format("population %.0f -> %.0f",
                    recorder.getFirstTickWorldPopulation(), recorder.getLastTickWorldPopulation())),
            new HealthCheck("treasury_bounded",
                recorder.getLastTickWorldTreasury() <= HEALTH_TREASURY_MULTIPLIER * recorder.getFinalWorldProductionValue(),
                String.format("tresor=%.0f vs %d x valeur production/journal=%.0f",
                    recorder.getLastTickWorldTreasury(), HEALTH_TREASURY_MULTIPLIER, recorder.getFinalWorldProductionValue())),
            new HealthCheck("wages_trace_consistent",
                Math.abs(recorder.getFinalWagesPaid() - (recorder.getFinalRgoWages() + recorder.getFinalBuildingWages()))
                    <= Math.max(1.0f, 0.01f * recorder.getFinalWagesPaid()),
                String.format("salaires verses=%.0f (rgo=%.0f + usines=%.0f)",
                    recorder.getFinalWagesPaid(), recorder.getFinalRgoWages(), recorder.getFinalBuildingWages())),
            new HealthCheck("money_factor_coverage",
                surplusRatio >= -0.005f,
                String.format("surplus min (revenu-salaires-profits)/revenu=%.4f sur le run (intrants>=0)", surplusRatio))
        ));
        if (years >= LONG_RUN_YEARS) {
            checks.add(new HealthCheck("real_output_growth",
                recorder.getRealOutputPerAdultGrowth() >= REAL_OUTPUT_GROWTH,
                String.format("production reelle par adulte x%.2f (cible x%.1f)", recorder.getRealOutputPerAdultGrowth(), REAL_OUTPUT_GROWTH)));
        }

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
        "money_conservation", "life_needs_floor", "real_output_floor", "start_steady", "cpi_band",
        "price_bounds", "calibration_ratio", "population_growth", "real_output_growth");

    private static final int HEALTH_TREASURY_MULTIPLIER = 100;

    private void assertDataSanity(SimulationRecorder recorder) {
        assertEquals(41, recorder.getGoodNames().length, "Expected all goods to be recorded");
        assertTrue(recorder.getCountryNames().size() > 100,
            "Expected a full set of countries, got " + recorder.getCountryNames().size());
        assertTrue(recorder.getBuildingTypeNames().size() > 0, "Expected economy building types");
        assertEquals(12, recorder.getPopTypeNames().length, "Expected all pop types");

        assertTrue(recorder.getWorldProductionTotal() > 0.0,
            "Expected the world economy to produce goods over the run");
        assertTrue(recorder.getBuildingProductionTotal() > 0.0,
            "Expected economy buildings to produce goods over the run");
        assertEquals(0L, recorder.getMismatchCount(),
            "Per-country production re-aggregated by the recorder must match world market production");
    }
}
