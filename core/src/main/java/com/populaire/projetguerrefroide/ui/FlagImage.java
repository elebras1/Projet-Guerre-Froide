package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.utils.TextureOperations;

public class FlagImage extends Image {
    private Texture flagTexture;
    private Texture overlayTexture;
    private Texture alphaTexture;
    private Texture defaultTexture;
    private Pixmap defaultPixmap;
    private ShaderProgram shader;
    public FlagImage(Drawable flag, TextureRegion overlay, TextureRegion alpha) {
        super(flag);
        this.flagTexture = new Texture(new Pixmap((int) flag.getMinWidth(), (int) flag.getMinHeight(), Pixmap.Format.RGBA8888));
        this.overlayTexture = new Texture(TextureOperations.extractPixmapFromTextureRegion(overlay));
        this.alphaTexture = new Texture(TextureOperations.extractPixmapFromTextureRegion(alpha));
        this.defaultPixmap = new Pixmap(this.overlayTexture.getWidth(), this.overlayTexture.getHeight(), Pixmap.Format.RGBA8888);
        this.defaultTexture = new Texture(this.defaultPixmap);
        String vertexShader = Gdx.files.internal("shaders/flag_v.glsl").readString();
        String fragmentShader = Gdx.files.internal("shaders/flag_f.glsl").readString();
        this.shader = new ShaderProgram(vertexShader, fragmentShader);
    }

    public void setFlag(TextureRegion flag) {
        if (this.flagTexture != null) {
            this.flagTexture.dispose();
        }

        Pixmap flagPixmap = TextureOperations.extractPixmapFromTextureRegion(flag);

        int flagWidth = flagPixmap.getWidth();
        int flagHeight = flagPixmap.getHeight();
        int overlayWidth = this.overlayTexture.getWidth();
        int overlayHeight = this.overlayTexture.getHeight();

        int x = (overlayWidth - flagWidth) / 2;
        int y = (overlayHeight - flagHeight) / 2;

        this.defaultPixmap.drawPixmap(flagPixmap, x, y);

        this.flagTexture = new Texture(this.defaultPixmap);

        flagPixmap.dispose();
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setShader(this.shader);

        this.flagTexture.bind(0);
        this.overlayTexture.bind(1);
        this.alphaTexture.bind(2);
        this.defaultTexture.bind(3);


        this.shader.setUniformi("u_textureFlag", 0);
        this.shader.setUniformi("u_textureOverlay", 1);
        this.shader.setUniformi("u_textureAlpha", 2);
        this.shader.setUniformf("u_flagSize", this.flagTexture.getWidth(), this.flagTexture.getHeight());
        this.shader.setUniformf("u_overlaySize", this.overlayTexture.getWidth(), this.overlayTexture.getHeight());

        super.draw(batch, parentAlpha);

        batch.setShader(null);
        Gdx.gl.glActiveTexture(GL32.GL_TEXTURE0);
    }

    public void dispose() {
        if (this.shader != null) {
            this.shader.dispose();
        }
        this.flagTexture.dispose();
        this.overlayTexture.dispose();
    }
}
