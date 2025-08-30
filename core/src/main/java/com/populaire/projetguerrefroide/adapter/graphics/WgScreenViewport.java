package com.populaire.projetguerrefroide.adapter.graphics;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class WgScreenViewport extends ScreenViewport {
    private final WgProjection projection;

    public WgScreenViewport() {
        super();
        this.projection = new WgProjection();
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        this.setScreenBounds(0, 0, screenWidth, screenHeight);
        this.setWorldSize(screenWidth * this.getUnitsPerPixel(), screenHeight * this.getUnitsPerPixel());
        this.apply(centerCamera);
        this.projection.setProjectionMatrix(this.getCamera().combined);
    }

    public Matrix4 getProjectionMatrix() {
        return this.projection.getCombinedMatrix();
    }
}
