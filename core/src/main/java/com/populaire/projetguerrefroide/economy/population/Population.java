package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.national.Culture;
import com.populaire.projetguerrefroide.national.Religion;

import java.util.Map;
import java.util.Objects;
import java.util.Random; // For simulation aspects

/**
 * Represents a population aggregate within a specific region or context (e.g., a province, a state).
 * Tracks demographic breakdown by age, type (job/stratum), culture, and religion,
 * along with socio-economic indicators and methods for simulation.
 */
public class Population {

    // --- Core Demographics ---
    private long amountChildren;
    private long amountAdults;
    private long amountSeniors;

    // --- Distributions (Using ObjectIntMap for performance) ---
    // Maps PopulationType (e.g., Farmer, Worker, Aristocrat) to count
    private final ObjectIntMap<PopulationType> populationsByType;
    // Maps Culture to count
    private final ObjectIntMap<Culture> populationsByCulture;
    // Maps Religion to count
    private final ObjectIntMap<Religion> populationsByReligion;

    // --- Socio-Cultural & Economic Indicators ---
    private double averageLiteracy; // e.g., 0.0 to 1.0
    private double averageHealth;   // e.g., arbitrary scale, influences birth/death rates
    private double averageHappiness; // e.g., 0.0 to 1.0, influences stability, migration
    private double unemploymentRate; // e.g., 0.0 to 1.0 (proportion of workforce)

    // Random number generator for stochastic simulation aspects
    private static final Random random = new Random();

    /**
     * Full constructor. It's recommended to use factory methods or ensure consistency externally.
     * Assumes the counts in the maps sum up correctly to the total population initially.
     *
     * @param amountChildren      Number of children.
     * @param amountAdults        Number of adults (typically the workforce).
     * @param amountSeniors       Number of seniors.
     * @param populationsByType   Map of PopulationType counts.
     * @param populationsByCulture Map of Culture counts.
     * @param populationsByReligion Map of Religion counts.
     * @param averageLiteracy     Initial average literacy (0-1).
     * @param averageHealth       Initial average health.
     * @param averageHappiness    Initial average happiness (0-1).
     * @param unemploymentRate    Initial unemployment rate (0-1).
     */
    public Population(long amountChildren, long amountAdults, long amountSeniors,
                      ObjectIntMap<PopulationType> populationsByType,
                      ObjectIntMap<Culture> populationsByCulture,
                      ObjectIntMap<Religion> populationsByReligion,
                      double averageLiteracy, double averageHealth, double averageHappiness, double unemploymentRate) {

        if (amountChildren < 0 || amountAdults < 0 || amountSeniors < 0) {
            throw new IllegalArgumentException("Population amounts cannot be negative.");
        }
        // Basic validation - more thorough checks in ensureConsistency/validateState
        long totalByAge = amountChildren + amountAdults + amountSeniors;
        if (sumMapValues(populationsByType) != totalByAge ||
            sumMapValues(populationsByCulture) != totalByAge ||
            sumMapValues(populationsByReligion) != totalByAge) {
             // Warning or exception based on desired strictness
             System.err.println("Warning: Initial map totals do not match age group total. Ensure consistency.");
             // Or throw new IllegalArgumentException("Initial map totals must match age group total.");
        }


        this.amountChildren = amountChildren;
        this.amountAdults = amountAdults;
        this.amountSeniors = amountSeniors;
        // Create copies to prevent external modification of original maps
        this.populationsByType = new ObjectIntMap<>(populationsByType);
        this.populationsByCulture = new ObjectIntMap<>(populationsByCulture);
        this.populationsByReligion = new ObjectIntMap<>(populationsByReligion);
        this.averageLiteracy = Math.max(0.0, Math.min(1.0, averageLiteracy));
        this.averageHealth = averageHealth;
        this.averageHappiness = Math.max(0.0, Math.min(1.0, averageHappiness));
        this.unemploymentRate = Math.max(0.0, Math.min(1.0, unemploymentRate));

        validateState(); // Validate initial state
    }

    /**
     * Creates an empty population object.
     */
    public Population() {
        this(0, 0, 0, new ObjectIntMap<>(), new ObjectIntMap<>(), new ObjectIntMap<>(), 0.0, 50.0, 0.5, 0.05); // Default values
    }

