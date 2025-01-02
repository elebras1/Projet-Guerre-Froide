package com.populaire.projetguerrefroide.national;

import java.util.Map;

public class NationalIdeas {
    private final Map<String, Culture> cultures;
    private final Map<String, Religion> religions;
    private final Map<String, Identity> identities;
    private final Map<String, Attitude> attitudes;

    public NationalIdeas(Map<String, Culture> cultures, Map<String, Religion> religions, Map<String, Identity> identities, Map<String, Attitude> attitudes) {
        this.cultures = cultures;
        this.religions = religions;
        this.identities = identities;
        this.attitudes = attitudes;
    }

    public Map<String, Culture> getCultures() {
        return this.cultures;
    }

    public Map<String, Religion> getReligions() {
        return this.religions;
    }

    public Map<String, Identity> getIdentities() {
        return this.identities;
    }

    public Map<String, Attitude> getAttitudes() {
        return this.attitudes;
    }

    @Override
    public String toString() {
        return "NationalIdeas{" +
            "cultures=" + this.cultures +
            ", religions=" + this.religions +
            ", identities=" + this.identities +
            ", attitudes=" + this.attitudes +
            '}';
    }
}
