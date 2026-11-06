package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ProvinceView;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.catalog.NamedTable;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.ProvinceHover;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;

public final class ProvinceHoverHandler extends RequestHandler<ProvinceHover> {
    private int[] elementIndexes = new int[16];
    private int[] elementAmounts = new int[16];
    private int[] elementPercents = new int[16];

    public ProvinceHoverHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
    }

    @Override
    protected boolean fill(RequestParams params, ProvinceHover response) {
        int provinceIndex = params.index();
        response.clear(provinceIndex);
        if (provinceIndex < 0 || provinceIndex >= this.catalog.provinces().size()) {
            return false;
        }
        ProvinceView province = this.ecsWorld.obtainEntityView(this.catalog.provinces().entityId(provinceIndex)).getMutView(Province.class);
        int count = this.collect(this.catalog.cultures(), province, true);
        for (int i = 0; i < count; i++) {
            response.addCulture(this.elementIndexes[i], this.elementPercents[i]);
        }
        count = this.collect(this.catalog.religions(), province, false);
        for (int i = 0; i < count; i++) {
            response.addReligion(this.elementIndexes[i], this.elementPercents[i]);
        }
        return true;
    }

    private int collect(NamedTable table, ProvinceView province, boolean cultures) {
        int length = cultures ? province.cultureIdsLength() : province.religionIdsLength();
        this.ensure(length);
        int count = 0;
        int amountTotal = 0;
        for (int i = 0; i < length; i++) {
            int amount = cultures ? province.cultureAmounts(i) : province.religionAmounts(i);
            if (amount == 0) {
                break;
            }
            long elementId = cultures ? province.cultureIds(i) : province.religionIds(i);
            this.elementIndexes[count] = table.indexOf(elementId);
            this.elementAmounts[count] = amount;
            amountTotal += amount;
            count++;
        }
        int total = 0;
        int biggest = -1;
        for (int i = 0; i < count; i++) {
            if (biggest == -1 || this.elementAmounts[i] > this.elementAmounts[biggest]) {
                biggest = i;
            }
            int percent = amountTotal != 0 ? (int) ((this.elementAmounts[i] / (float) amountTotal) * 100) : 0;
            this.elementPercents[i] = percent;
            total += percent;
        }
        if (total != 100 && biggest != -1) {
            this.elementPercents[biggest] += 100 - total;
        }
        for (int i = 1; i < count; i++) {
            int index = this.elementIndexes[i];
            int percent = this.elementPercents[i];
            int j = i - 1;
            while (j >= 0 && this.elementPercents[j] < percent) {
                this.elementIndexes[j + 1] = this.elementIndexes[j];
                this.elementPercents[j + 1] = this.elementPercents[j];
                j--;
            }
            this.elementIndexes[j + 1] = index;
            this.elementPercents[j + 1] = percent;
        }
        return count;
    }

    private void ensure(int size) {
        if (this.elementIndexes.length < size) {
            this.elementIndexes = new int[size];
            this.elementAmounts = new int[size];
            this.elementPercents = new int[size];
        }
    }
}
