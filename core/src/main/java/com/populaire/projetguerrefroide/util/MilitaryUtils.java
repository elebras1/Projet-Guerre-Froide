package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class MilitaryUtils {

    public static long countryId(World world, UnitInstanceView unitInstance, AirWingView airWing) {
        if (airWing != null) {
            return airWing.countryId();
        }
        FormationView formation = world.obtainEntityView(unitInstance.formationId()).getView(Formation.class);
        return formation.countryId();
    }

    public static float fillRatio(CountryMilitaryView countryMilitary) {
        float posts = 0f;
        float filled = 0f;
        for (int p = 0; p < countryMilitary.postsLength(); p++) {
            posts += countryMilitary.posts(p);
            filled += countryMilitary.filledPosts(p);
        }
        return posts > 0f ? Math.min(1f, filled / posts) : 1f;
    }
}
