package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.catalog.NamedTable;
import com.populaire.projetguerrefroide.protocol.response.PoliticalMap;
import com.populaire.projetguerrefroide.util.LocalisationUtils;

import java.util.Arrays;
import java.util.Map;

public final class UiCatalog {
    private static final String COLONY_FLAG_INFIX = "_COL_";
    private final Catalog catalog;
    private final Map<String, String> localisation;
    private final Skin skinFlags;
    private final int[] colonizers;
    private final String[] countryNames;
    private final TextureRegion[] flags;
    private final Drawable[] flagDrawables;
    private final TextureRegion[] plainFlags;

    public UiCatalog(Catalog catalog, Map<String, String> localisation, Skin skinFlags) {
        this.catalog = catalog;
        this.localisation = localisation;
        this.skinFlags = skinFlags;
        int countryCount = catalog.countries().size();
        this.colonizers = new int[countryCount];
        Arrays.fill(this.colonizers, -2);
        this.countryNames = new String[countryCount];
        this.flags = new TextureRegion[countryCount];
        this.flagDrawables = new Drawable[countryCount];
        this.plainFlags = new TextureRegion[countryCount];
        for (int countryIndex = 0; countryIndex < countryCount; countryIndex++) {
            String nameId = catalog.countries().nameId(countryIndex);
            this.plainFlags[countryIndex] = skinFlags.has(nameId, TextureRegion.class) ? skinFlags.getRegion(nameId) : null;
            this.updateCountry(countryIndex, -1);
        }
    }

    public Catalog catalog() {
        return this.catalog;
    }

    public Map<String, String> localisation() {
        return this.localisation;
    }

    public boolean applyPoliticalMap(PoliticalMap politicalMap) {
        boolean changed = false;
        for (int countryIndex = 0; countryIndex < this.colonizers.length; countryIndex++) {
            int colonizer = politicalMap.colonizer(countryIndex);
            if (colonizer != this.colonizers[countryIndex]) {
                this.updateCountry(countryIndex, colonizer);
                changed = true;
            }
        }
        return changed;
    }

    private void updateCountry(int countryIndex, int colonizerIndex) {
        this.colonizers[countryIndex] = colonizerIndex;
        NamedTable countries = this.catalog.countries();
        String nameId = countries.nameId(countryIndex);
        String colonizerNameId = colonizerIndex >= 0 ? countries.nameId(colonizerIndex) : null;
        this.countryNames[countryIndex] = LocalisationUtils.getCountryNameLocalisation(this.localisation, nameId, colonizerNameId);
        TextureRegion flag;
        if (colonizerNameId == null) {
            flag = this.plainFlags[countryIndex];
        } else if (this.skinFlags.has(nameId + COLONY_FLAG_INFIX + colonizerNameId, TextureRegion.class)) {
            flag = this.skinFlags.getRegion(nameId + COLONY_FLAG_INFIX + colonizerNameId);
        } else {
            flag = this.plainFlags[colonizerIndex];
        }
        this.flags[countryIndex] = flag;
        this.flagDrawables[countryIndex] = flag != null ? new TextureRegionDrawable(flag) : null;
    }

    public String countryName(int countryIndex) {
        return countryIndex >= 0 ? this.countryNames[countryIndex] : null;
    }

    public int colonizer(int countryIndex) {
        return countryIndex >= 0 ? this.colonizers[countryIndex] : -1;
    }

    public TextureRegion flag(int countryIndex) {
        return countryIndex >= 0 ? this.flags[countryIndex] : null;
    }

    public Drawable flagDrawable(int countryIndex) {
        return countryIndex >= 0 ? this.flagDrawables[countryIndex] : null;
    }

    public TextureRegion plainFlag(int countryIndex) {
        return countryIndex >= 0 ? this.plainFlags[countryIndex] : null;
    }

    public String localized(String nameId) {
        return nameId == null ? null : this.localisation.get(nameId);
    }

    public String provinceName(int provinceIndex) {
        return provinceIndex >= 0 ? this.localisation.get(this.catalog.provinces().nameId(provinceIndex)) : null;
    }

    public String regionName(int regionIndex) {
        return regionIndex >= 0 ? this.localisation.get(this.catalog.regions().nameId(regionIndex)) : null;
    }

    public String buildingTypeName(int buildingTypeIndex) {
        return buildingTypeIndex >= 0 ? this.localisation.get(this.catalog.buildingTypes().nameId(buildingTypeIndex)) : null;
    }
}
