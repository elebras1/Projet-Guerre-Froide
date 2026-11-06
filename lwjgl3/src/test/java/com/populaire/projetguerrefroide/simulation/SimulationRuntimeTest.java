package com.populaire.projetguerrefroide.simulation;

import com.populaire.projetguerrefroide.TestGdxFiles;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.protocol.LoadingStage;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.command.ChoosePlayerCountryCommand;
import com.populaire.projetguerrefroide.protocol.command.ExpandBuildingCommand;
import com.populaire.projetguerrefroide.protocol.command.SuspendBuildingCommand;
import com.populaire.projetguerrefroide.protocol.request.Request;
import com.populaire.projetguerrefroide.protocol.request.RequestBoard;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.request.Response;
import com.populaire.projetguerrefroide.protocol.request.SortType;
import com.populaire.projetguerrefroide.protocol.response.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimulationRuntimeTest {
    private static final LocalDate START_DATE = LocalDate.of(1946, 1, 1);
    private static final long NANOS_PER_TICK_AT_MAX_SPEED = 100_000_000L;
    private static final int TICKS = 12;
    private static final String PLAYER_COUNTRY = "USA";
    private static final long MAX_BYTES_PER_REQUEST = 16 * 1024;

    @BeforeAll
    public void setUp() {
        TestGdxFiles.install();
    }

    @Test
    public void replayIsIndependentOfClockCadence() {
        Run regular = this.play(NANOS_PER_TICK_AT_MAX_SPEED);
        Run irregular = this.play(37_000_000L);
        assertEquals(TICKS, regular.ticks);
        assertEquals(regular.ticks, irregular.ticks);
        assertEquals(regular.journal, irregular.journal);
        assertEquals(regular.digest, irregular.digest);
    }

    @Test
    public void servingRequestsAllocatesLittle() {
        FakeClock clock = new FakeClock();
        SimulationRuntime runtime = SimulationRuntime.inline(START_DATE, false, clock::now);
        runtime.start();
        assertEquals(LoadingStage.READY, runtime.loadingStage());
        Catalog catalog = runtime.catalog();
        int playerIndex = this.countryIndex(catalog, PLAYER_COUNTRY);
        runtime.send(new ChoosePlayerCountryCommand(catalog.countries().entityId(playerIndex)));
        runtime.pump();

        RequestBoard board = runtime.requests();
        Request<RegionsBuildings> regions = board.register(new Request<>(RequestKind.REGIONS_BUILDINGS, new RegionsBuildings(), new RegionsBuildings()));
        regions.params().option(SortType.POPULATION.ordinal());
        this.serve(runtime, regions);
        long buildingId = regions.response().buildingId(0);
        int provinceIndex = catalog.provinces().indexOfColor(catalog.provinces().color(0));
        List<Request<? extends Response>> requests = List.of(
            regions,
            this.request(board, RequestKind.COUNTRY_OVERVIEW, new CountryOverview(), new CountryOverview(), -1, 0, 0),
            this.request(board, RequestKind.COUNTRY_SUMMARY, new CountrySummary(), new CountrySummary(), playerIndex, 0, 0),
            this.request(board, RequestKind.PROVINCE_DETAILS, new ProvinceDetails(), new ProvinceDetails(), provinceIndex, 0, 0),
            this.request(board, RequestKind.PROVINCE_HOVER, new ProvinceHover(), new ProvinceHover(), provinceIndex, 0, 0),
            this.request(board, RequestKind.BUILDING_DETAILS, new BuildingDetails(), new BuildingDetails(), -1, buildingId, 0),
            this.request(board, RequestKind.POLITICAL_MAP, new PoliticalMap(), new PoliticalMap(), -1, 0, 0),
            this.request(board, RequestKind.MAP_MODE_COLORS, new MapModeColors(), new MapModeColors(), -1, 0, MapMode.POPULATION.ordinal()),
            this.request(board, RequestKind.MAP_BUILDINGS, new MapBuildings(), new MapBuildings(), -1, 0, 0));

        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        StringBuilder report = new StringBuilder("Bytes allocated per served request (after warm-up):");
        long worst = 0;
        for (Request<? extends Response> request : requests) {
            for (int i = 0; i < 50; i++) {
                this.serve(runtime, request);
            }
            int rounds = 200;
            long before = threads.getCurrentThreadAllocatedBytes();
            for (int i = 0; i < rounds; i++) {
                this.serve(runtime, request);
            }
            long perRequest = (threads.getCurrentThreadAllocatedBytes() - before) / rounds;
            worst = Math.max(worst, perRequest);
            report.append(System.lineSeparator()).append("  ").append(request.kind()).append(" : ").append(perRequest);
        }
        System.out.println(report);
        runtime.shutdown();
        assertEquals(LoadingStage.CLOSED, runtime.loadingStage());
        assertTrue(worst < MAX_BYTES_PER_REQUEST, report.toString());
    }

    private <R extends Response> Request<R> request(RequestBoard board, RequestKind kind, R first, R second, int index, long id, int option) {
        Request<R> request = board.register(new Request<>(kind, first, second));
        request.params().index(index).id(id).option(option);
        return request;
    }

    private Run play(long nanosPerPump) {
        FakeClock clock = new FakeClock();
        SimulationRuntime runtime = SimulationRuntime.inline(START_DATE, false, clock::now);
        runtime.start();
        assertEquals(LoadingStage.READY, runtime.loadingStage());
        Catalog catalog = runtime.catalog();
        int playerIndex = this.countryIndex(catalog, PLAYER_COUNTRY);
        runtime.send(new ChoosePlayerCountryCommand(catalog.countries().entityId(playerIndex)));
        runtime.pump();

        RequestBoard board = runtime.requests();
        Request<RegionsBuildings> regions = board.register(new Request<>(RequestKind.REGIONS_BUILDINGS, new RegionsBuildings(), new RegionsBuildings()));
        Request<CountryOverview> overview = board.register(new Request<>(RequestKind.COUNTRY_OVERVIEW, new CountryOverview(), new CountryOverview()));
        this.serve(runtime, regions);
        long expanded = regions.response().buildingId(0);
        long suspended = regions.response().buildingId(1);

        runtime.requestSpeed(5);
        runtime.requestPaused(false);
        boolean expandSent = false;
        boolean suspendSent = false;
        while (runtime.header().tick() < TICKS) {
            int tick = runtime.header().tick();
            if (tick == 2 && !expandSent) {
                runtime.send(new ExpandBuildingCommand(expanded));
                expandSent = true;
            }
            if (tick == 5 && !suspendSent) {
                runtime.send(new SuspendBuildingCommand(suspended));
                suspendSent = true;
            }
            clock.advance(nanosPerPump);
            runtime.pump();
        }
        runtime.requestPaused(true);
        runtime.pump();
        this.serve(runtime, regions);
        this.serve(runtime, overview);

        StringBuilder digest = new StringBuilder();
        RegionsBuildings result = regions.response();
        digest.append(overview.response().population()).append(';');
        for (int building = 0; building < result.buildingCount(); building++) {
            digest.append(result.buildingId(building)).append(':')
                .append(result.buildingSize(building)).append(':')
                .append(result.buildingLevelsQueued(building)).append(':')
                .append(result.buildingWorkers(building)).append(':')
                .append(Float.floatToIntBits(result.buildingProduction(building))).append(':')
                .append(result.buildingSuspended(building)).append(';');
        }
        StringBuilder journal = new StringBuilder();
        for (CommandJournal.Entry entry : runtime.journal().entries()) {
            journal.append(entry.tick()).append(' ').append(entry.command()).append(' ').append(entry.accepted()).append(';');
        }
        Run run = new Run(runtime.header().tick(), journal.toString(), digest.toString());
        runtime.shutdown();
        return run;
    }

    private void serve(SimulationRuntime runtime, Request<?> request) {
        assertTrue(runtime.requests().submit(request));
        runtime.requests().flush();
        runtime.pump();
        assertTrue(request.receive());
    }

    private int countryIndex(Catalog catalog, String nameId) {
        for (int index = 0; index < catalog.countries().size(); index++) {
            if (nameId.equals(catalog.countries().nameId(index))) {
                return index;
            }
        }
        throw new IllegalStateException("Country not found: " + nameId);
    }

    private record Run(int ticks, String journal, String digest) {
    }

    private static final class FakeClock {
        private long now = 1_000_000_000L;

        long now() {
            return this.now;
        }

        void advance(long nanos) {
            this.now += nanos;
        }
    }
}
