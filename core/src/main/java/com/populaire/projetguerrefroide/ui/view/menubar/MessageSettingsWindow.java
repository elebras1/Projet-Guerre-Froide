package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuIgSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class MessageSettingsWindow extends UiWindow {
    private final Image messagesettingsBg;
    private final Label messagesettingsLabel;
    private final Button messagecatAll;
    private final Label messagecatTextAll;
    private final Button messagecatCombat;
    private final Label messagecatTextCombat;
    private final Button messagecatDiplomacy;
    private final Label messagecatTextDiplomacy;
    private final Button messagecatUnits;
    private final Label messagecatTextUnits;
    private final Button messagecatProvinces;
    private final Label messagecatTextProvinces;
    private final Button messagecatEvents;
    private final Label messagecatTextEvents;
    private final Button messagecatOthers;
    private final Label messagecatTextOther;
    private final UiList<MessageEntry> settings;
    private final Button reset;
    private final Button close;

    public MessageSettingsWindow(UiBuilder ui, Runnable onClose) {
        super(ui, 544f, 428f);
        Skin ingameSkin = ui.skin(Skins.UI_MAINMENU_IG);
        this.messagesettingsBg = ui.image(this, ingameSkin, UiMainmenuIgSkin.Drawables.MSGSETTINGS_BG, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.messagesettingsLabel = ui.label(this, "MESSAGE_SETTINGS", FontsSkin.Styles.Label.JOCKEY_24, 19f, 14f, 506f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        this.messagecatAll = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGE_CAT_TAB_SMALL_TGL, 20f, 43f, UiAnchor.UPPER_LEFT);
        this.messagecatTextAll = ui.label(this, "MESSAGESETTINGS_ALL", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 25f, 48f, 52f, 13f, Align.top, UiAnchor.UPPER_LEFT);
        this.messagecatCombat = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGE_CAT_TAB_BIG_TGL, 85f, 43f, UiAnchor.UPPER_LEFT);
        this.messagecatTextCombat = ui.label(this, "MESSAGESETTINGS_COMBAT", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 90f, 48f, 65f, 13f, Align.top, UiAnchor.UPPER_LEFT);
        this.messagecatDiplomacy = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGE_CAT_TAB_BIG_TGL, 163f, 43f, UiAnchor.UPPER_LEFT);
        this.messagecatTextDiplomacy = ui.label(this, "MESSAGESETTINGS_DIPLOMACY", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 168f, 48f, 65f, 13f, Align.top, UiAnchor.UPPER_LEFT);
        this.messagecatUnits = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGE_CAT_TAB_BIG_TGL, 241f, 43f, UiAnchor.UPPER_LEFT);
        this.messagecatTextUnits = ui.label(this, "MESSAGESETTINGS_UNITS", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 246f, 48f, 65f, 13f, Align.top, UiAnchor.UPPER_LEFT);
        this.messagecatProvinces = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGE_CAT_TAB_BIG_TGL, 319f, 43f, UiAnchor.UPPER_LEFT);
        this.messagecatTextProvinces = ui.label(this, "MESSAGESETTINGS_PROVINCES", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 324f, 48f, 65f, 13f, Align.top, UiAnchor.UPPER_LEFT);
        this.messagecatEvents = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGE_CAT_TAB_SMALL_TGL, 397f, 43f, UiAnchor.UPPER_LEFT);
        this.messagecatTextEvents = ui.label(this, "MESSAGESETTINGS_EVENTS", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 402f, 48f, 52f, 13f, Align.top, UiAnchor.UPPER_LEFT);
        this.messagecatOthers = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGE_CAT_TAB_SMALL_TGL, 462f, 43f, UiAnchor.UPPER_LEFT);
        this.messagecatTextOther = ui.label(this, "MESSAGESETTINGS_OTHER", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, 467f, 48f, 52f, 13f, Align.top, UiAnchor.UPPER_LEFT);
        this.settings = ui.list(this, 30f, 100f, 475f, 262f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, MessageEntry::new);
        this.reset = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MSG_SETTINGS_CANCEL_BTN, 93f, 380f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.reset, "MENU_MESSAGES_RESET", FontsSkin.Styles.Label.JOCKEY_16_GLOW_RED);
        this.close = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MSG_SETTINGS_CONFIRM_BTN, 275f, 380f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.close, "CLOSE", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE);
        ButtonGroup<Button> categories = new ButtonGroup<>(this.messagecatAll, this.messagecatCombat, this.messagecatDiplomacy, this.messagecatUnits, this.messagecatProvinces, this.messagecatEvents, this.messagecatOthers);
        categories.setMinCheckCount(1);
        this.messagecatAll.setChecked(true);
        ui.onClick(this.close, onClose);
    }
}