    /**
     * Factory method to create a basic initial population structure.
     *
     * @param totalPopulation The desired total initial population.
     * @param initialCulture  The dominant starting culture.
     * @param initialReligion The dominant starting religion.
     * @param initialPopType  The dominant starting population type.
     * @return A newly configured Population object.
     */
    public static Population createInitialPopulation(long totalPopulation, Culture initialCulture, Religion initialReligion, PopulationType initialPopType) {
        if (totalPopulation < 0) totalPopulation = 0;

        // Example distribution - adjust as needed!
        long children = Math.round(totalPopulation * 0.25);
        long seniors = Math.round(totalPopulation * 0.15);
        long adults = totalPopulation - children - seniors;
        if (adults < 0) adults = 0; // Ensure non-negative

        ObjectIntMap<PopulationType> types = new ObjectIntMap<>();
        types.put(initialPopType, (int)totalPopulation); // Simplified: all one type

        ObjectIntMap<Culture> cultures = new ObjectIntMap<>();
        cultures.put(initialCulture, (int)totalPopulation); // Simplified: all one culture

        ObjectIntMap<Religion> religions = new ObjectIntMap<>();
        religions.put(initialReligion, (int)totalPopulation); // Simplified: all one religion

        // Default socio-economic values - adjust as needed
        double literacy = 0.3;
        double health = 60.0;
        double happiness = 0.5;
        double unemployment = 0.05;

        return new Population(children, adults, seniors, types, cultures, religions, literacy, health, happiness, unemployment);
    }

    // --- Getters ---

    public long getAmountChildren() { return this.amountChildren; }
    public long getAmountAdults() { return this.amountAdults; }
    public long getAmountSeniors() { return this.amountSeniors; }
    public double getAverageLiteracy() { return this.averageLiteracy; }
    public double getAverageHealth() { return this.averageHealth; }
    public double getAverageHappiness() { return this.averageHappiness; }
    public double getUnemploymentRate() { return this.unemploymentRate; }

    /**
     * Gets the total population count (children + adults + seniors).
     * @return Total population.
     */
    public long getTotalPopulation() {
        return this.amountChildren + this.amountAdults + this.amountSeniors;
    }

    /**
     * Gets the potential workforce size (typically the number of adults).
     * @return Workforce size.
     */
    public long getWorkforce() {
        return this.amountAdults;
    }

    /**
     * Gets the number of unemployed individuals.
     * @return Number of unemployed people.
     */
    public long getUnemployedCount() {
        return Math.round(getWorkforce() * this.unemploymentRate);
    }

    /**
     * Gets the number of employed individuals.
     * @return Number of employed people.
     */
     public long getEmployedCount() {
        return getWorkforce() - getUnemployedCount();
     }


    /**
     * Returns a defensive copy of the population distribution by type map.
     * @return A copy of the map.
     */
    public ObjectIntMap<PopulationType> getPopulationsByType() {
        return new ObjectIntMap<>(this.populationsByType); // Return copy
    }

    /**
     * Returns a defensive copy of the population distribution by culture map.
     * @return A copy of the map.
     */
    public ObjectIntMap<Culture> getPopulationsByCulture() {
        return new ObjectIntMap<>(this.populationsByCulture); // Return copy
    }

     /**
     * Returns a defensive copy of the population distribution by religion map.
     * @return A copy of the map.
     */
    public ObjectIntMap<Religion> getPopulationsByReligion() {
        return new ObjectIntMap<>(this.populationsByReligion); // Return copy
    }

    /**
     * Calculates the dependency ratio (proportion of non-working age population to working age).
     * @return Dependency ratio, or 0 if no adults.
     */
    public double getDependencyRatio() {
        if (this.amountAdults == 0) {
            return 0.0; // Or Infinity/NaN depending on desired handling
        }
        return (double)(this.amountChildren + this.amountSeniors) / this.amountAdults;
    }

    /**
     * Gets the percentage of the total population belonging to a specific culture.
     * @param culture The culture to check.
     * @return Percentage (0.0 to 1.0), or 0.0 if total population is zero.
     */
    public double getCulturePercentage(Culture culture) {
        long total = getTotalPopulation();
        if (total == 0) return 0.0;
        return (double) this.populationsByCulture.getOrDefault(culture, 0) / total;
    }

     /**
     * Gets the percentage of the total population belonging to a specific religion.
     * @param religion The religion to check.
     * @return Percentage (0.0 to 1.0), or 0.0 if total population is zero.
     */
     public double getReligionPercentage(Religion religion) {
         long total = getTotalPopulation();
         if (total == 0) return 0.0;
         return (double) this.populationsByReligion.getOrDefault(religion, 0) / total;
     }

     /**
      * Gets the percentage of the total population belonging to a specific population type.
      * @param type The PopulationType to check.
      * @return Percentage (0.0 to 1.0), or 0.0 if total population is zero.
      */
      public double getPopulationTypePercentage(PopulationType type) {
          long total = getTotalPopulation();
          if (total == 0) return 0.0;
          return (double) this.populationsByType.getOrDefault(type, 0) / total;
      }

