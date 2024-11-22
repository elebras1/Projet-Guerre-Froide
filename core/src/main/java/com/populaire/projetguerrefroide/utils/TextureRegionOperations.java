package com.populaire.projetguerrefroide.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureRegionOperations {

    /**
     * Extract a pixmap from a texture region
     *
     * @param textureRegion the texture region
     * @return the pixmap
     */
    public static Pixmap extractPixmapFromTextureRegion(TextureRegion textureRegion) {
        Texture texture = textureRegion.getTexture();
        TextureData textureData = texture.getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }

        // Create a pixmap with the size of the texture region
        Pixmap pixmap = new Pixmap(textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), textureData.getFormat());
        // Copy the region of the texture to the pixmap
        pixmap.drawPixmap(
                textureData.consumePixmap(), // The Pixmap containing the texture data
                0, // The target x-coordinate (top left corner)
                0, // The target y-coordinate (top left corner)
                textureRegion.getRegionX(), // The source x-coordinate (top left corner)
                textureRegion.getRegionY(), // The source y-coordinate (top left corner)
                textureRegion.getRegionWidth(), // The width of the area from the other Pixmap in pixels
                textureRegion.getRegionHeight() // The height of the area from the other Pixmap in pixels
        );

        return pixmap;
    }
}
