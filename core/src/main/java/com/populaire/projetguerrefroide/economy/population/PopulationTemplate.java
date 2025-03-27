package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.national.Culture;
import com.populaire.projetguerrefroide.national.Religion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong; // For precise sum in createPopulationInstance

/**
 * Represents an immutable template or archetype for a population group.
 * Defines default ratios, distributions, and characteristics used to initialize
 * or generate actual {@link Population} instances.
 */
public class PopulationTemplate {

    private static final double SUM_TOLERANCE = 0.01; // Tolerance for checking if ratios/distributions sum to 1.0

    // --- Core Identity ---
    private final short id;
    private final String name;
    private final String description;

    // --- Demographic Ratios & Modifiers ---
    private final float childrenRatio; // Should sum to approx 1.0 with adults & seniors
    private final float adultsRatio;
    private final float seniorsRatio;
    private final double baseBirthRateModifier; // Multiplier for standard birth rate
    private final double baseDeathRateModifier; // Multiplier for standard death rate

    // --- Socio-Cultural Defaults ---
    private final Map<Culture, Float> cultureDistribution; // Ratios summing to 1.0
    private final Map<Religion, Float> religionDistribution; // Ratios summing to 1.0
    private final double startingLiteracy; // 0.0 to 1.0
    private final double startingHealth;   // Game-specific scale
    private final double startingHappiness; // 0.0 to 1.0

    // --- Economic Defaults ---
    private final Map<PopulationType, Float> populationTypeDistribution; // Ratios summing to 1.0
    private final double startingUnemploymentRate; // 0.0 to 1.0
    // Future: Add baseNeedsProfile, baseWealthLevel, etc.

    // Private constructor - use Builder
    private PopulationTemplate(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "Template name cannot be null");
        this.description = Objects.requireNonNullElse(builder.description, ""); // Default to empty string

        // Validate and assign age ratios
        validateRatios("Age", builder.childrenRatio, builder.adultsRatio, builder.seniorsRatio);
        this.childrenRatio = builder.childrenRatio;
        this.adultsRatio = builder.adultsRatio;
        this.seniorsRatio = builder.seniorsRatio;

        this.baseBirthRateModifier = builder.baseBirthRateModifier;
        this.baseDeathRateModifier = builder.baseDeathRateModifier;

        // Validate and assign distributions (use immutable copies)
        this.cultureDistribution = validateDistribution("Culture", builder.cultureDistribution);
        this.religionDistribution = validateDistribution("Religion", builder.religionDistribution);
        this.populationTypeDistribution = validateDistribution("PopulationType", builder.populationTypeDistribution);

