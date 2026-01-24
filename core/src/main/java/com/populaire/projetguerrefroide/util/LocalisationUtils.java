package com.populaire.projetguerrefroide.util;

import java.util.Map;

public class LocalisationUtils {

    public static String getCountryNameLocalisation(Map<String, String> localisation, String countryNameId, String colonizerNameId) {
        if (localisation == null || countryNameId == null) {
            throw new IllegalArgumentException("localisation and countryNameId can't be null");
        }

        if (colonizerNameId != null) {
            String countryName = localisation.get(countryNameId + "_COL");
            if (countryName != null) {
                return countryName;
            }
        }

        return localisation.get(countryNameId);
    }

}
