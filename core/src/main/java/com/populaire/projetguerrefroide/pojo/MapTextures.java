package com.populaire.projetguerrefroide.pojo;

import com.badlogic.gdx.files.FileHandle;
import com.monstrous.gdx.webgpu.graphics.WgTexture;

public record MapTextures(WgTexture mapModeTexture, WgTexture provincesTexture, WgTexture waterTexture,
                          WgTexture colorMapWaterTexture, WgTexture provincesStripesTexture, WgTexture terrainTexture,
                          WgTexture stripesTexture, WgTexture colorMapTexture, WgTexture overlayTileTexture,
                          WgTexture riverBodyTexture, FileHandle[] terrainSheetFiles) {
}
