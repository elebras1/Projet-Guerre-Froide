package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.populaire.projetguerrefroide.utils.TextureRegionOperations;

import java.util.ArrayList;
import java.util.List;

public class AnimatedCursor {
    private static final float frameDuration = 1 / 11f;
    private float elapsedTime;
    private Animation<TextureRegion> animation;
    private List<Cursor> cursors;
    private int currentCursorIndex;

    public AnimatedCursor(String name) {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/cursor/" + name + "/" + name + ".atlas"));
        this.animation = new Animation<>(frameDuration, atlas.findRegions(name));
        this.animation.setFrameDuration(frameDuration);

        this.cursors = new ArrayList<>();
        for (TextureRegion region : this.animation.getKeyFrames()) {
            Pixmap pixmap = TextureRegionOperations.extractPixmapFromTextureRegion(region);
            this.cursors.add(Gdx.graphics.newCursor(pixmap, 0, 0));
            pixmap.dispose();
        }
    }

    public void update(float deltaTime) {
        elapsedTime += deltaTime;
        int frameIndex = this.animation.getKeyFrameIndex(this.elapsedTime % this.animation.getAnimationDuration());
        if (frameIndex != this.currentCursorIndex) {
            this.currentCursorIndex = frameIndex;
            Gdx.graphics.setCursor(this.cursors.get(this.currentCursorIndex));
        }
    }

    public void dispose() {
        for (Cursor cursor : this.cursors) {
            cursor.dispose();
        }
    }
}
