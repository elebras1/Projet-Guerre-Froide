package com.populaire.projetguerrefroide.system.technology;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public enum PolicyModifier {
    POOR_TAX(TAX_POOR),
    MIDDLE_TAX(TAX_MIDDLE),
    RICH_TAX(TAX_RICH),
    RESOURCE_TAX_RATE(RESOURCE_TAX),
    PROFIT_TAX_RATE(PROFIT_TAX),
    TARIFF(TARIFF_RATE),
    MILITARY(MILITARY_SPENDING),
    EDUCATION(EDUCATION_SPENDING),
    ADMINISTRATION(ADMINISTRATION_SPENDING),
    SOCIAL(SOCIAL_SPENDING),
    CAPITALIST_SHARE(CAPITALIST_PROFIT_SHARE),
    WORKER_SHARE(WORKER_PROFIT_SHARE),
    ARISTOCRAT_SHARE(ARISTOCRAT_PROFIT_SHARE),
    STATE_SHARE(STATE_PROFIT_SHARE),
    MIN_WAGE(MIN_WAGE_FACTOR),
    FACTORY_INPUT(FACTORY_INPUT_MODIFIER),
    FACTORY_OUTPUT(FACTORY_OUTPUT_MODIFIER),
    RGO_OUTPUT(RGO_OUTPUT_MODIFIER),
    CONSTRUCTION(CONSTRUCTION_SPEED),
    STATE_BUILDING(STATE_CONSTRUCTION),
    POP_GROWTH(POP_GROWTH_FACTOR),
    EDUCATION_EFFICIENCY_RATE(EDUCATION_EFFICIENCY),
    RESEARCH_EFFICIENCY_RATE(RESEARCH_EFFICIENCY),
    MIGRATION_PULL_RATE(MIGRATION_PULL),
    POP_SPENDING_RATE(POP_SPENDING),
    CONSCIOUSNESS(POLITICAL_CONSCIOUSNESS),
    RADICALISM(POLITICAL_RADICALISM),
    SUPPRESSION_RATE(SUPPRESSION),
    MOBILITY(SOCIAL_MOBILITY),
    RIGIDITY(CLASS_RIGIDITY),
    ADMINISTRATIVE(ADMINISTRATIVE_EFFICIENCY),
    CONVERSION(RELIGIOUS_CONVERSION_SPEED),
    SECULARISM_RATE(SECULARISM),
    ASSIMILATION(ASSIMILATION_RATE),
    MIGRATION_PUSH_RATE(MIGRATION_PUSH),
    DONATIONS(RELIGIOUS_DONATIONS),
    CLERGY_FUNDING(CLERGY_STATE_FUNDING),
    WORKFORCE(WORKFORCE_SHARE),
    SUBSIDIES(INDUSTRY_SUBSIDIES),
    HEALTH(PUBLIC_HEALTH),
    WORKS(PUBLIC_WORKS),
    PENSIONS(PENSION);

    private static final PolicyModifier[] VALUES = values();
    private static final String[] READ_LATER = {NAVAL_CAPACITY, AIR_CAPACITY, MILITARY_TRAINING, ESPIONAGE_BONUS};

    private final String key;

    PolicyModifier(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }

    public static PolicyModifier of(int ordinal) {
        return VALUES[ordinal];
    }

    /**
     * Policy modifier carried by a technology, or null for the names listed as read later (military, espionage).
     * An unknown name fails the loading.
     */
    public static PolicyModifier forTechnology(String name) {
        for (PolicyModifier modifier : VALUES) {
            if (modifier.key.equals(name)) {
                return modifier;
            }
        }
        for (String readLater : READ_LATER) {
            if (readLater.equals(name)) {
                return null;
            }
        }
        throw new IllegalArgumentException("Unknown technology modifier: " + name);
    }
}
