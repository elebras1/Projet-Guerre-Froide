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
    private final Label mainLabel;
    private final Label subLabel;
    private final Image image;
    private final float marginWidth;
    private final float heightWidth;

    public HoverBox(Skin skinUi, LabelStylePool labelStylePool) {
        NinePatch ninePatch = skinUi.getPatch("tiles_dialog");
        this.marginWidth = ninePatch.getLeftWidth() + ninePatch.getRightWidth();
        this.heightWidth = ninePatch.getTopHeight() + ninePatch.getBottomHeight();
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(ninePatch);

        Label.LabelStyle labelStyleArial14Glow = labelStylePool.getLabelStyle("arial_14_glow");
        Label.LabelStyle labelStyleArial14GlowYellow = labelStylePool.getLabelStyle("arial_14_glow", "yellow");

        this.mainLabel = new Label("", labelStyleArial14Glow);
        this.subLabel = new Label("", labelStyleArial14GlowYellow);
        this.image = new Image();
        this.image.setScaling(Scaling.fit);

        Table mainTable = new Table();
        mainTable.add(this.mainLabel);
        mainTable.add(this.image);

        this.setBackground(ninePatchDrawable);
        this.add(mainTable).left().row();
        this.add(this.subLabel).expandX().top().left();
    }

    public void update(String text, Drawable flag) {
        if(!text.equals(this.mainLabel.getText().toString())) {
            this.mainLabel.setText(text);
            this.image.setDrawable(flag);
            this.subLabel.remove();
            this.resize();
        }
    }

    public void update(String mainText, String subText, Drawable flag) {
        if(!mainText.equals(this.mainLabel.getText().toString())) {
            this.mainLabel.setText(mainText);
            this.image.setDrawable(flag);
            this.subLabel.setText(subText);
            if (!this.getChildren().contains(this.subLabel, true)) {
                this.row().top();
                this.add(this.subLabel).expandX().top().left();
            }
            this.resize();
        }
    }

    public void update(String text) {
        if(!text.equals(this.mainLabel.getText().toString())) {
            this.mainLabel.setText(text);
            this.image.setDrawable(null);
            this.subLabel.remove();
            this.resize();
        }
    }

    private void resize() {
        this.pack();
        float flagFitWidth = 0;

        if (this.image.getDrawable() != null) {
            flagFitWidth = this.image.getWidth() * (this.mainLabel.getHeight() / this.image.getHeight());
        }

        float marginBetween = this.marginWidth / 2f;
        float maxLabelWidth = this.mainLabel.getWidth();

        if (this.getChildren().contains(this.subLabel, true)) {
            maxLabelWidth = Math.max(maxLabelWidth, this.subLabel.getWidth());
        }

        float width = maxLabelWidth + marginBetween + flagFitWidth + this.marginWidth;
        float height = this.mainLabel.getHeight() + this.heightWidth
            + (this.getChildren().contains(this.subLabel, true) ? this.subLabel.getMinHeight() : 0f);

        this.setSize(width, height);
    }

}
