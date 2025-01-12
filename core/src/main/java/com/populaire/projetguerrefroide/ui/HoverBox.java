package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.populaire.projetguerrefroide.service.LabelStylePool;

public class HoverBox extends Table {
    private final Label label;
    private final Image image;
    private final float marginWidth;
    private final float heightWidth;

    public HoverBox(Skin skinUi, LabelStylePool labelStylePool) {
        NinePatch ninePatch = skinUi.getPatch("tiles_dialog");
        this.marginWidth = ninePatch.getLeftWidth() + ninePatch.getRightWidth();
        this.heightWidth = ninePatch.getTopHeight() + ninePatch.getBottomHeight();
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(ninePatch);

        Label.LabelStyle labelStyleArial14Glow = labelStylePool.getLabelStyle("arial_14_glow");

        this.label = new Label("", labelStyleArial14Glow);
        this.image = new Image();
        this.image.setScaling(Scaling.fit);

        this.setBackground(ninePatchDrawable);
        this.add(this.label);
        this.add(this.image);
        this.setVisible(false);
    }

    public void update(String text, Drawable flag) {
        if(!text.equals(this.label.getText().toString())) {
            this.label.setText(text);
            this.image.setDrawable(flag);
            this.resize();
        }
    }

    public void update(String text) {
        if(!text.equals(this.label.getText().toString())) {
            this.label.setText(text);
            this.image.setDrawable(null);
            this.resize();
        }
    }

    private void resize() {
        this.pack();
        float flagFitWidth = 0;
        if(this.image.getDrawable() != null) {
            flagFitWidth = this.image.getWidth() * (this.label.getHeight() / this.image.getHeight());
        }
        float marginBetween = this.marginWidth / 2f;
        float width = this.label.getWidth() + marginBetween + flagFitWidth + this.marginWidth;
        float height = this.label.getHeight() + this.heightWidth;
        this.setSize(width, height);
    }
}
