package com.populaire.projetguerrefroide.simulation;

import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.loader.WorldLoader;
import com.populaire.projetguerrefroide.pojo.WorldData;
import com.populaire.projetguerrefroide.protocol.LoadingStage;
import com.populaire.projetguerrefroide.protocol.SimulationChannel;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.command.GameCommand;
import com.populaire.projetguerrefroide.protocol.event.EventKind;
import com.populaire.projetguerrefroide.protocol.event.EventRing;
import com.populaire.projetguerrefroide.protocol.request.RequestBoard;
import com.populaire.projetguerrefroide.service.EconomyService;
import com.populaire.projetguerrefroide.simulation.request.RequestService;
import com.populaire.projetguerrefroide.system.BuildingLevelUpNotificationSystem;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Rest;
import io.github.elebras1.flecs.World;

import java.time.LocalDate;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongSupplier;

public final class SimulationRuntime implements SimulationChannel {
    private static final String THREAD_NAME = "simulation";
    private static final int FLECS_THREADS = 4;
    private static final int INBOX_CAPACITY = 256;
    private static final int EVENT_CAPACITY = 1024;
    private static final int REST_PORT = 27750;
    private final boolean threaded;
    private final boolean debugMode;
    private final LongSupplier nanoTime;
    private final SimulationHeader header;
    private final RequestBoard board;
    private final EventRing events;
    private final ArrayBlockingQueue<GameCommand> inbox;
    private final AtomicInteger requestedSpeed;
    private final AtomicBoolean requestedPaused;
    private final CommandJournal journal;
    private volatile boolean shutdownRequested;
    private volatile LoadingStage loadingStage;
    private volatile Catalog catalog;
    private volatile Throwable failure;
    private volatile Thread owner;
    private Thread thread;
    private World ecsWorld;
    private EconomyService economyService;
    private RequestService requestService;
    private CommandService commandService;
    private SimulationState state;
    private SimulationClock clock;

    private SimulationRuntime(LocalDate startDate, boolean threaded, boolean debugMode, LongSupplier nanoTime) {
        this.threaded = threaded;
        this.debugMode = debugMode;
        this.nanoTime = nanoTime;
        this.header = new SimulationHeader(startDate.toEpochDay());
        this.board = new RequestBoard(this::wake);
        this.events = new EventRing(EVENT_CAPACITY);
        this.inbox = new ArrayBlockingQueue<>(INBOX_CAPACITY);
        this.requestedSpeed = new AtomicInteger(SimulationClock.MIN_SPEED);
        this.requestedPaused = new AtomicBoolean(true);
        this.journal = new CommandJournal();
        this.loadingStage = LoadingStage.NOT_STARTED;
    }

    public static SimulationRuntime threaded(LocalDate startDate, boolean debugMode) {
        return new SimulationRuntime(startDate, true, debugMode, System::nanoTime);
    }

    public static SimulationRuntime inline(LocalDate startDate, boolean debugMode, LongSupplier nanoTime) {
        return new SimulationRuntime(startDate, false, debugMode, nanoTime);
    }

    @Override
    public void start() {
        if (this.loadingStage != LoadingStage.NOT_STARTED) {
            throw new IllegalStateException("Simulation already started");
        }
        if (this.threaded) {
            this.thread = new Thread(this::runThread, THREAD_NAME);
            this.thread.setDaemon(false);
            this.thread.setUncaughtExceptionHandler((failedThread, throwable) -> this.fail(throwable));
            this.loadingStage = LoadingStage.CREATING_WORLD;
            this.thread.start();
        } else {
            this.load();
        }
    }

    @Override
    public void send(GameCommand command) {
        if (!this.inbox.offer(command)) {
            System.err.println("Simulation inbox full, command dropped: " + command);
            return;
        }
        this.wake();
    }

    @Override
    public void requestSpeed(int speed) {
        this.requestedSpeed.set(Math.max(SimulationClock.MIN_SPEED, Math.min(SimulationClock.MAX_SPEED, speed)));
        this.wake();
    }

    @Override
    public void requestPaused(boolean paused) {
        this.requestedPaused.set(paused);
        this.wake();
    }

    @Override
    public RequestBoard requests() {
        return this.board;
    }

    @Override
    public SimulationHeader header() {
        return this.header;
    }

    @Override
    public EventRing events() {
        return this.events;
    }

    @Override
    public LoadingStage loadingStage() {
        return this.loadingStage;
    }

    @Override
    public Catalog catalog() {
        return this.catalog;
    }

    @Override
    public Throwable failure() {
        return this.failure;
    }

    public CommandJournal journal() {
        return this.journal;
    }

    @Override
    public void pump() {
        if (this.threaded || this.loadingStage != LoadingStage.READY) {
            return;
        }
        this.step();
    }

