package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class Popup extends Table {
    private final PopupListener listener;
    private FlagImage flagLeftImage;
    private FlagImage flagRightImage;

    public Popup(Skin skin, Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation, String title, String description, boolean doubleButton, boolean big, PopupListener listener) {
        this.listener = listener;
        this.setPopup(skin, labelStylePool, localisation, title, description, doubleButton, big, this.getIdButton(doubleButton, big));
        this.flagLeftImage = createFlagImage(skinUi, skinFlags, "comecon");
        this.flagLeftImage.setPosition(9, this.getHeight() - 75);
        this.flagRightImage = createFlagImage(skinUi, skinFlags, "nato");
        this.flagRightImage.setPosition(this.getWidth() - 72, this.getHeight() - 75);
        this.setTouchable(Touchable.enabled);
        this.addDragListener();
    }

    public Popup(Skin skin, Skin skinUi, Skin skinFlags, LabelStylePool labelStylePool, Map<String, String> localisation, String title, String description, String idCountry, boolean doubleButton, boolean big, PopupListener listener) {
        this.listener = listener;
        this.setPopup(skin, labelStylePool, localisation, title, description, doubleButton, big, this.getIdButton(doubleButton, big));
        this.flagLeftImage = createFlagImage(skinUi, skinFlags, idCountry);
        this.flagLeftImage.setPosition(9, this.getHeight() - 75);
        this.flagRightImage = createFlagImage(skinUi, skinFlags, idCountry);
        this.flagRightImage.setPosition(this.getWidth() - 72, this.getHeight() - 75);
        this.setTouchable(Touchable.enabled);
        this.addDragListener();
    }

    private String getIdButton(boolean doubleButton, boolean big) {
        if (doubleButton && big) {
            return "popup_bg_big_double_1";
        } else if (doubleButton) {
            return "popup_bg_standard_flag_double";
        } else if (big) {
            return "popup_bg_big_double_3";
        }

        return "popup_bg_standard_flag";
    }

    private void setPopup(Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, String title, String description, boolean doubleButton, boolean big, String backgroundName) {
        Drawable background = skin.getDrawable(backgroundName);
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

        Actor button = configureButton(skin, labelStylePool, localisation, doubleButton, big, background);

        Label titleLabel = createLabel(localisation.get(title), labelStylePool.getLabelStyle("jockey_20_glow_blue"),
            background.getMinWidth() / 2, background.getMinHeight() - 42);
        Label descriptionLabel = createWrappedLabel(localisation.get(description), labelStylePool.getLabelStyle("arial_18"),
            background.getMinWidth(), background.getMinHeight());

        this.addActor(titleLabel);
        this.addActor(descriptionLabel);
        this.addActor(button);
    }

    private FlagImage createFlagImage(Skin skinUi, Skin skinFlags, String idCountry) {
        TextureRegion alphaFlag = skinUi.getRegion("shield_big");
        TextureRegion overlayFlag = skinUi.getRegion("shield_big_overlay");
        Pixmap defaultPixmapFlag = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        TextureRegionDrawable defaultFlag = new TextureRegionDrawable(new Texture(defaultPixmapFlag));
        defaultPixmapFlag.dispose();
        FlagImage flagImage = new FlagImage(defaultFlag, overlayFlag, alphaFlag);
        flagImage.setFlag(skinFlags.getRegion(idCountry));
        this.addActor(flagImage);

        return flagImage;
    }

    private Actor configureButton(Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation, boolean doubleButton, boolean big, Drawable background) {
        Actor button = doubleButton ? getDoubleButton(skin, labelStylePool, localisation) : getSimpleButton(skin, labelStylePool, localisation);

        float x = doubleButton ? background.getMinWidth() / 2 : 165;
        float y = doubleButton ? (big ? 35 : 33) : (big ? 15 : 13);

        button.setPosition(x, y);
        return button;
    }

    private Label createLabel(String text, LabelStyle style, float centerX, float posY) {
        Label label = new Label(text, style);
        label.setX(centerX - label.getWidth() / 2);
        label.setY(posY);
        return label;
    }

    private Label createWrappedLabel(String text, LabelStyle style, float width, float height) {
        Label label = new Label(text, style);
        label.setWrap(true);
        label.setAlignment(Align.topLeft);
        label.setSize(width - 50, height - 150);
        label.setPosition(25, 75);
        return label;
    }


    private Actor getSimpleButton(Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation) {
        LabelStyle labelStyleJockey16GlowBlue = labelStylePool.getLabelStyle("jockey_16_glow_blue");

        Button button = new Button(skin, "popup_btn");
        button.add(new Label(localisation.get("OK"), labelStyleJockey16GlowBlue)).padBottom(5);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onOkClicked();
            }
        });

        return button;
    }

    private Actor getDoubleButton(Skin skin, LabelStylePool labelStylePool, Map<String, String> localisation) {
        LabelStyle labelStyleJockey16GlowRed = labelStylePool.getLabelStyle("jockey_16_glow_red");
        LabelStyle labelStyleJockey16GlowBlue = labelStylePool.getLabelStyle("jockey_16_glow_blue");

        Button cancelButton = new Button(skin, "popup_btn_red");
        cancelButton.add(new Label(localisation.get("CANCEL"), labelStyleJockey16GlowRed)).padBottom(5);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onCancelClicked();
            }
        });

        Button okButton = new Button(skin, "popup_btn");
        okButton.add(new Label(localisation.get("OK"), labelStyleJockey16GlowBlue)).padBottom(5);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onOkClicked();
            }
        });

        Table table = new Table();
        table.add(cancelButton).padRight(3);
        table.add(okButton);

        return table;
    }

    private void addDragListener() {
        this.addListener(new DragListener() {
            private float startX, startY;

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                startX = x;
                startY = y;
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                float deltaX = x - startX;
                float deltaY = y - startY;
                setPosition(getX() + deltaX, getY() + deltaY);
            }
        });
    }

    public void dispose() {
        this.flagLeftImage.dispose();
        this.flagRightImage.dispose();
    }
}
