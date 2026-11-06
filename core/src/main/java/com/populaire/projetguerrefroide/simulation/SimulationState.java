package com.populaire.projetguerrefroide.simulation;

public final class SimulationState {
    private long playerCountryId;
    private boolean economyInitialized;

    public long playerCountryId() {
        return this.playerCountryId;
    }

    public void playerCountryId(long playerCountryId) {
        this.playerCountryId = playerCountryId;
    }

    public boolean economyInitialized() {
        return this.economyInitialized;
    }

    public void markEconomyInitialized() {
        this.economyInitialized = true;
    }
}
