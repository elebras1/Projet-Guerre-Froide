package com.populaire.projetguerrefroide.ui.view.ledger;

import com.badlogic.gdx.utils.CharArray;
import com.populaire.projetguerrefroide.protocol.request.CountrySort;
import com.populaire.projetguerrefroide.protocol.response.CountryRankings;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;

public enum LedgerMetric {
    GDP(CountrySort.GDP),
    POPULATION(CountrySort.POPULATION),
    TREASURY(CountrySort.TREASURY),
    PROVINCES(CountrySort.PROVINCES),
    DEVELOPMENT_INDEX(CountrySort.DEVELOPMENT_INDEX),
    LITERACY(CountrySort.LITERACY),
    UNITS(CountrySort.UNITS),
    MILITARY_SPENDING(CountrySort.MILITARY_SPENDING),
    TAX_REVENUE(CountrySort.TAX_REVENUE),
    TARIFF_REVENUE(CountrySort.TARIFF_REVENUE),
    PROFIT_REVENUE(CountrySort.PROFIT_REVENUE),
    GOVERNMENT(CountrySort.GDP),
    HEAD_OF_STATE(CountrySort.GDP),
    SHARE(CountrySort.GDP);

    private final CountrySort sort;

    LedgerMetric(CountrySort sort) {
        this.sort = sort;
    }

    public CountrySort sort() {
        return this.sort;
    }

    public void append(CharArray text, CountryRankings rankings, int position, TextFormat format, UiCatalog catalog) {
        switch (this) {
            case GDP -> format.compact(text, rankings.grossDomesticProduct(position));
            case POPULATION -> format.compact(text, rankings.population(position));
            case TREASURY -> format.compact(text, rankings.treasury(position));
            case PROVINCES -> {
                text.clear();
                text.append(rankings.provinceCount(position));
            }
            case DEVELOPMENT_INDEX -> format.fixed(text, rankings.developmentIndex(position) * 100.0, 1).append('%');
            case LITERACY -> format.fixed(text, rankings.literacy(position) * 100.0, 1).append('%');
            case UNITS -> {
                text.clear();
                text.append(rankings.units(position));
            }
            case MILITARY_SPENDING -> format.compact(text, rankings.militarySpending(position));
            case TAX_REVENUE -> format.compact(text, rankings.taxRevenue(position));
            case TARIFF_REVENUE -> format.compact(text, rankings.tariffRevenue(position));
            case PROFIT_REVENUE -> format.compact(text, rankings.profitRevenue(position));
            case GOVERNMENT -> {
                text.clear();
                text.append(localizedName(catalog, rankings.governmentIndex(position), true));
            }
            case HEAD_OF_STATE -> {
                text.clear();
                text.append(localizedName(catalog, rankings.headOfStateIndex(position), false));
            }
            case SHARE -> {
                long worldGrossDomesticProduct = rankings.worldGrossDomesticProduct();
                double percentage = worldGrossDomesticProduct > 0L
                    ? 100.0 * rankings.grossDomesticProduct(position) / worldGrossDomesticProduct
                    : 0.0;
                format.fixed(text, percentage, 2).append('%');
            }
        }
    }

    private String localizedName(UiCatalog catalog, int index, boolean government) {
        if (index < 0) {
            return "-";
        }
        String nameId = government
            ? catalog.catalog().governments().nameId(index)
            : catalog.catalog().ministers().nameId(index);
        String name = catalog.localized(nameId);
        return name != null ? name : "-";
    }
}
