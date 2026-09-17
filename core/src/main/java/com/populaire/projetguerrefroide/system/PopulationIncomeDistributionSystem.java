package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.StrataUtils.*;

public class PopulationIncomeDistributionSystem {

    private final EcsConstants ecsConstants;

    public PopulationIncomeDistributionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("PopulationIncomeDistributionSystem")
            .with(Population.class)
            .kind(phaseId)
            .iter(this::distribute);
    }

    public void distribute(Iter iter) {
        long populationTypeId = 0;
        EntityView populationType = null;
        PopulationTypeView populationTypeData = null;

        long provinceId = 0;
        ProvinceView provinceData = null;

        long regionInstanceId = 0;
        RegionInstanceIncomeView regionInstanceIncome = null;
        DemographicsView regionDemographics = null;

        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryTaxPolicyView countryTaxPolicy = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);

            if (population.typeId() != populationTypeId) {
                populationTypeId = population.typeId();
                populationType = iter.world().obtainEntityView(populationTypeId);
                populationTypeData = populationType.getMutView(PopulationType.class);
            }

            if (population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                EntityView province = iter.world().obtainEntityView(provinceId);
                provinceData = province.getMutView(Province.class);
            }

            if (provinceData.regionInstanceId() != regionInstanceId) {
                regionInstanceId = provinceData.regionInstanceId();
                EntityView regionEntity = iter.world().obtainEntityView(regionInstanceId);
                regionInstanceIncome = regionEntity.getMutView(RegionInstanceIncome.class);
                regionDemographics = regionEntity.getMutView(Demographics.class);
            }

            if (population.countryId() != countryId) {
                countryId = population.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryTaxPolicy = country.getMutView(CountryTaxPolicy.class);
            }

            float grossIncome = 0f;
            int typeIdx = population.index();

            float popAmount = population.amount();

            float totalMinWages = regionInstanceIncome.minWagesByPopType(typeIdx);
            float totalBonus = regionInstanceIncome.profitShareByPopType(typeIdx);
            int totalPopType = regionDemographics.totalByPopType(typeIdx);
            if (totalPopType > 0) {
                float distributedIncome = (totalMinWages + totalBonus) * popAmount / totalPopType;
                grossIncome += distributedIncome;
                regionInstanceIncome.claimedIncomeByPopType(typeIdx, regionInstanceIncome.claimedIncomeByPopType(typeIdx) + distributedIncome);
            }

            if (populationType.has(this.ecsConstants.capitalistTag())) {
                float capitalistTotal = regionInstanceIncome.capitalistProfitShare();
                int totalCapitalists = regionDemographics.totalByPopType(typeIdx);
                if (totalCapitalists > 0) {
                    float distributedIncome = capitalistTotal * popAmount / totalCapitalists;
                    grossIncome += distributedIncome;
                    regionInstanceIncome.claimedCapitalistShare(regionInstanceIncome.claimedCapitalistShare() + distributedIncome);
                }
            }

            if (populationType.has(this.ecsConstants.aristocratTag())) {
                float aristocratTotal = regionInstanceIncome.aristocratProfitShare();
                int totalAristocrats = regionDemographics.totalByPopType(typeIdx);
                if (totalAristocrats > 0) {
                    float distributedIncome = aristocratTotal * popAmount / totalAristocrats;
                    grossIncome += distributedIncome;
                    regionInstanceIncome.claimedAristocratShare(regionInstanceIncome.claimedAristocratShare() + distributedIncome);
                }
            }

            float taxRate = 0f;
            switch (populationTypeData.strata()) {
                case POOR_STRATA -> taxRate = countryTaxPolicy.poorTaxRate();
                case MIDDLE_STRATA -> taxRate = countryTaxPolicy.middleTaxRate();
                case RICH_STRATA -> taxRate = countryTaxPolicy.richTaxRate();
            }
            float tax = grossIncome * taxRate;
            float netIncome = grossIncome - tax;

            population.savings(population.savings() + netIncome);
            countryMarket.treasury(countryMarket.treasury() + tax);
        }
    }
}
