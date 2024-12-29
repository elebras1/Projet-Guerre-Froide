package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class Debug extends Table {
    private final Label fps;
    private final Label mousePosition;
    private final Label memoryUsage;
    private final Label delta;
    private final Label heap;
    private final Label resolution;
    private final Label totalProvinces;
    private final Label threadCount;

    public Debug(short totalProvinces) {
        this.fps = this.createLabel();
        this.mousePosition = this.createLabel();
        this.memoryUsage = this.createLabel();
        this.delta = this.createLabel();
        this.heap = this.createLabel();
        this.resolution = this.createLabel();
        this.totalProvinces = this.createLabel();
        this.totalProvinces.setText("Number of provinces : " + totalProvinces);
        this.threadCount = this.createLabel();
        this.add(this.fps).left();
        this.row();
        this.add(this.mousePosition).left();
        this.row();
        this.add(this.heap).left();
        this.row();
        this.add(this.memoryUsage).left();
        this.row();
        this.add(this.delta).left();
        this.row();
        this.add(this.resolution).left();
        this.row();
        this.add(this.totalProvinces).left();
        this.row();
        this.add(this.threadCount).left();
    }

    public void actualize(float delta) {
        Runtime runtime = Runtime.getRuntime();
        long currentTime = System.currentTimeMillis();

        this.fps.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        this.mousePosition.setText("Mouse Position: " + Gdx.input.getX() + ", " + Gdx.input.getY());
        this.memoryUsage.setText("Memory Usage: " + getMemoryUsage());
        this.delta.setText("Delta: " + delta);
        this.heap.setText("Heap: " + runtime.totalMemory() / (1024 * 1024) + " MB");
        this.resolution.setText("Resolution: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());
        this.threadCount.setText("Thread Count: " + getThreadCount());
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        return usedMemory + " MB / ";
    }

    private String getThreadCount() {
        return String.valueOf(Thread.activeCount());
    }

    private Label createLabel() {
        return new Label("", new Label.LabelStyle(new BitmapFont(), Color.GREEN));
    }
}
