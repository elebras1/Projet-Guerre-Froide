package com.populaire.projetguerrefroide.simulation;

import com.populaire.projetguerrefroide.component.Color;
import com.populaire.projetguerrefroide.component.DevelopmentBuildingType;
import com.populaire.projetguerrefroide.component.EconomyBuildingType;
import com.populaire.projetguerrefroide.component.GlobalGood;
import com.populaire.projetguerrefroide.component.GlobalGoodView;
import com.populaire.projetguerrefroide.component.GlobalPopulationType;
import com.populaire.projetguerrefroide.component.Good;
import com.populaire.projetguerrefroide.component.GoodView;
import com.populaire.projetguerrefroide.component.GlobalPopulationTypeView;
import com.populaire.projetguerrefroide.component.Modifiers;
import com.populaire.projetguerrefroide.component.ModifiersView;
import com.populaire.projetguerrefroide.component.PopulationType;
import com.populaire.projetguerrefroide.component.PopulationTypeView;
import com.populaire.projetguerrefroide.component.SpecialBuildingType;
import com.populaire.projetguerrefroide.component.Technology;
import com.populaire.projetguerrefroide.component.TechnologyView;
import com.populaire.projetguerrefroide.system.state.StateBudgetUtils;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.simulation.Constants.MAX_TECHNOLOGIES;
import static com.populaire.projetguerrefroide.simulation.Constants.POP_TYPE_COUNT;
import static com.populaire.projetguerrefroide.simulation.Constants.RESEARCH_EFFICIENCY;

public class EcsConstants {
    private static final String SEA_PROVINCE_TAG = "SeaProvinceTag";
    private static final String REGION_TAG = "RegionTag";
    public static final String COUNTRY_TAG = "CountryTag";
    private static final String CULTURE_TAG = "CultureTag";
    private static final String RELIGION_TAG = "ReligionTag";
    private static final String IDENTITY_TAG = "IdentityTag";
    private static final String ATTITUDE_TAG = "AttitudeTag";
    private static final String ALLIED_WITH = "AlliedWith";
    private static final String GUARANTEES = "Guarantees";
    private static final String IS_GUARANTEED_BY = "IsGuaranteedBy";
    private static final String IS_PUPPET_MASTER_OF = "IsPuppetMasterOf";
    private static final String IS_PUPPET_OF = "IsPuppetOf";
    private static final String COLONIZES = "Colonizes";
    private static final String IS_COLONY_OF = "IsColonyOf";
    public static final String RESSOURCE_GOOD_TAG = "RessourceGoodTag";
    public static final String ADVANCED_GOOD_TAG = "AdvancedGoodTag";
    public static final String MILITARY_GOOD_TAG = "MilitaryGoodTag";
    public static final String SERVICE_GOOD_TAG = "ServiceGoodTag";
    public static final String ON_MAP = "OnMap";
    public static final String SUSPENDED = "Suspended";
    public static final String LEVELED_UP = "LeveledUp";
    public static final String RGO_TYPE = "RgoType";
    public static final String CAPITALIST_TAG = "CapitalistTag";
    public static final String WORKERS_TAG = "WorkersTag";
    public static final String ARISTOCRAT_TAG = "AristocratTag";
    public static final String RURAL_TAG = "RuralTag";
    public static final String EMPLOYEE_TAG = "EmployeeTag";
    public static final String RELIGIOUS_TAG = "ReligiousTag";
    public static final String SEA_FORCE_TAG = "SeaForceTypeTag";
    public static final String LAND_FORCE_TAG = "LandForceTypeTag";
    public static final String AIR_FORCE_TAG = "AirForceTypeTag";
    private static final String GLOBAL_MARKET = "global_market";
    private static final String GLOBAL_POPULATION_TYPE = "global_population_type";
    private static final String GLOBAL_GOOD = "global_good";
    private static final String GLOBAL_STRATEGIC_STORAGE = "global_strategic_storage";
    private static final String GLOBAL_MILITARY_FACILITIES = "global_military_facilities";
    private static final String GLOBAL_STATE_CONSUMPTION = "global_state_consumption";
    private static final String GLOBAL_ARTISAN_CRAFTS = "global_artisan_crafts";
    private static final String ECONOMY_CALENDAR = "economy_calendar";

