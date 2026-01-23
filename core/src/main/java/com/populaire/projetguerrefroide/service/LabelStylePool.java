package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectMap;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelStylePool {
    private final Map<String, LabelStyle> labelStyles;
    private final Map<String, Color> colors;
    private final Skin skinFonts;
    private final List<String> specialLanguages;
    private final String language;
    private final Pattern patternNumber = Pattern.compile( "(_[0-9]{2})");

    public LabelStylePool(Skin skinFonts, String language) {
        this.labelStyles = new ObjectObjectMap<>();
        this.colors = new ObjectObjectMap<>();
        this.specialLanguages = new ObjectList<>();
        this.specialLanguages.add("RUSSIAN");
        this.specialLanguages.add("CHINESE");
        this.specialLanguages.add("ARABIC");
        this.skinFonts = skinFonts;
        this.language = language;
    }

    private void addLabelStyle(String name) {
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = this.getFont(name);
        this.labelStyles.put(name, labelStyle);
    }

    private void addLabelStyle(String name, String color) {
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = this.getFont(name);
        labelStyle.fontColor = this.getColor(color);
        this.labelStyles.put(name + "_" + color, labelStyle);
    }

    public LabelStyle get(String name) {
        if (!this.hasFont(name)) {
            throw new IllegalArgumentException("Font with name '" + name + "' not found in Skin.");
        }

        if (!this.labelStyles.containsKey(name)) {
            this.addLabelStyle(name);
        }

        return this.labelStyles.get(name);
    }

    public LabelStyle get(String name, String color) {
        if (!this.hasFont(name)) {
            throw new IllegalArgumentException("Font with name '" + name + "' not found in Skin.");
        }

        if (!this.labelStyles.containsKey(name + "_" + color)) {
            this.addLabelStyle(name, color);
        }

        return this.labelStyles.get(name + "_" + color);
    }

    private void addColor(String name) {
        this.colors.put(name, this.skinFonts.getColor(name));
    }

    private Color getColor(String name) {
        if (!this.skinFonts.has(name, Color.class)) {
            throw new IllegalArgumentException("Color with name '" + name + "' not found in Skin.");
        }

        if (!this.colors.containsKey(name)) {
            this.addColor(name);
        }

        return this.colors.get(name);
    }

    private BitmapFont getFont(String name) {
        if(this.specialLanguages.contains(this.language)) {
            return this.skinFonts.getFont(this.latinToSpecial(name));
        }

        return this.skinFonts.getFont(name);
    }

    private boolean hasFont(String name) {
        if(this.specialLanguages.contains(this.language)) {
            return this.skinFonts.has(this.latinToSpecial(name), BitmapFont.class);
        }
        return this.skinFonts.has(name, BitmapFont.class);
    }

    private String latinToSpecial(String name) {
        String fontSize = "_16";
        Matcher matcher = this.patternNumber.matcher(name);
        if(matcher.find()) {
            fontSize = matcher.group(1);
        }

        return this.language.toLowerCase() + fontSize;
    }
}
