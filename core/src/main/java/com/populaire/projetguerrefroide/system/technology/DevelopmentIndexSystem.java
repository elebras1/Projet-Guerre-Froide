package com.populaire.projetguerrefroide.system.technology;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.population.PopulationMobilityUtils;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

public class DevelopmentIndexSystem {
    private final EcsConstants ecsConstants;
    private int worldDay = -1;
    private double worldOutput;
    private double worldAdults;

    public DevelopmentIndexSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("WorldOutputSystem")
            .kind(phaseId)
            .with(CountryDevelopment.class)
            .with(CountryMarket.class)
            .with(CountryStrategicStock.class)
            .with(CountryDemographics.class)
            .iter(this::sumWorld);
        ecsWorld.system("DevelopmentIndexSystem")
            .kind(phaseId)
            .with(CountryDevelopment.class)
            .with(CountryMarket.class)
            .with(CountryStrategicStock.class)
            .with(CountryDemographics.class)
            .iter(this::updateIndex);
    }

    private void sumWorld(Iter iter) {
        World world = iter.stage();
        if (!PopulationMobilityUtils.isMobilityDay(world, this.ecsConstants)) {
            return;
        }
        int day = world.obtainEntityView(this.ecsConstants.economyCalendar()).<EconomyCalendarView>getView(EconomyCalendar.class).day();
        if (day != this.worldDay) {
            this.worldDay = day;
            this.worldOutput = 0.0;
            this.worldAdults = 0.0;
        }
        float[] civilianGoodCosts = this.ecsConstants.civilianGoodCosts();
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 1);
        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 2);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 3);
        for (int i = 0; i < iter.count(); i++) {
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            this.worldOutput += this.realOutput(countryMarketField.getMutView(i), countryStrategicStockField.getMutView(i), civilianGoodCosts);
            this.worldAdults += countryDemographics.totalAdults();
        }
    }

    private void updateIndex(Iter iter) {
        if (!PopulationMobilityUtils.isMobilityDay(iter.stage(), this.ecsConstants)) {
            return;
        }
        float[] civilianGoodCosts = this.ecsConstants.civilianGoodCosts();
        double worldOutputPerAdult = this.worldAdults > 0.0 ? this.worldOutput / this.worldAdults : 0.0;
        Field<CountryDevelopment> countryDevelopmentField = iter.field(CountryDevelopment.class, 0);
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 1);
        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 2);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 3);
        for (int i = 0; i < iter.count(); i++) {
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            long adults = countryDemographics.totalAdults();
            if (adults <= 0L) {
                continue;
            }
            CountryDevelopmentMutView countryDevelopment = countryDevelopmentField.getMutView(i);
            double outputPerAdult = this.realOutput(countryMarketField.getMutView(i), countryStrategicStockField.getMutView(i), civilianGoodCosts) / adults;
            double outputRatio = worldOutputPerAdult > 0.0 ? outputPerAdult / worldOutputPerAdult : 0.0;
            float outputPosition = ResearchUtils.outputPosition(outputRatio);
            float literacy = Math.clamp(countryDemographics.literacy() / adults, 0f, 1f);
            float index = ResearchUtils.developmentIndex(outputPosition, literacy);
            countryDevelopment.outputRatio((float) outputRatio);
            countryDevelopment.outputPosition(outputPosition);
            countryDevelopment.literacy(literacy);
            countryDevelopment.index(index);
            countryDevelopment.researchFactor(ResearchUtils.researchFactor(index));
        }
    }

    private double realOutput(CountryMarketView countryMarket, CountryStrategicStockView countryStrategicStock, float[] civilianGoodCosts) {
        double output = 0.0;
        for (int g = 0; g < civilianGoodCosts.length; g++) {
            output += (double) (countryMarket.goodProducedAmounts(g) - countryStrategicStock.releasedAmounts(g)) * civilianGoodCosts[g];
        }
        return output;
    }
}