    /**
     * Finds the dominant culture (most numerous).
     * @return The dominant Culture, or null if population is empty.
     */
    public Culture getDominantCulture() {
        return getDominantKey(this.populationsByCulture);
    }

    /**
     * Finds the dominant religion (most numerous).
     * @return The dominant Religion, or null if population is empty.
     */
    public Religion getDominantReligion() {
        return getDominantKey(this.populationsByReligion);
    }

     /**
      * Finds the dominant population type (most numerous).
      * @return The dominant PopulationType, or null if population is empty.
      */
      public PopulationType getDominantPopulationType() {
          return getDominantKey(this.populationsByType);
      }

    // Helper for finding dominant key in maps
    private <K> K getDominantKey(ObjectIntMap<K> map) {
        if (map.isEmpty()) return null;
        K dominantKey = null;
        int maxCount = -1;
        for (ObjectIntMap.Entry<K> entry : map) {
            if (entry.value > maxCount) {
                maxCount = entry.value;
                dominantKey = entry.key;
            }
        }
        return dominantKey;
    }


    // --- Setters and Modifiers ---

    // Direct setters - use with caution, prefer add/modify methods for consistency
    public void setAmountChildren(long amount) { this.amountChildren = Math.max(0, amount); ensureConsistency(); }
    public void setAmountAdults(long amount) { this.amountAdults = Math.max(0, amount); ensureConsistency(); }
    public void setAmountSeniors(long amount) { this.amountSeniors = Math.max(0, amount); ensureConsistency(); }
    public void setAverageLiteracy(double value) { this.averageLiteracy = Math.max(0.0, Math.min(1.0, value)); }
    public void setAverageHealth(double value) { this.averageHealth = value; } // Consider bounds?
    public void setAverageHappiness(double value) { this.averageHappiness = Math.max(0.0, Math.min(1.0, value)); }
    public void setUnemploymentRate(double value) { this.unemploymentRate = Math.max(0.0, Math.min(1.0, value)); }

    /** Adds (or removes, if negative) population to a specific age group.
     * IMPORTANT: This method ONLY changes the age group total. It does NOT automatically
     * adjust the distribution maps (cultures, religions, types). Call ensureConsistency()
     * or handle distribution changes manually after using this.
     *
     * @param delta The change in population (can be negative).
     * @param ageGroup The age group to modify ("children", "adults", "seniors").
     * @return The actual change applied (e.g., won't go below zero).
     */
    public long addPopulationByAge(long delta, String ageGroup) {
         long actualDelta = 0;
         switch (ageGroup.toLowerCase()) {
             case "children":
                 actualDelta = Math.max(-this.amountChildren, delta); // Don't go below 0
                 this.amountChildren += actualDelta;
                 break;
             case "adults":
                 actualDelta = Math.max(-this.amountAdults, delta);
                 this.amountAdults += actualDelta;
                 break;
             case "seniors":
                 actualDelta = Math.max(-this.amountSeniors, delta);
                 this.amountSeniors += actualDelta;
                 break;
             default:
                 throw new IllegalArgumentException("Invalid age group: " + ageGroup);
         }
         // Caller MUST handle distribution changes or call ensureConsistency()
         return actualDelta;
     }


    /**
     * Modifies the count for a specific PopulationType.
     * Ensures the count doesn't go below zero.
     * IMPORTANT: This assumes the change corresponds to a change in *total* population.
     * Call ensureConsistency() or manually adjust age group totals if needed.
     *
     * @param type  The PopulationType to modify.
     * @param delta The change in count (can be negative).
     */
    public void modifyPopulationTypeCount(PopulationType type, int delta) {
        Objects.requireNonNull(type, "PopulationType cannot be null");
        int current = this.populationsByType.getOrDefault(type, 0);
        int actualDelta = Math.max(-current, delta); // Prevent going below zero
        this.populationsByType.put(type, current + actualDelta);
        if (this.populationsByType.get(type) == 0) { // Clean up zero entries
            this.populationsByType.remove(type);
        }
        // Caller may need to adjust age totals or call ensureConsistency()
    }

    /**
     * Modifies the count for a specific Culture.
     * Ensures the count doesn't go below zero.
     * IMPORTANT: This assumes the change corresponds to a change in *total* population
     * OR is part of an assimilation/conversion process where total pop remains constant.
     * Call ensureConsistency() or manually adjust age group totals if adding/removing population.
     *
     * @param culture The Culture to modify.
     * @param delta   The change in count (can be negative).
     */
    public void modifyCultureCount(Culture culture, int delta) {
        Objects.requireNonNull(culture, "Culture cannot be null");
        int current = this.populationsByCulture.getOrDefault(culture, 0);
        int actualDelta = Math.max(-current, delta);
        this.populationsByCulture.put(culture, current + actualDelta);
         if (this.populationsByCulture.get(culture) == 0) {
             this.populationsByCulture.remove(culture);
         }
        // Caller may need to adjust age totals or call ensureConsistency()
    }