    private final long seaProvinceTag;
    private final long regionTag;
    private final long countryTag;
    private final long cultureTag;
    private final long religionTag;
    private final long identityTag;
    private final long attitudeTag;
    private final long alliedWith;
    private final long guarantees;
    private final long isGuaranteedBy;
    private final long isPuppetMasterOf;
    private final long isPuppetOf;
    private final long colonizes;
    private final long isColonyOf;
    private final long ressourceGoodTag;
    private final long advancedGoodTag;
    private final long militaryGoodTag;
    private final long serviceGoodTag;
    private final long onMap;
    private final long suspended;
    private final long leveledUp;
    private final long rgoType;
    private final long capitalistTag;
    private final long workersTag;
    private final long aristocratTag;
    private final long ruralTag;
    private final long employeeTag;
    private final long religiousTag;
    private final long seaForceTag;
    private final long landForceTag;
    private final long airForceTag;
    private final float[] baseWorkforceShares;
    private final int[] incomeTypes;
    private final boolean[] assistedTypes;
    private final boolean[] ruralPopTypes;
    private final boolean[] employeePopTypes;
    private final float[] goodCosts;
    private final float[] civilianGoodCosts;
    private final long[] technologyIds;
    private final int[] technologyYears;
    private final int[] technologyTimes;
    private final int[] technologyTrees;
    private final int[][] technologyPrerequisites;
    private final float[] technologyResearchEfficiencies;
    private int technologyCount;
    private final float[] goodStockDecays;
    private long globalMarket;
    private long globalPopulationType;
    private long globalGood;
    private long globalStrategicStorage;
    private long globalMilitaryFacilities;
    private long globalStateConsumption;
    private long globalArtisanCrafts;
    private long economyCalendar;
    private int statePostIndex;
    private boolean globalsResolved;

    public EcsConstants(World ecsWorld) {
        this.seaProvinceTag = ecsWorld.entity(SEA_PROVINCE_TAG);
        this.regionTag = ecsWorld.entity(REGION_TAG);
        this.countryTag = ecsWorld.entity(COUNTRY_TAG);
        this.cultureTag = ecsWorld.entity(CULTURE_TAG);
        this.religionTag = ecsWorld.entity(RELIGION_TAG);
        this.identityTag = ecsWorld.entity(IDENTITY_TAG);
        this.attitudeTag = ecsWorld.entity(ATTITUDE_TAG);
        this.alliedWith = ecsWorld.entity(ALLIED_WITH);
        this.guarantees = ecsWorld.entity(GUARANTEES);
        this.isGuaranteedBy = ecsWorld.entity(IS_GUARANTEED_BY);
        this.isPuppetMasterOf = ecsWorld.entity(IS_PUPPET_MASTER_OF);
        this.isPuppetOf = ecsWorld.entity(IS_PUPPET_OF);
        this.colonizes = ecsWorld.entity(COLONIZES);
        this.isColonyOf = ecsWorld.entity(IS_COLONY_OF);
        this.ressourceGoodTag = ecsWorld.entity(RESSOURCE_GOOD_TAG);
        this.advancedGoodTag = ecsWorld.entity(ADVANCED_GOOD_TAG);
        this.militaryGoodTag = ecsWorld.entity(MILITARY_GOOD_TAG);
        this.serviceGoodTag = ecsWorld.entity(SERVICE_GOOD_TAG);
        this.onMap = ecsWorld.entity(ON_MAP);
        this.suspended = ecsWorld.entity(SUSPENDED);
        this.leveledUp = ecsWorld.entity(LEVELED_UP);
        this.rgoType = ecsWorld.entity(RGO_TYPE);
        ecsWorld.obtainEntity(this.rgoType).add(Flecs.Traversable);
        this.capitalistTag = ecsWorld.entity(CAPITALIST_TAG);
        this.workersTag = ecsWorld.entity(WORKERS_TAG);
        this.aristocratTag = ecsWorld.entity(ARISTOCRAT_TAG);
        this.ruralTag = ecsWorld.entity(RURAL_TAG);
        this.employeeTag = ecsWorld.entity(EMPLOYEE_TAG);
        this.religiousTag = ecsWorld.entity(RELIGIOUS_TAG);
        this.seaForceTag = ecsWorld.entity(SEA_FORCE_TAG);
        this.landForceTag = ecsWorld.entity(LAND_FORCE_TAG);
        this.airForceTag = ecsWorld.entity(AIR_FORCE_TAG);
        this.baseWorkforceShares = new float[POP_TYPE_COUNT];
        this.incomeTypes = new int[POP_TYPE_COUNT];
        this.assistedTypes = new boolean[POP_TYPE_COUNT];
        this.ruralPopTypes = new boolean[POP_TYPE_COUNT];
        this.employeePopTypes = new boolean[POP_TYPE_COUNT];
        this.goodCosts = new float[GOOD_COUNT];
        this.civilianGoodCosts = new float[GOOD_COUNT];
        this.technologyIds = new long[MAX_TECHNOLOGIES];
        this.technologyYears = new int[MAX_TECHNOLOGIES];
        this.technologyTimes = new int[MAX_TECHNOLOGIES];
        this.technologyTrees = new int[MAX_TECHNOLOGIES];
        this.technologyPrerequisites = new int[MAX_TECHNOLOGIES][];
        this.technologyResearchEfficiencies = new float[MAX_TECHNOLOGIES];
        this.goodStockDecays = new float[GOOD_COUNT];
        this.declareTypeInheritance(ecsWorld);
    }

