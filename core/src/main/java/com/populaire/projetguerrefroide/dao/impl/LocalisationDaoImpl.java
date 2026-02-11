package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.populaire.projetguerrefroide.dao.LocalisationDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LocalisationDaoImpl implements LocalisationDao {
    private final String localisationPath = "localisation/";
    private final String provincesCsvFile = this.localisationPath + "provinces.csv";
    private final String countriesCsvFile = this.localisationPath + "countries.csv";
    private final String mainmenuCsvFile = this.localisationPath + "mainmenu.csv";
    private final String mainemenuInGameCsvFile = this.localisationPath + "mainmenu_ig.csv";
    private final String newgameCsvFile = this.localisationPath + "newgame.csv";
    private final String bookmarkCsvFile = this.localisationPath + "bookmark.csv";
    private final String politicsCsvFile = this.localisationPath + "politics.csv";
    private final String popupCsvFile = this.localisationPath + "popup.csv";
    private final String languageCsvFile = this.localisationPath + "language.csv";
    private final String interfaceFile = this.localisationPath + "interface.csv";
    private final String regionsCsvFile = this.localisationPath + "regions.csv";
    private final String economyCsvFile = this.localisationPath + "economy.csv";
    private String language = "ENGLISH";

    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public Map<String, String> readMainMenu() {
        return this.readLocalisationCsv(this.language, this.mainmenuCsvFile);
    }

    @Override
    public Map<String, String> readNewgame() {
        return this.readLocalisationCsv(this.language, this.newgameCsvFile);
    }

    @Override
    public Map<String, String> readBookmark() {
        return this.readLocalisationCsv(this.language, this.bookmarkCsvFile);
    }

    @Override
    public Map<String, String> readPolitics() {
        return this.readLocalisationCsv(this.language, this.politicsCsvFile);
    }

    @Override
    public Map<String, String> readMainMenuInGame() {
        return this.readLocalisationCsv(this.language, this.mainemenuInGameCsvFile);
    }

    @Override
    public Map<String, String> readPopup() {
        return this.readLocalisationCsv(this.language, this.popupCsvFile);
    }

    @Override
    public Map<String, String> readLanguage() {
        return this.readLocalisationCsv(this.language, this.languageCsvFile);
    }

    @Override
    public Map<String, String> readCountries() {
        return this.readLocalisationCsv(this.language, this.countriesCsvFile);
    }

    @Override
    public Map<String, String> readProvinces() {
        return this.readLocalisationCsv("ENGLISH", this.provincesCsvFile);
    }

    @Override
    public Map<String, String> readRegions() {
        return this.readLocalisationCsv(this.language, this.regionsCsvFile);
    }

    @Override
    public Map<String, String> readInterface() {
        return this.readLocalisationCsv(this.language, this.interfaceFile);
    }

    @Override
    public Map<String, String> readEconomy() {
        return this.readLocalisationCsv(this.language, this.economyCsvFile);
    }

    private Map<String, String> readLocalisationCsv(String language, String filename) {
        Map<String, String> localisation = new ObjectObjectMap<>();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(
                Gdx.files.internal(filename).read(), StandardCharsets.UTF_8))) {
            String[] headers = br.readLine().split(";");
            int localisationIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equals(language)) {
                    localisationIndex = i;
                    break;
                }
            }
            if (localisationIndex == -1) {
                throw new IllegalArgumentException("Localisation not found in CSV headers.");
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                String code = values[0];
                String translation = values[localisationIndex];
                localisation.put(code, translation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return localisation;
    }
}
