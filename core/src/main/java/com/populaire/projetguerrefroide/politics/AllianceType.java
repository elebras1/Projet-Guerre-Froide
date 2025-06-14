package com.populaire.projetguerrefroide.politics;

public enum AllianceType {
    STANDARD,
    GUARANTOR,
    GUARANTEED_NATION,
    PUPPET_MASTER,
    PUPPET;

    public static AllianceType getAllianceType(String type, boolean isFirstCountry) {
        return switch (type) {
            case "standard" -> AllianceType.STANDARD;
            case "guarantee" -> isFirstCountry ? AllianceType.GUARANTOR : AllianceType.GUARANTEED_NATION;
            case "puppet_state" -> isFirstCountry ? AllianceType.PUPPET_MASTER : AllianceType.PUPPET;
            default -> throw new IllegalArgumentException("Invalid alliance type: " + type);
        };
    }
}
