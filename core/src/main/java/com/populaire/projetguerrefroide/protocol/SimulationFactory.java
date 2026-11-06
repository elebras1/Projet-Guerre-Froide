package com.populaire.projetguerrefroide.protocol;

import java.time.LocalDate;

public interface SimulationFactory {
    SimulationChannel create(LocalDate startDate, boolean debugMode);
}
