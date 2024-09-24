package com.populaire.projetguerrefroide.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.populaire.projetguerrefroide.map.World;

public class NewGameInputHandler<T extends Screen> extends InputHandler<T> {
    public NewGameInputHandler(OrthographicCamera cam, World world, T screen) {
        super(cam, world, screen);
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            Vector3 worldCoordinates = new Vector3(screenX, screenY, 0);
            this.cam.unproject(worldCoordinates);
            short x = (short) Math.round(worldCoordinates.x);
            short y = (short) Math.round(worldCoordinates.y);
            this.world.selectProvince(x, y);
            ((NewGameScreen) this.screen).updateCountrySelected();

            return true;
        }

        return false;
    }

}