        // Assign starting socio-economic values (with bounds checking)
        this.startingLiteracy = Math.max(0.0, Math.min(1.0, builder.startingLiteracy));
        this.startingHealth = builder.startingHealth;
        this.startingHappiness = Math.max(0.0, Math.min(1.0, builder.startingHappiness));
        this.startingUnemploymentRate = Math.max(0.0, Math.min(1.0, builder.startingUnemploymentRate));
    }

    // --- Validation Helpers (used by constructor) ---

    private void validateRatios(String type, float r1, float r2, float r3) {
        if (r1 < 0 || r2 < 0 || r3 < 0) {
            throw new IllegalArgumentException(type + " ratios cannot be negative.");
        }
        float sum = r1 + r2 + r3;
        if (Math.abs(sum - 1.0f) > SUM_TOLERANCE) {
            throw new IllegalArgumentException(type + " ratios must sum to approximately 1.0 (currently sum to " + sum + ")");
        }
    }

    private <K> Map<K, Float> validateDistribution(String type, Map<K, Float> distribution) {
        Objects.requireNonNull(distribution, type + " distribution map cannot be null.");
        if (distribution.isEmpty()) {
             // Allow empty distribution? Or require at least one entry? Assuming allow for now.
             return Collections.unmodifiableMap(new HashMap<>()); // Return empty immutable map
        }
        float sum = 0f;
        for (Map.Entry<K, Float> entry : distribution.entrySet()) {
             Objects.requireNonNull(entry.getKey(), type + " distribution key cannot be null.");
             float value = entry.getValue();
            if (value < 0) {
                throw new IllegalArgumentException(type + " distribution ratios cannot be negative for key: " + entry.getKey());
            }
            sum += value;
        }
        if (Math.abs(sum - 1.0f) > SUM_TOLERANCE) {
            throw new IllegalArgumentException(type + " distribution ratios must sum to approximately 1.0 (currently sum to " + sum + ")");
        }
        // Return an immutable copy
        return Collections.unmodifiableMap(new HashMap<>(distribution));
    }


    // --- Getters ---

    public short getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getChildrenRatio() { return childrenRatio; }
    public float getAdultsRatio() { return adultsRatio; }
    public float getSeniorsRatio() { return seniorsRatio; }
    public double getBaseBirthRateModifier() { return baseBirthRateModifier; }
    public double getBaseDeathRateModifier() { return baseDeathRateModifier; }
    public Map<Culture, Float> getCultureDistribution() { return cultureDistribution; } // Already immutable
    public Map<Religion, Float> getReligionDistribution() { return religionDistribution; } // Already immutable
    public double getStartingLiteracy() { return startingLiteracy; }
    public double getStartingHealth() { return startingHealth; }
    public double getStartingHappiness() { return startingHappiness; }
    public Map<PopulationType, Float> getPopulationTypeDistribution() { return populationTypeDistribution; } // Already immutable
    public double getStartingUnemploymentRate() { return startingUnemploymentRate; }

    // --- Core Functionality ---

    /**
     * Creates a new {@link Population} instance based on this template and a target total size.
     * It calculates absolute numbers and distributions according to the template's ratios.
     *
     * @param totalSize The desired total population for the new instance. Must be non-negative.
     * @return A configured Population object.
     * @throws IllegalArgumentException if totalSize is negative.
     */
    public Population createPopulationInstance(long totalSize) {
        if (totalSize < 0) {
            throw new IllegalArgumentException("Total population size cannot be negative.");
        }
        if (totalSize == 0) {
            // Return an empty population using default averages from template
            return new Population(0, 0, 0, new ObjectIntMap<>(), new ObjectIntMap<>(), new ObjectIntMap<>(),
                                  this.startingLiteracy, this.startingHealth, this.startingHappiness, this.startingUnemploymentRate);
        }

        // Calculate age group counts, handling rounding by adjusting the largest group (adults)
        long children = Math.round(totalSize * this.childrenRatio);
        long seniors = Math.round(totalSize * this.seniorsRatio);
        // Adults get the remainder to ensure the sum is exactly totalSize
        long adults = totalSize - children - seniors;

        // Correct potential negative values due to rounding edge cases with small totalSize
        if (adults < 0) {
             children += adults; // Reduce children first
             adults = 0;
             if (children < 0) {
                 seniors += children; // Reduce seniors if children also went negative
                 children = 0;
                 if (seniors < 0) seniors = 0; // Should not happen if totalSize >= 0
             }
             // Recalculate adults just in case
             adults = totalSize - children - seniors;
        }
         // Fallback if something went very wrong
         if (children < 0) children = 0;
         if (adults < 0) adults = 0;
         if (seniors < 0) seniors = 0;
         // Ensure total matches after corrections
         long currentTotal = children + adults + seniors;
         if (currentTotal != totalSize) {
             adults += (totalSize - currentTotal); // Adjust adults to match exact total
             adults = Math.max(0, adults); // Final safety check
         }


        // Create and populate distribution maps (using ObjectIntMap for Population)
        ObjectIntMap<PopulationType> typesMap = createDistributionMap(this.populationTypeDistribution, totalSize);
        ObjectIntMap<Culture> culturesMap = createDistributionMap(this.cultureDistribution, totalSize);
        ObjectIntMap<Religion> religionsMap = createDistributionMap(this.religionDistribution, totalSize);

        // Instantiate the Population object using its full constructor
        return new Population(
                children, adults, seniors,
                typesMap, culturesMap, religionsMap,
                this.startingLiteracy, this.startingHealth, this.startingHappiness, this.startingUnemploymentRate
        );
    }

    /**
     * Helper method to convert a distribution map (float ratios) into a count map (int counts)
     * for a given total population size, handling rounding carefully.
     */
    private <K> ObjectIntMap<K> createDistributionMap(Map<K, Float> distributionRatioMap, long totalPopulation) {
        ObjectIntMap<K> countMap = new ObjectIntMap<>();
        if (totalPopulation == 0 || distributionRatioMap.isEmpty()) {
            return countMap; // Return empty map
        }

        AtomicLong currentSum = new AtomicLong(0); // Use AtomicLong for safe modification in lambda/stream
        K largestShareKey = null;
        float largestShareRatio = -1f;

        // Initial calculation and find key with largest share for rounding adjustment
        for (Map.Entry<K, Float> entry : distributionRatioMap.entrySet()) {
            long count = Math.round(totalPopulation * entry.getValue());
             if (count > 0) { // Only add if count is positive
                countMap.put(entry.getKey(), (int) count); // Cast to int, assumes count < IntMax
                currentSum.addAndGet(count);
            }
             // Track largest for later adjustment
             if (entry.getValue() > largestShareRatio) {
                 largestShareRatio = entry.getValue();
                 largestShareKey = entry.getKey();
             }
        }

        // Adjust for rounding errors to match totalPopulation exactly
        long difference = totalPopulation - currentSum.get();

        if (difference != 0 && largestShareKey != null) {
            // Adjust the count of the group with the largest ratio
             int currentLargestCount = countMap.getOrDefault(largestShareKey, 0);
             int adjustedCount = Math.max(0, currentLargestCount + (int)difference); // Prevent negative count

             if (adjustedCount == 0) {
                 countMap.remove(largestShareKey); // Remove if adjustment makes it zero
             } else {
                 countMap.put(largestShareKey, adjustedCount);
             }
        } else if (difference != 0 && !countMap.isEmpty()) {
            // Fallback if largestShareKey somehow wasn't found but map isn't empty
            // Add/remove difference to the first element found
             Map.Entry<K, Integer> firstEntry = countMap.entrySet().iterator().next();
             int adjustedCount = Math.max(0, firstEntry.getValue() + (int)difference);
              if (adjustedCount == 0) {
                  countMap.remove(firstEntry.getKey());
              } else {
                  countMap.put(firstEntry.getKey(), adjustedCount);
              }
        }

         // Final check (optional assertion)
         long finalSum = 0;
         for(int count : countMap.values()) finalSum += count;
         assert finalSum == totalPopulation : "Final distribution sum (" + finalSum + ") doesn't match total population ("+ totalPopulation +")!";

        return countMap;
    }


    // --- Overrides ---

    @Override
    public String toString() {
        return "PopulationTemplate{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", ageRatios[C/A/S]=" + String.format("%.2f/%.2f/%.2f", childrenRatio, adultsRatio, seniorsRatio) +
               ", cultures=" + cultureDistribution.size() +
               ", religions=" + religionDistribution.size() +
               ", types=" + populationTypeDistribution.size() +
               // Add more fields if needed
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PopulationTemplate that = (PopulationTemplate) o;
        return id == that.id; // Equality is solely based on the unique ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash code must match equals, based only on ID
    }

    // --- Builder Class ---

    /**
     * Builder pattern for creating PopulationTemplate instances.
     */
    public static class Builder {
        // Required parameters
        private final short id;
        private String name;

        // Optional parameters with defaults
        private String description = "";
        private float childrenRatio = 0.25f;
        private float adultsRatio = 0.60f;
        private float seniorsRatio = 0.15f;
        private double baseBirthRateModifier = 1.0;
        private double baseDeathRateModifier = 1.0;
        private Map<Culture, Float> cultureDistribution = new HashMap<>();
        private Map<Religion, Float> religionDistribution = new HashMap<>();
        private Map<PopulationType, Float> populationTypeDistribution = new HashMap<>();
        private double startingLiteracy = 0.5;
        private double startingHealth = 60.0;
        private double startingHappiness = 0.5;
        private double startingUnemploymentRate = 0.05;

        /**
         * Starts building a template with required ID and name.
         * @param id A unique short identifier for this template.
         * @param name A human-readable name for this template.
         */
        public Builder(short id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /** Sets age ratios. Must sum to approximately 1.0. */
        public Builder ageRatios(float children, float adults, float seniors) {
            this.childrenRatio = children;
            this.adultsRatio = adults;
            this.seniorsRatio = seniors;
            return this;
        }

        public Builder birthRateModifier(double modifier) {
            this.baseBirthRateModifier = modifier;
            return this;
        }

        public Builder deathRateModifier(double modifier) {
            this.baseDeathRateModifier = modifier;
            return this;
        }

         /** Sets the culture distribution map. Ratios must sum to approx 1.0. */
        public Builder cultureDistribution(Map<Culture, Float> distribution) {
             // Store a copy to prevent external modification before build()
            this.cultureDistribution = new HashMap<>(distribution);
            return this;
        }

        /** Adds or updates a single culture ratio in the distribution. */
         public Builder culture(Culture culture, float ratio) {
             this.cultureDistribution.put(culture, ratio);
             return this;
         }


        /** Sets the religion distribution map. Ratios must sum to approx 1.0. */
        public Builder religionDistribution(Map<Religion, Float> distribution) {
            this.religionDistribution = new HashMap<>(distribution);
            return this;
        }

         /** Adds or updates a single religion ratio in the distribution. */
         public Builder religion(Religion religion, float ratio) {
             this.religionDistribution.put(religion, ratio);
             return this;
         }


        /** Sets the population type distribution map. Ratios must sum to approx 1.0. */
        public Builder populationTypeDistribution(Map<PopulationType, Float> distribution) {
            this.populationTypeDistribution = new HashMap<>(distribution);
            return this;
        }

        /** Adds or updates a single population type ratio in the distribution. */
        public Builder populationType(PopulationType type, float ratio) {
             this.populationTypeDistribution.put(type, ratio);
             return this;
         }

        public Builder literacy(double literacy) {
            this.startingLiteracy = literacy;
            return this;
        }

        public Builder health(double health) {
            this.startingHealth = health;
            return this;
        }

        public Builder happiness(double happiness) {
            this.startingHappiness = happiness;
            return this;
        }

        public Builder unemploymentRate(double rate) {
            this.startingUnemploymentRate = rate;
            return this;
        }

        /**
         * Constructs the PopulationTemplate instance with validation.
         * @return The immutable PopulationTemplate object.
         * @throws IllegalArgumentException if validation fails (e.g., ratios don't sum correctly).
         */
        public PopulationTemplate build() {
            // The PopulationTemplate constructor performs the validation
            return new PopulationTemplate(this);
        }
    }
}
