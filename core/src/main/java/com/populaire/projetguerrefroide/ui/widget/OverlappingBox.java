package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.SnapshotArray;

public class OverlappingBox extends WidgetGroup {
    private final float spacing;
    private final boolean alignRight;

    public OverlappingBox(float width, float height, float spacing, boolean alignRight) {
        this.spacing = spacing;
        this.alignRight = alignRight;
        this.setSize(width, height);
    }

    @Override
    public void layout() {
        SnapshotArray<Actor> children = this.getChildren();
        int count = children.size;
        if (count == 0) {
            return;
        }
        float childWidth = children.get(0).getWidth();
        float step = childWidth + this.spacing;
        if (count > 1 && childWidth + step * (count - 1) > this.getWidth()) {
            step = (this.getWidth() - childWidth) / (count - 1);
        }
        float used = childWidth + step * (count - 1);
        float start = this.alignRight ? this.getWidth() - used : 0f;
        for (int i = 0; i < count; i++) {
            Actor child = children.get(i);
            child.setPosition(start + i * step, this.getHeight() - child.getHeight());
        }
    }

    @Override
    public float getPrefWidth() {
        return this.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return this.getHeight();
    }
}
