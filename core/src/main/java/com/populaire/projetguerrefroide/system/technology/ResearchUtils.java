package com.populaire.projetguerrefroide.system.technology;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public final class ResearchUtils {

    private ResearchUtils() {
    }

    public static float outputPosition(double outputRatio) {
        if (outputRatio <= 0.0) {
            return 0f;
        }
        double position = Math.log(outputRatio / DEVELOPMENT_OUTPUT_RATIO_LOW) / Math.log(DEVELOPMENT_OUTPUT_RATIO_HIGH / DEVELOPMENT_OUTPUT_RATIO_LOW);
        return (float) Math.clamp(position, 0.0, 1.0);
    }

    public static float developmentIndex(float outputPosition, float literacy) {
        return (outputPosition + literacy) / 2f;
    }

    public static float researchFactor(float index) {
        return (float) (DEVELOPMENT_FACTOR_LOWEST_INDEX * Math.pow(DEVELOPMENT_FACTOR_HIGHEST_INDEX / DEVELOPMENT_FACTOR_LOWEST_INDEX, index));
    }

    public static float dateFactor(int referenceYear, float year) {
        return (float) Math.max(TECHNOLOGY_DATE_FLOOR, Math.pow(2.0, (referenceYear - year) / TECHNOLOGY_DATE_DOUBLING_YEARS));
    }

    public static float effectiveDays(int baseDays, int referenceYear, float year, float researchFactor, float researchEfficiency) {
        return baseDays * dateFactor(referenceYear, year) * researchFactor / (1f + researchEfficiency);
    }
}