    /**
     * Modifies the count for a specific Religion.
     * Ensures the count doesn't go below zero.
     * IMPORTANT: See importance notice on modifyCultureCount.
     *
     * @param religion The Religion to modify.
     * @param delta    The change in count (can be negative).
     */
    public void modifyReligionCount(Religion religion, int delta) {
        Objects.requireNonNull(religion, "Religion cannot be null");
        int current = this.populationsByReligion.getOrDefault(religion, 0);
        int actualDelta = Math.max(-current, delta);
        this.populationsByReligion.put(religion, current + actualDelta);
        if (this.populationsByReligion.get(religion) == 0) {
            this.populationsByReligion.remove(religion);
        }
       // Caller may need to adjust age totals or call ensureConsistency()
    }


    // --- Simulation Methods ---

    /**
     * Updates population based on birth, death, and aging processes over a time step.
     * This is a simplified model; specifics depend heavily on game design.
     *
     * @param timeStep             Time elapsed (e.g., in days, months, years).
     * @param baseBirthRate        Base births per 1000 adults per time unit (modified by health).
     * @param baseChildDeathRate   Base deaths per 1000 children per time unit.
     * @param baseAdultDeathRate   Base deaths per 1000 adults per time unit.
     * @param baseSeniorDeathRate  Base deaths per 1000 seniors per time unit.
     * @param agingRateFactor      Factor determining how quickly people age between groups.
     */
    public void updateDemographics(double timeStep, double baseBirthRate,
                                    double baseChildDeathRate, double baseAdultDeathRate, double baseSeniorDeathRate,
                                    double agingRateFactor) {

        long initialTotal = getTotalPopulation();
        if (initialTotal == 0) return; // No population to update

        // --- Calculate factors ---
        // Example: Higher health slightly increases birth rate and decreases death rates
        double healthFactorBirth = 1.0 + (this.averageHealth - 50.0) / 100.0 * 0.5; // Scaled effect
        double healthFactorDeath = Math.max(0.1, 1.0 - (this.averageHealth - 50.0) / 100.0 * 0.8); // Scaled effect, min 0.1

        // --- Births ---
        // Births depend on adult population and birth rate (influenced by health)
        double effectiveBirthRate = baseBirthRate * healthFactorBirth * timeStep / 1000.0;
        long newBirths = Math.round(this.amountAdults * effectiveBirthRate * (1.0 + (random.nextDouble() - 0.5) * 0.1)); // Add small randomness
        newBirths = Math.max(0, newBirths);

        // --- Deaths ---
        // Deaths occur in each age group based on their respective rates (influenced by health)
        double effectiveChildDeathRate = baseChildDeathRate * healthFactorDeath * timeStep / 1000.0;
        double effectiveAdultDeathRate = baseAdultDeathRate * healthFactorDeath * timeStep / 1000.0;
        double effectiveSeniorDeathRate = baseSeniorDeathRate * healthFactorDeath * timeStep / 1000.0;

        long childDeaths = Math.min(this.amountChildren, Math.round(this.amountChildren * effectiveChildDeathRate * (1.0 + (random.nextDouble() - 0.5) * 0.1)));
        long adultDeaths = Math.min(this.amountAdults, Math.round(this.amountAdults * effectiveAdultDeathRate * (1.0 + (random.nextDouble() - 0.5) * 0.1)));
        long seniorDeaths = Math.min(this.amountSeniors, Math.round(this.amountSeniors * effectiveSeniorDeathRate * (1.0 + (random.nextDouble() - 0.5) * 0.1)));
        childDeaths = Math.max(0, childDeaths);
        adultDeaths = Math.max(0, adultDeaths);
        seniorDeaths = Math.max(0, seniorDeaths);


        // --- Aging ---
        // A fraction of each group ages into the next group
        double agingRate = agingRateFactor * timeStep; // Rate per time step
        long childrenAging = Math.min(this.amountChildren - childDeaths, Math.round((this.amountChildren - childDeaths) * agingRate * (1.0 + (random.nextDouble() - 0.5) * 0.05)));
        long adultsAging = Math.min(this.amountAdults - adultDeaths, Math.round((this.amountAdults - adultDeaths) * agingRate * 0.8 * (1.0 + (random.nextDouble() - 0.5) * 0.05))); // Adults age slower?
        childrenAging = Math.max(0, childrenAging);
        adultsAging = Math.max(0, adultsAging);

        // --- Apply Changes ---
        this.amountChildren += newBirths - childDeaths - childrenAging;
        this.amountAdults += childrenAging - adultDeaths - adultsAging;
        this.amountSeniors += adultsAging - seniorDeaths;

        // Ensure non-negative amounts
        this.amountChildren = Math.max(0, this.amountChildren);
        this.amountAdults = Math.max(0, this.amountAdults);
        this.amountSeniors = Math.max(0, this.amountSeniors);

        // --- Update Distributions (CRITICAL & SIMPLIFIED) ---
        // This is a major simplification. Births should ideally inherit parent culture/religion.
        // Deaths/aging should proportionally reduce counts across all distributions.
        // A truly accurate model requires more complex tracking or assumptions.
        // Here, we crudely scale existing distributions to the new total.
        long newTotal = getTotalPopulation();
        if (initialTotal > 0 && newTotal != initialTotal) {
             double scaleFactor = (double) newTotal / initialTotal;
             scaleMapValues(this.populationsByType, scaleFactor);
             scaleMapValues(this.populationsByCulture, scaleFactor);
             scaleMapValues(this.populationsByReligion, scaleFactor);
        }

        // Add new births to dominant groups (another simplification)
        Culture dominantCulture = getDominantCulture();
        Religion dominantReligion = getDominantReligion();
        PopulationType dominantType = getDominantPopulationType(); // Or assign to a default type like 'Unemployed'/'Child'

        if (dominantCulture != null && newBirths > 0) modifyCultureCount(dominantCulture, (int) newBirths); // Potential overflow if newBirths > IntMax
        if (dominantReligion != null && newBirths > 0) modifyReligionCount(dominantReligion, (int) newBirths);
        if (dominantType != null && newBirths > 0) modifyPopulationTypeCount(dominantType, (int) newBirths); // Needs refinement - children might not have a type


        ensureConsistency(); // Recalculate and validate totals
    }

