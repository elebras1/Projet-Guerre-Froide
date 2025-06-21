package com.populaire.projetguerrefroide.politics;

public class Leader {
    private final String name;
    private final byte skill;
    private final ForceType forceType;
    private final Trait trait;

    public Leader(String name, byte skill, ForceType forceType, Trait trait) {
        this.name = name;
        this.skill = skill;
        this.forceType = forceType;
        this.trait = trait;
    }

    public String getName() {
        return this.name;
    }

    public byte getSkill() {
        return this.skill;
    }

    public ForceType getForceType() {
        return this.forceType;
    }

    public Trait getTrait() {
        return this.trait;
    }

    @Override
    public String toString() {
        return "Leader{" +
                "name='" + this.name + '\'' +
                ", skill=" + this.skill +
                ", forceType=" + this.forceType +
                ", trait=" + this.trait +
                '}';
    }
}
