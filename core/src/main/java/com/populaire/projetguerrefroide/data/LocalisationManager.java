package com.populaire.projetguerrefroide.data;

import com.badlogic.gdx.Gdx;
import com.github.tommyettinger.ds.ObjectObjectMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class LocalisationManager {
    private final String localisationPath = "localisation/";
    private final String provinceNamesCsvFile = this.localisationPath + "province_names.csv";
    private final String mainmenuCsvFile = this.localisationPath + "mainmenu.csv";
    private final String mainemenuInGameCsvFile = this.localisationPath + "mainmenu_ig.csv";
    private final String newgameCsvFile = this.localisationPath + "newgame.csv";
    private final String bookmarkCsvFile = this.localisationPath + "bookmark.csv";
    private final String politicsCsvFile = this.localisationPath + "politics.csv";

    public Map<String, String> readMainMenuLocalisationCsv() {
        return readLocalisationCsv(this.mainmenuCsvFile);
    }

    public Map<String, String> readNewgameLocalisationCsv() {
        return readLocalisationCsv(this.newgameCsvFile);
    }

    public Map<String, String> readBookmarkLocalisationCsv() {
        return readLocalisationCsv(this.bookmarkCsvFile);
    }

    public Map<String, String> readPoliticsLocalisationCsv() {
        return readLocalisationCsv(this.politicsCsvFile);
    }

    public Map<String, String> readMainMenuInGameCsv() { return readLocalisationCsv(this.mainemenuInGameCsvFile); }

    private Map<String, String> readLocalisationCsv(String filename) {
        Map<String, String> localisation = new ObjectObjectMap<>();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(
                Gdx.files.internal(filename).read(), StandardCharsets.UTF_8))) {
            String[] headers = br.readLine().split(";");
            int localisationIndex = Arrays.asList(headers).indexOf("ENGLISH");
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
