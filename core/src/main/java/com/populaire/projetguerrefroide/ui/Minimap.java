package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.screen.MinimapListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class Minimap extends Table {
    public Minimap(Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, MinimapListener listener) {
        Table minimap = this.getMinimap(skin);
        Table menuBarPanel = this.getMenuBarPanel(skin);

        this.setSize(minimap.getWidth() + menuBarPanel.getWidth(), menuBarPanel.getHeight());
        minimap.setPosition(22, 0);
        menuBarPanel.setPosition(minimap.getWidth() + 5, -8);

        this.addActor(minimap);
        this.addActor(menuBarPanel);
    }

    private Table getMinimap(Skin skin) {
        Table minimap = new Table();

        Drawable background = skin.getDrawable("minimap_bg");
        minimap.setBackground(background);
        minimap.setSize(background.getMinWidth(), background.getMinHeight());

        Drawable mapDrawable = skin.getDrawable("minimap");
        Image map = new Image(mapDrawable);
        map.setPosition(11, 11);

        Button zoomInButton = new Button(skin, "map_zoom_in");
        zoomInButton.setPosition(332, 103);

        Button zoomOutButton = new Button(skin, "map_zoom_out");
        zoomOutButton.setPosition(332, 10);

        minimap.addActor(map);
        minimap.addActor(zoomInButton);
        minimap.addActor(zoomOutButton);

        return minimap;
    }

    private Table getMenuBarPanel(Skin skin) {
        Table menuBarPanel = new Table();

        Drawable background = skin.getDrawable("menubar_panel_bg");
        menuBarPanel.setBackground(background);
        menuBarPanel.setSize(background.getMinWidth(), background.getMinHeight());

        return menuBarPanel;
    }
}
