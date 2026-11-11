package com.populaire.projetguerrefroide.ui.view.ledger;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class LinechartOptions extends UiWindow {
    private final UiList<UiWindow> chartTemplateListbox;
    private final UiList<UiWindow> chartCountryListbox;
    private final Button linechartPrevPage;
    private final Button linechartNextPage;

    public LinechartOptions(UiBuilder ui) {
        super(ui, 1120f, 768f);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        // ledger_grid_mil : sprite GFX_bg_grid_mil sans asset PGF
        // ledger_listbox_bg : sprite GFX_bg_stat_listbox sans asset PGF
        this.chartTemplateListbox = ui.list(this, 795f, 22f, 164f, 400f, UiAnchor.UPPER_LEFT, 1f, 1f, 1f, UiWindow::new);
        this.chartCountryListbox = ui.list(this, 790f, 150f, 181f, 358f, UiAnchor.UPPER_LEFT, 1f, 1f, 1f, UiWindow::new);
        this.linechartPrevPage = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_LEFT_BTN, 73f, 474f, UiAnchor.UPPER_LEFT);
        // raccourci EvW linechart_prev_page : "LEFT" non traduit
        this.linechartNextPage = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_RIGHT_BTN, 735f, 474f, UiAnchor.UPPER_LEFT);
        // raccourci EvW linechart_next_page : "RIGHT" non traduit
        // linechart_mp_button : sprite GFX_chart_mp_button sans asset PGF
        // linechart_eq_button : sprite GFX_chart_eq_button sans asset PGF
    }
}
