package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class ProvincePanel extends Table {
    public ProvincePanel(Skin skin, Skin skinUi, LabelStylePool labelStylePool, Map<String, String> localisation) {
        Drawable background = skin.getDrawable("bg_province");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
    }
}
