package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.Scaling;
import com.populaire.projetguerrefroide.protocol.catalog.NamedTable;
import com.populaire.projetguerrefroide.protocol.response.ProvinceHover;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class Tooltip extends Table {
    private final WidgetFactory widgetFactory;
    private final Map<String, String> localisation;
    private final Skin skinFlags;
    private final Label mainLabel;
    private final Label subLabel;
    private final Image image;
    private final float marginWidth;
    private final float heightWidth;
    private final CharArray text;
    private final CharArray subText;
    private final Cell<Label> subCell;

    public Tooltip(WidgetFactory widgetFactory, Skin skinUi, Skin skinFlags, Skin skinFonts, Map<String, String> localisation) {
        this.widgetFactory = widgetFactory;
        this.localisation = localisation;
        this.skinFlags = skinFlags;
        NinePatch ninePatch = skinUi.getPatch(UiSkin.Drawables.TILES_DIALOG);
        this.marginWidth = ninePatch.getLeftWidth() + ninePatch.getRightWidth();
        this.heightWidth = ninePatch.getTopHeight() + ninePatch.getBottomHeight();
        this.text = new CharArray(64);
        this.subText = new CharArray(128);
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(ninePatch);

        Label.LabelStyle labelStyleArial14Glow = skinFonts.get(FontsSkin.Styles.Label.ARIAL_14_GLOW, Label.LabelStyle.class);
        Label.LabelStyle labelStyleArial14GlowYellow = skinFonts.get(FontsSkin.Styles.Label.ARIAL_14_GLOW_YELLOW, Label.LabelStyle.class);

        this.mainLabel = new Label("", labelStyleArial14Glow);
        this.subLabel = new Label("", labelStyleArial14GlowYellow);
        this.image = new Image();
        this.image.setScaling(Scaling.fit);

        Table mainTable = new Table();
        mainTable.add(this.mainLabel);
        mainTable.add(this.image);

        this.setBackground(ninePatchDrawable);
        this.add(mainTable).left().row();
        this.subCell = this.add(this.subLabel).expandX().top().left();
    }

    public void updateProvince(UiCatalog uiCatalog, int provinceIndex, int ownerIndex, ProvinceHover hover, boolean cultures) {
        this.text.clear();
        this.text.append(uiCatalog.provinceName(provinceIndex)).append(" (").append(uiCatalog.countryName(ownerIndex)).append(")");
        boolean withElements = hover != null;
        if (withElements) {
            this.subText.clear();
            NamedTable elements = cultures ? uiCatalog.catalog().cultures() : uiCatalog.catalog().religions();
            int count = cultures ? hover.cultureCount() : hover.religionCount();
            for (int i = 0; i < count; i++) {
                int element = cultures ? hover.culture(i) : hover.religion(i);
                int percent = cultures ? hover.culturePercent(i) : hover.religionPercent(i);
                this.subText.append(element >= 0 ? uiCatalog.localized(elements.nameId(element)) : null).append(" (").append(percent).append("%)");
                if (i + 1 < count) {
                    this.subText.append('\n');
                }
            }
        }
        boolean subShown = this.getChildren().contains(this.subLabel, true);
        boolean mainChanged = !this.mainLabel.textEquals(this.text);
        boolean subChanged = withElements != subShown || (withElements && !this.subLabel.textEquals(this.subText));
        if (!mainChanged && !subChanged) {
            return;
        }
        if (mainChanged) {
            this.mainLabel.setText(this.text);
            this.image.setDrawable(uiCatalog.flagDrawable(ownerIndex));
        }
        if (withElements) {
            this.subLabel.setText(this.subText);
            if (!subShown) {
                this.subCell.setActor(this.subLabel);
            }
        } else if (subShown) {
            this.subCell.clearActor();
        }
        this.resize();
    }

    public void update(String text) {
        if (!this.mainLabel.textEquals(text)) {
            this.mainLabel.setText(text);
            this.image.setDrawable(null);
            this.subCell.clearActor();
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
