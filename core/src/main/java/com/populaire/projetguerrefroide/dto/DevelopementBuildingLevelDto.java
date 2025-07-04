package com.populaire.projetguerrefroide.dto;

public class DevelopementBuildingLevel {
    private final byte navalBaseLevel;
    private final byte airBaseLevel;
    private final byte radarStationLevel;
    private final byte antiAircraftGunsLevel;

    public DevelopementBuildingLevel(byte navalBaseLevel, byte airBaseLevel, byte radarStationLevel, byte antiAircraftGunsLevel) {
        this.navalBaseLevel = navalBaseLevel;
        this.airBaseLevel = airBaseLevel;
        this.radarStationLevel = radarStationLevel;
        this.antiAircraftGunsLevel = antiAircraftGunsLevel;
    }

    public byte getNavalBaseLevel() {
        return this.navalBaseLevel;
    }

    public byte getAirBaseLevel() {
        return this.airBaseLevel;
    }

    public byte getRadarStationLevel() {
        return this.radarStationLevel;
    }

    public byte getAntiAircraftGunsLevel() {
        return this.antiAircraftGunsLevel;
    }
}
