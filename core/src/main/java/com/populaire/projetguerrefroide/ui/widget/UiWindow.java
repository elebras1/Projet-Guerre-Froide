package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

public class UiWindow extends WidgetGroup {
    protected final UiBuilder ui;
    private final Array<Actor> placedActors;
    private final FloatArray placedX;
    private final FloatArray placedY;
    private final Array<UiAnchor> placedAnchors;
    private final IntArray shortcutKeys;
    private final Array<Actor> shortcutOwners;
    private final Array<Runnable> shortcutActions;
    private final InputListener shortcutListener;

    public UiWindow(UiBuilder ui, float width, float height) {
        this.ui = ui;
        this.placedActors = new Array<>();
        this.placedX = new FloatArray();
        this.placedY = new FloatArray();
        this.placedAnchors = new Array<>();
        this.shortcutKeys = new IntArray();
        this.shortcutOwners = new Array<>();
        this.shortcutActions = new Array<>();
        this.shortcutListener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return UiWindow.this.runShortcut(keycode);
            }
        };
        this.setTouchable(Touchable.childrenOnly);
        this.setSize(width, height);
    }

    public UiWindow(UiBuilder ui) {
        this(ui, 0f, 0f);
        this.setFillParent(true);
    }

    void track(Actor actor, float x, float y, UiAnchor anchor) {
        int index = this.placedActors.indexOf(actor, true);
        if (index < 0) {
            this.placedActors.add(actor);
            this.placedX.add(x);
            this.placedY.add(y);
            this.placedAnchors.add(anchor);
        } else {
            this.placedX.set(index, x);
            this.placedY.set(index, y);
            this.placedAnchors.set(index, anchor);
        }
    }

    void shortcut(Actor owner, int keycode, Runnable action) {
        this.shortcutKeys.add(keycode);
        this.shortcutOwners.add(owner);
        this.shortcutActions.add(action);
    }

    @Override
    protected void setStage(Stage stage) {
        Stage previous = this.getStage();
        if (previous != null && previous != stage) {
            previous.removeListener(this.shortcutListener);
        }
        super.setStage(stage);
        if (stage != null && previous != stage && this.shortcutKeys.size > 0) {
            stage.addListener(this.shortcutListener);
        }
    }

    private boolean runShortcut(int keycode) {
        for (int i = 0; i < this.shortcutKeys.size; i++) {
            Actor owner = this.shortcutOwners.get(i);
            if (this.shortcutKeys.get(i) == keycode && owner.isVisible() && owner.ascendantsVisible() && owner.getStage() != null) {
                this.shortcutActions.get(i).run();
                return true;
            }
        }
        return false;
    }

    @Override
    public void layout() {
        for (int i = 0; i < this.placedActors.size; i++) {
            this.ui.position(this.placedActors.get(i), this.placedX.get(i), this.placedY.get(i), this.placedAnchors.get(i));
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
