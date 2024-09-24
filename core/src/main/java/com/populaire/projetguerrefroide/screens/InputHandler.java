package com.populaire.projetguerrefroide.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.populaire.projetguerrefroide.map.LandProvince;
import com.populaire.projetguerrefroide.map.World;

import java.util.List;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class InputHandler<T extends Screen> implements InputProcessor {
    final OrthographicCamera cam;
    final World world;
    private float delta = 0;
    final T screen;
    private final int edgeSize = 50;

    public InputHandler(OrthographicCamera cam, World world, T screen) {

        this.cam = cam;
        this.world = world;
        this.screen = screen;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    public LandProvince getProvinceHover(int screenX, int screenY) {
        Vector3 worldPosition = new Vector3(screenX, screenY, 0);
        this.cam.unproject(worldPosition);
        short x = (short) Math.round(worldPosition.x);
        short y = (short) Math.round(worldPosition.y);
        return this.world.getProvinceByPixel(x, y);
    }

    public void handleInput(List<Table> uiTables) {
        float speed = 1000f * this.delta;
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        for(Table table : uiTables) {
            Vector2 tablePos = table.localToStageCoordinates(new Vector2(0, 0));
            Vector2 tableDim = new Vector2(table.getWidth(), table.getHeight());
            Vector2 tableOver = new Vector2(screenX, screenHeight - screenY);
            if (tablePos.x < tableOver.x && tableOver.x < tablePos.x + tableDim.x && tablePos.y < tableOver.y
                    && tableOver.y < tablePos.y + tableDim.y && table.isVisible()) {
                return;
            }
        }

        if (screenX < this.edgeSize || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            this.cam.translate(-speed * this.cam.zoom, 0, 0);
        } else if (screenX > screenWidth - this.edgeSize || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            this.cam.translate(speed * this.cam.zoom, 0, 0);
        }

        if (screenY < this.edgeSize || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            this.cam.translate(0, speed * this.cam.zoom, 0);
        } else if (screenY > screenHeight - this.edgeSize || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            this.cam.translate(0, -speed * this.cam.zoom, 0);
        }

        this.cam.position.y = MathUtils.clamp(this.cam.position.y, -50, WORLD_HEIGHT + 50);
        this.cam.zoom = MathUtils.clamp(this.cam.zoom, 0f, WORLD_WIDTH / this.cam.viewportWidth);

        LandProvince provinceHover = this.getProvinceHover(screenX, screenY);

        if (provinceHover != null) {
            ((NewGameScreen) this.screen).updateHoverBox(provinceHover);
        } else {
            ((NewGameScreen) this.screen).hideHoverBox();
        }
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
            Vector3 worldCoordinates = new Vector3(screenX, screenY, 0);
            this.cam.unproject(worldCoordinates);
            short x = (short) Math.round(worldCoordinates.x);
            short y = (short) Math.round(worldCoordinates.y);
            this.world.selectProvince(x, y);

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
        LandProvince provinceHover = this.getProvinceHover(screenX, screenY);

        if (provinceHover != null) {
            ((NewGameScreen) this.screen).updateHoverBox(provinceHover);
        } else {
            ((NewGameScreen) this.screen).hideHoverBox();
        }

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        LandProvince provinceHover = this.getProvinceHover(screenX, screenY);

        if (provinceHover != null) {
            ((NewGameScreen) this.screen).updateHoverBox(provinceHover);
        } else {
            ((NewGameScreen) this.screen).hideHoverBox();
        }

        return true;
    }


    @Override
    public boolean scrolled(float amountX, float amountY) {
        float speed = 0.25f;
        float zoom = amountY * speed;
        float speed2 = 0.05f;
        float zoom2 = amountY * speed2;

        if((this.cam.zoom + zoom2 > 0f) && (this.cam.zoom < 1f)) {
            this.cam.zoom += zoom2;
        }
        else if ((this.cam.zoom + zoom) <= WORLD_WIDTH / this.cam.viewportWidth && (this.cam.zoom + zoom > 0f)) {
            this.cam.zoom += zoom;
        }

        LandProvince provinceHover = this.getProvinceHover(Gdx.input.getX(), Gdx.input.getY());

        if (provinceHover != null) {
            ((NewGameScreen) this.screen).updateHoverBox(provinceHover);
        } else {
            ((NewGameScreen) this.screen).hideHoverBox();
        }

        return true;
    }

}
