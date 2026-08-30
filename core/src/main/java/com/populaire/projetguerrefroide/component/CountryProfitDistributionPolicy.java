package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryProfitDistributionPolicy(float capitalistProfitShareRate, float workerProfitShareRate, float aristocratProfitShareRate, float stateProfitShareRate) {
}
