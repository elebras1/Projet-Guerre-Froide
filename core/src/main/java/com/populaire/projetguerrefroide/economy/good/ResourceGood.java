package com.populaire.projetguerrefroide.economy.good;

public class ResourceGood extends Good {
    private ResourceProductionType productionType;
    private final float value;

    public ResourceGood(String name, float cost, int color, float value) {
        super(name, cost, color);
        this.value = value;
    }

    public void setProductionType(ResourceProductionType productionType) {
        this.productionType = productionType;
    }

    public ResourceProductionType getProductionType() {
        return this.productionType;
    }

    public float getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "ResourceGood{" +
            "name=" + this.getName() +
            ", cost=" + this.getCost() +
            ", color=" + this.getColor() +
            ", value=" + this.value +
            '}';
    }
}
