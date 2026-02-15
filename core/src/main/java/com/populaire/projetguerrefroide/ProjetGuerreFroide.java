package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.command.CommandBus;
import com.populaire.projetguerrefroide.command.handler.DemolishBuildingHandler;
import com.populaire.projetguerrefroide.command.handler.ExpandBuildingHandler;
import com.populaire.projetguerrefroide.command.handler.ResumeBuildingHandler;
import com.populaire.projetguerrefroide.command.handler.SuspendBuildingHandler;
import com.populaire.projetguerrefroide.command.request.DemolishBuildingCommand;
import com.populaire.projetguerrefroide.command.request.ExpandBuildingCommand;
import com.populaire.projetguerrefroide.command.request.ResumeBuildingCommand;
import com.populaire.projetguerrefroide.command.request.SuspendBuildingCommand;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.service.*;
import com.populaire.projetguerrefroide.system.ExpandBuildingSystem;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationHireSystem;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationProduceSystem;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationSizeSystem;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ProjetGuerreFroide extends Game {
    public static final int WORLD_WIDTH = 5616;
    public static final int WORLD_HEIGHT = 2160;
    private final ConfigurationService configurationService;
    private ScreenManager screenManager;
    private GameContext gameContext;
    private final World ecsWorld;

    public ProjetGuerreFroide() {
        this.configurationService = new ConfigurationService();
        this.ecsWorld = new World();
    }

    @Override
    public void create() {
        this.ecsWorld.setThreads(4);
        this.registerComponents();
        this.gameContext = this.configurationService.getGameContext(this.ecsWorld);
        ExpandBuildingSystem expandBuildingSystem = new ExpandBuildingSystem(this.ecsWorld);
        QueryRepository queryRepository = new QueryRepository(this.gameContext.getEcsWorld(), this.gameContext.getEcsConstants());
        BuildingService buildingService = new BuildingService(this.gameContext, expandBuildingSystem);
        ResourceGatheringOperationSizeSystem rgoSizeSystem = new ResourceGatheringOperationSizeSystem(this.gameContext.getEcsWorld());
        ResourceGatheringOperationHireSystem rgoHireSystem = new ResourceGatheringOperationHireSystem(this.gameContext.getEcsWorld());
        ResourceGatheringOperationProduceSystem rgoProduceSystem = new ResourceGatheringOperationProduceSystem(this.gameContext.getEcsWorld());
        EconomyService economyService = new EconomyService(this.gameContext, rgoSizeSystem, rgoHireSystem, rgoProduceSystem);
        RegionService regionService = new RegionService(this.gameContext, buildingService, queryRepository);
        CountryService countryService = new CountryService(this.gameContext, queryRepository, regionService);
        ProvinceService provinceService = new ProvinceService(this.gameContext, queryRepository, countryService, regionService);
        WorldService worldService = new WorldService(this.gameContext, queryRepository, buildingService, economyService, regionService, countryService, provinceService);
        TimeService timeService = new TimeService(this.gameContext.getBookmark().date());
        CommandBus commandBus = new CommandBus();
        this.registerCommands(commandBus, buildingService);
        this.screenManager = new ScreenManager(this, this.gameContext, this.configurationService, worldService, timeService, commandBus);
        this.loadAssets(this.gameContext.getAssetManager());
        this.screenManager.showMainMenuScreen();
        this.ecsDebug(gameContext);
    }

    private void registerComponents() {
        this.ecsWorld.component(Modifiers.class);
        this.ecsWorld.component(Minister.class);
        this.ecsWorld.component(Ideology.class);
        this.ecsWorld.component(Terrain.class);
        this.ecsWorld.component(ElectoralMechanism.class);
        this.ecsWorld.component(Leader.class);
        this.ecsWorld.component(EnactmentDuration.class);
        this.ecsWorld.component(Color.class);
        this.ecsWorld.component(Position.class);
        this.ecsWorld.component(Border.class);
        this.ecsWorld.component(DiplomaticRelation.class);
        this.ecsWorld.component(Adjacencies.class);
        this.ecsWorld.component(Country.class);
        this.ecsWorld.component(Province.class);
        this.ecsWorld.component(GeoHierarchy.class);
        this.ecsWorld.component(Law.class);
        this.ecsWorld.component(GovernmentPolicy.class);
        this.ecsWorld.component(PopulationType.class);
        this.ecsWorld.component(Good.class);
        this.ecsWorld.component(ResourceProduction.class);
        this.ecsWorld.component(EmployeeType.class);
        this.ecsWorld.component(ProductionType.class);
        this.ecsWorld.component(EconomyBuildingType.class);
        this.ecsWorld.component(SpecialBuildingType.class);
        this.ecsWorld.component(DevelopmentBuildingType.class);
        this.ecsWorld.component(Building.class);
        this.ecsWorld.component(BuildingEconomy.class);
        this.ecsWorld.component(PopulationTemplate.class);
        this.ecsWorld.component(CultureDistribution.class);
        this.ecsWorld.component(PopulationDistribution.class);
        this.ecsWorld.component(ReligionDistribution.class);
        this.ecsWorld.component(ResourceGathering.class);
        this.ecsWorld.component(ExpansionBuilding.class);
        this.ecsWorld.component(LocalMarket.class);
    }

    public void registerCommands(CommandBus commandBus, BuildingService buildingService) {
        commandBus.register(ExpandBuildingCommand.class, new ExpandBuildingHandler(buildingService));
        commandBus.register(SuspendBuildingCommand.class, new SuspendBuildingHandler(buildingService));
        commandBus.register(DemolishBuildingCommand.class, new DemolishBuildingHandler(buildingService));
        commandBus.register(ResumeBuildingCommand.class, new ResumeBuildingHandler(buildingService));
    }

    private void loadAssets(AssetManager assetManager) {
        assetManager.load("ui/ui_skin.json", Skin.class);
        assetManager.load("fonts/fonts_skin.json", Skin.class);
        assetManager.load("ui/scrollbars/scrollbars_skin.json", Skin.class);
        assetManager.finishLoading();
    }

    private void ecsDebug(GameContext gameContext) {
        Settings settings = gameContext.getSettings();
        if(settings.isDebugMode()) {
            this.ecsWorld.enableRest((short) 27750);
        }
    }

    @Override
    public void dispose() {
        this.ecsWorld.disableRest();
        this.ecsWorld.close();
        this.screenManager.dispose();
        this.gameContext.dispose();
        super.dispose();
    }
}
