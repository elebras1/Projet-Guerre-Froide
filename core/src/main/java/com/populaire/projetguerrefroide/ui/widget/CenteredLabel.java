package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class CenteredLabel extends Label {
    private float centerX;
    private float centerY;

    public CenteredLabel(CharSequence text, LabelStyle style) {
        super(text, style);
        this.centerX = 0;
        this.centerY = 0;
    }

    @Override
    public void setPosition(float x, float y) {
        this.centerX = x;
        this.centerY = y;
        super.setPosition(this.centerX - this.getWidth() / 2f, this.centerY);
    }

    @Override
    public void setText(CharSequence newText) {
        super.setText(newText);
        this.setSize(getPrefWidth(), getPrefHeight());
        super.setPosition(this.centerX - this.getWidth() / 2f, this.centerY);
    }
}
