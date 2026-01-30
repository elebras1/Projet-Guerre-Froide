package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.List;

public class WidgetFactory {

    public WidgetFactory() {
    }

    public Table createBackgroundTable(Skin skin, String backgroundName, float x, float y) {
        Drawable background = skin.getDrawable(backgroundName);
        Table table = new Table();
        table.setBackground(background);
        table.setSize(background.getMinWidth(), background.getMinHeight());
        table.setPosition(x, y);
        return table;
    }

    public Drawable applyBackgroundToTable(Skin skin, String backgroundName, Table table) {
        Drawable background = skin.getDrawable(backgroundName);
        table.setBackground(background);
        table.setSize(background.getMinWidth(), background.getMinHeight());
        return background;
    }

    public Label createLabel(String text, Label.LabelStyle labelStyle, float x, float y, Group parent) {
        Label label = new Label(text, labelStyle);
        label.setPosition(x, y);
        parent.addActor(label);
        return label;
    }

    public Label createLabel(Label.LabelStyle labelStyle, Group parent) {
        Label label = new Label("", labelStyle);
        parent.addActor(label);
        return label;
    }

    public Label createLabel(String text, Label.LabelStyle labelStyle, Group parent) {
        Label label = new Label(text, labelStyle);
        parent.addActor(label);
        return label;
    }

    public Label createLabel(String text, Label.LabelStyle labelStyle, float x, float y, float width, float height, int align, Group parent) {
        Label label = new Label(text, labelStyle);
        label.setBounds(x, y, width, height);
        label.setAlignment(align);
        parent.addActor(label);
        return label;
    }

    public Label createLabelCentered(String text, Label.LabelStyle labelStyle, float x, float y, Group parent) {
        CenteredLabel label = new CenteredLabel(text, labelStyle);
        label.setPosition(x, y);
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

    public Image createImageWithOverlay(Skin skin, String drawableName, float x, float y, float width, float height, Group parent) {
        Image image = new Image();
        Stack stack = new Stack();
        stack.add(image);
        stack.add(new Image(skin.getDrawable(drawableName)));
        stack.setBounds(x, y, width, height);
        parent.addActor(stack);
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

    public FlagImage createFlagImage(Skin skin, String alphaFlagName, String overlayFlagName) {
        TextureRegion alphaFlag = skin.getRegion(alphaFlagName);
        TextureRegion overlayFlag = skin.getRegion(overlayFlagName);
        return new FlagImage(overlayFlag, alphaFlag);
    }

    public Drawable getFlagDrawable(Skin skin, String countryNameId, String colonizerNameId) {
        if (colonizerNameId == null) {
            return skin.getDrawable(countryNameId);
        }

        if (skin.has(countryNameId + "_COL_" + colonizerNameId, TextureRegion.class)) {
            return skin.getDrawable(countryNameId + "_COL_" + colonizerNameId);
        }

        return skin.getDrawable(colonizerNameId);
    }

    public TextureRegion getFlagTextureRegion(Skin skin, String countryNameId, String colonizerNameId) {
        if (colonizerNameId == null) {
            return skin.getRegion(countryNameId);
        }

        if (skin.has(countryNameId + "_COL_" + colonizerNameId, TextureRegion.class)) {
            return skin.getRegion(countryNameId + "_COL_" + colonizerNameId);
        }

        return skin.getRegion(colonizerNameId);
    }
}
