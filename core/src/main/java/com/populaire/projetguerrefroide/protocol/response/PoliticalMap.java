package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class PoliticalMap extends Response {
    private int[] owners = new int[0];
    private int[] controllers = new int[0];
    private int[] colonizers = new int[0];
    private int[] capitals = new int[0];
    private boolean[] withProvinces = new boolean[0];

    public void clear(int provinceCount, int countryCount) {
        this.owners = Columns.fit(this.owners, provinceCount);
        this.controllers = Columns.fit(this.controllers, provinceCount);
        this.colonizers = Columns.fit(this.colonizers, countryCount);
        this.capitals = Columns.fit(this.capitals, countryCount);
        this.withProvinces = Columns.fit(this.withProvinces, countryCount);
        for (int i = 0; i < countryCount; i++) {
            this.colonizers[i] = -1;
            this.capitals[i] = -1;
            this.withProvinces[i] = false;
        }
        for (int i = 0; i < provinceCount; i++) {
            this.owners[i] = -1;
            this.controllers[i] = -1;
        }
    }

    public void setProvince(int provinceIndex, int ownerIndex, int controllerIndex) {
        this.owners[provinceIndex] = ownerIndex;
        this.controllers[provinceIndex] = controllerIndex;
        if (ownerIndex >= 0) {
            this.withProvinces[ownerIndex] = true;
        }
    }

    public void setCountry(int countryIndex, int colonizerIndex, int capitalProvinceIndex) {
        this.colonizers[countryIndex] = colonizerIndex;
        this.capitals[countryIndex] = capitalProvinceIndex;
    }

    public int owner(int provinceIndex) {
        return this.owners[provinceIndex];
    }

    public int controller(int provinceIndex) {
        return this.controllers[provinceIndex];
    }

    public int colonizer(int countryIndex) {
        return this.colonizers[countryIndex];
    }

    public int capital(int countryIndex) {
        return this.capitals[countryIndex];
    }

    public boolean hasProvinces(int countryIndex) {
        return this.withProvinces[countryIndex];
    }
}
