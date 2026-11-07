package com.populaire.projetguerrefroide.simulation.command;

import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.protocol.command.GameCommand;

import java.util.List;

public final class CommandJournal {
    private final List<Entry> entries = new ObjectList<>();

    public void record(int tick, long countryId, GameCommand command, boolean accepted) {
        this.entries.add(new Entry(tick, countryId, command, accepted));
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public record Entry(int tick, long countryId, GameCommand command, boolean accepted) {
    }
}
