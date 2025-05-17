package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.populaire.projetguerrefroide.util.TextureRegionOperations;

import java.util.ArrayList;
import java.util.List;

public class AnimatedCursor {
    private static final float frameDuration = 1f;
    private float elapsedTime;
    private final Animation<TextureRegion> animation;
    private final List<Cursor> cursors;
    private int currentCursorIndex;

    public AnimatedCursor(String name) {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/cursor/" + name + "/" + name + ".atlas"));
        this.animation = new Animation<>(frameDuration, atlas.findRegions(name));
        this.animation.setFrameDuration(frameDuration);
        this.animation.setPlayMode(Animation.PlayMode.LOOP);

        this.cursors = new ArrayList<>();
        for (TextureRegion region : this.animation.getKeyFrames()) {
            Pixmap pixmap = TextureRegionOperations.extractPixmapFromTextureRegion(region);
            this.cursors.add(Gdx.graphics.newCursor(pixmap, 0, 0));
            pixmap.dispose();
        }
    }

    public void update(float deltaTime) {
        this.elapsedTime += deltaTime;
        int frameIndex = this.animation.getKeyFrameIndex(this.elapsedTime);

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
