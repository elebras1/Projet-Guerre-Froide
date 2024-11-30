package com.populaire.projetguerrefroide.entity;

public class Election {
    private final boolean headOfState;
    private final boolean headOfGovernment;
    private final short duration;

    public Election(boolean headOfState, boolean headOfGovernment, short duration) {
        this.headOfState = headOfState;
        this.headOfGovernment = headOfGovernment;
        this.duration = duration;
    }

    public boolean electionOfHeadOfState() {
        return this.headOfState;
    }

    public boolean electionOfHeadOfGovernment() {
        return this.headOfGovernment;
    }

    public short getDuration() {
        return this.duration;
    }

    @Override
    public String toString() {
        return "Election{" +
            "headOfState=" + this.headOfState +
            ", headOfGovernment=" + this.headOfGovernment +
            ", duration=" + this.duration +
            '}';
    }
}
