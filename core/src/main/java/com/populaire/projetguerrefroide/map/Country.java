package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.*;

public class Country {
    private final String id;
    private long governmentId;
    private long headOfGovernmentId;
    private long headOfStateId;
    private LongList ministerIds;
    private long identityId;
    private long attitudeId;
    private LongList lawIds;
    private IntList leadersIds;

    public Country(String id) {
        this.id = id;
        this.governmentId = -1;
        this.headOfGovernmentId = -1;
        this.headOfStateId = -1;
        this.identityId = -1;
        this.attitudeId = -1;
    }

    public String getId() {
        return this.id;
    }

    public void setGovernmentId(long governmentId) {
        this.governmentId = governmentId;
    }

    public long getGovernmentId() {
        return this.governmentId;
    }

    public void setHeadOfGovernmentId(long idMinister) {
        this.headOfGovernmentId = idMinister;
    }

    public long getHeadOfStateId() {
        return this.headOfStateId;
    }

    public void setMinisterIds(LongList ministerIds) {
        this.ministerIds = ministerIds;
    }

    public LongList getMinisterIds() {
        return this.ministerIds;
    }

    public void setHeadOfStateId(long idMinister) {
        this.headOfStateId = idMinister;
    }

    public long getHeadOfGovernmentId() {
        return this.headOfGovernmentId;
    }

    public void setIdentityId(long identityId) {
        this.identityId = identityId;
    }

    public long getIdentityId() {
        return this.identityId;
    }

    public void setAttitudeId(long attitudeId) {
        this.attitudeId = attitudeId;
    }

    public long getAttitudeId() {
        return this.attitudeId;
    }

    public void setLawIds(LongList lawIds) {
        this.lawIds = lawIds;
    }

    public LongList getLawIds() {
        return this.lawIds;
    }

    public void setLeadersIds(IntList leadersIds) {
        this.leadersIds = leadersIds;
    }

    public IntList getLeadersIds() {
        return this.leadersIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return this.id.equals(country.id);
    }

    @Override
    public String toString() {
        return "Country{" +
                "id='" + this.id + '\'' +
                '}';
    }
}