    @Override
    public void shutdown() {
        if (this.threaded) {
            Thread simulationThread = this.thread;
            if (simulationThread == null) {
                return;
            }
            this.shutdownRequested = true;
            LockSupport.unpark(simulationThread);
            try {
                simulationThread.join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            this.thread = null;
        } else {
            this.close();
        }
    }

    private void runThread() {
        try {
            this.load();
            while (!this.shutdownRequested) {
                this.step();
                if (this.shutdownRequested) {
                    break;
                }
                long wait = this.clock.nanosUntilNextTick(this.nanoTime.getAsLong());
                if (wait == Long.MAX_VALUE) {
                    LockSupport.park(this);
                } else if (wait > 0) {
                    LockSupport.parkNanos(this, wait);
                }
            }
        } catch (Throwable throwable) {
            this.fail(throwable);
        } finally {
            this.close();
        }
    }

    private void fail(Throwable throwable) {
        if (this.failure == null) {
            this.failure = throwable;
        }
        this.loadingStage = LoadingStage.FAILED;
    }

    private void load() {
        this.owner = Thread.currentThread();
        this.loadingStage = LoadingStage.CREATING_WORLD;
        this.ecsWorld = new World();
        this.ecsWorld.setThreads(FLECS_THREADS);
        ComponentRegistry.register(this.ecsWorld);
        SimulationContext simulationContext = new SimulationContext(this.ecsWorld);
        new BuildingLevelUpNotificationSystem(this.ecsWorld, simulationContext.getEcsConstants(), this.events);
        this.economyService = new EconomyService(simulationContext);
        this.state = new SimulationState();
        this.requestService = new RequestService(simulationContext, this.state, this.header);
        this.commandService = new CommandService(simulationContext, this.economyService, this.state);
        this.clock = new SimulationClock();
        if (this.debugMode) {
            this.startRest();
        }
        this.loadingStage = LoadingStage.LOADING_WORLD;
        WorldData worldData = new WorldLoader(new WorldDaoImpl()).createWorld(simulationContext);
        this.ecsWorld.shrink();
        this.loadingStage = LoadingStage.BUILDING_CATALOG;
        Catalog builtCatalog = new CatalogBuilder(simulationContext).build(worldData);
        this.requestService.setCatalog(builtCatalog);
        this.catalog = builtCatalog;
        this.header.publishClock(this.clock.speed(), this.clock.paused());
        this.header.publishChange();
        this.loadingStage = LoadingStage.READY;
    }

    private void startRest() {
        this.ecsWorld.set(new Rest().port(REST_PORT));
        this.ecsWorld.importModule(Flecs.Doc);
        this.ecsWorld.importModule(Flecs.Alerts);
        this.ecsWorld.importModule(Flecs.Stats);
        this.ecsWorld.importModule(Flecs.Metrics);
    }

    private void step() {
        if (Thread.currentThread() != this.owner) {
            throw new IllegalStateException("Simulation stepped outside its owner thread: " + Thread.currentThread().getName());
        }
        long now = this.nanoTime.getAsLong();
        if (this.clock.apply(this.requestedSpeed.get(), this.requestedPaused.get(), now)) {
            this.header.publishClock(this.clock.speed(), this.clock.paused());
        }
        long publishedBatch = this.board.publishedBatch();
        this.applyCommands();
        this.requestService.serve(this.board, publishedBatch);
        if (this.state.economyInitialized() && this.clock.tickDue(now)) {
            this.economyService.advanceDay();
            this.clock.ticked(now, this.nanoTime.getAsLong());
            this.header.publishTick(this.clock.tick());
        }
    }

    private void applyCommands() {
        GameCommand command = this.inbox.poll();
        boolean applied = false;
        while (command != null) {
            boolean accepted;
            try {
                accepted = this.commandService.handle(command);
            } catch (RuntimeException exception) {
                exception.printStackTrace();
                accepted = false;
            }
            this.journal.record(this.clock.tick(), this.state.playerCountryId(), command, accepted);
            if (!accepted) {
                this.events.publish(EventKind.COMMAND_REJECTED, this.journal.entries().size() - 1, 0L, 0.0);
            }
            applied = true;
            command = this.inbox.poll();
        }
        if (applied) {
            this.header.publishChange();
        }
    }

    private void wake() {
        Thread simulationThread = this.thread;
        if (simulationThread != null) {
            LockSupport.unpark(simulationThread);
        }
    }

    private void close() {
        if (this.ecsWorld == null) {
            this.loadingStage = LoadingStage.CLOSED;
            return;
        }
        if (this.debugMode) {
            this.ecsWorld.removeAll(Rest.class);
        }
        this.ecsWorld.close();
        this.ecsWorld = null;
        if (this.loadingStage != LoadingStage.FAILED) {
            this.loadingStage = LoadingStage.CLOSED;
        }
    }
}
