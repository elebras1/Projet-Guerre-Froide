package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

public class FillBar extends ProgressBar {

    public FillBar(float min, float max, float stepSize, boolean vertical, ProgressBar.ProgressBarStyle style) {
        super(min, max, stepSize, vertical, style);
        this.setAnimateDuration(0.0f);
    }

    public void setValue(int value, int maxValue) {
        this.setRange(0, maxValue);
        super.setValue(value);
    }
}
