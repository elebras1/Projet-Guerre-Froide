package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.Country;
import com.populaire.projetguerrefroide.component.CountryView;
import com.populaire.projetguerrefroide.component.CountryDemographics;
import com.populaire.projetguerrefroide.component.CountryDemographicsView;
import com.populaire.projetguerrefroide.component.CountryDevelopment;
import com.populaire.projetguerrefroide.component.CountryDevelopmentView;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;
import com.populaire.projetguerrefroide.component.CountryMilitary;
import com.populaire.projetguerrefroide.component.CountryMilitaryView;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ProvinceView;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.CountrySort;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.CountryRankings;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Iter;

import java.util.Arrays;

public final class CountryRankingsHandler extends RequestHandler<CountryRankings> {
    private static final CountrySort[] SORTS = CountrySort.values();
    private final FlecsSystem marketSystem;
    private final FlecsSystem demographicsSystem;
    private final FlecsSystem developmentSystem;
    private final FlecsSystem militarySystem;
    private final FlecsSystem provincesSystem;
    private final FlecsSystem countrySystem;
    private long[] grossDomesticProducts = new long[0];
    private long[] populations = new long[0];
    private long[] provincePopulations = new long[0];
    private long[] treasuries = new long[0];
    private long[] taxRevenues = new long[0];
    private long[] tariffRevenues = new long[0];
    private long[] profitRevenues = new long[0];
    private long[] militarySpendings = new long[0];
    private int[] provinceCounts = new int[0];
    private float[] developmentIndexes = new float[0];
    private float[] literacySums = new float[0];
    private float[] literacies = new float[0];
    private int[] units = new int[0];
    private int[] governmentIndexes = new int[0];
    private int[] headOfStateIndexes = new int[0];
    private int[] countryOrder = new int[0];
    private double[] sortValues = new double[0];
    private int rankingCount;

