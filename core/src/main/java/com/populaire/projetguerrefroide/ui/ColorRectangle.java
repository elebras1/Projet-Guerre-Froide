package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ColorRectangle extends Actor {
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

    public void dispose() {
        this.texture.dispose();
    }
}
