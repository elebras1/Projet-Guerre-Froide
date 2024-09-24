package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class Debug extends Table {
    private Label fps;
    private Label mousePosition;
    private Label memoryUsage;
    private Label delta;

    public Debug(int x, int y) {
        this.setPosition(x, y);
        this.fps = this.createLabel();
        this.mousePosition = this.createLabel();
        this.memoryUsage = this.createLabel();
        this.delta = this.createLabel();
        this.add(this.fps).left();
        this.row();
        this.add(this.mousePosition).left();
        this.row();
        this.add(this.memoryUsage).left();
        this.row();
        this.add(this.delta).left();
    }

    public void actualize(float delta) {
        this.fps.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        this.mousePosition.setText("Mouse Position: " + Gdx.input.getX() + ", " + Gdx.input.getY());
        this.memoryUsage.setText("Memory Usage: " + getMemoryUsage());
        this.delta.setText("Delta: " + delta);
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        return usedMemory + " MB / ";
    }
    private Label createLabel() {
        return new Label("", new Label.LabelStyle(new BitmapFont(), Color.GREEN));
    }
}