    public void resolveGlobals(World ecsWorld) {
        this.globalMarket = ecsWorld.lookup(GLOBAL_MARKET);
        this.globalPopulationType = ecsWorld.lookup(GLOBAL_POPULATION_TYPE);
        this.globalGood = ecsWorld.lookup(GLOBAL_GOOD);
        this.globalStrategicStorage = ecsWorld.lookup(GLOBAL_STRATEGIC_STORAGE);
        this.globalMilitaryFacilities = ecsWorld.lookup(GLOBAL_MILITARY_FACILITIES);
        this.globalStateConsumption = ecsWorld.lookup(GLOBAL_STATE_CONSUMPTION);
        this.globalArtisanCrafts = ecsWorld.lookup(GLOBAL_ARTISAN_CRAFTS);
        this.economyCalendar = ecsWorld.lookup(ECONOMY_CALENDAR);
        GlobalPopulationTypeView globalPopulationTypeData = ecsWorld.obtainEntityView(this.globalPopulationType).getView(GlobalPopulationType.class);
        this.statePostIndex = globalPopulationTypeData.statePostPopTypeIndex();
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            this.baseWorkforceShares[p] = globalPopulationTypeData.workforceShareByPopType(p);
            EntityView populationTypeEntity = ecsWorld.obtainEntityView(globalPopulationTypeData.popTypeIds(p));
            PopulationTypeView populationType = populationTypeEntity.getView(PopulationType.class);
            this.incomeTypes[p] = populationType.incomeType();
            this.assistedTypes[p] = StateBudgetUtils.assisted(populationType.strata(), populationType.incomeType());
            this.ruralPopTypes[p] = populationTypeEntity.has(this.ruralTag);
            this.employeePopTypes[p] = populationTypeEntity.has(this.employeeTag);
        }
        GlobalGoodView globalGoodData = ecsWorld.obtainEntityView(this.globalGood).getView(GlobalGood.class);
        for (int g = 0; g < GOOD_COUNT; g++) {
            EntityView goodEntity = ecsWorld.obtainEntityView(globalGoodData.goodIds(g));
            GoodView good = goodEntity.getView(Good.class);
            this.goodCosts[g] = good.cost();
            this.goodStockDecays[g] = good.stockDecay();
            this.civilianGoodCosts[g] = goodEntity.has(this.militaryGoodTag) ? 0f : good.cost();
        }
        this.resolveTechnologies(ecsWorld);
        this.globalsResolved = true;
    }

    private void resolveTechnologies(World ecsWorld) {
        long researchEfficiencyTag = ecsWorld.lookup(RESEARCH_EFFICIENCY);
        Query technologyQuery = ecsWorld.query().with(Technology.class).build();
        technologyQuery.each(technologyId -> {
            TechnologyView technology = ecsWorld.obtainEntityView(technologyId).getView(Technology.class);
            int index = technology.index();
            this.technologyIds[index] = technologyId;
            this.technologyYears[index] = technology.year();
            this.technologyTimes[index] = technology.time();
            this.technologyTrees[index] = technology.tree();
            this.technologyCount = Math.max(this.technologyCount, index + 1);
            long[] prerequisiteIds = new long[technology.prerequisiteIdsLength()];
            int prerequisiteCount = 0;
            for (int p = 0; p < prerequisiteIds.length; p++) {
                if (technology.prerequisiteIds(p) != 0) {
                    prerequisiteIds[prerequisiteCount++] = technology.prerequisiteIds(p);
                }
            }
            int[] prerequisites = new int[prerequisiteCount];
            for (int p = 0; p < prerequisiteCount; p++) {
                prerequisites[p] = ecsWorld.obtainEntityView(prerequisiteIds[p]).<TechnologyView>getView(Technology.class).index();
            }
            this.technologyPrerequisites[index] = prerequisites;
            EntityView technologyEntity = ecsWorld.obtainEntityView(technologyId);
            if (technologyEntity.has(Modifiers.class)) {
                ModifiersView modifiers = technologyEntity.getView(Modifiers.class);
                for (int m = 0; m < modifiers.tagIdsLength(); m++) {
                    if (modifiers.tagIds(m) == researchEfficiencyTag) {
                        this.technologyResearchEfficiencies[index] = modifiers.values(m);
                    }
                }
            }
        });
        technologyQuery.destruct();
    }

    private void requireGlobals() {
        if (!this.globalsResolved) {
            throw new IllegalStateException("Global entities are resolved by EconomyPipelines.initialize()");
        }
    }

    public long globalMarket() {
        this.requireGlobals();
        return this.globalMarket;
    }

    public long globalPopulationType() {
        this.requireGlobals();
        return this.globalPopulationType;
    }

    public long globalGood() {
        this.requireGlobals();
        return this.globalGood;
    }

    public long globalStrategicStorage() {
        this.requireGlobals();
        return this.globalStrategicStorage;
    }

    public long globalMilitaryFacilities() {
        this.requireGlobals();
        return this.globalMilitaryFacilities;
    }

    public long globalStateConsumption() {
        this.requireGlobals();
        return this.globalStateConsumption;
    }

    public long globalArtisanCrafts() {
        this.requireGlobals();
        return this.globalArtisanCrafts;
    }

    public long economyCalendar() {
        this.requireGlobals();
        return this.economyCalendar;
    }

    public int statePostIndex() {
        this.requireGlobals();
        return this.statePostIndex;
    }

    public float[] baseWorkforceShares() {
        this.requireGlobals();
        return this.baseWorkforceShares;
    }

    public int[] incomeTypes() {
        this.requireGlobals();
        return this.incomeTypes;
    }

    public boolean[] assistedTypes() {
        this.requireGlobals();
        return this.assistedTypes;
    }

    public boolean[] ruralPopTypes() {
        this.requireGlobals();
        return this.ruralPopTypes;
    }

    public boolean[] employeePopTypes() {
        this.requireGlobals();
        return this.employeePopTypes;
    }

    public float[] civilianGoodCosts() {
        this.requireGlobals();
        return this.civilianGoodCosts;
    }

    public int technologyCount() {
        this.requireGlobals();
        return this.technologyCount;
    }

    public long[] technologyIds() {
        this.requireGlobals();
        return this.technologyIds;
    }

    public int[] technologyYears() {
        this.requireGlobals();
        return this.technologyYears;
    }

    public int[] technologyTimes() {
        this.requireGlobals();
        return this.technologyTimes;
    }

    public int[] technologyTrees() {
        this.requireGlobals();
        return this.technologyTrees;
    }

    public int[][] technologyPrerequisites() {
        this.requireGlobals();
        return this.technologyPrerequisites;
    }

    public float[] technologyResearchEfficiencies() {
        this.requireGlobals();
        return this.technologyResearchEfficiencies;
    }

    public float[] goodCosts() {
        this.requireGlobals();
        return this.goodCosts;
    }

    public float[] goodStockDecays() {
        this.requireGlobals();
        return this.goodStockDecays;
    }

    private void declareTypeInheritance(World ecsWorld) {
        ecsWorld.obtainEntity(ecsWorld.component(PopulationType.class)).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(ecsWorld.component(Color.class)).add(Flecs.OnInstantiate, Flecs.DontInherit);
        ecsWorld.obtainEntity(this.capitalistTag).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(this.aristocratTag).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(this.ruralTag).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(this.religiousTag).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(ecsWorld.component(EconomyBuildingType.class)).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(ecsWorld.component(SpecialBuildingType.class)).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(ecsWorld.component(DevelopmentBuildingType.class)).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(ecsWorld.component(Modifiers.class)).add(Flecs.OnInstantiate, Flecs.Inherit);
        ecsWorld.obtainEntity(this.onMap).add(Flecs.OnInstantiate, Flecs.Inherit);
    }

    public long seaProvinceTag() {
        return this.seaProvinceTag;
    }

    public long regionTag() {
        return this.regionTag;
    }

    public long countryTag() {
        return this.countryTag;
    }

    public long cultureTag() {
        return this.cultureTag;
    }

    public long religionTag() {
        return this.religionTag;
    }

    public long identityTag() {
        return this.identityTag;
    }

    public long attitudeTag() {
        return this.attitudeTag;
    }

    public long alliedWith() {
        return this.alliedWith;
    }

    public long guarantees() {
        return this.guarantees;
    }

    public long isGuaranteedBy() {
        return this.isGuaranteedBy;
    }

    public long isPuppetMasterOf() {
        return this.isPuppetMasterOf;
    }

    public long isPuppetOf() {
        return this.isPuppetOf;
    }

    public long colonizes() {
        return this.colonizes;
    }

    public long isColonyOf() {
        return this.isColonyOf;
    }

    public long ressourceGoodTag() {
        return this.ressourceGoodTag;
    }

    public long advancedGoodTag() {
        return this.advancedGoodTag;
    }

    public long militaryGoodTag() {
        return this.militaryGoodTag;
    }

    public long serviceGoodTag() {
        return this.serviceGoodTag;
    }

    public long onMap() {
        return this.onMap;
    }

    public long suspended() {
        return this.suspended;
    }

    public long leveledUp() {
        return this.leveledUp;
    }

    public long rgoType() {
        return this.rgoType;
    }

    public long workersTag() {
        return this.workersTag;
    }

    public long capitalistTag() {
        return this.capitalistTag;
    }

    public long aristocratTag() {
        return this.aristocratTag;
    }

    public long ruralTag() {
        return this.ruralTag;
    }

    public long employeeTag() {
        return this.employeeTag;
    }

    public long religiousTag() {
        return this.religiousTag;
    }

    public long seaForceTag() {
        return this.seaForceTag;
    }

    public long landForceTag() {
        return this.landForceTag;
    }

    public long airForceTag() {
        return this.airForceTag;
    }

    public long getAllianceRelation(String type, boolean isFirstCountry) {
        return switch (type) {
            case "standard" -> this.alliedWith();
            case "guarantee" -> isFirstCountry ? this.guarantees() : this.isGuaranteedBy();
            case "puppet_state" -> isFirstCountry ? this.isPuppetMasterOf() : this.isPuppetOf();
            case "colony" -> isFirstCountry ? this.colonizes() : this.isColonyOf();
            default -> throw new IllegalArgumentException("Invalid alliance type: " + type);
        };
    }

    public long getForceTypeTag(String forceType) {
        return switch (forceType) {
            case "land" -> this.landForceTag();
            case "sea" -> this.seaForceTag();
            case "air" -> this.airForceTag();
            default -> throw new IllegalArgumentException("Invalid force type: " + forceType);
        };
    }

}
