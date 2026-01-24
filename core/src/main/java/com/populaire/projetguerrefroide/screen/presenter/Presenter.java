package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

public interface Presenter extends Disposable {
    void initialize(Stage stage);
    void refresh();
}
