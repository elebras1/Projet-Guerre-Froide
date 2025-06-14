package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

public class CursorManager implements Disposable {
    private AnimatedCursor animatedCursor;
    private Cursor cursor;
    private int width;
    private int height;

    public CursorManager() {
        this.defaultCursor();
    }

    public void defaultCursor() {
        if(this.animatedCursor != null) {
            this.animatedCursor.dispose();
            this.animatedCursor = null;
        }
        Pixmap pixmap = new Pixmap(Gdx.files.internal("ui/cursor/normal.png"));
        int xHotspot = 0, yHotspot = 0;
        this.cursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
        this.width = pixmap.getWidth();
        this.height = pixmap.getHeight();
        pixmap.dispose();
        Gdx.graphics.setCursor(this.cursor);
    }

    public void animatedCursor(String name) {
        if(this.animatedCursor == null) {
            this.animatedCursor = new AnimatedCursor(name);
            this.animatedCursor.update(Gdx.graphics.getDeltaTime());
        }
    }

    public void update(float deltaTime) {
        if(this.animatedCursor != null) {
            this.animatedCursor.update(deltaTime);
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public void dispose() {
        if(this.animatedCursor != null) {
            this.animatedCursor.dispose();
        }
        this.cursor.dispose();
    }
}
