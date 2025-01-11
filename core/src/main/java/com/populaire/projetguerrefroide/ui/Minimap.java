package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.screen.MinimapListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class Minimap extends Table {
    private String currentMapMode;

    public Minimap(Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, MinimapListener listener) {
        this.currentMapMode = "mapmode_political";
        Table minimap = this.getMinimap(skin, listener);
        Table menuBarPanel = this.getMenuBarPanel(skin);

        this.setSize(minimap.getWidth() + menuBarPanel.getWidth(), menuBarPanel.getHeight());
        minimap.setPosition(22, 0);
        menuBarPanel.setPosition(minimap.getWidth() + 5, -8);

        this.addActor(minimap);
        this.addActor(menuBarPanel);
    }

    private Table getMinimap(Skin skin, MinimapListener listener) {
        Table minimap = new Table();

        Drawable background = skin.getDrawable("minimap_bg");
        minimap.setBackground(background);
        minimap.setSize(background.getMinWidth(), background.getMinHeight());

        Drawable mapDrawable = skin.getDrawable("minimap");
        Image map = new Image(mapDrawable);
        map.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                short worldX = (short) ((x / map.getWidth()) * WORLD_WIDTH);
                short worldY = (short) ((y / map.getHeight()) * WORLD_HEIGHT);
                listener.moveCamera(worldX, worldY);
            }
        });
        map.setPosition(11, 11);

        Button zoomInButton = new Button(skin, "map_zoom_in");
        zoomInButton.setPosition(332, 103);
        zoomInButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                listener.zoomIn();
            }
        });

        Button zoomOutButton = new Button(skin, "map_zoom_out");
        zoomOutButton.setPosition(332, 10);
        zoomOutButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                listener.zoomOut();
            }
        });

        this.setMapMode(minimap, skin, listener);

        minimap.addActor(map);
        minimap.addActor(zoomInButton);
        minimap.addActor(zoomOutButton);

        return minimap;
    }

    private void setMapMode(Table minimap, Skin skin, MinimapListener listener) {
        int y = 134;
        ButtonGroup<Button> buttonGroup = new ButtonGroup<>();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(1);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_political", 10, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_terrain", 32, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_terrain_2", 54, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_economical", 76, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_strength", 98, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_diplomatic", 120, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_intel", 142, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_region", 164, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_infra", 186, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_resources", 208, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_revolt", 230, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_supply", 252, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_weather", 274, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_theatre", 296, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_air", 318, y);
        this.setButtonMapMode(minimap, buttonGroup, skin, listener, "mapmode_naval", 340, y);
    }

    private void setButtonMapMode(Table minimap, ButtonGroup<Button> buttonGroup, Skin skin, MinimapListener listener, String buttonName, int x, int y) {
        Button button = new Button(skin, buttonName);
        button.setPosition(x, y);
        button.setChecked(this.currentMapMode.equals(buttonName));
        buttonGroup.add(button);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(button.isChecked() && !currentMapMode.equals(buttonName)) {
                    listener.changeMapMode(buttonName);
                    currentMapMode = buttonName;
                }
            }
        });
        minimap.addActor(button);
    }

    private Table getMenuBarPanel(Skin skin) {
        Table menuBarPanel = new Table();

        Drawable background = skin.getDrawable("menubar_panel_bg");
        menuBarPanel.setBackground(background);
        menuBarPanel.setSize(background.getMinWidth(), background.getMinHeight());

        int x = 20;
        this.setButtonMenuBarPanel(menuBarPanel, skin, "minimap_find_prov_btn", x, 12);
        this.setButtonMenuBarPanel(menuBarPanel, skin, "minimap_strategic_btn", x, 50);
        this.setButtonMenuBarPanel(menuBarPanel, skin, "minimap_victory_btn", x, 88);
        this.setButtonMenuBarPanel(menuBarPanel, skin, "minimap_spy_btn", x, 126);
        this.setButtonMenuBarPanel(menuBarPanel, skin, "minimap_nuke_btn", x, 164);
        this.setButtonMenuBarPanel(menuBarPanel, skin, "minimap_mission_btn", x, 202);

        return menuBarPanel;
    }

    private void setButtonMenuBarPanel(Table menuBarPanel, Skin skin, String buttonName, int x, int y) {
        Button button = new Button(skin, buttonName);
        button.setPosition(x, y);
        menuBarPanel.addActor(button);
    }
}
