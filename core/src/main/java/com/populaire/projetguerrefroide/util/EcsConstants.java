package com.populaire.projetguerrefroide.util;

import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.util.FlecsConstants;

public class EcsConstants {
    private static final String ALIGNED_WITH = "AlignedWith";
    private static final String LOCATED_IN_REGION = "LocatedInRegion";
    private static final String LOCATED_IN_CONTINENT = "LocatedInContinent";
    private static final String LOCATED_IN_PROVINCE = "LocatedInProvince";
    private static final String ACCEPTANCE = "Acceptance";
    private static final String HAS = "Has";
    private static final String BELONGS_TO = "BelongsTo";
    private static final String SUPPORTED_BY = "SupportedBy";
    private static final String OPPOSED_BY = "OpposedBy";
    private static final String LAW_GROUP_TAG = "LawGroupTag";
    private static final String PROVINCE_TAG = "ProvinceTag";
    private static final String LAND_PROVINCE_TAG = "LandProvinceTag";
    private static final String SEA_PROVINCE_TAG = "SeaProvinceTag";
    private static final String REGION_TAG = "RegionTag";
    private static final String COUNTRY_TAG = "CountryTag";
    private static final String CONTINENT_TAG = "ContinentTag";
    private static final String ADJACENT_TO = "AdjacentTo";
    private static final String CONTROLLED_BY = "ControlledBy";
    private static final String OWNED_BY = "OwnedBy";
    private static final String CORE_OF = "CoreOf";
    private static final String POSITION_ELEMENT_TAG = "PositionElementTag";
    private static final String HAS_TERRAIN = "HasTerrain";
    private static final String ALLIED_WITH = "AlliedWith";
    private static final String GUARANTEES = "Guarantees";
    private static final String IS_GUARANTEED_BY = "IsGuaranteedBy";
    private static final String IS_PUPPET_MASTER_OF = "IsPuppetMasterOf";
    private static final String IS_PUPPET_OF = "IsPuppetOf";
    private static final String COLONIZES = "Colonizes";
    private static final String IS_COLONY_OF = "IsColonyOf";
    private static final String HAS_CAPITAL = "HasCapital";
    private static final String HAS_GOVERNMENT = "HasGovernment";
    private static final String HAS_IDENTITY = "HasIdentity";
    private static final String HAS_ATTITUDE = "HasAttitude";
    private static final String HAS_IDEOLOGY = "HasIdeology";
    public static final String HEAD_OF_STATE = "HeadOfState";
    public static final String HEAD_OF_GOVERNMENT = "HeadOfGovernment";

    private long alignedWith = -1;
    private long locatedInRegion = -1;
    private long locatedInContinent = -1;
    private long locatedInProvince = -1;
    private long acceptance = -1;
    private long has = -1;
    private long belongsTo = -1;
    private long supportedBy = -1;
    private long opposedBy = -1;
    private long lawGroupTag = -1;
    private long provinceTag = -1;
    private long landProvinceTag = -1;
    private long seaProvinceTag = -1;
    private long regionTag = -1;
    private long countryTag = -1;
    private long continentTag = -1;
    private long adjacentTo = -1;
    private long controlledBy = -1;
    private long ownedBy = -1;
    private long coreOf = -1;
    private long positionElementTag = -1;
    private long hasTerrain = -1;
    private long alliedWith = -1;
    private long guarantees = -1;
    private long isGuaranteedBy = -1;
    private long isPuppetMasterOf = -1;
    private long isPuppetOf = -1;
    private long colonizes = -1;
    private long isColonyOf = -1;
    private long hasCapital = -1;
    private long hasGovernment = -1;
    private long hasIdentity = -1;
    private long hasAttitude = -1;
    private long hasIdeology = -1;
    private long headOfState = -1;
    private long headOfGovernment = -1;

    private final World ecsWorld;

    public EcsConstants(World ecsWorld) {
        this.ecsWorld = ecsWorld;
    }

    public long alignedWith() {
        if(alignedWith == -1) {
            this.alignedWith = this.ecsWorld.entity(ALIGNED_WITH);
            this.ecsWorld.obtainEntity(this.alignedWith).add(FlecsConstants.EcsExclusive);
        }

        return this.alignedWith;
    }

    public long locatedInRegion() {
        if(locatedInRegion == -1) {
            this.locatedInRegion = this.ecsWorld.entity(LOCATED_IN_REGION);
            this.ecsWorld.obtainEntity(this.locatedInRegion).add(FlecsConstants.EcsExclusive);
        }

        return this.locatedInRegion;
    }

