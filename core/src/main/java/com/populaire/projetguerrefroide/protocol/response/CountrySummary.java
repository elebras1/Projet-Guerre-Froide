package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class CountrySummary extends Response {
    private int countryIndex;
    private int population;
    private int governmentIndex;
    private int headOfStateIndex;
    private int colonizerIndex;
    private int[] allies = new int[8];
    private int allyCount;

    public void set(int countryIndex, int population, int governmentIndex, int headOfStateIndex, int colonizerIndex) {
        this.countryIndex = countryIndex;
        this.population = population;
        this.governmentIndex = governmentIndex;
        this.headOfStateIndex = headOfStateIndex;
        this.colonizerIndex = colonizerIndex;
    }

    public void clearAllies() {
        this.allyCount = 0;
    }

    public void addAlly(int countryIndex) {
        for (int i = 0; i < this.allyCount; i++) {
            if (this.allies[i] == countryIndex) {
                return;
            }
        }
        this.allies = Columns.fit(this.allies, this.allyCount + 1);
        this.allies[this.allyCount++] = countryIndex;
    }

    public int countryIndex() {
        return this.countryIndex;
    }

    public int population() {
        return this.population;
    }

    public int governmentIndex() {
        return this.governmentIndex;
    }

    public int headOfStateIndex() {
        return this.headOfStateIndex;
    }

    public int colonizerIndex() {
        return this.colonizerIndex;
    }

    public int allyCount() {
        return this.allyCount;
    }

    public int ally(int position) {
        return this.allies[position];
    }
}
