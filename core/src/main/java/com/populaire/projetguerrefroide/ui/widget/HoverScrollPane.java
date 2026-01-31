package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.monstrous.gdx.webgpu.scene2d.WgScrollPane;

public class HoverScrollPane extends WgScrollPane {

    public HoverScrollPane(Actor actor, Skin skin, String styleName) {
        super(actor, skin, styleName);
        this.setVariableSizeKnobs(false);
        this.setFadeScrollBars(false);
    }

    @Override
    protected void addScrollListener() {
        this.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (getStage() != null) {
                    getStage().setScrollFocus(HoverScrollPane.this);
                }
                return false;
            }
        });

        this.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (getStage() == null) return;

                Actor hitActor = getStage().hit(event.getStageX(), event.getStageY(), true);

                if (hitActor != null && (hitActor == HoverScrollPane.this || hitActor.isDescendantOf(HoverScrollPane.this))) {
                    getStage().setScrollFocus(HoverScrollPane.this);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                boolean isExitingToChild = toActor != null && toActor.isDescendantOf(HoverScrollPane.this);

                if (!isExitingToChild && getStage().getScrollFocus() == HoverScrollPane.this) {
                    getStage().setScrollFocus(null);
                }
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (getStage() == null) return false;

                Actor hitActor = getStage().hit(event.getStageX(), event.getStageY(), true);
                boolean isActuallyTouchable = hitActor != null && (hitActor == HoverScrollPane.this || hitActor.isDescendantOf(HoverScrollPane.this));

                if (isActuallyTouchable && isOver(x, y)) {
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
        return x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight();
    }
}
