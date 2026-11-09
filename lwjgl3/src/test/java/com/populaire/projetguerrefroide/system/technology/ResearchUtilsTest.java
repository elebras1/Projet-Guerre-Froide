package com.populaire.projetguerrefroide.system.technology;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResearchUtilsTest {
    private static final float TOLERANCE = 1e-4f;

    @Test
    public void dateFactorDoublesEveryThreeYearsAheadAndStopsAtTheFloor() {
        assertEquals(16f, ResearchUtils.dateFactor(1990, 1978f), TOLERANCE);
        assertEquals(4f, ResearchUtils.dateFactor(1990, 1984f), TOLERANCE);
        assertEquals(1f, ResearchUtils.dateFactor(1990, 1990f), TOLERANCE);
        assertEquals(0.5f, ResearchUtils.dateFactor(1990, 1993f), TOLERANCE);
        assertEquals(0.5f, ResearchUtils.dateFactor(1950, 2000f), TOLERANCE);
    }

    @Test
    public void outputPositionIsLogarithmicBetweenAQuarterAndFourTimesTheWorld() {
        assertEquals(0f, ResearchUtils.outputPosition(0.0), TOLERANCE);
        assertEquals(0f, ResearchUtils.outputPosition(0.1), TOLERANCE);
        assertEquals(0f, ResearchUtils.outputPosition(0.25), TOLERANCE);
        assertEquals(0.5f, ResearchUtils.outputPosition(1.0), TOLERANCE);
        assertEquals(1f, ResearchUtils.outputPosition(4.0), TOLERANCE);
        assertEquals(1f, ResearchUtils.outputPosition(10.0), TOLERANCE);
    }

    @Test
    public void developmentIndexIsTheMeanOfOutputAndLiteracy() {
        assertEquals(0.75f, ResearchUtils.developmentIndex(0.5f, 1f), TOLERANCE);
    }

    @Test
    public void researchFactorGoesFromThreeToOneHalf() {
        assertEquals(3f, ResearchUtils.researchFactor(0f), TOLERANCE);
        assertEquals(1f, ResearchUtils.researchFactor((float) (Math.log(3.0) / Math.log(6.0))), TOLERANCE);
        assertEquals(0.5f, ResearchUtils.researchFactor(1f), TOLERANCE);
    }

    @Test
    public void effectiveDaysDividesByResearchEfficiency() {
        assertEquals(660f * 16f * 0.5f, ResearchUtils.effectiveDays(660, 1990, 1978f, 0.5f, 0f), 0.01f);
        assertEquals(660f * 16f * 0.5f / 1.47f, ResearchUtils.effectiveDays(660, 1990, 1978f, 0.5f, 0.47f), 0.01f);
    }
}
