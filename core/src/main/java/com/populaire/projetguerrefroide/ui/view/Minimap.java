package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.github.tommyettinger.ds.IntObjectMap;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMinimapSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.ColorRectangle;

import java.util.Map;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class Minimap extends Table {
    private Table informationsMapMode;
    private String currentMapMode;

    public Minimap(Skin skin, Skin skinUi, Skin skinFonts, Map<String, String> localisation, MinimapListener listener) {
        this.currentMapMode = "mapmode_political";
        Table minimap = this.getMinimap(skin, skinUi, skinFonts, localisation, listener);
        Table menuBarPanel = this.getMenuBarPanel(skin);

        this.setSize(minimap.getWidth() + menuBarPanel.getWidth(), menuBarPanel.getHeight());
        minimap.setPosition(22, 0);
        menuBarPanel.setPosition(minimap.getWidth() + 5, -8);

        this.addActor(minimap);
        this.addActor(menuBarPanel);
    }

    private Table getMinimap(Skin skin, Skin skinUi, Skin skinFonts, Map<String, String> localisation, MinimapListener listener) {
        Table minimap = new Table();

        Drawable background = skin.getDrawable(UiMinimapSkin.Drawables.MINIMAP_BG);
        minimap.setBackground(background);
        minimap.setSize(background.getMinWidth(), background.getMinHeight());

        Drawable mapDrawable = skin.getDrawable(UiMinimapSkin.Drawables.MINIMAP);
        Image map = new Image(mapDrawable);
        map.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                int worldX = (int) ((x / map.getWidth()) * WORLD_WIDTH);
                int worldY = (int) ((y / map.getHeight()) * WORLD_HEIGHT);
                listener.moveCamera(worldX, worldY);
            }
        });
        map.setPosition(11, 11);

        Button zoomInButton = new Button(skin, UiMinimapSkin.Styles.Button.MAP_ZOOM_IN_BTN);
        zoomInButton.setPosition(332, 103);
        zoomInButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.zoomIn();
            }
        });

        Button zoomOutButton = new Button(skin, UiMinimapSkin.Styles.Button.MAP_ZOOM_OUT_BTN);
        zoomOutButton.setPosition(332, 10);
        zoomOutButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.zoomOut();
            }
        });

        this.setMapMode(minimap, skin, skinUi, skinFonts, localisation, listener);

        minimap.addActor(map);
        minimap.addActor(zoomInButton);
        minimap.addActor(zoomOutButton);

        return minimap;
    }

    private void setMapMode(Table minimap, Skin skin, Skin skinUi, Skin skinFonts, Map<String, String> localisation, MinimapListener listener) {
        int y = 134;
        ButtonGroup<Button> buttonGroup = new ButtonGroup<>();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(1);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_political", 10, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_terrain", 32, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_terrain_2", 54, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_economical", 76, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_strength", 98, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_diplomatic", 120, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_intel", 142, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_region", 164, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_infra", 186, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_resources", 208, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_revolt", 230, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_supply", 252, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_weather", 274, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_theatre", 296, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_air", 318, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, skinUi, skinFonts, localisation, listener, "mapmode_naval", 340, y);
    }

    private void setButtonMapMode(Table minimap, ButtonGroup<Button> buttonGroup, Skin skin, Skin skinUi, Skin skinFonts, Map<String, String> localisation,  MinimapListener listener, String mapMode, int x, int y) {
        Button button = new Button(skin, mapMode + "_tgl");
        button.setPosition(x, y);
        button.setChecked(this.currentMapMode.equals(mapMode));
        buttonGroup.add(button);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(button.isChecked() && !currentMapMode.equals(mapMode)) {
                    listener.changeMapMode(mapMode);
                    currentMapMode = mapMode;
                    setInformationsMapMode(skinUi,  skinFonts, listener.getInformationsMapMode(mapMode), localisation);
                }
            }
        });

        button.addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                listener.updateHoverTooltip(localisation.get(mapMode.toUpperCase()));
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                listener.hideUiTooltip();
            }
        });
        minimap.addActor(button);
    }

    private Table getMenuBarPanel(Skin skin) {
        Table menuBarPanel = new Table();

        Drawable background = skin.getDrawable(UiMinimapSkin.Drawables.MENUBAR_PANEL_BG);
        menuBarPanel.setBackground(background);
        menuBarPanel.setSize(background.getMinWidth(), background.getMinHeight());

        int x = 20;
        this.setButtonMenuBarPanel(menuBarPanel, skin, UiMinimapSkin.Styles.Button.MINIMAP_FIND_PROV_TGL, x, 12);
        this.setButtonMenuBarPanel(menuBarPanel, skin, UiMinimapSkin.Styles.Button.MINIMAP_STRATEGIC_TGL, x, 50);
        this.setButtonMenuBarPanel(menuBarPanel, skin, UiMinimapSkin.Styles.Button.MINIMAP_VICTORY_TGL, x, 88);
        this.setButtonMenuBarPanel(menuBarPanel, skin, UiMinimapSkin.Styles.Button.MINIMAP_SPY_TGL, x, 126);
        this.setButtonMenuBarPanel(menuBarPanel, skin, UiMinimapSkin.Styles.Button.MINIMAP_NUKE_TGL, x, 164);
        this.setButtonMenuBarPanel(menuBarPanel, skin, UiMinimapSkin.Styles.Button.MINIMAP_MISSION_TGL, x, 202);

        return menuBarPanel;
    }

    private void setButtonMenuBarPanel(Table menuBarPanel, Skin skin, String buttonName, int x, int y) {
        Button button = new Button(skin, buttonName);
        button.setPosition(x, y);
        menuBarPanel.addActor(button);
    }

    private void setInformationsMapMode(Skin skinUi, Skin skinFonts, IntObjectMap<String> informations, Map<String, String> localisation) {
        if(this.informationsMapMode != null) {
            for (Actor child : this.informationsMapMode.getChildren()) {
                if (child instanceof ColorRectangle colorRectangle) {
                    colorRectangle.dispose();
                }
            }
            this.informationsMapMode.clearChildren();
            this.informationsMapMode.setVisible(true);
        } else {
            this.informationsMapMode = new Table();
            this.informationsMapMode.setPosition(25, 170);
            this.addActor(this.informationsMapMode);
        }
        if(informations == null) {
            this.informationsMapMode.setVisible(false);
            return;
        }
        NinePatch ninePatch = skinUi.getPatch(UiSkin.Drawables.TILES_DIALOG);
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(ninePatch);
        this.informationsMapMode.setBackground(ninePatchDrawable);

        Label.LabelStyle labelStyleArial14Glow = skinFonts.get(FontsSkin.Styles.Label.ARIAL_14_GLOW, Label.LabelStyle.class);

        for(IntObjectMap.Entry<String> entry : informations) {
            int color = entry.key;
            Label label = new Label(localisation.get(entry.value), labelStyleArial14Glow);

            ColorRectangle colorRectangle = new ColorRectangle(color);
            colorRectangle.setSize(label.getHeight() * 1.2f, label.getHeight() * 0.85f);

            this.informationsMapMode.add(colorRectangle);
            this.informationsMapMode.add(label).left().padLeft(5).row();
        }

        this.informationsMapMode.pack();
    }
}