    // Helper to scale map values
    private <K> void scaleMapValues(ObjectIntMap<K> map, double factor) {
        if (factor <= 0) {
            map.clear();
            return;
        }
        ObjectIntMap<K> tempMap = new ObjectIntMap<>(map.size());
        long currentSum = 0;
        for (ObjectIntMap.Entry<K> entry : map) {
            int newValue = Math.max(0, (int) Math.round(entry.value * factor));
            if (newValue > 0) {
                tempMap.put(entry.key, newValue);
                currentSum += newValue;
            }
        }
         map.clear();
         map.putAll(tempMap);

         // Adjust for rounding errors - add/remove difference to largest group
         long targetSum = getTotalPopulation(); // Use the adjusted total pop
         long diff = targetSum - currentSum;
         if (diff != 0 && !map.isEmpty()) {
              K dominantKey = getDominantKey(map);
              if (dominantKey != null) {
                   map.put(dominantKey, Math.max(0, map.getOrDefault(dominantKey, 0) + (int)diff)); // Potential overflow
                   if (map.get(dominantKey) <= 0) map.remove(dominantKey);
              }
         }
    }


    /**
     * Applies assimilation pressure, potentially converting population from minority cultures
     * to the dominant culture over time. Simplified model.
     * @param assimilationFactor Base rate of assimilation per time unit.
     * @param dominantCulturePressure Pressure multiplier from dominant culture presence/policies.
     * @param timeStep Time elapsed.
     */
    public void applyAssimilation(double assimilationFactor, double dominantCulturePressure, double timeStep) {
        Culture dominantCulture = getDominantCulture();
        if (dominantCulture == null || this.populationsByCulture.size() <= 1) {
            return; // No assimilation needed if empty or monocultural
        }

        long totalPop = getTotalPopulation();
        if (totalPop == 0) return;

        double dominantCultureRatio = getCulturePercentage(dominantCulture);

        // Iterate over a copy of keys to avoid concurrent modification issues
        var keys = populationsByCulture.keySet().toArray(new Culture[0]);
        int totalConverted = 0;

        for (Culture culture : keys) {
            if (culture.equals(dominantCulture)) continue;

            int currentCount = this.populationsByCulture.getOrDefault(culture, 0);
            if (currentCount == 0) continue;

            // Rate influenced by base factor, dominant culture ratio, pressure, and maybe pop happiness/stability
            double conversionRate = assimilationFactor * dominantCultureRatio * dominantCulturePressure * timeStep;
            conversionRate *= (1.0 + (random.nextDouble() - 0.5) * 0.1); // Randomness

            int converted = Math.min(currentCount, (int) Math.round(currentCount * conversionRate)); // Potential overflow
            converted = Math.max(0, converted);

            if (converted > 0) {
                // Decrease minority culture, increase dominant culture
                modifyCultureCount(culture, -converted); // Will clean up if it hits zero
                totalConverted += converted;
            }
        }

        if (totalConverted > 0) {
             modifyCultureCount(dominantCulture, totalConverted);
        }
        // No need for ensureConsistency here as total population *should* remain the same
        validateState(); // Validate just in case
    }


