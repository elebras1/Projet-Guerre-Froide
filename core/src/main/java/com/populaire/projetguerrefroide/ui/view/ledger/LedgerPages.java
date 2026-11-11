package com.populaire.projetguerrefroide.ui.view.ledger;

import com.populaire.projetguerrefroide.protocol.request.CountrySort;

import java.util.List;

public final class LedgerPages {
    public static final int OVERVIEW = 0;
    public static final int ECONOMY = 1;
    public static final int MISC = 2;
    public static final int MILITARY = 3;
    public static final int HISTORY = 4;

    /*
     * Pages in reading order: the arrows walk through all of them across the tabs, a tab button opens its first page.
     * Only pages backed by PGF data are listed; the other EvW pages (armies, resources, charts...) come back with their
     * data. A tab without page is hidden.
     */
    public static final LedgerPage[] PAGES = {
        page(OVERVIEW, "ELEDGER_NATIONAL_STATISTICS", CountrySort.GDP,
            column("GDP", LedgerMetric.GDP),
            column("LEDGER_POP_SIZE", LedgerMetric.POPULATION),
            column("DEVELOPMENT_INDEX", LedgerMetric.DEVELOPMENT_INDEX)),
        page(OVERVIEW, "POPULATION", CountrySort.POPULATION,
            column("LEDGER_POP_SIZE", LedgerMetric.POPULATION),
            column("LITERACY", LedgerMetric.LITERACY)),
        page(OVERVIEW, "Nation Statistics", CountrySort.PROVINCES,
            column("PROVINCES", LedgerMetric.PROVINCES)),
        page(ECONOMY, "GDP", CountrySort.GDP,
            column("GDP", LedgerMetric.GDP),
            column("SHARE", LedgerMetric.SHARE)),
        page(ECONOMY, "LEDGER_UNIT_MAINTENANCE", CountrySort.MILITARY_SPENDING,
            column("MILITARY_SPENDING", LedgerMetric.MILITARY_SPENDING)),
        page(MISC, "LEDGER_POLITICIANS_TITLE", CountrySort.GDP,
            column("GOVERNMENT", LedgerMetric.GOVERNMENT),
            column("HEAD_OF_STATE", LedgerMetric.HEAD_OF_STATE)),
        page(MILITARY, "LEDGER_BRIGADE_TITLE", CountrySort.UNITS,
            column("UNITS", LedgerMetric.UNITS))
    };

    private LedgerPages() {
    }

    public static int firstPage(int tab) {
        for (int i = 0; i < PAGES.length; i++) {
            if (PAGES[i].tab() == tab) {
                return i;
            }
        }
        return -1;
    }

    private static LedgerColumn column(String headerKey, LedgerMetric metric) {
        return new LedgerColumn(headerKey, metric);
    }

    private static LedgerPage page(int tab, String titleKey, CountrySort sort, LedgerColumn... columns) {
        return new LedgerPage(tab, titleKey, sort, List.of(columns));
    }
}
