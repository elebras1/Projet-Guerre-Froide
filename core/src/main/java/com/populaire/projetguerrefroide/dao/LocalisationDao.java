package com.populaire.projetguerrefroide.dao;

import java.util.Map;

public interface LocalisationDao {
    void setLanguage(String language);
    Map<String, String> readMainMenu();
    Map<String, String> readNewgame();
    Map<String, String> readBookmark();
    Map<String, String> readPolitics();
    Map<String, String> readMainMenuInGame();
    Map<String, String> readPopup();
    Map<String, String> readLanguage();
    Map<String, String> readCountries();
    Map<String, String> readProvinces();
    Map<String, String> readRegions();
    Map<String, String> readInterface();
    Map<String , String> readEconomy();
}
