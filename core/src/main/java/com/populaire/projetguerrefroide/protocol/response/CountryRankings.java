package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class CountryRankings extends Response {
    private int[] countryIndexes = new int[32];
    private long[] grossDomesticProducts = new long[32];
    private long[] populations = new long[32];
    private long[] treasuries = new long[32];
    private long[] taxRevenues = new long[32];
    private long[] tariffRevenues = new long[32];
    private long[] profitRevenues = new long[32];
    private long[] militarySpendings = new long[32];
    private int[] provinceCounts = new int[32];
    private float[] developmentIndexes = new float[32];
    private float[] literacies = new float[32];
    private int[] units = new int[32];
    private int[] governmentIndexes = new int[32];
    private int[] headOfStateIndexes = new int[32];
    private int count;
    private long worldGrossDomesticProduct;
    private long worldPopulation;

    public void clear() {
        this.count = 0;
        this.worldGrossDomesticProduct = 0L;
        this.worldPopulation = 0L;
    }

    public void addCountry(int countryIndex, long grossDomesticProduct, long population, long treasury,
                           long taxRevenue, long tariffRevenue, long profitRevenue, long militarySpending,
                           int provinceCount, float developmentIndex, float literacy, int unitCount,
                           int governmentIndex, int headOfStateIndex) {
        int size = this.count + 1;
        this.countryIndexes = Columns.fit(this.countryIndexes, size);
        this.grossDomesticProducts = Columns.fit(this.grossDomesticProducts, size);
        this.populations = Columns.fit(this.populations, size);
        this.treasuries = Columns.fit(this.treasuries, size);
        this.taxRevenues = Columns.fit(this.taxRevenues, size);
        this.tariffRevenues = Columns.fit(this.tariffRevenues, size);
        this.profitRevenues = Columns.fit(this.profitRevenues, size);
        this.militarySpendings = Columns.fit(this.militarySpendings, size);
        this.provinceCounts = Columns.fit(this.provinceCounts, size);
        this.developmentIndexes = Columns.fit(this.developmentIndexes, size);
        this.literacies = Columns.fit(this.literacies, size);
        this.units = Columns.fit(this.units, size);
        this.governmentIndexes = Columns.fit(this.governmentIndexes, size);
        this.headOfStateIndexes = Columns.fit(this.headOfStateIndexes, size);
        this.countryIndexes[this.count] = countryIndex;
        this.grossDomesticProducts[this.count] = grossDomesticProduct;
        this.populations[this.count] = population;
        this.treasuries[this.count] = treasury;
        this.taxRevenues[this.count] = taxRevenue;
        this.tariffRevenues[this.count] = tariffRevenue;
        this.profitRevenues[this.count] = profitRevenue;
        this.militarySpendings[this.count] = militarySpending;
        this.provinceCounts[this.count] = provinceCount;
        this.developmentIndexes[this.count] = developmentIndex;
        this.literacies[this.count] = literacy;
        this.units[this.count] = unitCount;
        this.governmentIndexes[this.count] = governmentIndex;
        this.headOfStateIndexes[this.count] = headOfStateIndex;
        this.worldGrossDomesticProduct += grossDomesticProduct;
        this.worldPopulation += population;
        this.count++;
    }

    public int count() {
        return this.count;
    }

    public int countryIndex(int position) {
        return this.countryIndexes[position];
    }

    public long grossDomesticProduct(int position) {
        return this.grossDomesticProducts[position];
    }

    public long population(int position) {
        return this.populations[position];
    }

    public long treasury(int position) {
        return this.treasuries[position];
    }

    public long taxRevenue(int position) {
        return this.taxRevenues[position];
    }

    public long tariffRevenue(int position) {
        return this.tariffRevenues[position];
    }

    public long profitRevenue(int position) {
        return this.profitRevenues[position];
    }

    public long militarySpending(int position) {
        return this.militarySpendings[position];
    }

    public int provinceCount(int position) {
        return this.provinceCounts[position];
    }

    public float developmentIndex(int position) {
        return this.developmentIndexes[position];
    }

    public float literacy(int position) {
        return this.literacies[position];
    }

    public int units(int position) {
        return this.units[position];
    }

    public int governmentIndex(int position) {
        return this.governmentIndexes[position];
    }

    public int headOfStateIndex(int position) {
        return this.headOfStateIndexes[position];
    }

    public long worldGrossDomesticProduct() {
        return this.worldGrossDomesticProduct;
    }

    public long worldPopulation() {
        return this.worldPopulation;
    }
}
