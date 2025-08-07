package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.populaire.projetguerrefroide.util.WgslUtils;

public class FlagImage extends Image {
    private TextureRegion flagTexture;
    private final TextureRegion overlayTexture;
    private final TextureRegion alphaTexture;
    private final Texture defaultTexture;
    //private final WgShaderProgram shader;

    public FlagImage(Drawable flag, TextureRegion overlay, TextureRegion alpha) {
        super(flag);
        this.overlayTexture = overlay;
        this.alphaTexture = alpha;
        Pixmap defaultPixmap = new Pixmap(this.overlayTexture.getRegionWidth(), this.overlayTexture.getRegionHeight(), Pixmap.Format.RGBA8888);
        this.defaultTexture = new WgTexture(defaultPixmap);
        //String shaderSource = WgslUtils.buildShaderSource("flag_v.wgsl", "flag_f.wgsl");
        //this.shader = new WgShaderProgram("flagShader", shaderSource, "");
    }

    public void setFlag(TextureRegion flag) {
        this.flagTexture = flag;
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        /*batch.setShader(this.shader);

        this.flagTexture.getTexture().bind(0);
        this.overlayTexture.getTexture().bind(1);
        this.alphaTexture.getTexture().bind(2);
        this.defaultTexture.bind(3);

        this.shader.setUniformi("u_textureFlag", 0);
        this.shader.setUniformi("u_textureOverlay", 1);
        this.shader.setUniformi("u_textureAlpha", 2);
        this.shader.setUniformf(
            "u_uvFlag",
            this.flagTexture.getU(),
            this.flagTexture.getV(),
            this.flagTexture.getU2(),
            this.flagTexture.getV2()
        );
        this.shader.setUniformf(
            "u_uvOverlay",
            this.overlayTexture.getU(),
            this.overlayTexture.getV(),
            this.overlayTexture.getU2(),
            this.overlayTexture.getV2()
        );
        this.shader.setUniformf(
            "u_uvAlpha",
            this.alphaTexture.getU(),
            this.alphaTexture.getV(),
            this.alphaTexture.getU2(),
            this.alphaTexture.getV2()
        );*/

        super.draw(batch, parentAlpha);

        //batch.setShader(null);
        //Gdx.gl.glActiveTexture(GL32.GL_TEXTURE0);
    }

    public void dispose() {
        this.defaultTexture.dispose();
        //this.shader.dispose();
        this.flagTexture.getTexture().dispose();
        this.overlayTexture.getTexture().dispose();
        this.alphaTexture.getTexture().dispose();
    }
}
