package com.populaire.projetguerrefroide.protocol.catalog;

public final class Catalog {
    public static final int ECONOMY_BUILDING = 0;
    public static final int SPECIAL_BUILDING = 1;
    public static final int DEVELOPMENT_BUILDING = 2;
    private final ProvinceTable provinces;
    private final NamedTable regions;
    private final NamedTable countries;
    private final NamedTable goods;
    private final NamedTable buildingTypes;
    private final int[] buildingTypeKinds;
    private final NamedTable terrains;
    private final NamedTable cultures;
    private final NamedTable religions;
    private final NamedTable governments;
    private final NamedTable ministers;
    private final String[] ministerImages;
    private final int[] borderPixels;

    public Catalog(ProvinceTable provinces, NamedTable regions, NamedTable countries, NamedTable goods, NamedTable buildingTypes, int[] buildingTypeKinds, NamedTable terrains, NamedTable cultures, NamedTable religions, NamedTable governments, NamedTable ministers, String[] ministerImages, int[] borderPixels) {
        this.provinces = provinces;
        this.regions = regions;
        this.countries = countries;
        this.goods = goods;
        this.buildingTypes = buildingTypes;
        this.buildingTypeKinds = buildingTypeKinds;
        this.terrains = terrains;
        this.cultures = cultures;
        this.religions = religions;
        this.governments = governments;
        this.ministers = ministers;
        this.ministerImages = ministerImages;
        this.borderPixels = borderPixels;
    }

    public ProvinceTable provinces() {
        return this.provinces;
    }

    public NamedTable regions() {
        return this.regions;
    }

    public NamedTable countries() {
        return this.countries;
    }

    public NamedTable goods() {
        return this.goods;
    }

    public NamedTable buildingTypes() {
        return this.buildingTypes;
    }

    public int buildingTypeKind(int buildingTypeIndex) {
        return this.buildingTypeKinds[buildingTypeIndex];
    }

    public NamedTable terrains() {
        return this.terrains;
    }

    public NamedTable cultures() {
        return this.cultures;
    }

    public NamedTable religions() {
        return this.religions;
    }

    public NamedTable governments() {
        return this.governments;
    }

    public NamedTable ministers() {
        return this.ministers;
    }

    public String ministerImage(int ministerIndex) {
        return this.ministerImages[ministerIndex];
    }

    public int[] borderPixels() {
        return this.borderPixels;
    }
}
