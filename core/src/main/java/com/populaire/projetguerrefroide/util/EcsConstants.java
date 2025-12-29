package com.populaire.projetguerrefroide.util;

public class EcsConstants {
    public static final String EcsAlignedWith = "AlignedWith";
    public static final String EcsLocatedInRegion = "LocatedInRegion";
    public static final String EcsLocatedInContinent = "LocatedInContinent";
    public static final String EcsAcceptance = "Acceptance";
    public static final String EcsHas = "Has";
    public static final String EcsBelongsTo = "BelongsTo";
    public static final String EcsSupportedBy = "SupportedBy";
    public static final String EcsOpposedBy = "OpposedBy";
    public static final String EcsLawGroupTag = "LawGroupTag";
    public static final String EcsProvinceTag = "ProvinceTag";
    public static final String EcsLandProvinceTag = "LandProvinceTag";
    public static final String EcsSeaProvinceTag = "SeaProvinceTag";
    public static final String EcsRegionTag = "RegionTag";
    public static final String EcsCountryTag = "CountryTag";
    public static final String EcsContinentTag = "ContinentTag";
    public static final String EcsAdjacentTo = "AdjacentTo";
    public static final String EcsControlledBy = "ControlledBy";
    public static final String EcsOwnedBy = "OwnedBy";
    public static final String EcsCoreOf = "CoreOf";
    public static final String EcsPositionElementTag = "PositionElementTag";
    public static final String EcsHasTerrain = "HasTerrain";
    public static final String EcsAlliedWith = "AlliedWith";
    public static final String EcsGuarantees = "Guarantees";
    public static final String EcsIsGuaranteedBy = "IsGuaranteedBy";
    public static final String EcsIsPuppetMasterOf = "IsPuppetMasterOf";
    public static final String EcsIsPuppetOf = "IsPuppetOf";
    public static final String EcsColonizes = "Colonizes";
    public static final String EcsIsColonyOf = "IsColonyOf";


    public static String getAllianceRelation(String type, boolean isFirstCountry) {
        return switch (type) {
            case "standard" -> EcsAlliedWith;
            case "guarantee" -> isFirstCountry ? EcsGuarantees : EcsIsGuaranteedBy;
            case "puppet_state" -> isFirstCountry ? EcsIsPuppetMasterOf : EcsIsPuppetOf;
            case "colony" -> isFirstCountry ? EcsColonizes : EcsIsColonyOf;
            default -> throw new IllegalArgumentException("Invalid alliance type: " + type);
        };
    }

}
