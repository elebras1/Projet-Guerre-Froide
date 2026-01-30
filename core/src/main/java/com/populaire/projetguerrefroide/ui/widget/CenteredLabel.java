package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class CenteredLabel extends Label {
    public CenteredLabel(CharSequence text, LabelStyle style) {
        super(text, style);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x - this.getWidth() / 2, y);
    }

    @Override
    public void setText(CharSequence newText) {
        super.setText(newText);
        this.pack();
        this.setPosition(this.getX() + this.getWidth() / 2, this.getY());
    }
}
