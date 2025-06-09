package com.populaire.projetguerrefroide.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.populaire.projetguerrefroide.screen.GameInputListener;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class GameInputHandler implements InputProcessor {
    private final OrthographicCamera cam;
    private final GameInputListener gameInputListener;
    private float delta;
    private final Vector2 posVelocity;
    private float zoomChange;
    private Vector3 worldCoordinates;
    private Vector2 inputDirection;
    private Vector2 cameraVelocity;
    private Vector3 preZoomCoordinates;
    private Vector3 postZoomCoordinates;
    private static final float ZOOM_SPEED_DECAY = 6f;
    private static final float ZOOM_FACTOR = 3f;
    private static final float VELOCITY_DECAY = 8f;
    private static final float MAX_VELOCITY = 800f;
    private static final float ACCELERATION = 6400f;

    public GameInputHandler(OrthographicCamera cam, GameInputListener gameInputListener) {
        this.cam = cam;
        this.gameInputListener = gameInputListener;
        this.delta = 0f;
        this.posVelocity = new Vector2(0, 0);
        this.zoomChange = 0f;
        this.worldCoordinates = new Vector3(0, 0, 0);
        this.inputDirection = new Vector2(0, 0);
        this.cameraVelocity = new Vector2(0, 0);
        this.preZoomCoordinates = new Vector3(0, 0, 0);
        this.postZoomCoordinates = new Vector3(0, 0, 0);
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    private int getWorldPositions(int screenX, int screenY) {
        this.worldCoordinates.set(screenX, screenY, 0);
        this.cam.unproject(this.worldCoordinates);
        short x = (short) Math.round(this.worldCoordinates.x);
        short y = (short) Math.round(this.worldCoordinates.y);
        return (x << 16) | (y & 0xFFFF);
    }

    public void handleInput() {
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        this.inputDirection.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            this.inputDirection.x -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            this.inputDirection.x += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            this.inputDirection.y += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            this.inputDirection.y -= 1;
        }

        float edgeRatio = 0.02f;
        if (screenX < screenWidth * edgeRatio) {
            this.inputDirection.x -= 1;
        } else if (screenX > screenWidth * (1 - edgeRatio)) {
            this.inputDirection.x += 1;
        }
        if (screenY < screenHeight * edgeRatio) {
            this.inputDirection.y += 1;
        } else if (screenY > screenHeight * (1 - edgeRatio)) {
            this.inputDirection.y -= 1;
        }

        if (this.inputDirection.len() > 0) {
            this.inputDirection.nor().scl(ACCELERATION * this.delta);
            this.posVelocity.add(this.inputDirection);

            if (this.posVelocity.len() > MAX_VELOCITY) {
                this.posVelocity.nor().scl(MAX_VELOCITY);
            }
        } else {
            float decayFactor = (float) Math.exp(-this.delta * VELOCITY_DECAY);
            this.posVelocity.scl(decayFactor);

            if (this.posVelocity.len() < 0.1f) {
                this.posVelocity.set(0, 0);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            this.gameInputListener.onEscape();
        }

        int position = this.getWorldPositions(screenX, screenY);
        this.gameInputListener.onHover((short) (position >> 16), (short) (position & 0xFFFF));
    }

    public void updateCamera() {
        if (this.zoomChange != 0f) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.input.getY();

            this.preZoomCoordinates.set(mouseX, mouseY, 0);
            this.cam.unproject(this.preZoomCoordinates);
            float worldXBefore = this.preZoomCoordinates.x;
            float worldYBefore = this.preZoomCoordinates.y;

            float zoomDiff = (this.zoomChange * this.delta) * this.cam.zoom;
            this.cam.zoom = MathUtils.clamp(this.cam.zoom + zoomDiff, 0.05f, WORLD_WIDTH / this.cam.viewportWidth
            );
            this.cam.update();

            this.postZoomCoordinates.set(mouseX, mouseY, 0);
            this.cam.unproject(this.postZoomCoordinates);
            float worldXAfter = this.postZoomCoordinates.x;
            float worldYAfter = this.postZoomCoordinates.y;

            this.cam.position.x += (worldXBefore - worldXAfter);
            this.cam.position.y += (worldYBefore - worldYAfter);

            this.zoomChange *= (float) Math.exp(-this.delta * ZOOM_SPEED_DECAY);
            if (Math.abs(this.zoomChange) < 0.01f) {
                this.zoomChange = 0f;
            }
        }

        this.cam.zoom = MathUtils.clamp(this.cam.zoom, 0.05f, WORLD_WIDTH / this.cam.viewportWidth);

        this.cameraVelocity.set(this.posVelocity).scl(this.cam.zoom * this.delta);
        this.cam.position.x += this.cameraVelocity.x;
        this.cam.position.y += this.cameraVelocity.y;

        this.cam.position.y = MathUtils.clamp(this.cam.position.y, -50, WORLD_HEIGHT + 50);
        this.cam.update();
    }

    @Override
    public boolean keyDown (int keycode) {
        return false;
    }

    @Override
    public boolean keyUp (int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped (char character) {
        return false;
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button)  {
        return false;
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            int position = this.getWorldPositions(screenX, screenY);
            this.gameInputListener.onClick((short) (position >> 16), (short) (position & 0xFFFF));
            return true;
        }

        return false;
    }

    @Override
    public boolean touchCancelled (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        int position = this.getWorldPositions(screenX, screenY);
        this.gameInputListener.onHover((short) (position >> 16), (short) (position & 0xFFFF));
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        int position = this.getWorldPositions(screenX, screenY);
        this.gameInputListener.onHover((short) (position >> 16), (short) (position & 0xFFFF));
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        this.zoomChange += (amountY / 5f) * ZOOM_FACTOR;
        return true;
    }


    public void moveCamera(short x, short y) {
        this.cam.position.x = x;
        this.cam.position.y = y;
    }

    public void zoomIn() {
        if(this.cam.zoom - 0.15f > 0f)
            this.cam.zoom -= 0.15f;
    }

    public void zoomOut() {
        if((this.cam.zoom + 0.15f) <= WORLD_WIDTH / this.cam.viewportWidth)
            this.cam.zoom += 0.15f;
    }
}
