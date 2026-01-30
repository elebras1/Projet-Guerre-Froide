package com.populaire.projetguerrefroide.dao;

import java.util.Map;

public interface LocalisationDao {
    void setLanguage(String language);
    Map<String, String> readMainMenuCsv();
    Map<String, String> readNewgameCsv();
    Map<String, String> readBookmarkCsv();
    Map<String, String> readPoliticsCsv();
    Map<String, String> readMainMenuInGameCsv();
    Map<String, String> readPopupCsv();
    Map<String, String> readLanguageCsv();
    Map<String, String> readCountriesCsv();
    Map<String, String> readProvincesCsv();
    Map<String, String> readRegionsCsv();
    Map<String, String> readInterfaceCsv();
    Map<String , String> readEconomyCsv();
}
