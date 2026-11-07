package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class WidgetFactory {

    public WidgetFactory() {
    }

    public Table createBackgroundTable(Skin skin, String backgroundName, float x, float y, Group parent) {
        Drawable background = skin.getDrawable(backgroundName);
        Table table = new Table();
        table.setBackground(background);
        table.setSize(background.getMinWidth(), background.getMinHeight());
        table.setPosition(x, y);
        parent.addActor(table);
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

    public Label createLabelCentered(String text, Label.LabelStyle labelStyle, float x, float y, Group parent) {
        CenteredLabel label = new CenteredLabel(text, labelStyle);
        label.setPosition(x, y);
        parent.addActor(label);
        return label;
    }

    public Label createLabelAlignRight(String text, Label.LabelStyle labelStyle, float x, float y, Group parent) {
        AlignRightLabel label = new AlignRightLabel(text, labelStyle);
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

    public Image createImage(float x, float y, Group parent) {
        Image image = new Image();
        image.setPosition(x, y);
        parent.addActor(image);
        return image;
    }

    public Image createImage(Skin skin, String drawableName, float x, float y, Group parent) {
        Image image = new Image(skin.getDrawable(drawableName));
        image.setPosition(x, y);
        parent.addActor(image);
        return image;
    }

    public FillBar createFillBar(Drawable leftColor, Drawable rightColor, float x, float y, float width, float height, Group parent) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = rightColor;
        style.knobBefore = leftColor;

        style.knobBefore.setMinHeight(height);
        style.background.setMinHeight(height);

        FillBar bar = new FillBar(0, 100, 1, false, style);

        bar.setPosition(x, y);
        bar.setSize(width, height);
        bar.setAnimateDuration(0f);

        if (parent != null) {
            parent.addActor(bar);
        }

        return bar;
    }
}
