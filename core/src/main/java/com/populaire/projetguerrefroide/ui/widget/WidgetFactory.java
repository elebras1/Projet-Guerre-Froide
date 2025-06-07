package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.List;

public class WidgetFactory {

    public Table createBackgroundTable(Skin skin, String backgroundName, float x, float y) {
        Drawable background = skin.getDrawable(backgroundName);
        Table table = new Table();
        table.setBackground(background);
        table.setSize(background.getMinWidth(), background.getMinHeight());
        table.setPosition(x, y);
        return table;
    }

    public void applyBackgroundToTable(Skin skin, String backgroundName, Table table) {
        Drawable background = skin.getDrawable(backgroundName);
        table.setBackground(background);
        table.setSize(background.getMinWidth(), background.getMinHeight());
    }

    public Label createLabel(String text, Label.LabelStyle style, float x, float y, Group parent) {
        Label label = new Label(text, style);
        label.setPosition(x, y);
        parent.addActor(label);
        return label;
    }

    public Label createLabel(Label.LabelStyle style, Group parent) {
        Label label = new Label("", style);
        parent.addActor(label);
        return label;
    }

    public Button createButton(Skin skin, String styleName, float x, float y, Group parent) {
        Button button = new Button(skin, styleName);
        button.setPosition(x, y);
        parent.addActor(button);
        return button;
    }

    public Button createFlatButton(Skin skin, String drawableName, float x, float y, Group parent) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = skin.getDrawable(drawableName);
        Button button = new Button(style);
        button.setPosition(x, y);
        parent.addActor(button);
        return button;
    }

    public Image createImage(Group parent) {
        Image image = new Image();
        parent.addActor(image);
        return image;
    }

    public Image createImage(Skin skin, String drawableName, float x, float y) {
        Image image = new Image(skin.getDrawable(drawableName));
        image.setPosition(x, y);
        return image;
    }

    public Image createImage(Skin skin, String drawableName, float x, float y, Group parent) {
        Image image = new Image(skin.getDrawable(drawableName));
        image.setPosition(x, y);
        parent.addActor(image);
        return image;
    }

    public Table createImageRow(Skin skin, String textureName, int count, float spacing, float x, float y, List<Image> targetList, Group parent) {
        Table table = new Table();
        float imageX = 0;
        for (int i = 0; i < count; i++) {
            Image image = this.createImage(skin, textureName, imageX, 0);
            targetList.add(image);
            table.addActor(image);
            imageX += spacing;
        }
        table.setPosition(x, y);
        parent.addActor(table);
        return table;
    }

    public FlagImage createFlagImage(Skin skinUi, String alphaFlagName, String overlayFlagName, int width, int height) {
        TextureRegion alphaFlag = skinUi.getRegion(alphaFlagName);
        TextureRegion overlayFlag = skinUi.getRegion(overlayFlagName);
        Pixmap defaultPixmapFlag = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        TextureRegionDrawable defaultFlag = new TextureRegionDrawable(new Texture(defaultPixmapFlag));
        defaultPixmapFlag.dispose();
        return new FlagImage(defaultFlag, overlayFlag, alphaFlag);
    }
}
