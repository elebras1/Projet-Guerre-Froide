package com.populaire.projetguerrefroide.util;

import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.util.FlecsConstants;

public class EcsConstants {
    private static final String SEA_PROVINCE_TAG = "SeaProvinceTag";
    private static final String REGION_TAG = "RegionTag";
    private static final String COUNTRY_TAG = "CountryTag";
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
    public static final String ON_MAP = "OnMap";
    public static final String SUSPENDED = "Suspended";
    public static final String CAPITALIST_TAG = "CapitalistTag";
    public static final String ARISTOCRAT_TAG = "AristocratTag";
    public static final String SEA_FORCE_TAG = "SeaForceTypeTag";
    public static final String LAND_FORCE_TAG = "LandForceTypeTag";
    public static final String AIR_FORCE_TAG = "AirForceTypeTag";

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
    private final long onMap;
    private final long suspended;
    private final long capitalistTag;
    private final long aristocratTag;
    private final long seaForceTag;
    private final long landForceTag;
    private final long airForceTag;

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
        this.onMap = ecsWorld.entity(ON_MAP);
        this.suspended = ecsWorld.entity(SUSPENDED);
        this.capitalistTag = ecsWorld.entity(CAPITALIST_TAG);
        this.aristocratTag = ecsWorld.entity(ARISTOCRAT_TAG);
        this.seaForceTag = ecsWorld.entity(SEA_FORCE_TAG);
        this.landForceTag = ecsWorld.entity(LAND_FORCE_TAG);
        this.airForceTag = ecsWorld.entity(AIR_FORCE_TAG);
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

    public long onMap() {
        return this.onMap;
    }

    public long suspended() {
        return this.suspended;
    }

    public long capitalistTag() {
        return this.capitalistTag;
    }

    public long aristocratTag() {
        return this.aristocratTag;
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

}
