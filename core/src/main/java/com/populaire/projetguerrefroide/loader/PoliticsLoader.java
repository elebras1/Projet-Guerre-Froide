package com.populaire.projetguerrefroide.loader;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.json.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import java.util.Arrays;
import java.util.Map;

import static com.populaire.projetguerrefroide.util.Constants.*;

public final class PoliticsLoader {
    private final WorldLoadingContext context;
    private final World ecsWorld;
    private final EcsConstants ecsConstants;

    public PoliticsLoader(WorldLoadingContext context) {
        this.context = context;
        this.ecsWorld = context.ecsWorld();
        this.ecsConstants = context.ecsConstants();
    }

    public void loadIdeologies(Map<String, IdeologyJson> ideologies) {
        try {
            for(var ideologyEntry : ideologies.entrySet()) {
                String ideologyName = ideologyEntry.getKey();
                IdeologyJson ideology = ideologyEntry.getValue();
                int color = this.context.parseColor(ideology.color());
                byte factionDriftingSpeed = (byte) ideology.factionDriftingSpeed();

                long ideologyEntityId = this.ecsWorld.entity(ideologyName);
                EntityView ideologyEntity = this.ecsWorld.obtainEntityView(ideologyEntityId);
                ideologyEntity.set(new Ideology(color, factionDriftingSpeed));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadLaws(LawsJson laws) {
        try {
            int baseEnactmentDaysLaw = (int) laws.baseEnactmentDays();
            int lawGroupIndex = 0;
            for(var lawGroupEntry : laws.groups().entrySet()) {
                LawGroupJson lawGroupJson = lawGroupEntry.getValue();
                int enactmentDuration = baseEnactmentDaysLaw * (int) lawGroupJson.factorEnactmentDays();
                long lawGroupId = this.ecsWorld.entity(lawGroupEntry.getKey());
                Entity lawGroup = this.ecsWorld.obtainEntity(lawGroupId);
                lawGroup.set(new LawGroup(lawGroupIndex, enactmentDuration));
                this.context.lawGroupIds()[lawGroupIndex] = lawGroupId;

                for(var lawEntry : lawGroupJson.laws().entrySet()) {
                    long lawEntityId = this.ecsWorld.entity(lawEntry.getKey());
                    EntityView lawEntity = this.ecsWorld.obtainEntityView(lawEntityId);
                    LawJson law = lawEntry.getValue();

                    float[] modifierValues = new float[MAX_MODIFIERS];
                    long[] modifierTagIds = new long[MAX_MODIFIERS];
                    Arrays.fill(modifierTagIds, -1);
                    int modifierIndex = 0;
                    for(var modifierEntry : law.modifiers().entrySet()) {
                        modifierValues[modifierIndex] = modifierEntry.getValue().floatValue();
                        modifierTagIds[modifierIndex] = this.ecsWorld.entity(modifierEntry.getKey());
                        modifierIndex++;
                    }
                    lawEntity.set(new Modifiers(modifierValues, modifierTagIds));

                    float[] overrideValues = new float[MAX_OVERRIDES];
                    long[] overrideTagIds = new long[MAX_OVERRIDES];
                    Arrays.fill(overrideTagIds, -1);
                    int overrideIndex = 0;
                    if(law.overrides() != null) {
                        for(var overrideEntry : law.overrides().entrySet()) {
                            overrideValues[overrideIndex] = overrideEntry.getValue().floatValue();
                            overrideTagIds[overrideIndex] = this.ecsWorld.entity(overrideEntry.getKey());
                            overrideIndex++;
                        }
                    }
                    lawEntity.set(new Overrides(overrideValues, overrideTagIds));

                    long[] supportIdeologieIds = new long[MAX_LAW_IDEOLOGIES];
                    long[] opponentIdeologieIds = new long[MAX_LAW_IDEOLOGIES];
                    int indexSupporters = 0;
                    int indexOpponents = 0;
                    for(var interestIdeologyEntry : law.interestIdeologies().entrySet()) {
                        long ideologyId = this.ecsWorld.lookup(interestIdeologyEntry.getKey());
                        long value = interestIdeologyEntry.getValue();
                        if(value > 0) {
                            supportIdeologieIds[indexSupporters] = ideologyId;
                            indexSupporters++;
                        } else if (value < 0) {
                            opponentIdeologieIds[indexOpponents] = ideologyId;
                            indexOpponents++;
                        }
                    }
                    lawEntity.set(new Law(lawGroupId, supportIdeologieIds, opponentIdeologieIds));
                }
                lawGroupIndex++;
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadGovernments(Map<String, GovernmentJson> governments) {
        try {

            for(var governmentEntry : governments.entrySet()) {
                String governmentName = governmentEntry.getKey();
                GovernmentJson government = governmentEntry.getValue();

                long governmentEntityId = this.ecsWorld.entity(governmentName);
                EntityView governmentEntity = this.ecsWorld.obtainEntityView(governmentEntityId);

                long[] associatedIdeologieIds = new long[MAX_GOVERNMENT_ASSOCIATED_IDEOLOGIES];
                int i = 0;
                for(String associatedIdeology : government.associatedIdeologies()) {
                    long ideologyEntityId = this.ecsWorld.lookup(associatedIdeology);
                    associatedIdeologieIds[i] = ideologyEntityId;
                    i++;
                }

                ElectionJson election = government.election();
                if(election.duration() != null) {
                    governmentEntity.set(new ElectoralMechanism(election.headOfState(), election.headOfGovernment(), election.duration().intValue()));
                }

                long[] supportedLawGroupIds = new long[LAW_COUNT];
                long[] supportedLawIds = new long[LAW_COUNT];
                int lawIndex = 0;
                for(var lawGroupEntry : government.laws().entrySet()) {
                    long lawGroupId = this.ecsWorld.lookup(lawGroupEntry.getKey());
                    for(String supportedLaw : lawGroupEntry.getValue()) {
                        supportedLawGroupIds[lawIndex] = lawGroupId;
                        supportedLawIds[lawIndex] = this.ecsWorld.lookup(supportedLaw);
                        lawIndex++;
                    }
                }

                governmentEntity.set(new GovernmentPolicy(associatedIdeologieIds, supportedLawGroupIds, supportedLawIds));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadNationalIdeas(NationalIdeasJson nationalIdeas) {
        try {

            for(var cultureEntry : nationalIdeas.cultures().entrySet()) {
                String name = cultureEntry.getKey();
                int color = this.context.parseColor(cultureEntry.getValue().color());
                long cultureEntityId = this.ecsWorld.entity(name);
                EntityView cultureEntity = this.ecsWorld.obtainEntityView(cultureEntityId);
                cultureEntity.set(new Color(color));
                cultureEntity.add(this.ecsConstants.cultureTag());
            }

            for(var religionEntry : nationalIdeas.religions().entrySet()) {
                String name = religionEntry.getKey();
                int color = this.context.parseColor(religionEntry.getValue().color());
                long religionEntityId = this.ecsWorld.entity(name);
                EntityView religionEntity = this.ecsWorld.obtainEntityView(religionEntityId);
                religionEntity.set(new Color(color));
                religionEntity.add(this.ecsConstants.religionTag());
            }

            for(var identityEntry : nationalIdeas.nationalIdentity().entrySet()) {
                String name = identityEntry.getKey();
                long identityEntityId = this.ecsWorld.entity(name);
                EntityView identityEntity = this.ecsWorld.obtainEntityView(identityEntityId);
                float[] modifierValues = new float[MAX_MODIFIERS];
                long[] modifierTagIds = new long[MAX_MODIFIERS];
                Arrays.fill(modifierTagIds, -1);
                int i = 0;
                for(var modifierEntry : identityEntry.getValue().entrySet()) {
                    modifierValues[i] = modifierEntry.getValue().floatValue();
                    modifierTagIds[i] = this.ecsWorld.entity(modifierEntry.getKey());
                    i++;
                }
                identityEntity.set(new Modifiers(modifierValues, modifierTagIds));
                identityEntity.add(this.ecsConstants.identityTag());
            }

            for(var attitudeEntry : nationalIdeas.nationalAttitude().entrySet()) {
                String attitudeName = attitudeEntry.getKey();
                long attitudeEntityId = this.ecsWorld.entity(attitudeName);
                EntityView attitudeEntity = this.ecsWorld.obtainEntityView(attitudeEntityId);
                float[] modifierValues = new float[MAX_MODIFIERS];
                long[] modifierTagIds = new long[MAX_MODIFIERS];
                Arrays.fill(modifierTagIds, -1);
                int i = 0;
                for(var modifierEntry : attitudeEntry.getValue().entrySet()) {
                    modifierValues[i] = modifierEntry.getValue().floatValue();
                    modifierTagIds[i] = this.ecsWorld.entity(modifierEntry.getKey());
                    i++;
                }
                attitudeEntity.set(new Modifiers(modifierValues, modifierTagIds));
                attitudeEntity.add(this.ecsConstants.attitudeTag());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadMinisterTypes(Map<String, Map<String, Double>> ministerTypes) {
        try {
            for(var ministerTypeEntry : ministerTypes.entrySet()) {
                long ministerTypeEntityId = this.ecsWorld.entity(ministerTypeEntry.getKey());
                EntityView ministerTypeEntity = this.ecsWorld.obtainEntityView(ministerTypeEntityId);
                float[] modifierValues = new float[MAX_MODIFIERS];
                long[] modifierTagIds = new long[MAX_MODIFIERS];
                Arrays.fill(modifierTagIds, -1);
                int i = 0;
                for(var modifierEntry : ministerTypeEntry.getValue().entrySet()) {
                    modifierValues[i] = modifierEntry.getValue().floatValue();
                    modifierTagIds[i] = this.ecsWorld.entity(modifierEntry.getKey());
                    i++;
                }
                ministerTypeEntity.set(new Modifiers(modifierValues, modifierTagIds));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
