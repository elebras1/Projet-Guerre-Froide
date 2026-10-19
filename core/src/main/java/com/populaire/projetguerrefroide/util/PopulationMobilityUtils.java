package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.DAYS_PER_MONTH;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.PROMOTION_RATE;

public class PopulationMobilityUtils {

    public static boolean isMobilityDay(World world) {
        EconomyCalendarView calendar = world.obtainEntityView(world.lookup("economy_calendar")).getView(EconomyCalendar.class);
        return (calendar.day() + 1) % DAYS_PER_MONTH == 0;
    }

    public static float[] normalizedWeights(PopulationTypeView populationType, boolean promotion) {
        float[] weights = new float[POP_TYPE_COUNT];
        float total = 0f;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            if (p != populationType.index()) {
                weights[p] = promotion ? populationType.promotionWeights(p) : populationType.demotionWeights(p);
                total += weights[p];
            }
        }
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            weights[p] = total > 0f ? weights[p] / total : 0f;
        }
        return weights;
    }

    public static boolean hasWeights(float[] promotionWeights, float[] demotionWeights) {
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            if (promotionWeights[p] > 0f || demotionWeights[p] > 0f) {
                return true;
            }
        }
        return false;
    }

    public static boolean[] popTypesWith(World world, long tagId) {
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(world.lookup("global_population_type")).getView(GlobalPopulationType.class);
        boolean[] popTypes = new boolean[POP_TYPE_COUNT];
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            popTypes[p] = world.obtainEntityView(globalPopulationType.popTypeIds(p)).has(tagId);
        }
        return popTypes;
    }

    public static void shares(PopulationView population, float classRigidity, float[] promotionWeights, float[] demotionWeights,
                              boolean[] employeeTypes, boolean[] ruralTypes, int rgoWorkerIndex, DemographicsView demographics,
                              float[] vacancyShares, float[] openShares) {
        int amount = population.amount();
        float unemployedShare = Math.max(0f, (amount - population.employment()) / (float) amount);
        float openness = Math.max(0f, 1f - classRigidity);
        float ambition = PROMOTION_RATE * Math.clamp(population.everydayNeedsSatisfaction(), 0f, 1f) * openness;
        float jobSearch = PROMOTION_RATE * unemployedShare * openness;
        float demotion = PROMOTION_RATE * Math.min(1f, unemployedShare + Math.max(0f, 1f - population.lifeNeedsSatisfaction()));
        float sourceIncome = population.income() / amount;
        for (int b = 0; b < POP_TYPE_COUNT; b++) {
            float demotionPart = demotion * demotionWeights[b];
            vacancyShares[b] = 0f;
            openShares[b] = 0f;
            if (employeeTypes[b]) {
                vacancyShares[b] = (ambition + jobSearch) * promotionWeights[b] + demotionPart;
            } else if (ruralTypes[b]) {
                vacancyShares[b] = b == rgoWorkerIndex ? (ambition + jobSearch) * promotionWeights[b] : 0f;
                openShares[b] = demotionPart;
            } else {
                int targetTotal = demographics.totalByPopType(b);
                float attraction = targetTotal > 0 && sourceIncome > 0f
                    ? Math.clamp(demographics.incomeByPopType(b) / targetTotal / sourceIncome - 1f, 0f, 1f)
                    : 0f;
                openShares[b] = (ambition * promotionWeights[b] + demotionPart) * attraction;
            }
        }
    }
}
