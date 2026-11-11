package com.populaire.projetguerrefroide.ui.view.ledger;

import com.populaire.projetguerrefroide.protocol.request.CountrySort;

import java.util.List;

public record LedgerPage(int tab, String titleKey, CountrySort sort, List<LedgerColumn> columns) {
}
