package com.populaire.projetguerrefroide.util;

import java.util.Map;

public class LocalisationUtils {

    public static String getCountryNameLocalisation(Map<String, String> localisation, String countryId, String colonizerId) {
        if (localisation == null || countryId == null) {
            throw new IllegalArgumentException("localisation and countryId can't be null");
        }

        if (colonizerId != null) {
            String countryName = localisation.get(countryId + "_COL");
            if (countryName != null) {
                return countryName;
            }
        }

        return localisation.get(countryId);
    }

}
