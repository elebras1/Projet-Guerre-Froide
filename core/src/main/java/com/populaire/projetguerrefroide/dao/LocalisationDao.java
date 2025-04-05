package com.populaire.projetguerrefroide.dao;

import com.badlogic.gdx.Gdx;
import com.github.tommyettinger.ds.ObjectObjectMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class LocalisationDao {
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
    private String language = "ENGLISH";

    public void setLanguage(String language) {
        this.language = language;
    }

    public Map<String, String> readMainMenuCsv() {
        return this.readLocalisationCsv(this.language, this.mainmenuCsvFile);
    }

    public Map<String, String> readNewgameCsv() {
        return this.readLocalisationCsv(this.language, this.newgameCsvFile);
    }

    public Map<String, String> readBookmarkCsv() {
        return this.readLocalisationCsv(this.language, this.bookmarkCsvFile);
    }

    public Map<String, String> readPoliticsCsv() {
        return this.readLocalisationCsv(this.language, this.politicsCsvFile);
    }

    public Map<String, String> readMainMenuInGameCsv() {
        return this.readLocalisationCsv(this.language, this.mainemenuInGameCsvFile);
    }

    public Map<String, String> readPopupCsv() {
        return this.readLocalisationCsv(this.language, this.popupCsvFile);
    }

    public Map<String, String> readLanguageCsv() {
        return this.readLocalisationCsv(this.language, this.languageCsvFile);
    }

    public Map<String, String> readCountriesCsv() {
        return this.readLocalisationCsv(this.language, this.countriesCsvFile);
    }

    public Map<String, String> readProvincesCsv() {
        return this.readLocalisationCsv("ENGLISH", this.provincesCsvFile);
    }

    public Map<String, String> readRegionsCsv() {
        return this.readLocalisationCsv("ENGLISH", this.regionsCsvFile);
    }

    public Map<String, String> readInterfaceCsv() {
        return this.readLocalisationCsv(this.language, this.interfaceFile);
    }

    private Map<String, String> readLocalisationCsv(String language, String filename) {
        Map<String, String> localisation = new ObjectObjectMap<>();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(
                Gdx.files.internal(filename).read(), StandardCharsets.UTF_8))) {
            String[] headers = br.readLine().split(";");
            int localisationIndex = Arrays.asList(headers).indexOf(language);
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
