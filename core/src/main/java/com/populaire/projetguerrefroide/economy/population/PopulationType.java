package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.ObjectFloatMap;
import com.populaire.projetguerrefroide.economy.good.Good;
// Import other necessary classes/enums conceptually
// import com.populaire.projetguerrefroide.building.BuildingType; // Or use tags
// import com.populaire.projetguerrefroide.national.SkillType;

import java.awt.Color; // Example: Using AWT Color
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap; // For thread-safe registry


/**
 * Represents an immutable definition of a population type (stratum, profession, social group).
 * Defines its characteristics, economic role, needs, social standing, and simulation parameters.
 * Instances are typically created via the nested Builder and managed globally via the static registry.
 */
public class PopulationType {

    // --- Static Registry ---
    private static final Map<String, PopulationType> registry = new ConcurrentHashMap<>();

    /**
     * Registers a PopulationType globally. Ensures uniqueness by name.
     * @param type The PopulationType instance to register.
     * @throws IllegalArgumentException if a type with the same name is already registered.
     */
    public static void register(PopulationType type) {
        Objects.requireNonNull(type, "Cannot register a null PopulationType.");
        Objects.requireNonNull(type.getName(), "PopulationType name cannot be null for registration.");
        if (registry.containsKey(type.getName())) {
            throw new IllegalArgumentException("PopulationType with name '" + type.getName() + "' already registered.");
        }
        registry.put(type.getName(), type);
    }

    /**
     * Retrieves a registered PopulationType by its unique name.
     * @param name The name of the PopulationType.
     * @return The registered PopulationType instance.
     * @throws IllegalArgumentException if no type with the given name is registered.
     */
    public static PopulationType getByName(String name) {
        PopulationType type = registry.get(name);
        if (type == null) {
            throw new IllegalArgumentException("No PopulationType registered with name '" + name + "'.");
        }
        return type;
    }

    /**
     * Gets an unmodifiable view of all registered PopulationType instances.
     * @return An unmodifiable Collection of PopulationTypes.
     */
    public static java.util.Collection<PopulationType> getAllTypes() {
        return Collections.unmodifiableCollection(registry.values());
    }
    // --- End Static Registry ---


    // --- Core Identity & UI ---
    private final String name; // Unique identifier
    private final String description;
    private final Color color; // Use java.awt.Color or a custom Color class
    private final String iconPath; // Optional path to UI icon

    // --- Economic Role ---
    /** Base goods produced per 1k employed workers per day. */
    private final Map<Good, Float> producesGoods;
    /** Tags of building types this population can work in. */
    private final Set<String> workBuildingTags;
    /** Base income level (arbitrary units, influences wealth gain and demand). */
    private final double baseIncome;
    /** Base starting wealth level (influences initial demand). */
    private final double baseWealth;
    /** Modifiers for learning/performing specific skills (e.g., 1.0 = normal, 1.2 = 20% better). */
    private final Map<String /*SkillType enum/string*/, Float> skillAffinities;

    // --- Social & Political ---
    private final SocialStratum socialStratum;
    private final double politicalWeightModifier; // Influence in political calculations
    private final double baseLiteracy; // Default literacy (0.0 - 1.0)
    private final EducationLevel educationRequirement; // Minimum education needed to be this type

    // --- Demands & Needs (Units: per 1000 people per day) ---
    /** Essential goods required for basic function/happiness. */
    private final Map<Good, Float> standardDemands;
    /** Non-essential goods desired for higher happiness/status. */
    private final Map<Good, Float> luxuryDemands;
    /** Describes consequences of unmet needs (e.g., happiness penalty, productivity loss). Key=Good Name or category, Value=Description/Effect ID */
    private final Map<String, String> fulfillmentEffects;
    // private final Map<Good, Float> demandElasticity; // Advanced: How demand changes with price/wealth

    // --- Military ---
    private final boolean canConscript; // Can this type be conscripted into military service?
    private final double militaryModifier; // Combat effectiveness modifier

    // --- Promotion / Lifecycle ---
    /** Target ratio this pop type aims for in the overall population (for promotion/demotion logic). */
    private final float promotionTargetRatio;
    // Future: Add List<Condition> promotionConditions, List<Condition> demotionConditions


