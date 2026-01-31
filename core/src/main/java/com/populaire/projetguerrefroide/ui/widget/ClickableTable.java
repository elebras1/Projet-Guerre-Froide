package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ClickableTable extends Table {
    private final Color normalColor;
    private final Color clickColor;

    public ClickableTable() {
        super();
        this.normalColor = new Color(1f, 1f, 1f, 1f);
        this.clickColor = new Color(0.82f, 0.82f, 0.82f, 1f);
        this.setColor(this.normalColor);
        this.setTouchable(Touchable.enabled);

        this.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                setColorRecursively(clickColor);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                setColorRecursively(normalColor);
            }
        });
    }

    private void setColorRecursively(Color color) {
        this.setColor(color);
        for (Actor child : this.getChildren()) {
            child.setColor(color);
        }
    }
}
