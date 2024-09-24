package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Scaling;

public class HoverBox extends Table {
    private Label label;
    private Image flag;
    private float marginWidth;
    private float heightWidth;

    public HoverBox(Skin skinUi, Skin skinFonts) {
        NinePatch ninePatch = skinUi.getPatch("small_tiles_dialog");
        this.marginWidth = ninePatch.getLeftWidth() + ninePatch.getRightWidth();
        this.heightWidth = ninePatch.getTopHeight() + ninePatch.getBottomHeight();
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(ninePatch);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skinFonts.getFont("arial_14_glow");

        this.label = new Label("", labelStyle);
        this.flag = new Image();
        this.flag.setScaling(Scaling.fit);

        this.setBackground(ninePatchDrawable);
        this.add(this.label);
        this.add(this.flag);
        this.setVisible(false);
    }

    public void update(String text, Drawable flag) {
        if(!text.equals(this.label.getText().toString())) {
            this.label.setText(text);
            this.flag.setDrawable(flag);
            this.resize();
        }
    }

    private void resize() {
        this.pack();
        float flagFitWidth = this.flag.getWidth() * (this.label.getHeight() / this.flag.getHeight());
        float marginBetween = this.marginWidth / 2f;
        this.setSize(this.label.getWidth() + marginBetween + flagFitWidth + this.marginWidth,
                this.label.getHeight() + this.heightWidth);
    }
}
