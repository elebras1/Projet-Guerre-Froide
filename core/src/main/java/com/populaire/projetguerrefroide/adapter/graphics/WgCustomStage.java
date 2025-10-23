/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package com.populaire.projetguerrefroide.adapter.graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.xpenatan.webgpu.WGPUTextureFormat;
import com.monstrous.gdx.webgpu.graphics.utils.WgFrameBuffer;
import com.monstrous.gdx.webgpu.scene2d.WgStage;
import com.populaire.projetguerrefroide.ui.renderer.FlagImageRenderer;

public class WgCustomStage extends WgStage {

    private final WgFrameBuffer frameBuffer;
    private final FlagImageRenderer flagImageRenderer;
    private boolean frameBufferIsDirty;

    public WgCustomStage(Viewport viewport, Skin skinUi, Skin skinFlags) {
        super(viewport);
        this.frameBuffer = new WgFrameBuffer(WGPUTextureFormat.BGRA8UnormSrgb, viewport.getScreenWidth(), viewport.getScreenHeight(), true);
        this.flagImageRenderer = new FlagImageRenderer(skinUi.getAtlas().getTextures().first(), skinUi.getAtlas().getTextures().first(), skinFlags.getAtlas().getTextures().first(), viewport.getScreenWidth(), viewport.getScreenHeight());
        this.frameBufferIsDirty = true;
    }

    /** Override to call an alternative debug drawer */
    @Override
    public void draw() {
        if (!getRoot().isVisible()) {
            return;
        }

        if(this.frameBufferIsDirty) {
            this.frameBuffer.begin();
            this.flagImageRenderer.render();
            this.frameBuffer.end();
            this.frameBufferIsDirty = false;
        }

        getViewport().apply(); // Apply viewport changes (updates camera viewport dimensions)
        Camera camera = getViewport().getCamera();
        camera.update();

        Batch batch = this.getBatch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        getRoot().draw(batch, 1);
        batch.end();
    }

    public void updateRendererProjection() {
        this.flagImageRenderer.setProjectionMatrix(((WgScreenViewport) this.getViewport()).getProjectionMatrix());
        this.frameBufferIsDirty = true;
    }

    public FlagImageRenderer getFlagImageRenderer() {
        return this.flagImageRenderer;
    }

    public WgFrameBuffer getFrameBuffer() {
        return this.frameBuffer;
    }

    public void setFrameBufferIsDirty() {
        this.frameBufferIsDirty = true;
    }
}