    public CountryRankingsHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
        this.marketSystem = this.ecsWorld.system("CountryRankingsMarketRequest")
            .kind(0)
            .with(CountryMarket.class)
            .iter(this::collectMarket);
        this.demographicsSystem = this.ecsWorld.system("CountryRankingsDemographicsRequest")
            .kind(0)
            .with(CountryDemographics.class)
            .iter(this::collectDemographics);
        this.developmentSystem = this.ecsWorld.system("CountryRankingsDevelopmentRequest")
            .kind(0)
            .with(CountryDevelopment.class)
            .iter(this::collectDevelopment);
        this.militarySystem = this.ecsWorld.system("CountryRankingsMilitaryRequest")
            .kind(0)
            .with(CountryMilitary.class)
            .iter(this::collectMilitary);
        this.provincesSystem = this.ecsWorld.system("CountryRankingsProvincesRequest")
            .kind(0)
            .with(Province.class)
            .iter(this::collectProvinces);
        this.countrySystem = this.ecsWorld.system("CountryRankingsCountryRequest")
            .kind(0)
            .with(Country.class)
            .iter(this::collectCountry);
    }

    @Override
    protected boolean fill(RequestParams params, CountryRankings response) {
        response.clear();
        int countryCount = this.catalog.countries().size();
        this.ensureCapacity(countryCount);
        this.reset();
        this.marketSystem.run();
        this.demographicsSystem.run();
        this.developmentSystem.run();
        this.militarySystem.run();
        this.provincesSystem.run();
        this.countrySystem.run();

        this.rankingCount = 0;
        for (int countryIndex = 0; countryIndex < countryCount; countryIndex++) {
            long population = this.populations[countryIndex] > 0L ? this.populations[countryIndex] : this.provincePopulations[countryIndex];
            this.populations[countryIndex] = population;
            if (population > 0L) {
                this.literacies[countryIndex] = this.literacySums[countryIndex] / population;
            }
            if (this.grossDomesticProducts[countryIndex] <= 0L
                && population <= 0L
                && this.provinceCounts[countryIndex] <= 0) {
                continue;
            }
            this.countryOrder[this.rankingCount] = countryIndex;
            this.rankingCount++;
        }

        CountrySort sort = this.sort(params.option());
        for (int position = 0; position < this.rankingCount; position++) {
            this.sortValues[position] = this.sortValue(sort, this.countryOrder[position]);
        }
        this.sort();

        for (int position = 0; position < this.rankingCount; position++) {
            int countryIndex = this.countryOrder[position];
            response.addCountry(countryIndex, this.grossDomesticProducts[countryIndex], this.populations[countryIndex],
                this.treasuries[countryIndex], this.taxRevenues[countryIndex], this.tariffRevenues[countryIndex],
                this.profitRevenues[countryIndex], this.militarySpendings[countryIndex], this.provinceCounts[countryIndex],
                this.developmentIndexes[countryIndex], this.literacies[countryIndex], this.units[countryIndex],
                this.governmentIndexes[countryIndex], this.headOfStateIndexes[countryIndex]);
        }
        return true;
    }

    private void ensureCapacity(int countryCount) {
        if (this.grossDomesticProducts.length >= countryCount) {
            return;
        }
        this.grossDomesticProducts = new long[countryCount];
        this.populations = new long[countryCount];
        this.provincePopulations = new long[countryCount];
        this.treasuries = new long[countryCount];
        this.taxRevenues = new long[countryCount];
        this.tariffRevenues = new long[countryCount];
        this.profitRevenues = new long[countryCount];
        this.militarySpendings = new long[countryCount];
        this.provinceCounts = new int[countryCount];
        this.developmentIndexes = new float[countryCount];
        this.literacySums = new float[countryCount];
        this.literacies = new float[countryCount];
        this.units = new int[countryCount];
        this.governmentIndexes = new int[countryCount];
        this.headOfStateIndexes = new int[countryCount];
        this.countryOrder = new int[countryCount];
        this.sortValues = new double[countryCount];
    }

    private void reset() {
        Arrays.fill(this.grossDomesticProducts, 0L);
        Arrays.fill(this.populations, 0L);
        Arrays.fill(this.provincePopulations, 0L);
        Arrays.fill(this.treasuries, 0L);
        Arrays.fill(this.taxRevenues, 0L);
        Arrays.fill(this.tariffRevenues, 0L);
        Arrays.fill(this.profitRevenues, 0L);
        Arrays.fill(this.militarySpendings, 0L);
        Arrays.fill(this.provinceCounts, 0);
        Arrays.fill(this.developmentIndexes, 0f);
        Arrays.fill(this.literacySums, 0f);
        Arrays.fill(this.literacies, 0f);
        Arrays.fill(this.units, 0);
        Arrays.fill(this.governmentIndexes, -1);
        Arrays.fill(this.headOfStateIndexes, -1);
    }

    private void collectMarket(Iter iter) {
        Field<CountryMarket> marketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            int countryIndex = this.catalog.countries().indexOf(iter.entity(i));
            if (countryIndex < 0) {
                continue;
            }
            CountryMarketView market = marketField.getMutView(i);
            this.grossDomesticProducts[countryIndex] = Math.round(market.productionValue());
            this.treasuries[countryIndex] = Math.round(market.treasury());
            this.taxRevenues[countryIndex] = Math.round(market.taxRevenue());
            this.tariffRevenues[countryIndex] = Math.round(market.tariffRevenue());
            this.profitRevenues[countryIndex] = Math.round(market.profitRevenue());
        }
    }

    private void collectDemographics(Iter iter) {
        Field<CountryDemographics> demographicsField = iter.field(CountryDemographics.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            int countryIndex = this.catalog.countries().indexOf(iter.entity(i));
            if (countryIndex < 0) {
                continue;
            }
            CountryDemographicsView demographics = demographicsField.getMutView(i);
            this.populations[countryIndex] = demographics.totalPopulation();
            this.literacySums[countryIndex] = demographics.literacy();
        }
    }

    private void collectDevelopment(Iter iter) {
        Field<CountryDevelopment> developmentField = iter.field(CountryDevelopment.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            int countryIndex = this.catalog.countries().indexOf(iter.entity(i));
            if (countryIndex < 0) {
                continue;
            }
            CountryDevelopmentView development = developmentField.getMutView(i);
            this.developmentIndexes[countryIndex] = development.index();
        }
    }

    private void collectMilitary(Iter iter) {
        Field<CountryMilitary> militaryField = iter.field(CountryMilitary.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            int countryIndex = this.catalog.countries().indexOf(iter.entity(i));
            if (countryIndex < 0) {
                continue;
            }
            CountryMilitaryView military = militaryField.getMutView(i);
            this.units[countryIndex] = military.units();
            this.militarySpendings[countryIndex] = Math.round(military.spentToday());
        }
    }

    private void collectProvinces(Iter iter) {
        Field<Province> provinceField = iter.field(Province.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            int countryIndex = this.catalog.countries().indexOf(province.ownerId());
            if (countryIndex < 0) {
                continue;
            }
            this.provinceCounts[countryIndex]++;
            this.provincePopulations[countryIndex] += (long) province.childrenAmount() + province.adultsAmount() + province.seniorsAmount();
        }
    }

    private void collectCountry(Iter iter) {
        Field<Country> countryField = iter.field(Country.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            int countryIndex = this.catalog.countries().indexOf(iter.entity(i));
            if (countryIndex < 0) {
                continue;
            }
            CountryView country = countryField.getMutView(i);
            this.governmentIndexes[countryIndex] = this.catalog.governments().indexOf(country.governmentId());
            this.headOfStateIndexes[countryIndex] = this.catalog.ministers().indexOf(country.headOfStateId());
        }
    }

    private CountrySort sort(int ordinal) {
        if (ordinal < 0 || ordinal >= SORTS.length) {
            return CountrySort.GDP;
        }
        return SORTS[ordinal];
    }

    private double sortValue(CountrySort sort, int countryIndex) {
        return switch (sort) {
            case GDP -> this.grossDomesticProducts[countryIndex];
            case POPULATION -> this.populations[countryIndex];
            case TREASURY -> this.treasuries[countryIndex];
            case PROVINCES -> this.provinceCounts[countryIndex];
            case DEVELOPMENT_INDEX -> this.developmentIndexes[countryIndex];
            case LITERACY -> this.literacies[countryIndex];
            case UNITS -> this.units[countryIndex];
            case MILITARY_SPENDING -> this.militarySpendings[countryIndex];
            case TAX_REVENUE -> this.taxRevenues[countryIndex];
            case TARIFF_REVENUE -> this.tariffRevenues[countryIndex];
            case PROFIT_REVENUE -> this.profitRevenues[countryIndex];
        };
    }

    private void sort() {
        for (int i = 1; i < this.rankingCount; i++) {
            int countryIndex = this.countryOrder[i];
            double value = this.sortValues[i];
            int j = i - 1;
            while (j >= 0 && this.sortValues[j] < value) {
                this.countryOrder[j + 1] = this.countryOrder[j];
                this.sortValues[j + 1] = this.sortValues[j];
                j--;
            }
            this.countryOrder[j + 1] = countryIndex;
            this.sortValues[j + 1] = value;
        }
    }
}
