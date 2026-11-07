package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMinimapSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class MenuBar extends UiWindow {
    private final MessageLogWindow messagelogWindow;
    private final Button oPENbutton;
    private final ChatWindow chatWindow;
    private final Image menubarPanelBg;
    private final Button menubarMission;
    private final Button menubarNuke;
    private final Button menubarSpy;
    private final Button menubarGoalsButton;
    private final Button menubarModifiersButton;
    private final Button buttonGoto;
    private final Button mapZoomIn;
    private final Button mapZoomOut;
    private final Button mapmode16;
    private final Button mapmode15;
    private final Button mapmode12;
    private final Button mapmode11;
    private final Button mapmode4;
    private final Button mapmode5;
    private final Button mapmode8;
    private final Button mapmode9;
    private final Button mapmode13;
    private final Button mapmode10;
    private final Button mapmode7;
    private final Button mapmode6;
    private final Button mapmode2;
    private final Button mapmode3;
    private final Button mapmode14;
    private final Button mapmode1;
    private final Image minimapBg;
    private final Image minimap;
    private final ButtonGroup<Button> mapModes;
    private final MenuBarListener listener;

    public MenuBar(UiBuilder ui, MenuBarListener listener) {
        super(ui);
        this.listener = listener;
        Skin minimapSkin = ui.skin(Skins.UI_MINIMAP);
        this.minimapBg = ui.image(this, minimapSkin, UiMinimapSkin.Drawables.MINIMAP_BG, -417f, -164f, UiAnchor.LOWER_RIGHT);
        this.minimap = ui.image(this, minimapSkin, UiMinimapSkin.Drawables.MINIMAP, -406f, -128f, UiAnchor.LOWER_RIGHT);
        this.messagelogWindow = ui.add(this, new MessageLogWindow(ui), -826f, -156f, UiAnchor.LOWER_RIGHT);
        this.messagelogWindow.setVisible(false);
        this.oPENbutton = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.OPENLOG_BUTTON_BTN, -436f, -22f, UiAnchor.LOWER_RIGHT);
        // raccourci EvW OPENbutton : "<" non traduit
        this.chatWindow = ui.add(this, new ChatWindow(ui), 0f, -136f, UiAnchor.LOWER_LEFT);
        this.chatWindow.setVisible(false);
        this.menubarPanelBg = ui.image(this, minimapSkin, UiMinimapSkin.Drawables.MENUBAR_PANEL_BG, -60f, -295f, UiAnchor.LOWER_RIGHT);
        this.menubarMission = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MINIMAP_MISSION_TGL, -41f, -224f, UiAnchor.LOWER_RIGHT);
        this.menubarNuke = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MINIMAP_NUKE_TGL, -41f, -187f, UiAnchor.LOWER_RIGHT);
        this.menubarSpy = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MINIMAP_SPY_TGL, -41f, -150f, UiAnchor.LOWER_RIGHT);
        this.menubarGoalsButton = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MINIMAP_VICTORY_TGL, -41f, -113f, UiAnchor.LOWER_RIGHT);
        this.menubarModifiersButton = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MINIMAP_STRATEGIC_TGL, -41f, -76f, UiAnchor.LOWER_RIGHT);
        this.buttonGoto = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MINIMAP_FIND_PROV_TGL, -41f, -39f, UiAnchor.LOWER_RIGHT);
        this.mapZoomIn = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAP_ZOOM_IN_BTN, -85f, -130f, UiAnchor.LOWER_RIGHT);
        ui.shortcut(this, this.mapZoomIn, Input.Keys.PAGE_DOWN);
        this.mapZoomOut = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAP_ZOOM_OUT_BTN, -85f, -37f, UiAnchor.LOWER_RIGHT);
        ui.shortcut(this, this.mapZoomOut, Input.Keys.PAGE_UP);
        this.mapmode16 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_NAVAL_TGL, -77f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode15 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_AIR_TGL, -99f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode12 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_STRENGTH_TGL, -121f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode11 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_THEATRE_TGL, -143f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode4 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_INTEL_TGL, -165f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode5 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_REVOLT_TGL, -187f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode8 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_SUPPLY_TGL, -209f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode9 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_INFRA_TGL, -231f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode13 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_RESOURCES_TGL, -253f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode10 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_ECONOMICAL_TGL, -275f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode7 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_REGION_TGL, -297f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode6 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_DIPLOMATIC_TGL, -319f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode2 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_POLITICAL_TGL, -341f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode3 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_WEATHER_TGL, -363f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode14 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_TERRAIN_2_TGL, -385f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapmode1 = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.MAPMODE_TERRAIN_TGL, -407f, -154f, UiAnchor.LOWER_RIGHT);
        this.mapModes = new ButtonGroup<>();
        this.mapModes.setMinCheckCount(1);
        this.mapModes.setMaxCheckCount(1);
        this.mapMode(this.mapmode16, "mapmode_naval");
        this.mapMode(this.mapmode15, "mapmode_air");
        this.mapMode(this.mapmode12, "mapmode_strength");
        this.mapMode(this.mapmode11, "mapmode_theatre");
        this.mapMode(this.mapmode4, "mapmode_intel");
        this.mapMode(this.mapmode5, "mapmode_revolt");
        this.mapMode(this.mapmode8, "mapmode_supply");
        this.mapMode(this.mapmode9, "mapmode_infra");
        this.mapMode(this.mapmode13, "mapmode_resources");
        this.mapMode(this.mapmode10, "mapmode_economical");
        this.mapMode(this.mapmode7, "mapmode_region");
        this.mapMode(this.mapmode6, "mapmode_diplomatic");
        this.mapMode(this.mapmode2, "mapmode_political");
        this.mapMode(this.mapmode3, "mapmode_weather");
        this.mapMode(this.mapmode14, "mapmode_terrain_2");
        this.mapMode(this.mapmode1, "mapmode_terrain");
        this.mapmode2.setChecked(true);
        this.minimap.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.moveCamera((int) (x / MenuBar.this.minimap.getWidth() * WORLD_WIDTH), (int) (y / MenuBar.this.minimap.getHeight() * WORLD_HEIGHT));
            }
        });
        ui.onClick(this.mapZoomIn, listener::zoomIn);
        ui.onClick(this.mapZoomOut, listener::zoomOut);
        ui.onClick(this.oPENbutton, () -> this.messagelogWindow.setVisible(!this.messagelogWindow.isVisible()));
    }

    public void showChat(boolean visible) {
        this.chatWindow.setVisible(visible);
    }

    private void mapMode(Button button, String mapMode) {
        this.mapModes.add(button);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (button.isChecked()) {
                    MenuBar.this.listener.changeMapMode(mapMode);
                }
            }
        });
        button.addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                MenuBar.this.listener.updateHoverTooltip(MenuBar.this.ui.localise(mapMode.toUpperCase()));
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                MenuBar.this.listener.hideUiTooltip();
            }
        });
    }
}
