package com.populaire.projetguerrefroide.loader;

import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.World;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.populaire.projetguerrefroide.util.Constants.*;

public final class WorldLoadingContext {
    private final World ecsWorld;
    private final EcsConstants ecsConstants;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final long[] populationTypeIds = new long[POP_TYPE_COUNT];
    private final long[] goodIds = new long[GOOD_COUNT];
    private final long[] lawGroupIds = new long[LAW_GROUP_COUNT];

    public WorldLoadingContext(World ecsWorld, EcsConstants ecsConstants) {
        this.ecsWorld = ecsWorld;
        this.ecsConstants = ecsConstants;
    }

    public World ecsWorld() {
        return this.ecsWorld;
    }

    public EcsConstants ecsConstants() {
        return this.ecsConstants;
    }

    public DateTimeFormatter dateFormatter() {
        return this.dateFormatter;
    }

    public long[] populationTypeIds() {
        return this.populationTypeIds;
    }

    public long[] goodIds() {
        return this.goodIds;
    }

    public long[] lawGroupIds() {
        return this.lawGroupIds;
    }

    public int parseColor(int[] color) {
        int alpha = 255;
        return (color[0] << 24) | (color[1] << 16) | (color[2] << 8) | alpha;
    }

    public void fillGoods(Map<String, Double> goods, int[] goodIndexes, long[] goodIds, float[] goodAmounts) {
        int i = 0;
        for(var goodEntry : goods.entrySet()) {
            long goodId = this.ecsWorld.lookup(goodEntry.getKey());
            goodIndexes[i] = this.getGoodIndex(goodId);
            goodIds[i] = goodId;
            goodAmounts[i] = goodEntry.getValue().floatValue();
            i++;
        }
    }

    public void fillGoodCosts(Map<String, Double> goods, long[] goodIds, float[] goodAmounts) {
        int i = 0;
        for(var goodEntry : goods.entrySet()) {
            goodIds[i] = this.ecsWorld.lookup(goodEntry.getKey());
            goodAmounts[i] = goodEntry.getValue().floatValue();
            i++;
        }
    }

    public int getPopulationTypeIndex(long popTypeId) {
        for (int i = 0; i < this.populationTypeIds.length; i++) {
            if (this.populationTypeIds[i] == popTypeId) {
                return i;
            }
        }
        return -1;
    }

    public int getGoodIndex(long goodId) {
        for (int i = 0; i < this.goodIds.length; i++) {
            if (this.goodIds[i] == goodId) {
                return i;
            }
        }
        return -1;
    }

    public int getLawGroupIndex(long lawGroupId) {
        for (int i = 0; i < this.lawGroupIds.length; i++) {
            if (this.lawGroupIds[i] == lawGroupId) {
                return i;
            }
        }
        return -1;
    }
}