    // --- Enums (Conceptual - define these properly elsewhere) ---
    public enum SocialStratum { LOWER, MIDDLE, UPPER }
    public enum EducationLevel { NONE, BASIC, INTERMEDIATE, ADVANCED }


    // Private constructor - Use Builder pattern
    private PopulationType(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "PopulationType name cannot be null.");
        this.description = Objects.requireNonNullElse(builder.description, "");
        this.color = Objects.requireNonNullElse(builder.color, Color.GRAY); // Default color
        this.iconPath = builder.iconPath; // Can be null

        // Economic
        this.producesGoods = copyAndMakeUnmodifiable(builder.producesGoods);
        this.workBuildingTags = copyAndMakeUnmodifiable(builder.workBuildingTags);
        this.baseIncome = builder.baseIncome;
        this.baseWealth = builder.baseWealth;
        this.skillAffinities = copyAndMakeUnmodifiable(builder.skillAffinities);

        // Social/Political
        this.socialStratum = Objects.requireNonNullElse(builder.socialStratum, SocialStratum.MIDDLE);
        this.politicalWeightModifier = Math.max(0, builder.politicalWeightModifier);
        this.baseLiteracy = Math.max(0.0, Math.min(1.0, builder.baseLiteracy));
        this.educationRequirement = Objects.requireNonNullElse(builder.educationRequirement, EducationLevel.NONE);

        // Demands (Units: per 1k people per day)
        // Use the ObjectFloatMap directly if guaranteed immutable, or copy
        this.standardDemands = copyAndMakeUnmodifiable(builder.standardDemands);
        this.luxuryDemands = copyAndMakeUnmodifiable(builder.luxuryDemands);
        this.fulfillmentEffects = copyAndMakeUnmodifiable(builder.fulfillmentEffects);

        // Military
        this.canConscript = builder.canConscript;
        this.militaryModifier = builder.militaryModifier;

        // Promotion
        this.promotionTargetRatio = Math.max(0f, builder.promotionTargetRatio);

