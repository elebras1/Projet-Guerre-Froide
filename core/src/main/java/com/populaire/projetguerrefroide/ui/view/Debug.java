package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.monstrous.gdx.webgpu.graphics.g2d.WgBitmapFont;

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
    private final StringBuilder text = new StringBuilder(64);

    private float timeSinceLastStats;

    public Debug(int totalProvinces) {
        Label.LabelStyle labelStyle = new Label.LabelStyle(new WgBitmapFont(), Color.GREEN);

        this.fpsLabel = new Label("", labelStyle);
        this.mousePositionLabel = new Label("", labelStyle);
        this.memoryLabel = new Label("", labelStyle);
        this.deltaLabel = new Label("", labelStyle);
        this.heapLabel = new Label("", labelStyle);
        this.resolutionLabel = new Label("", labelStyle);
        this.totalProvincesLabel = new Label("", labelStyle);
        this.threadCountLabel = new Label("", labelStyle);

        this.text.setLength(0);
        this.text.append("Number of provinces : ").append(totalProvinces);
        this.totalProvincesLabel.setText(this.text.toString());

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

        this.text.setLength(0);
        this.fpsLabel.setText(this.text.append("FPS : ").append(fps).toString());

        this.text.setLength(0);
        this.mousePositionLabel.setText(this.text.append("Mouse : ").append(mouseX).append(", ").append(mouseY).toString());

        this.text.setLength(0);
        float roundedDelta = Math.round(deltaTime * 1000f) / 1000f;
        this.text.setLength(0);
        this.deltaLabel.setText(this.text.append("Delta : ").append(roundedDelta).append(" s").toString());

        this.text.setLength(0);
        this.resolutionLabel.setText(this.text.append("Resolution : ").append(screenWidth).append("x").append(screenHeight).toString());

        this.timeSinceLastStats += deltaTime;
        if (this.timeSinceLastStats >= MEMORY_UPDATE_INTERVAL) {
            this.timeSinceLastStats = 0;

            Runtime runtime = Runtime.getRuntime();
            long totalHeapMo = runtime.totalMemory() / (1024 * 1024);
            long freeHeapMo = runtime.freeMemory() / (1024 * 1024);
            long usedHeapMo = totalHeapMo - freeHeapMo;
            int threads = Thread.activeCount();

            this.text.setLength(0);
            this.heapLabel.setText(this.text.append("Heap : ").append(usedHeapMo).append(" Mo / ").append(totalHeapMo).append(" Mo").toString());

            this.text.setLength(0);
            this.memoryLabel.setText(this.text.append("Memory used : ").append(usedHeapMo).append(" Mo").toString());

            this.text.setLength(0);
            this.threadCountLabel.setText(this.text.append("Active threads : ").append(threads).toString());
        }
    }
}
