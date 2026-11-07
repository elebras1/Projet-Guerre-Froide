package com.populaire.projetguerrefroide.ui.view.economy;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiEconomySkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class EconomyResourceView extends UiWindow {
    private final Image bgTrade;
    private final Label decisionsLabel;
    private final UiList<ResourceEntry> resourcesListbox;

    public EconomyResourceView(UiBuilder ui) {
        super(ui, 300f, 400f);
        Skin economySkin = ui.skin(Skins.UI_ECONOMY);
        this.bgTrade = ui.image(this, economySkin, UiEconomySkin.Drawables.ECO_TRADE_BG, 15f, 43f, UiAnchor.UPPER_LEFT);
        this.decisionsLabel = ui.label(this, "TRADE_MARKET", FontsSkin.Styles.Label.JOCKEY_24, -15f, 254f, 320f, 32f, Align.top, UiAnchor.UPPER_LEFT, 4f, 0f);
        // resources_linechart : GFX_resources_linechart (sprite absent des .gfx EvW)
        // groups_button : GFX_eco_groups_button (sans asset PGF)
        // price_button : GFX_eco_price_button (sans asset PGF)
        // export_button : GFX_eco_groups_button (sans asset PGF)
        this.resourcesListbox = ui.list(this, -14f, 312f, 262f, 336f, UiAnchor.UPPER_LEFT, 3f, 0f, 0f, ResourceEntry::new);
        this.decisionsLabel.setVisible(false);
        this.resourcesListbox.setVisible(false);
    }
}
