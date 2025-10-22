package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.populaire.projetguerrefroide.adapter.graphics.WgCustomStage;
import com.populaire.projetguerrefroide.ui.renderer.FlagImageRenderer;

public class FlagImage extends Actor {
    private final TextureRegion overlayTexture;
    private final TextureRegion alphaTexture;
    private final Vector2 frameBufferPosition;
    private TextureRegion flagTexture;

    public FlagImage(TextureRegion overlay, TextureRegion alpha) {
        this.setSize(overlay.getRegionWidth(), overlay.getRegionHeight());
        this.overlayTexture = overlay;
        this.alphaTexture = alpha;
        this.frameBufferPosition = new Vector2();
    }

    public void setFlag(TextureRegion flag) {
        this.flagTexture = flag;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(this.flagTexture == null) {
            return;
        }

        FlagImageRenderer renderer = ((WgCustomStage) this.getStage()).getFlagImageRenderer();
        renderer.add(this.frameBufferPosition, this.overlayTexture, this.alphaTexture, this.flagTexture, this.getWidth(), this.getHeight());
        Texture frameBufferTexture = ((WgCustomStage) this.getStage()).getFrameBuffer().getColorBufferTexture();
        batch.draw(frameBufferTexture, this.getX(), this.getY(), 0, 0, 110, 73);
    }
}