        // --- Post-Construction Validation ---
        validateMapValuesNonNegative(this.producesGoods, "Production");
        validateMapValuesNonNegative(this.standardDemands, "Standard Demand");
        validateMapValuesNonNegative(this.luxuryDemands, "Luxury Demand");
        // skillAffinities can potentially be negative? Depends on design. Assume non-negative for now.
        validateMapValuesNonNegativeFloat(this.skillAffinities, "Skill Affinity");
        if (this.baseIncome < 0) throw new IllegalArgumentException("Base income cannot be negative.");
        if (this.baseWealth < 0) throw new IllegalArgumentException("Base wealth cannot be negative.");

    }

    // --- Helper for defensive copying ---
    private <K, V> Map<K, V> copyAndMakeUnmodifiable(Map<K, V> source) {
        return (source == null || source.isEmpty())
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(source)); // Use HashMap for ObjectFloatMap input
    }
    // Specific version for ObjectFloatMap input
     private <K> Map<K, Float> copyAndMakeUnmodifiable(ObjectFloatMap<K> source) {
         if (source == null || source.isEmpty()) {
             return Collections.emptyMap();
         }
         // Convert ObjectFloatMap to HashMap for standard Map interface
         Map<K, Float> copy = new HashMap<>();
         for(ObjectFloatMap.Entry<K> entry : source) {
             copy.put(entry.key, entry.value);
         }
         return Collections.unmodifiableMap(copy);
     }
    private <E> Set<E> copyAndMakeUnmodifiable(Set<E> source) {
        return (source == null || source.isEmpty())
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(source));
    }

    // --- Validation Helpers ---
     private <K> void validateMapValuesNonNegative(Map<K, Float> map, String mapName) {
         if (map == null) return; // Null map is allowed (becomes emptyMap)
         for (Map.Entry<K, Float> entry : map.entrySet()) {
             if (entry.getValue() < 0f) {
                 throw new IllegalArgumentException(mapName + " value for '" + entry.getKey() + "' cannot be negative.");
             }
         }
     }
      // Specific version for ObjectFloatMap input
      private <K> void validateMapValuesNonNegative(ObjectFloatMap<K> map, String mapName) {
          if (map == null) return;
          for (ObjectFloatMap.Entry<K> entry : map) {
              if (entry.value < 0f) {
                  throw new IllegalArgumentException(mapName + " value for '" + entry.key + "' cannot be negative.");
              }
          }
      }
      // Version for Float values specifically
      private <K> void validateMapValuesNonNegativeFloat(Map<K, Float> map, String mapName) {
            if (map == null) return;
            for (Map.Entry<K, Float> entry : map.entrySet()) {
                if (entry.getValue() < 0f) {
                    throw new IllegalArgumentException(mapName + " value for '" + entry.getKey() + "' cannot be negative.");
                }
            }
        }


    // --- Getters ---
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Color getColor() { return color; }
    public String getIconPath() { return iconPath; }
    public Map<Good, Float> getProducesGoods() { return producesGoods; } // Already unmodifiable
    public Set<String> getWorkBuildingTags() { return workBuildingTags; } // Already unmodifiable
    public double getBaseIncome() { return baseIncome; }
    public double getBaseWealth() { return baseWealth; }
    public Map<String, Float> getSkillAffinities() { return skillAffinities; } // Already unmodifiable
    public SocialStratum getSocialStratum() { return socialStratum; }
    public double getPoliticalWeightModifier() { return politicalWeightModifier; }
    public double getBaseLiteracy() { return baseLiteracy; }
    public EducationLevel getEducationRequirement() { return educationRequirement; }
    /** Gets standard demands per 1000 people per day. Returns an unmodifiable map. */
    public Map<Good, Float> getStandardDemands() { return standardDemands; } // Already unmodifiable
    /** Gets luxury demands per 1000 people per day. Returns an unmodifiable map. */
    public Map<Good, Float> getLuxuryDemands() { return luxuryDemands; } // Already unmodifiable
    public Map<String, String> getFulfillmentEffects() { return fulfillmentEffects; } // Already unmodifiable
    public boolean isCanConscript() { return canConscript; }
    public double getMilitaryModifier() { return militaryModifier; }
    public float getPromotionTargetRatio() { return promotionTargetRatio; }


    // --- Utility Methods ---

    /**
     * Gets the combined demand (standard + luxury) for a specific good.
     * Units: per 1000 people per day.
     * @param good The good in question.
     * @return The total base demand for the good, or 0f if not demanded.
     */
    public float getDemandFor(Good good) {
        return standardDemands.getOrDefault(good, 0f) + luxuryDemands.getOrDefault(good, 0f);
    }

     /**
      * Gets the base production output for a specific good.
      * Units: per 1000 employed workers per day.
      * @param good The good in question.
      * @return The base production amount, or 0f if not produced.
      */
     public float getProductionFor(Good good) {
         return producesGoods.getOrDefault(good, 0f);
     }

    /**
     * Checks if this population type can work in a building with the given tag.
     * @param buildingTag The tag to check (e.g., "farm", "factory", "mine").
     * @return true if the tag is present in workBuildingTags, false otherwise.
     */
    public boolean canWorkInBuildingTagged(String buildingTag) {
        return workBuildingTags.contains(buildingTag);
    }


    // --- Overrides ---

    @Override
    public final String toString() { // Make final as equals/hashCode are
        return "PopulationType{" +
               "name='" + name + '\'' +
               ", stratum=" + socialStratum +
               ", income=" + String.format("%.2f", baseIncome) +
               ", demands=" + (standardDemands.size() + luxuryDemands.size()) +
               ", produces=" + producesGoods.size() +
               ", worksIn=" + workBuildingTags.size() + " tags" +
               '}';
    }

    /**
     * Equality based solely on the unique name.
     */
    @Override
    public final boolean equals(Object o) { // Make final to match hashCode contract
        if (this == o) return true;
        // Use pattern matching for instanceof (Java 16+)
        if (!(o instanceof PopulationType that)) return false;
        return name.equals(that.name);
    }

    /**
     * Hash code based solely on the unique name.
     */
    @Override
    public final int hashCode() { // Make final to match equals contract
        return Objects.hash(name);
    }


    // --- Builder Class ---
    public static class Builder {
        // Required
        private final String name;

        // Optional with defaults
        private String description = "";
        private Color color = Color.GRAY;
        private String iconPath = null;
        private Map<Good, Float> producesGoods = new HashMap<>();
        private Set<String> workBuildingTags = new HashSet<>();
        private double baseIncome = 1.0;
        private double baseWealth = 1.0;
        private Map<String, Float> skillAffinities = new HashMap<>();
        private SocialStratum socialStratum = SocialStratum.MIDDLE;
        private double politicalWeightModifier = 1.0;
        private double baseLiteracy = 0.5;
        private EducationLevel educationRequirement = EducationLevel.NONE;
        private ObjectFloatMap<Good> standardDemands = new ObjectFloatMap<>(); // Use provided map type for input flexibility
        private ObjectFloatMap<Good> luxuryDemands = new ObjectFloatMap<>();
        private Map<String, String> fulfillmentEffects = new HashMap<>();
        private boolean canConscript = true;
        private double militaryModifier = 1.0;
        private float promotionTargetRatio = 0f;


        /**
         * Start building a PopulationType definition.
         * @param name The unique name for this type (e.g., "Farmers", "Aristocrats").
         */
        public Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) { this.description = description; return this; }
        public Builder color(Color color) { this.color = color; return this; }
        public Builder iconPath(String iconPath) { this.iconPath = iconPath; return this; }

        public Builder produces(Good good, float amountPer1kWorkerDay) { this.producesGoods.put(good, amountPer1kWorkerDay); return this; }
        public Builder productionMap(Map<Good, Float> production) { this.producesGoods = new HashMap<>(production); return this; } // Copy input map

        public Builder canWorkIn(String buildingTag) { this.workBuildingTags.add(buildingTag); return this; }
        public Builder workTagSet(Set<String> tags) { this.workBuildingTags = new HashSet<>(tags); return this; } // Copy input set

        public Builder income(double baseIncome) { this.baseIncome = baseIncome; return this; }
        public Builder wealth(double baseWealth) { this.baseWealth = baseWealth; return this; }

        public Builder skillAffinity(String skill, float modifier) { this.skillAffinities.put(skill, modifier); return this; }
        public Builder skillAffinityMap(Map<String, Float> affinities) { this.skillAffinities = new HashMap<>(affinities); return this; } // Copy input map

        public Builder stratum(SocialStratum stratum) { this.socialStratum = stratum; return this; }
        public Builder politicalWeight(double weight) { this.politicalWeightModifier = weight; return this; }
        public Builder literacy(double baseLiteracy) { this.baseLiteracy = baseLiteracy; return this; }
        public Builder educationRequired(EducationLevel level) { this.educationRequirement = level; return this; }

        /** Set standard demands (per 1k people per day). Replaces existing map. */
        public Builder standardDemandsMap(ObjectFloatMap<Good> demands) { this.standardDemands = new ObjectFloatMap<>(demands); return this; } // Copy input map
        public Builder standardDemand(Good good, float amountPer1kDay) { this.standardDemands.put(good, amountPer1kDay); return this; }

        /** Set luxury demands (per 1k people per day). Replaces existing map. */
        public Builder luxuryDemandsMap(ObjectFloatMap<Good> demands) { this.luxuryDemands = new ObjectFloatMap<>(demands); return this; } // Copy input map
        public Builder luxuryDemand(Good good, float amountPer1kDay) { this.luxuryDemands.put(good, amountPer1kDay); return this; }

        public Builder fulfillmentEffect(String goodOrCategory, String effectDescriptionOrId) { this.fulfillmentEffects.put(goodOrCategory, effectDescriptionOrId); return this; }
        public Builder fulfillmentEffectsMap(Map<String, String> effects) { this.fulfillmentEffects = new HashMap<>(effects); return this; } // Copy input map

        public Builder conscriptable(boolean canConscript) { this.canConscript = canConscript; return this; }
        public Builder militaryModifier(double modifier) { this.militaryModifier = modifier; return this; }

        public Builder targetRatio(float ratio) { this.promotionTargetRatio = ratio; return this; }

        /**
         * Builds the immutable PopulationType instance.
         * Performs validation on values set in the builder.
         * It's recommended to {@link #register(PopulationType)} the result.
         * @return The configured PopulationType.
         * @throws IllegalArgumentException if validation fails (e.g., negative values).
         * @throws NullPointerException if required fields like name are null.
         */
        public PopulationType build() {
             // Constructor handles validation and copying to immutable collections
            return new PopulationType(this);
        }
    }
}
