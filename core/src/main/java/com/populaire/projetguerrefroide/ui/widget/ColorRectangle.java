package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;

public class ColorRectangle extends Actor implements Disposable {
    private final Texture texture;

    public ColorRectangle(int color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        this.texture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(this.texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    @Override
    public void dispose() {
        this.texture.dispose();
    }
}
