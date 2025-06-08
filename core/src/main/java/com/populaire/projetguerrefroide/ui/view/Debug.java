package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class Debug extends Table {
    private static final float MEMORY_UPDATE_INTERVAL = 0.5f;

    private final Label fpsLabel;
    private final Label mousePositionLabel;
    private final Label memoryLabel;
    private final Label deltaLabel;
    private final Label heapLabel;
    private final Label resolutionLabel;
    private final Label totalProvincesLabel;
    private final Label threadCountLabel;

    private float timeSinceLastStats;

    public Debug(short totalProvinces) {
        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.GREEN);

        this.fpsLabel = new Label("", labelStyle);
        this.mousePositionLabel = new Label("", labelStyle);
        this.memoryLabel = new Label("", labelStyle);
        this.deltaLabel = new Label("", labelStyle);
        this.heapLabel = new Label("", labelStyle);
        this.resolutionLabel = new Label("", labelStyle);
        this.totalProvincesLabel = new Label("", labelStyle);
        this.threadCountLabel = new Label("", labelStyle);

        this.totalProvincesLabel.setText("Number of provinces : " + totalProvinces);

        Label[] allLabels = {this.fpsLabel, this.mousePositionLabel, this.heapLabel, this.memoryLabel, this.deltaLabel, this.resolutionLabel, this.totalProvincesLabel, this.threadCountLabel};
        for (Label lbl : allLabels) {
            this.add(lbl).left().pad(2f);
            this.row();
        }
    }

    public void update(float deltaTime) {
        int fps = Gdx.graphics.getFramesPerSecond();
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        this.fpsLabel.setText(String.format("FPS : %d", fps));
        this.mousePositionLabel.setText(String.format("Mouse : %d, %d", mouseX, mouseY));
        this.deltaLabel.setText(String.format("Delta : %.3f s", deltaTime));
        this.resolutionLabel.setText(String.format("Resolution : %dx%d", screenWidth, screenHeight));

        this.timeSinceLastStats += deltaTime;
        if (this.timeSinceLastStats >= MEMORY_UPDATE_INTERVAL) {
            this.timeSinceLastStats = 0;

            Runtime runtime = Runtime.getRuntime();
            long totalHeapMo = runtime.totalMemory()  / (1024 * 1024);
            long freeHeapMo = runtime.freeMemory()   / (1024 * 1024);
            long usedHeapMo = totalHeapMo - freeHeapMo;
            int  threads = Thread.activeCount();

            this.heapLabel.setText(String.format("Heap : %d Mo / %d Mo", usedHeapMo, totalHeapMo));
            this.memoryLabel.setText(String.format("Memory used : %d Mo", usedHeapMo));
            this.threadCountLabel.setText(String.format("Active threads : %d", threads));
        }
    }
}
