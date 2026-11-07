package com.populaire.projetguerrefroide.ui.view.economy;

import com.populaire.projetguerrefroide.ui.widget.OverlappingBox;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class RegionEntryFactories extends UiWindow {
    private final OverlappingBox factoryBox;

    public RegionEntryFactories(UiBuilder ui) {
        super(ui, 20f, 108f);
        this.factoryBox = ui.overlappingBox(this, 228f, -5f, 830f, 108f, 1f, false, UiAnchor.UPPER_LEFT);
    }

    void addEntry(FactoryEntry entry) {
        this.factoryBox.addActor(entry);
    }

    void clearEntries() {
        this.factoryBox.clearChildren();
    }
}