    public long locatedInContinent() {
        if(locatedInContinent == -1) {
            this.locatedInContinent = this.ecsWorld.entity(LOCATED_IN_CONTINENT);
            this.ecsWorld.obtainEntity(this.locatedInContinent).add(FlecsConstants.EcsExclusive);
        }

        return this.locatedInContinent;
    }

    public long locatedInProvince() {
        if(locatedInProvince == -1) {
            this.locatedInProvince = this.ecsWorld.entity(LOCATED_IN_PROVINCE);
            this.ecsWorld.obtainEntity(this.locatedInProvince).add(FlecsConstants.EcsExclusive);
        }

        return this.locatedInProvince;
    }

    public long acceptance() {
        if(acceptance == -1) {
            this.acceptance = this.ecsWorld.entity(ACCEPTANCE);
        }

        return this.acceptance;
    }

    public long has() {
        if(this.has == -1) {
            this.has = this.ecsWorld.entity(HAS);
        }

        return this.has;
    }

    public long belongsTo() {
        if(this.belongsTo == -1) {
            this.belongsTo = this.ecsWorld.entity(BELONGS_TO);
        }

        return this.belongsTo;
    }

    public long supportedBy() {
        if(this.supportedBy == -1) {
            this.supportedBy = this.ecsWorld.entity(SUPPORTED_BY);
        }

        return this.supportedBy;
    }

    public long opposedBy() {
        if(this.opposedBy == -1) {
            this.opposedBy = this.ecsWorld.entity(OPPOSED_BY);
        }

        return this.opposedBy;
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

    public long landProvinceTag() {
        if(this.landProvinceTag == -1) {
            this.landProvinceTag = this.ecsWorld.entity(LAND_PROVINCE_TAG);
            this.ecsWorld.obtainEntity(this.landProvinceTag).isA(this.provinceTag());
        }

        return this.landProvinceTag;
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

    public long continentTag() {
        if(this.continentTag == -1) {
            this.continentTag = this.ecsWorld.entity(CONTINENT_TAG);
        }

        return this.continentTag;
    }

    public long adjacentTo() {
        if(this.adjacentTo == -1) {
            this.adjacentTo = this.ecsWorld.entity(ADJACENT_TO);
        }

        return this.adjacentTo;
    }

    public long controlledBy() {
        if(this.controlledBy == -1) {
            this.controlledBy = this.ecsWorld.entity(CONTROLLED_BY);
        }

        return this.controlledBy;
    }

    public long ownedBy() {
        if(this.ownedBy == -1) {
            this.ownedBy = this.ecsWorld.entity(OWNED_BY);
        }

        return this.ownedBy;
    }

    public long coreOf() {
        if(this.coreOf == -1) {
            this.coreOf = this.ecsWorld.entity(CORE_OF);
        }

        return this.coreOf;
    }

    public long positionElementTag() {
        if(this.positionElementTag == -1) {
            this.positionElementTag = this.ecsWorld.entity(POSITION_ELEMENT_TAG);
        }

        return this.positionElementTag;
    }

    public long hasTerrain() {
        if(this.hasTerrain == -1) {
            this.hasTerrain = this.ecsWorld.entity(HAS_TERRAIN);
        }

        return this.hasTerrain;
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

    public long hasCapital() {
        if(this.hasCapital == -1) {
            this.hasCapital = this.ecsWorld.entity(HAS_CAPITAL);
        }

        return this.hasCapital;
    }

    public long hasGovernment() {
        if(this.hasGovernment == -1) {
            this.hasGovernment = this.ecsWorld.entity(HAS_GOVERNMENT);
        }

        return this.hasGovernment;
    }

    public long hasIdentity() {
        if(this.hasIdentity == -1) {
            this.hasIdentity = this.ecsWorld.entity(HAS_IDENTITY);
        }

        return this.hasIdentity;
    }

    public long hasAttitude() {
        if(this.hasAttitude == -1) {
            this.hasAttitude = this.ecsWorld.entity(HAS_ATTITUDE);
        }

        return this.hasAttitude;
    }

    public long hasIdeology() {
        if(this.hasIdeology == -1) {
            this.hasIdeology = this.ecsWorld.entity(HAS_IDEOLOGY);
        }

        return this.hasIdeology;
    }

    public long headOfState() {
        if(this.headOfState == -1) {
            this.headOfState = this.ecsWorld.entity(HEAD_OF_STATE);
        }

        return this.headOfState;
    }

    public long headOfGovernment() {
        if(this.headOfGovernment == -1) {
            this.headOfGovernment = this.ecsWorld.entity(HEAD_OF_GOVERNMENT);
        }

        return this.headOfGovernment;
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
