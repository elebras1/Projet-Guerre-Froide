package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class HoverTooltip extends Table {
    private final Map<String, String> localisation;
    private final Skin skinFlags;
    private final Label mainLabel;
    private final Label subLabel;
    private final Image image;
    private final float marginWidth;
    private final float heightWidth;

    public HoverTooltip(Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation) {
        this.localisation = localisation;
        this.skinFlags = skinFlags;
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

    public void update(short provinceId, String countryName, String countryId) {
        String mainText = this.localisation.get(String.valueOf(provinceId)) + " (" + countryName + ")";
        if(!mainText.equals(this.mainLabel.getText().toString())) {
            this.mainLabel.setText(mainText);
            this.image.setDrawable(this.skinFlags.getDrawable(countryId));
            this.subLabel.remove();
            this.resize();
        }
    }

    public void update(short provinceId, String countryName, String countryId, ObjectIntMap<String> elements) {
        String mainText = this.localisation.get(String.valueOf(provinceId)) + " (" + countryName + ")";
        if (!mainText.equals(this.mainLabel.getText().toString())) {
            this.mainLabel.setText(mainText);
            this.image.setDrawable(this.skinFlags.getDrawable(countryId));

            StringBuilder subText = new StringBuilder();
            for (ObjectIntMap.Entry<String> entry : elements) {
                if (!subText.isEmpty()) {
                    subText.append("\n");
                }
                subText.append(this.localisation.get(entry.getKey())).append(" (").append(entry.getValue()).append("%)");
            }

            this.subLabel.setText(subText.toString());

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
