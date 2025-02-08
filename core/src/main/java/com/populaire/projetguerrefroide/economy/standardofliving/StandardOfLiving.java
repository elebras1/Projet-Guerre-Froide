package com.populaire.projetguerrefroide.economy.standardofliving;

import java.util.List;

public class StandardOfLiving {
    private final short amount;
    private final List<StandardOfLivingLevel> levels;

    public StandardOfLiving(short amount, List<StandardOfLivingLevel> levels) {
        this.amount = amount;
        this.levels = levels;
    }

    public short getAmount() {
        return this.amount;
    }

    public List<StandardOfLivingLevel> getLevels() {
        return this.levels;
    }

    @Override
    public String toString() {
        return "StandardOfLiving{" +
                "amount=" + this.amount +
                ", levels=" + this.levels +
                '}';
    }
}
