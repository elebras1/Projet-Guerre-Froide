package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class AlignRightLabel extends Label {
    private float x;
    private float y;

    public AlignRightLabel(CharSequence text, LabelStyle style) {
        super(text, style);
        this.x = 0;
        this.y = 0;
    }

    @Override
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        super.setPosition(this.x - this.getWidth(), this.y);
    }

    @Override
    public void setText(CharSequence newText) {
        super.setText(newText);
        this.setSize(getPrefWidth(), getPrefHeight());
        super.setPosition(this.y - this.getWidth(), this.y);
    }
}
