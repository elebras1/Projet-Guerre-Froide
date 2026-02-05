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

    private long lawGroupTag = -1;
    private long provinceTag = -1;
    private long seaProvinceTag = -1;
    private long regionTag = -1;
    private long countryTag = -1;
    private long alliedWith = -1;
    private long guarantees = -1;
    private long isGuaranteedBy = -1;
    private long isPuppetMasterOf = -1;
    private long isPuppetOf = -1;
    private long colonizes = -1;
    private long isColonyOf = -1;
    private long ressourceGoodTag = -1;
    private long advancedGoodTag = -1;
    private long militaryGoodTag = -1;
    private long onMap = -1;
    private long suspended = -1;

    private final World ecsWorld;

    public EcsConstants(World ecsWorld) {
        this.ecsWorld = ecsWorld;
    }

    public long lawGroupTag() {
        if(this.lawGroupTag == -1) {
            this.lawGroupTag = this.ecsWorld.entity(LAW_GROUP_TAG);
        }

        return this.lawGroupTag;
    }

    public long provinceTag() {
        if(this.provinceTag == -1) {
            this.provinceTag = this.ecsWorld.entity(PROVINCE_TAG);
        }

        return this.provinceTag;
    }

    public long seaProvinceTag() {
        if(this.seaProvinceTag == -1) {
            this.seaProvinceTag = this.ecsWorld.entity(SEA_PROVINCE_TAG);
            this.ecsWorld.obtainEntity(this.seaProvinceTag).isA(this.provinceTag());
        }

        return this.seaProvinceTag;
    }

    public long regionTag() {
        if(this.regionTag == -1) {
            this.regionTag = this.ecsWorld.entity(REGION_TAG);
        }

        return this.regionTag;
    }

    public long countryTag() {
        if(this.countryTag == -1) {
            this.countryTag = this.ecsWorld.entity(COUNTRY_TAG);
        }

        return this.countryTag;
    }

    public long alliedWith() {
        if(this.alliedWith == -1) {
            this.alliedWith = this.ecsWorld.entity(ALLIED_WITH);
        }

        return this.alliedWith;
    }

    public long guarantees() {
        if(this.guarantees == -1) {
            this.guarantees = this.ecsWorld.entity(GUARANTEES);
        }

        return this.guarantees;
    }

    public long isGuaranteedBy() {
        if(this.isGuaranteedBy == -1) {
            this.isGuaranteedBy = this.ecsWorld.entity(IS_GUARANTEED_BY);
        }

        return this.isGuaranteedBy;
    }

    public long isPuppetMasterOf() {
        if(this.isPuppetMasterOf == -1) {
            this.isPuppetMasterOf = this.ecsWorld.entity(IS_PUPPET_MASTER_OF);
        }

        return this.isPuppetMasterOf;
    }

    public long isPuppetOf() {
        if(this.isPuppetOf == -1) {
            this.isPuppetOf = this.ecsWorld.entity(IS_PUPPET_OF);
        }

        return this.isPuppetOf;
    }

    public long colonizes() {
        if(this.colonizes == -1) {
            this.colonizes = this.ecsWorld.entity(COLONIZES);
        }

        return this.colonizes;
    }

    public long isColonyOf() {
        if(this.isColonyOf == -1) {
            this.isColonyOf = this.ecsWorld.entity(IS_COLONY_OF);
        }

        return this.isColonyOf;
    }

    public long ressourceGoodTag() {
        if(this.ressourceGoodTag == -1) {
            this.ressourceGoodTag = this.ecsWorld.entity(RESSOURCE_GOOD_TAG);
        }

        return this.ressourceGoodTag;
    }

    public long advancedGoodTag() {
        if(this.advancedGoodTag == -1) {
            this.advancedGoodTag = this.ecsWorld.entity(ADVANCED_GOOD_TAG);
        }

        return this.advancedGoodTag;
    }

    public long militaryGoodTag() {
        if(this.militaryGoodTag == -1) {
            this.militaryGoodTag = this.ecsWorld.entity(MILITARY_GOOD_TAG);
        }

        return this.militaryGoodTag;
    }

    public long onMap() {
        if(this.onMap == -1) {
            this.onMap = this.ecsWorld.entity(ON_MAP);
        }

        return this.onMap;
    }

    public long suspended() {
        if(this.suspended == -1) {
            this.suspended = this.ecsWorld.entity(SUSPENDED);
        }

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
