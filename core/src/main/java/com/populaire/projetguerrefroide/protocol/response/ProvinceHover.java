package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class ProvinceHover extends Response {
    private int provinceIndex;
    private int[] cultures = new int[8];
    private int[] culturePercents = new int[8];
    private int cultureCount;
    private int[] religions = new int[8];
    private int[] religionPercents = new int[8];
    private int religionCount;

    public void clear(int provinceIndex) {
        this.provinceIndex = provinceIndex;
        this.cultureCount = 0;
        this.religionCount = 0;
    }

    public void addCulture(int cultureIndex, int percent) {
        this.cultures = Columns.fit(this.cultures, this.cultureCount + 1);
        this.culturePercents = Columns.fit(this.culturePercents, this.cultureCount + 1);
        this.cultures[this.cultureCount] = cultureIndex;
        this.culturePercents[this.cultureCount] = percent;
        this.cultureCount++;
    }

    public void addReligion(int religionIndex, int percent) {
        this.religions = Columns.fit(this.religions, this.religionCount + 1);
        this.religionPercents = Columns.fit(this.religionPercents, this.religionCount + 1);
        this.religions[this.religionCount] = religionIndex;
        this.religionPercents[this.religionCount] = percent;
        this.religionCount++;
    }

    public int provinceIndex() {
        return this.provinceIndex;
    }

    public int cultureCount() {
        return this.cultureCount;
    }

    public int culture(int position) {
        return this.cultures[position];
    }

    public int culturePercent(int position) {
        return this.culturePercents[position];
    }

    public int religionCount() {
        return this.religionCount;
    }

    public int religion(int position) {
        return this.religions[position];
    }

    public int religionPercent(int position) {
        return this.religionPercents[position];
    }
}
