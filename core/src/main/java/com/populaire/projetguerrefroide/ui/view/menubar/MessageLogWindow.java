package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMinimapSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class MessageLogWindow extends UiWindow {
    private final Image logBg;
    private final UiList<LogText> messagelog;
    private final Button closebutton;
    private final Button messagecatCombat;
    private final Button messagecatDiplomacy;
    private final Button messagecatUnits;
    private final Button messagecatProvinces;
    private final Button messagecatEvents;
    private final Button messagecatOthers;

    public MessageLogWindow(UiBuilder ui) {
        super(ui, 720f, 100f);
        Skin minimapSkin = ui.skin(Skins.UI_MINIMAP);
        this.logBg = ui.image(this, minimapSkin, UiMinimapSkin.Drawables.LOG_BG, -19f, 13f, UiAnchor.UPPER_LEFT);
        this.messagelog = ui.list(this, 0f, 39f, 411f, 115f, UiAnchor.UPPER_LEFT, 1f, 0f, 0f, LogText::new);
        this.closebutton = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.CLOSELOG_BUTTON_BTN, -331f, -83f, UiAnchor.LOWER_RIGHT);
        // raccourci EvW closebutton : "<" non traduit
        this.messagecatCombat = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MESSAGECAT_COMBAT_TGL, -460f, -83f, UiAnchor.LOWER_RIGHT);
        this.messagecatDiplomacy = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MESSAGECAT_DIPLO_TGL, -440f, -83f, UiAnchor.LOWER_RIGHT);
        this.messagecatUnits = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MESSAGECAT_UNITS_TGL, -420f, -83f, UiAnchor.LOWER_RIGHT);
        this.messagecatProvinces = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MESSAGECAT_PROVINCES_TGL, -400f, -83f, UiAnchor.LOWER_RIGHT);
        this.messagecatEvents = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MESSAGECAT_EVENTS_TGL, -380f, -83f, UiAnchor.LOWER_RIGHT);
        this.messagecatOthers = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MESSAGECAT_OTHER_TGL, -360f, -83f, UiAnchor.LOWER_RIGHT);
        ui.onClick(this.closebutton, () -> this.setVisible(false));
    }
}
