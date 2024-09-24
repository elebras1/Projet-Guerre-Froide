package com.populaire.projetguerrefroide.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class MainMenuScreen implements Screen {
    private final Stage stage;
    private final Skin skin;

    public MainMenuScreen(Game game) {
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(this.stage);
        this.skin = new Skin(Gdx.files.internal("temp/ui/mainmenu/skin/mainmenuskin.json"));
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(this.skin.getDrawable("frontend_main_bg"));
        Table menuTable = new Table();
        menuTable.setBackground(this.skin.getDrawable("frontend_mainmenu_bg"));
        menuTable.padBottom(35);
        TextButton playButton = new TextButton("Play", this.skin, "frontend_button_big");
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LoadScreen(game));
                dispose();
            }
        });
        menuTable.add(playButton).expandY().bottom().spaceBottom(83);
        menuTable.row();
        TextButton optionsButton = new TextButton("Options", this.skin, "frontend_button_small");
        menuTable.add(optionsButton).spaceBottom(10);
        menuTable.row();
        TextButton creditButton = new TextButton("Credit", this.skin, "frontend_button_small");
        creditButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dispose();
            }
        });
        menuTable.add(creditButton).spaceBottom(10);
        menuTable.row();
        TextButton helpButton = new TextButton("Help", this.skin, "frontend_button_small");
        menuTable.add(helpButton).spaceBottom(58);
        menuTable.row();
        TextButton exitButton = new TextButton("Exit", this.skin, "frontend_button_exit");
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        menuTable.add(exitButton);
        rootTable.add(menuTable).expand().center();
        this.stage.addActor(rootTable);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        this.stage.act();
        this.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        this.stage.dispose();
        this.skin.dispose();
    }
}



