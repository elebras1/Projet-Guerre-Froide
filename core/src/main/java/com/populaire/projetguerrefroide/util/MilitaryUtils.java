package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.INSTRUCTORS_PER_OFFICER;
import static com.populaire.projetguerrefroide.util.Constants.UNTRAINED_ORGANISATION_SHARE;

public class MilitaryUtils {

    public static long countryId(World world, UnitInstanceView unitInstance, AirWingView airWing) {
        if (airWing != null) {
            return airWing.countryId();
        }
        FormationView formation = world.obtainEntityView(unitInstance.formationId()).getView(Formation.class);
        return formation.countryId();
    }

    public static float baseCoverage(CountryMilitaryView countryMilitary, long forceTypeTagId, EcsConstants ecsConstants) {
        if (forceTypeTagId == ecsConstants.seaForceTag()) {
            return coverage(countryMilitary.navalCapacity(), countryMilitary.navalLoad());
        }
        if (forceTypeTagId == ecsConstants.airForceTag()) {
            return coverage(countryMilitary.airCapacity(), countryMilitary.airLoad());
        }
        return 1f;
    }

    public static float coverage(float capacity, float load) {
        return load > 0f ? Math.min(1f, capacity / load) : 1f;
    }

    public static float fillRatio(CountryMilitaryView countryMilitary) {
        float posts = -countryMilitary.academyPosts();
        float filled = -countryMilitary.academyFilled();
        for (int p = 0; p < countryMilitary.postsLength(); p++) {
            posts += countryMilitary.posts(p);
            filled += countryMilitary.filledPosts(p);
        }
        return posts > 0f ? Math.clamp(filled / posts, 0f, 1f) : 1f;
    }

    public static float trainingCoverage(CountryMilitaryView countryMilitary, int staffPopTypeIndex) {
        if (staffPopTypeIndex < 0) {
            return 1f;
        }
        float unitPosts = countryMilitary.posts(staffPopTypeIndex) - countryMilitary.academyPosts();
        if (unitPosts <= 0f) {
            return 1f;
        }
        return Math.min(1f, countryMilitary.instructors() / (unitPosts * INSTRUCTORS_PER_OFFICER));
    }

    public static float trainedOrganisationShare(float trainingCoverage) {
        return UNTRAINED_ORGANISATION_SHARE + (1f - UNTRAINED_ORGANISATION_SHARE) * trainingCoverage;
    }
}
