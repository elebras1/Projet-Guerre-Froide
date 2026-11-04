package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.math.MathUtils;
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
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FlagsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiPopupSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class Popup extends Table {
    private final PopupListener listener;
    private FlagImage flagLeftImage;
    private FlagImage flagRightImage;
    private WidgetFactory widgetFactory;

    public Popup(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinFonts, Map<String, String> localisation, String title, String description, boolean doubleButton, boolean big, PopupListener listener) {
        this.listener = listener;
        this.widgetFactory = widgetFactory;
        this.setPopup(skin, skinFonts, localisation, title, description, doubleButton, big, this.getIdButton(doubleButton, big));
        this.flagLeftImage = createFlagImage(skinUi, skinFlags, FlagsSkin.Drawables.COMECON, null);
        this.flagLeftImage.setPosition(9, this.getHeight() - 75);
        this.flagRightImage = createFlagImage(skinUi, skinFlags, FlagsSkin.Drawables.NATO, null);
        this.flagRightImage.setPosition(this.getWidth() - 72, this.getHeight() - 75);
        this.setTouchable(Touchable.enabled);
        this.addDragListener();
    }

    public Popup(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinFlags, Skin skinFonts, Map<String, String> localisation, String title, String description, String countryNameId, String colonizerNameId, boolean doubleButton, boolean big, PopupListener listener) {
        this.listener = listener;
        this.widgetFactory = widgetFactory;
        this.setPopup(skin, skinFonts, localisation, title, description, doubleButton, big, this.getIdButton(doubleButton, big));
        this.flagLeftImage = createFlagImage(skinUi, skinFlags, countryNameId, colonizerNameId);
        this.flagLeftImage.setPosition(9, this.getHeight() - 75);
        this.flagRightImage = createFlagImage(skinUi, skinFlags, countryNameId, colonizerNameId);
        this.flagRightImage.setPosition(this.getWidth() - 72, this.getHeight() - 75);
        this.setTouchable(Touchable.enabled);
        this.addDragListener();
    }

    private String getIdButton(boolean doubleButton, boolean big) {
        if (doubleButton && big) {
            return UiPopupSkin.Drawables.POPUP_BG_BIG_DOUBLE_1;
        } else if (doubleButton) {
            return UiPopupSkin.Drawables.POPUP_BG_STANDARD_FLAG_DOUBLE;
        } else if (big) {
            return UiPopupSkin.Drawables.POPUP_BG_BIG_DOUBLE_3;
        }

        return UiPopupSkin.Drawables.POPUP_BG_STANDARD_FLAG;
    }

    private void setPopup(Skin skin, Skin skinFonts, Map<String, String> localisation, String title, String description, boolean doubleButton, boolean big, String backgroundName) {
        Drawable background = skin.getDrawable(backgroundName);
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

        Actor button = configureButton(skin, skinFonts, localisation, doubleButton, big, background);

        Label titleLabel = createLabel(localisation.get(title), skinFonts.get(FontsSkin.Styles.Label.JOCKEY_20_GLOW_BLUE, LabelStyle.class),
            background.getMinWidth() / 2, background.getMinHeight() - 42);
        Label descriptionLabel = createWrappedLabel(localisation.get(description), skinFonts.get(FontsSkin.Styles.Label.ARIAL_18, LabelStyle.class),
            background.getMinWidth(), background.getMinHeight());

        this.addActor(titleLabel);
        this.addActor(descriptionLabel);
        this.addActor(button);
    }

    private FlagImage createFlagImage(Skin skinUi, Skin skinFlags, String countryNameId, String colonizerNameId) {
        FlagImage flagImage = this.widgetFactory.createFlagImage(skinUi, UiSkin.Drawables.SHIELD_BIG, UiSkin.Drawables.SHIELD_BIG_OVERLAY);
        flagImage.setFlag(this.widgetFactory.getFlagTextureRegion(skinFlags, countryNameId, colonizerNameId));
        this.addActor(flagImage);

        return flagImage;
    }

    private Actor configureButton(Skin skin, Skin skinFonts, Map<String, String> localisation, boolean doubleButton, boolean big, Drawable background) {
        Actor button = doubleButton ? getDoubleButton(skin, skinFonts, localisation) : getSimpleButton(skin, skinFonts, localisation);

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


    private Actor getSimpleButton(Skin skin, Skin skinFonts, Map<String, String> localisation) {
        LabelStyle labelStyleJockey16GlowBlue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, LabelStyle.class);

        Button button = new Button(skin, UiPopupSkin.Styles.Button.POPUP_BTN);
        button.add(new Label(localisation.get("OK"), labelStyleJockey16GlowBlue)).padBottom(5);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onOkClicked();
            }
        });

        return button;
    }

    private Actor getDoubleButton(Skin skin, Skin skinFonts, Map<String, String> localisation) {
        LabelStyle labelStyleJockey16GlowRed = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_GLOW_RED, LabelStyle.class);
        LabelStyle labelStyleJockey16GlowBlue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, LabelStyle.class);

        Button cancelButton = new Button(skin, UiPopupSkin.Styles.Button.POPUP_RED_BTN);
        cancelButton.add(new Label(localisation.get("CANCEL"), labelStyleJockey16GlowRed)).padBottom(5);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onCancelClicked();
            }
        });

        Button okButton = new Button(skin, UiPopupSkin.Styles.Button.POPUP_BTN);
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
                float newX = MathUtils.clamp(getX() + deltaX, 0, getStage().getWidth() - getWidth());
                float newY = MathUtils.clamp(getY() + deltaY, 0, getStage().getHeight() - getHeight());
                setPosition(newX, newY);
            }
        });
    }
}
