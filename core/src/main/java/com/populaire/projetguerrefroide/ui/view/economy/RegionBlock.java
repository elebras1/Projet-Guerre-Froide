package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.utils.CharArray;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.protocol.response.RegionsBuildings;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

/**
 * Ligne de `regions_listbox` : la liste EvW alterne `region_entry_title` et `region_entry_factories`. Une région PGF
 * peut avoir plus de bâtiments que la largeur visible (six cases) : elle reçoit alors plusieurs rangées
 * `region_entry_factories`, complétées par des emplacements vides comme le panneau PGF précédent.
 */
public class RegionBlock extends UiWindow {
    private static final int FACTORIES_PER_ROW = 6;
    private static final float TITLE_HEIGHT = 42f;
    private static final float FIRST_ROW_HEIGHT = 108f;
    private static final float ROW_PITCH = 121f;
    private final UiCatalog uiCatalog;
    private final TextFormat textFormat;
    private final EconomyDrawables drawables;
    private final EconomyPanelListener listener;
    private final boolean[] collapsedRegions;
    private final CharArray text;
    private final RegionEntryTitle title;
    private final ObjectList<RegionEntryFactories> rows;
    private final ObjectList<FactoryEntry> entries;
    private int regionIndex;
    private int shownSlots;
    private boolean collapsed;

    RegionBlock(UiBuilder ui, UiCatalog uiCatalog, TextFormat textFormat, EconomyDrawables drawables, EconomyPanelListener listener, boolean[] collapsedRegions) {
        super(ui, 980f, TITLE_HEIGHT);
        this.uiCatalog = uiCatalog;
        this.textFormat = textFormat;
        this.drawables = drawables;
        this.listener = listener;
        this.collapsedRegions = collapsedRegions;
        this.text = new CharArray(32);
        this.rows = new ObjectList<>();
        this.entries = new ObjectList<>();
        this.regionIndex = -1;
        this.shownSlots = -1;
        this.title = ui.add(this, new RegionEntryTitle(ui), 0f, 0f, UiAnchor.UPPER_LEFT);
        this.title.setOnToggle(() -> this.setCollapsed(!this.collapsed));
    }

    public void bind(RegionsBuildings regions, int position) {
        int regionIndex = regions.regionIndex(position);
        if (regionIndex != this.regionIndex) {
            this.regionIndex = regionIndex;
            this.title.setRegionName(this.uiCatalog.regionName(regionIndex));
            this.collapsed = regionIndex >= 0 && this.collapsedRegions[regionIndex];
            this.shownSlots = -1;
        }
        this.title.bind(regions, position, this.textFormat, this.text);
        int count = regions.regionBuildingCount(position);
        int slots = count == 0 ? FACTORIES_PER_ROW : (count + FACTORIES_PER_ROW - 1) / FACTORIES_PER_ROW * FACTORIES_PER_ROW;
        while (this.entries.size() < slots) {
            this.entries.add(new FactoryEntry(this.ui, this.textFormat, this.drawables, this.listener));
        }
        int first = regions.regionBuildingStart(position);
        for (int i = 0; i < count; i++) {
            this.entries.get(i).bind(regions, first + i);
        }
        for (int i = count; i < slots; i++) {
            this.entries.get(i).bindEmpty();
        }
        if (slots != this.shownSlots) {
            this.shownSlots = slots;
            this.layoutRows();
        }
    }

    private void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        if (this.regionIndex >= 0) {
            this.collapsedRegions[this.regionIndex] = collapsed;
        }
        this.layoutRows();
    }

    private void layoutRows() {
        int rowCount = this.collapsed ? 0 : this.shownSlots / FACTORIES_PER_ROW;
        while (this.rows.size() < rowCount) {
            this.rows.add(this.ui.add(this, new RegionEntryFactories(this.ui), 0f, TITLE_HEIGHT + this.rows.size() * ROW_PITCH, UiAnchor.UPPER_LEFT));
        }
        for (int r = 0; r < this.rows.size(); r++) {
            RegionEntryFactories row = this.rows.get(r);
            boolean visible = r < rowCount;
            row.setVisible(visible);
            row.clearEntries();
            if (visible) {
                for (int i = 0; i < FACTORIES_PER_ROW; i++) {
                    row.addEntry(this.entries.get(r * FACTORIES_PER_ROW + i));
                }
            }
        }
        this.title.setCollapsed(this.collapsed);
        float height = rowCount == 0 ? TITLE_HEIGHT : TITLE_HEIGHT + FIRST_ROW_HEIGHT + (rowCount - 1) * ROW_PITCH;
        if (height != this.getHeight()) {
            this.setHeight(height);
            this.invalidateHierarchy();
        }
    }
}