   /**
    * Applies religious conversion pressure. Similar logic to assimilation.
    * @param conversionFactor Base rate of conversion per time unit.
    * @param dominantReligionPressure Pressure multiplier.
    * @param timeStep Time elapsed.
    */
   public void applyConversion(double conversionFactor, double dominantReligionPressure, double timeStep) {
       Religion dominantReligion = getDominantReligion();
       if (dominantReligion == null || this.populationsByReligion.size() <= 1) {
           return;
       }
       // ... similar logic to applyAssimilation ...
        var keys = populationsByReligion.keySet().toArray(new Religion[0]);
        int totalConverted = 0;

        for (Religion religion : keys) {
            // ... calculate conversion rate and amount ...
            // ... modifyReligionCount(religion, -converted); ...
            // ... totalConverted += converted; ...
        }
        // ... modifyReligionCount(dominantReligion, totalConverted); ...

       validateState();
   }


    /**
     * Applies migration effects to the population.
     * @param immigrants A Population object representing arriving migrants (null if none).
     * @param emigrationRate Proportion of the current population emigrating (0.0 to 1.0).
     * @return A Population object representing emigrants (can be null).
     */
    public Population applyMigration(Population immigrants, double emigrationRate) {
        Population emigrants = null;
        emigrationRate = Math.max(0.0, Math.min(1.0, emigrationRate));

        // Handle emigration first
        if (emigrationRate > 0 && getTotalPopulation() > 0) {
            emigrants = this.split(emigrationRate); // Split creates the emigrant pop and removes them from this
        }

        // Handle immigration
        if (immigrants != null && immigrants.getTotalPopulation() > 0) {
            this.merge(immigrants); // Merge adds the immigrant pop to this
        }

        ensureConsistency();
        return emigrants;
    }

    // --- Merging and Splitting ---

    /**
     * Merges another Population object's data into this one.
     * @param other The Population object to merge (will not be modified).
     */
    public void merge(Population other) {
        if (other == null) return;

        this.amountChildren += other.amountChildren;
        this.amountAdults += other.amountAdults;
        this.amountSeniors += other.amountSeniors;

        // Merge maps by adding counts
        mergeMap(this.populationsByType, other.populationsByType);
        mergeMap(this.populationsByCulture, other.populationsByCulture);
        mergeMap(this.populationsByReligion, other.populationsByReligion);

        // Average socio-economic indicators (weighted by population)
        long totalPop = getTotalPopulation();
        long otherTotalPop = other.getTotalPopulation();
        long combinedTotalPop = totalPop + otherTotalPop; // Note: totalPop already includes other pop counts here

        if (combinedTotalPop > 0) {
            long currentTotalBeforeMerge = totalPop; // Total pop of *this* before adding other's counts
             this.averageLiteracy = ((this.averageLiteracy * currentTotalBeforeMerge) + (other.averageLiteracy * otherTotalPop)) / combinedTotalPop;
             this.averageHealth = ((this.averageHealth * currentTotalBeforeMerge) + (other.averageHealth * otherTotalPop)) / combinedTotalPop;
             this.averageHappiness = ((this.averageHappiness * currentTotalBeforeMerge) + (other.averageHappiness * otherTotalPop)) / combinedTotalPop;
             // Unemployment needs careful thought - maybe weighted average or recalculate based on jobs?
             // Simplified weighted average:
             this.unemploymentRate = ((this.unemploymentRate * this.getWorkforce()) + (other.unemploymentRate * other.getWorkforce())) / (this.getWorkforce() + other.getWorkforce());
        }

        ensureConsistency();
    }

    // Helper for merging maps
    private <K> void mergeMap(ObjectIntMap<K> targetMap, ObjectIntMap<K> sourceMap) {
        for (ObjectIntMap.Entry<K> entry : sourceMap) {
            targetMap.put(entry.key, targetMap.getOrDefault(entry.key, 0) + entry.value);
        }
    }

