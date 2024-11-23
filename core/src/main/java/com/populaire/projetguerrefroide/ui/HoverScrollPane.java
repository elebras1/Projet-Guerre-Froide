package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class HoverScrollPane extends ScrollPane {
    public HoverScrollPane(Actor actor, Skin skin) {
        super(actor, skin);
    }

    @Override
    protected void addScrollListener() {
        this.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (isOver(x, y)) {
                    setScrollbarsVisible(true);
                    if (isScrollY() || isScrollX()) {
                        if (isScrollY()) {
                            if (!isScrollX() && amountY == 0) amountY = amountX;
                        } else {
                            if (isScrollX() && amountX == 0) amountX = amountY;
                        }
                        setScrollY(getScrollY() + getMouseWheelY() * amountY);
                        setScrollX(getScrollX() + getMouseWheelX() * amountX);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private boolean isOver(float x, float y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }
}
