package com.populaire.projetguerrefroide.util;

import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.util.FlecsConstants;

public class EcsConstants {
    private static final String LAW_GROUP_TAG = "LawGroupTag";
    private static final String PROVINCE_TAG = "ProvinceTag";
    private static final String SEA_PROVINCE_TAG = "SeaProvinceTag";
    private static final String REGION_TAG = "RegionTag";
    private static final String COUNTRY_TAG = "CountryTag";
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

    private final long lawGroupTag;
    private final long provinceTag;
    private final long seaProvinceTag;
    private final long regionTag;
    private final long countryTag;
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

    public EcsConstants(World ecsWorld) {
        this.lawGroupTag = ecsWorld.entity(LAW_GROUP_TAG);
        this.provinceTag = ecsWorld.entity(PROVINCE_TAG);
        this.seaProvinceTag = ecsWorld.entity(SEA_PROVINCE_TAG);
        ecsWorld.obtainEntity(this.seaProvinceTag).isA(this.provinceTag);
        this.regionTag = ecsWorld.entity(REGION_TAG);
        this.countryTag = ecsWorld.entity(COUNTRY_TAG);
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
    }

    public long lawGroupTag() {
        return this.lawGroupTag;
    }

    public long provinceTag() {
        return this.provinceTag;
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