    /**
     * Splits off a fraction of this population into a new Population object.
     * This population object is reduced by the split amount.
     *
     * @param fraction The fraction to split off (0.0 to 1.0).
     * @return A new Population object representing the split portion.
     */
    public Population split(double fraction) {
        fraction = Math.max(0.0, Math.min(1.0, fraction));
        if (fraction == 0.0) return new Population(); // Split nothing
        if (fraction == 1.0) {
            // Split everything - create a copy and clear this one
            Population splitPop = new Population(this.amountChildren, this.amountAdults, this.amountSeniors,
                                                 this.populationsByType, this.populationsByCulture, this.populationsByReligion,
                                                 this.averageLiteracy, this.averageHealth, this.averageHappiness, this.unemploymentRate);
            this.amountChildren = 0;
            this.amountAdults = 0;
            this.amountSeniors = 0;
            this.populationsByType.clear();
            this.populationsByCulture.clear();
            this.populationsByReligion.clear();
            // Reset averages? Or keep them? Let's keep them for the original pop shell.
            ensureConsistency();
            return splitPop;
        }

        // Calculate split amounts for age groups
        long splitChildren = Math.round(this.amountChildren * fraction);
        long splitAdults = Math.round(this.amountAdults * fraction);
        long splitSeniors = Math.round(this.amountSeniors * fraction);

        // Create new maps for the split population
        ObjectIntMap<PopulationType> splitTypes = splitMapFraction(this.populationsByType, fraction);
        ObjectIntMap<Culture> splitCultures = splitMapFraction(this.populationsByCulture, fraction);
        ObjectIntMap<Religion> splitReligions = splitMapFraction(this.populationsByReligion, fraction);

        // Reduce this population
        this.amountChildren -= splitChildren;
        this.amountAdults -= splitAdults;
        this.amountSeniors -= splitSeniors;
        subtractMap(this.populationsByType, splitTypes);
        subtractMap(this.populationsByCulture, splitCultures);
        subtractMap(this.populationsByReligion, splitReligions);

        // Create the new split population object (keeps same socio-economic averages)
        Population splitPop = new Population(splitChildren, splitAdults, splitSeniors,
                                             splitTypes, splitCultures, splitReligions,
                                             this.averageLiteracy, this.averageHealth, this.averageHappiness, this.unemploymentRate); // Maybe recalculate unemployment?

        ensureConsistency(); // Validate both populations
        splitPop.ensureConsistency();

        return splitPop;
    }

    // Helper to create a map with fractional amounts
     private <K> ObjectIntMap<K> splitMapFraction(ObjectIntMap<K> sourceMap, double fraction) {
         ObjectIntMap<K> splitMap = new ObjectIntMap<>();
         long sourceSum = sumMapValues(sourceMap);
         long targetSplitSum = Math.round(sourceSum * fraction);
         long currentSplitSum = 0;

         for (ObjectIntMap.Entry<K> entry : sourceMap) {
             int splitAmount = (int) Math.round(entry.value * fraction); // Potential overflow
             if (splitAmount > 0) {
                 splitMap.put(entry.key, splitAmount);
                 currentSplitSum += splitAmount;
             }
         }
         // Adjust for rounding errors in the split map
         long diff = targetSplitSum - currentSplitSum;
         if (diff != 0 && !splitMap.isEmpty()) {
              K dominantKey = getDominantKey(splitMap); // Add/remove difference to largest group in split map
              if (dominantKey != null) {
                  splitMap.put(dominantKey, Math.max(0, splitMap.getOrDefault(dominantKey, 0) + (int)diff));
                   if (splitMap.get(dominantKey) <= 0) splitMap.remove(dominantKey);
              }
         }
         return splitMap;
     }


    // Helper to subtract map counts
    private <K> void subtractMap(ObjectIntMap<K> targetMap, ObjectIntMap<K> subtractMap) {
        for (ObjectIntMap.Entry<K> entry : subtractMap) {
            int current = targetMap.getOrDefault(entry.key, 0);
            int newValue = Math.max(0, current - entry.value);
            if (newValue == 0) {
                targetMap.remove(entry.key);
            } else {
                targetMap.put(entry.key, newValue);
            }
        }
    }

    // --- Validation and Consistency ---

    /**
     * Attempts to ensure consistency between age group totals and distribution map totals.
     * This is a basic implementation; complex scenarios might require manual adjustments.
     * It scales the maps to match the current age group total.
     */
    public void ensureConsistency() {
        long totalByAge = getTotalPopulation();
        long totalByType = sumMapValues(populationsByType);
        long totalByCulture = sumMapValues(populationsByCulture);
        long totalByReligion = sumMapValues(populationsByReligion);

        // If totals don't match, rescale maps. This loses granular info but enforces totals.
        if (totalByAge != totalByType && totalByType != 0) {
            System.err.printf("Warning: Rescaling PopType map (Age: %d, Map: %d)%n", totalByAge, totalByType);
            scaleMapValues(populationsByType, (double) totalByAge / totalByType);
        } else if (totalByAge != 0 && totalByType == 0 && !populationsByType.isEmpty()) {
            populationsByType.clear(); // Clear map if total is zero
        }

        if (totalByAge != totalByCulture && totalByCulture != 0) {
             System.err.printf("Warning: Rescaling Culture map (Age: %d, Map: %d)%n", totalByAge, totalByCulture);
             scaleMapValues(populationsByCulture, (double) totalByAge / totalByCulture);
        } else if (totalByAge != 0 && totalByCulture == 0 && !populationsByCulture.isEmpty()) {
             populationsByCulture.clear();
        }


        if (totalByAge != totalByReligion && totalByReligion != 0) {
             System.err.printf("Warning: Rescaling Religion map (Age: %d, Map: %d)%n", totalByAge, totalByReligion);
             scaleMapValues(populationsByReligion, (double) totalByAge / totalByReligion);
        } else if (totalByAge != 0 && totalByReligion == 0 && !populationsByReligion.isEmpty()) {
             populationsByReligion.clear();
        }


        validateState(); // Final validation check
    }

