package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;
import com.populaire.projetguerrefroide.util.LocalisationUtils;

import java.util.Map;

public class HoverTooltip extends Table {
    private final WidgetFactory widgetFactory;
    private final Map<String, String> localisation;
    private final Skin skinFlags;
    private final Label mainLabel;
    private final Label subLabel;
    private final Image image;
    private final float marginWidth;
    private final float heightWidth;
    private final StringBuilder text;

    public HoverTooltip(WidgetFactory widgetFactory, Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation) {
        this.widgetFactory = widgetFactory;
        this.localisation = localisation;
        this.skinFlags = skinFlags;
        NinePatch ninePatch = skinUi.getPatch("tiles_dialog");
        this.marginWidth = ninePatch.getLeftWidth() + ninePatch.getRightWidth();
        this.heightWidth = ninePatch.getTopHeight() + ninePatch.getBottomHeight();
        this.text = new StringBuilder();
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

    public void update(int provinceId, String countryId, String colonizerId) {
        String mainText = this.localisation.get(String.valueOf(provinceId)) + " (" + LocalisationUtils.getCountryNameLocalisation(localisation, countryId, colonizerId) + ")";
        if(!mainText.equals(this.mainLabel.getText().toString())) {
            this.mainLabel.setText(mainText);
            this.image.setDrawable(this.widgetFactory.getFlagDrawable(this.skinFlags, countryId, colonizerId));
            this.subLabel.remove();
            this.resize();
        }
    }

    public void update(int provinceId, String countryId, String colonizerId, ObjectIntMap<String> elements) {
        this.text.setLength(0);
        this.text.append(this.localisation.get(String.valueOf(provinceId))).append(" (").append(LocalisationUtils.getCountryNameLocalisation(localisation, countryId, colonizerId)).append(")");
        if (!this.text.toString().equals(this.mainLabel.getText().toString())) {
            this.mainLabel.setText(this.text.toString());
            this.image.setDrawable(this.widgetFactory.getFlagDrawable(this.skinFlags, countryId, colonizerId));

            this.text.setLength(0);
            int i = 0, size = elements.size();
            for (ObjectIntMap.Entry<String> entry : elements) {
                this.text.append(this.localisation.get(entry.getKey())).append(" (").append(entry.getValue()).append("%)");
                if (++i < size) {
                    this.text.append("\n");
                }
            }


            this.subLabel.setText(this.text.toString());

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
