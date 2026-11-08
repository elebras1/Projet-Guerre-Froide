package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiScrollbarsSkin;

import java.util.Map;

public class UiBuilder {
    private static final int FRAME_CORNER = 8;
    private final UiContext context;
    private final Skin fontSkin;
    private final Skin scrollbarSkin;

    public UiBuilder(UiContext context) {
        this.context = context;
        this.fontSkin = context.getSkinFonts();
        this.scrollbarSkin = context.getAssetManager().get(Skins.UI_SCROLLBARS);
    }

    public UiContext context() {
        return this.context;
    }

    public Skin skin(String skin) {
        return this.context.getAssetManager().get(skin);
    }

    public Label.LabelStyle font(String font) {
        return this.fontSkin.get(font, Label.LabelStyle.class);
    }

    public String localise(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        Map<String, String> localisation = this.context.getLocalisation();
        return localisation.getOrDefault(key, key).replace("\\n", "\n");
    }

    public Image image(Group parent, Skin skin, String drawable, float x, float y, UiAnchor anchor) {
        return this.add(parent, new Image(skin.getDrawable(drawable)), x, y, anchor);
    }

    public Image ninePatch(Group parent, Skin skin, String region, int corner, float x, float y, float width, float height, UiAnchor anchor) {
        Image image = new Image(new NinePatchDrawable(new NinePatch(skin.getRegion(region), corner, corner, corner, corner)));
        image.setSize(width, height);
        return this.add(parent, image, x, y, anchor);
    }

    public Button button(Group parent, Skin skin, String style, float x, float y, UiAnchor anchor) {
        return this.add(parent, new Button(skin, style), x, y, anchor);
    }

    public Button flatButton(Group parent, Skin skin, String drawable, float x, float y, UiAnchor anchor) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = skin.getDrawable(drawable);
        return this.add(parent, new Button(style), x, y, anchor);
    }

    public FlagImage flag(Group parent, Skin skin, String mask, String overlay, float x, float y, UiAnchor anchor) {
        return this.add(parent, new FlagImage(skin.getRegion(overlay), skin.getRegion(mask)), x, y, anchor);
    }

    public Label buttonLabel(Button button, String key, String font) {
        Label label = new Label(this.localise(key), this.font(font));
        label.setAlignment(Align.center);
        button.add(label).expand().fill().center();
        return label;
    }

    public Label label(Group parent, String key, String font, float x, float y, float width, float height, int align, UiAnchor anchor) {
        return this.label(parent, key, font, x, y, width, height, align, anchor, 0f, 0f);
    }

    public Label label(Group parent, String key, String font, float x, float y, float width, float height, int align, UiAnchor anchor, float borderWidth, float borderHeight) {
        Label label = new Label(this.localise(key), this.font(font));
        label.setTouchable(Touchable.disabled);
        label.setAlignment(align);
        label.setSize(width > 0f ? width : label.getPrefWidth(), height > 0f ? height : label.getPrefHeight());
        BitmapFont bitmapFont = label.getStyle().font;
        float lineTopShift = -(bitmapFont.getAscent() + bitmapFont.getDescent());
        return this.add(parent, label, x + borderWidth, y + borderHeight - lineTopShift, anchor);
    }

    public TextField textField(Group parent, Skin skin, String background, String key, String font, float x, float y, float width, float height, UiAnchor anchor, float borderWidth, float borderHeight) {
        Label.LabelStyle labelStyle = this.font(font);
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = labelStyle.font;
        style.fontColor = labelStyle.fontColor != null ? labelStyle.fontColor : Color.WHITE;
        SpriteDrawable cursor = new SpriteDrawable((SpriteDrawable) this.context.getColorTexturePool().get(Color.rgba8888(Color.WHITE)));
        cursor.setMinWidth(1f);
        style.cursor = cursor;
        style.selection = this.context.getColorTexturePool().get(Color.rgba8888(0.3f, 0.5f, 0.8f, 0.6f));
        if (skin != null) {
            NinePatchDrawable frame = new NinePatchDrawable(new NinePatch(skin.getRegion(background), FRAME_CORNER, FRAME_CORNER, FRAME_CORNER, FRAME_CORNER));
            frame.setLeftWidth(borderWidth);
            frame.setRightWidth(borderWidth);
            frame.setTopHeight(borderHeight);
            frame.setBottomHeight(borderHeight);
            style.background = frame;
        }
        TextField textField = new TextField(this.localise(key), style);
        textField.setSize(width, height);
        return this.add(parent, textField, x, y, anchor);
    }

    public Slider slider(Group parent, float x, float y, float width, float height, UiAnchor anchor, float min, float max, float step, float start) {
        Slider slider = new Slider(min, max, step, false, this.scrollbarSkin, UiScrollbarsSkin.Styles.Slider.DEFAULT_HORIZONTAL);
        slider.setValue(start);
        slider.setSize(width, height);
        return this.add(parent, slider, x, y, anchor);
    }

    public OverlappingBox overlappingBox(Group parent, float x, float y, float width, float height, float spacing, boolean alignRight, UiAnchor anchor) {
        return this.add(parent, new OverlappingBox(width, height, spacing, alignRight), x, y, anchor);
    }

    public <T extends UiWindow> UiList<T> list(Group parent, float x, float y, float width, float height, UiAnchor anchor, float spacing, float borderWidth, float borderHeight, UiList.RowFactory<T> rows) {
        return this.add(parent, new UiList<>(this, this.scrollbarSkin, UiScrollbarsSkin.Styles.Scrollpane.DEFAULT, width, height, spacing, borderWidth, borderHeight, rows), x, y, anchor);
    }

    public void onClick(Actor actor, Runnable action) {
        actor.addListener(new UiClick(action));
    }

    public void onChange(Actor actor, Runnable action) {
        actor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
    }

    public void shortcut(UiWindow window, Actor owner, int keycode, Runnable action) {
        window.shortcut(owner, keycode, action);
    }

    public void shortcut(UiWindow window, Actor owner, int keycode) {
        window.shortcut(owner, keycode, () -> {
            for (EventListener listener : owner.getListeners()) {
                if (listener instanceof UiClick click) {
                    click.action.run();
                }
            }
        });
    }

    public <T extends Actor> T add(Group parent, T actor, float x, float y, UiAnchor anchor) {
        parent.addActor(actor);
        if (parent instanceof UiWindow window) {
            window.track(actor, x, y, anchor);
        }
        this.position(actor, x, y, anchor);
        return actor;
    }

    private static final class UiClick extends ClickListener {
        private final Runnable action;

        private UiClick(Runnable action) {
            this.action = action;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            this.action.run();
        }
    }

    void position(Actor actor, float x, float y, UiAnchor anchor) {
        Group parent = actor.getParent();
        float parentWidth = parent.getWidth();
        float parentHeight = parent.getHeight();
        float left = switch (anchor) {
            case UPPER_LEFT, LOWER_LEFT -> x;
            case UPPER_RIGHT, LOWER_RIGHT -> parentWidth + x;
            case CENTER, CENTER_UP, CENTER_DOWN -> parentWidth / 2f + x;
        };
        float top = switch (anchor) {
            case UPPER_LEFT, UPPER_RIGHT, CENTER_UP -> y;
            case LOWER_LEFT, LOWER_RIGHT, CENTER_DOWN -> parentHeight + y;
            case CENTER -> parentHeight / 2f + y;
        };
        actor.setPosition(left, parentHeight - top - actor.getHeight());
    }
}