    /**
     * Validates the internal state using assertions. Checks for negative values
     * and consistency between age totals and map totals.
     */
    public void validateState() {
        assert amountChildren >= 0 : "Negative children count";
        assert amountAdults >= 0 : "Negative adults count";
        assert amountSeniors >= 0 : "Negative seniors count";

        long totalByAge = getTotalPopulation();
        long totalByType = sumMapValues(populationsByType);
        long totalByCulture = sumMapValues(populationsByCulture);
        long totalByReligion = sumMapValues(populationsByReligion);

        assert totalByAge == totalByType : "Mismatch between total population by age (" + totalByAge + ") and by type (" + totalByType + ")";
        assert totalByAge == totalByCulture : "Mismatch between total population by age (" + totalByAge + ") and by culture (" + totalByCulture + ")";
        assert totalByAge == totalByReligion : "Mismatch between total population by age (" + totalByAge + ") and by religion (" + totalByReligion + ")";

        assert averageLiteracy >= 0.0 && averageLiteracy <= 1.0 : "Literacy out of bounds";
        assert averageHappiness >= 0.0 && averageHappiness <= 1.0 : "Happiness out of bounds";
        assert unemploymentRate >= 0.0 && unemploymentRate <= 1.0 : "Unemployment out of bounds";

        // Check map values are non-negative (handled by modifiers, but good check)
        for (int count : populationsByType.values()) assert count >= 0 : "Negative count in PopType map";
        for (int count : populationsByCulture.values()) assert count >= 0 : "Negative count in Culture map";
        for (int count : populationsByReligion.values()) assert count >= 0 : "Negative count in Religion map";
    }

    // Helper to sum map values
    private <K> long sumMapValues(ObjectIntMap<K> map) {
        long sum = 0;
        for (int value : map.values()) {
            if (value < 0) {
                 System.err.println("Warning: Negative value found in map during sum: " + value);
                 // Or assert false;
            }
            sum += value;
        }
        return sum;
    }


    // --- Overrides ---

    @Override
    public String toString() {
        return "Population{" +
               "Total=" + getTotalPopulation() +
               " [C=" + amountChildren +
               ", A=" + amountAdults +
               ", S=" + amountSeniors +
               "], Workforce=" + getWorkforce() +
               ", Unemployed=" + String.format("%.1f%%", unemploymentRate * 100) +
               ", Literacy=" + String.format("%.1f%%", averageLiteracy * 100) +
               ", Health=" + String.format("%.1f", averageHealth) +
               ", Happiness=" + String.format("%.1f%%", averageHappiness * 100) +
               ", Types=" + populationsByType.size() +
               ", Cultures=" + populationsByCulture.size() +
               ", Religions=" + populationsByReligion.size() +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Population that = (Population) o;
        // Compare all fields for equality
        return amountChildren == that.amountChildren &&
               amountAdults == that.amountAdults &&
               amountSeniors == that.amountSeniors &&
               Double.compare(that.averageLiteracy, averageLiteracy) == 0 &&
               Double.compare(that.averageHealth, averageHealth) == 0 &&
               Double.compare(that.averageHappiness, averageHappiness) == 0 &&
               Double.compare(that.unemploymentRate, unemploymentRate) == 0 &&
               Objects.equals(populationsByType, that.populationsByType) && // Relies on ObjectIntMap equals
               Objects.equals(populationsByCulture, that.populationsByCulture) &&
               Objects.equals(populationsByReligion, that.populationsByReligion);
    }

    @Override
    public int hashCode() {
        // Generate hash code based on all fields
        return Objects.hash(amountChildren, amountAdults, amountSeniors,
                            populationsByType, populationsByCulture, populationsByReligion, // Relies on ObjectIntMap hashCode
                            averageLiteracy, averageHealth, averageHappiness, unemploymentRate);
    }
}
