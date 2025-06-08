package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.screen.MainMenuInGameListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.List;
import java.util.Map;

public class MainMenuInGame extends Table implements PopupListener {
    private final WidgetFactory widgetFactory;
    private final List<String> languages;
    private final List<String> framerates;
    private Settings settings;
    private final Skin skin;
    private final Skin skinUi;
    private final Skin skinScrollbars;
    private final LabelStylePool labelStylePool;
    private final Map<String, String> localisation;
    private final MainMenuInGameListener listener;

    public MainMenuInGame(WidgetFactory widgetFactory, Skin skin, Skin skinUi, Skin skinScrollbars, LabelStylePool labelStylePool, Map<String, String> localisation, MainMenuInGameListener listener) {
        this.widgetFactory = widgetFactory;
        this.languages = ObjectList.with("ENGLISH", "FRENCH", "GERMAN", "POLSKI", "SPANISH", "ITALIAN", "SWEDISH", "CZECH", "HUNGARIAN", "DUTCH", "PORTUGUESE", "RUSSIAN", "FINNISH", "CHINESE");
        this.framerates = ObjectList.with("30", "60", "120", "144", "240", "300", "360", "420", "480", "540", "600");
        this.skin = skin;
        this.skinUi = skinUi;
        this.skinScrollbars = skinScrollbars;
        this.labelStylePool = labelStylePool;
        this.localisation = localisation;
        this.listener = listener;
        this.setMenu();
    }

    private void setMenu() {
        this.widgetFactory.applyBackgroundToTable(skin, "menu_background", this);

        LabelStyle labelStyleJockey20GlowBlue = this.labelStylePool.getLabelStyle("jockey_20_glow_blue");
        LabelStyle labelStyleJockey16GlowRed = this.labelStylePool.getLabelStyle("jockey_16_glow_red");
        LabelStyle labelStyleJockey24GlowRed = this.labelStylePool.getLabelStyle("jockey_24_glow_red");

        Button savegameButton = this.widgetFactory.createButton(this.skin, "menu_button", 56, 360, this);
        savegameButton.add(new Label(this.localisation.get("SAVE_GAME"), labelStyleJockey20GlowBlue)).padBottom(5);
        savegameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button gameOptionsButton = this.widgetFactory.createButton(this.skin, "menu_button", 56, 318, this);
        gameOptionsButton.add(new Label(this.localisation.get("GAME_OPTIONS"), labelStyleJockey20GlowBlue)).padBottom(5);
        gameOptionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings = listener.onShowSettingsClicked();
                settingsGame();
            }
        });

        Button resignButton = this.widgetFactory.createButton(this.skin, "menu_button", 56, 276, this);
        resignButton.add(new Label(this.localisation.get("GAME_RESIGN"), labelStyleJockey20GlowBlue)).padBottom(5);
        resignButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button messageSettingsButton = this.widgetFactory.createButton(this.skin, "menu_button", 56, 234, this);
        messageSettingsButton.add(new Label(this.localisation.get("MESSAGE_SETTINGS"), labelStyleJockey20GlowBlue)).padBottom(5);
        messageSettingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        Button quitButton = this.widgetFactory.createButton(this.skin, "menu_button_quit", 56, 103, this);
        quitButton.add(new Label(this.localisation.get("QUIT"), labelStyleJockey24GlowRed)).padBottom(5);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onQuitClicked(MainMenuInGame.this);
            }
        });

        Button cancelButton = this.widgetFactory.createButton(this.skin, "menu_button_cancel", 108, 39, this);
        cancelButton.add(new Label(this.localisation.get("CLOSE"), labelStyleJockey16GlowRed)).padBottom(10);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onCloseClicked();
            }
        });
    }

    private void settingsGame() {
        this.setupSettings("ingame_settings_game_naked");

        LabelStyle labelStyleJockey14GlowBlue = this.labelStylePool.getLabelStyle("jockey_14_glow_blue");
        LabelStyle labelStyleJockey18Yellow = this.labelStylePool.getLabelStyle("jockey_18", "yellow");
        LabelStyle labelStyleJockey18Black = this.labelStylePool.getLabelStyle("jockey_18_black");

        Label valueLabel = new Label(this.localisation.get(settings.getLanguage()), labelStyleJockey18Yellow);

        ClickListener lessListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int currentIndex = languages.indexOf(settings.getLanguage());
                int nextIndex = (currentIndex - 1 + languages.size()) % languages.size();
                settings.setLanguage(languages.get(nextIndex));
                valueLabel.setText(localisation.get(settings.getLanguage()));
            }
        };

        ClickListener moreListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int currentIndex = languages.indexOf(settings.getLanguage());
                int nextIndex = (currentIndex + 1) % languages.size();
                settings.setLanguage(languages.get(nextIndex));
                valueLabel.setText(localisation.get(settings.getLanguage()));
            }
        };

        this.addStepperSettings(valueLabel, labelStyleJockey14GlowBlue, lessListener, moreListener, this.localisation.get("LANGUAGE"), 441);

        ClickListener checkListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.setDebugMode(!settings.isDebugMode());
            }
        };
        this.addCheckboxSettings(labelStyleJockey18Black, this.localisation.get("DEBUG_MODE"), checkListener, this.settings.isDebugMode(), 107);
    }

    private void setSettingsVideo() {
        this.setupSettings("ingame_settings_video_naked");

        LabelStyle labelStyleJockey14GlowBlue = this.labelStylePool.getLabelStyle("jockey_14_glow_blue");
        LabelStyle labelStyleJockey18Yellow = this.labelStylePool.getLabelStyle("jockey_18", "yellow");
        LabelStyle labelStyleJockey18Black = this.labelStylePool.getLabelStyle("jockey_18_black");

        Label valueLabel = new Label(String.valueOf(this.settings.getCapFrameRate()), labelStyleJockey18Yellow);

        ClickListener lessListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int currentIndex = framerates.indexOf(String.valueOf(settings.getCapFrameRate()));
                int nextIndex = (currentIndex - 1 + framerates.size()) % framerates.size();
                settings.setCapFrameRate(Short.parseShort(framerates.get(nextIndex)));
                valueLabel.setText(String.valueOf(settings.getCapFrameRate()));
            }
        };

        ClickListener moreListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int currentIndex = framerates.indexOf(String.valueOf(settings.getCapFrameRate()));
                int nextIndex = (currentIndex + 1) % framerates.size();
                settings.setCapFrameRate(Short.parseShort(framerates.get(nextIndex)));
                valueLabel.setText(String.valueOf(settings.getCapFrameRate()));
            }
        };

        this.addStepperSettings(valueLabel, labelStyleJockey14GlowBlue, lessListener, moreListener, this.localisation.get("FRAMERATE"), 473);

        ClickListener checkVsyncListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.setVsync(!settings.isVsync());
            }
        };

        this.addCheckboxSettings(labelStyleJockey18Black, "VSync", checkVsyncListener, this.settings.isVsync(), 171);

        ClickListener checkFullscreenListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.setFullscreen(!settings.isFullscreen());
            }
        };

        this.addCheckboxSettings(labelStyleJockey18Black, this.localisation.get("FULLSCREEN"), checkFullscreenListener, this.settings.isFullscreen(), 139);
    }

    private void setSettingsAudio() {
        this.setupSettings("ingame_settings_audio_naked");

        LabelStyle labelStyleJockey14GlowBlue = this.labelStylePool.getLabelStyle("jockey_14_glow_blue");

        ChangeListener masterVolumeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Slider slider = (Slider) actor;
                settings.setMasterVolume((short) slider.getValue());
            }
        };
        this.addSliderSettings(labelStyleJockey14GlowBlue, this.localisation.get("MASTER_VOLUME"), masterVolumeListener, this.settings.getMasterVolume(), 476);

        ChangeListener musicVolumeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Slider slider = (Slider) actor;
                settings.setMusicVolume((short) slider.getValue());
            }
        };
        this.addSliderSettings(labelStyleJockey14GlowBlue, this.localisation.get("MUSIC_VOLUME"), musicVolumeListener, this.settings.getMusicVolume(), 415);

        ChangeListener effectsVolumeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Slider slider = (Slider) actor;
                settings.setEffectsVolume((short) slider.getValue());
            }
        };
        this.addSliderSettings(labelStyleJockey14GlowBlue, this.localisation.get("EFFECT_VOLUME"), effectsVolumeListener, this.settings.getEffectsVolume(), 355);
    }

    private void setSettingsControls() {
        this.setupSettings("ingame_settings_controls_naked");
    }

    public void setupSettings(String backgroundName) {
        this.clearChildren();

        Drawable background = this.skin.getDrawable(backgroundName);
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());

        this.addTitle();
        this.addTopButtonSettings();
        this.addBottomButtonSettings();
    }

    private void addTitle() {
        LabelStyle labelStyleJockey24 = this.labelStylePool.getLabelStyle("jockey_24");
        Label title = new Label(this.localisation.get("SETTINGS"), labelStyleJockey24);

        title.setPosition(this.getWidth() / 2 - title.getWidth() / 2, 536);
        this.addActor(title);
    }

    private void addTopButtonSettings() {
        LabelStyle labelStyleArial11BoldBlack = this.labelStylePool.getLabelStyle("arial_11_bold_black");
        short yPosition = 504;

        Button gameButton = this.widgetFactory.createButton(this.skinUi, "settings_tab_btn_1", 47, yPosition, this);
        gameButton.add(new Label(this.localisation.get("GAME"), labelStyleArial11BoldBlack)).padBottom(3);
        gameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsGame();
            }
        });

        Button videoButton = this.widgetFactory.createButton(this.skinUi, "settings_tab_btn_1", 125, yPosition, this);;
        videoButton.add(new Label(this.localisation.get("VIDEO"), labelStyleArial11BoldBlack)).padBottom(3);
        videoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setSettingsVideo();
            }
        });

        Button audioButton = this.widgetFactory.createButton(this.skinUi, "settings_tab_btn_1", 203, yPosition, this);;
        audioButton.add(new Label(this.localisation.get("AUDIO"), labelStyleArial11BoldBlack)).padBottom(3);
        audioButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setSettingsAudio();
            }
        });

        Button controlsButton = this.widgetFactory.createButton(this.skinUi, "settings_tab_btn_1", 281, yPosition, this);;
        controlsButton.add(new Label(this.localisation.get("CONTROLS"), labelStyleArial11BoldBlack)).padBottom(3);
        controlsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setSettingsControls();
            }
        });
    }

    private void addBottomButtonSettings() {
        LabelStyle labelStyleJockey16GlowRed = this.labelStylePool.getLabelStyle("jockey_16_glow_red");
        LabelStyle labelStyleJockey16GlowBlue = this.labelStylePool.getLabelStyle("jockey_16_glow_blue");

        Button cancelButton = new Button(this.skin, "settings_cancel_btn");
        cancelButton.add(new Label(this.localisation.get("BACK"), labelStyleJockey16GlowRed)).padBottom(10);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearChildren();
                setMenu();
            }
        });

        Button okButton = new Button(this.skin, "settings_ok_btn");
        okButton.add(new Label(this.localisation.get("APPLY"), labelStyleJockey16GlowBlue)).padBottom(10);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onApplySettingsClicked(settings);
            }
        });

        Table table = new Table();
        table.add(cancelButton).padRight(3);
        table.add(okButton);
        table.setPosition(205, 47);

        this.addActor(table);
    }

    public void addStepperSettings(Label valueLabel, LabelStyle labelStyle, ClickListener lessListener, ClickListener moreListener, String textLabel, int y) {
        this.widgetFactory.createLabel(textLabel, labelStyle, 82, y, this);

        Button lessButton = this.widgetFactory.createButton(this.skinUi, "settings_less", 194, y - 7, this);
        lessButton.addListener(lessListener);

        valueLabel.setPosition(225, y - 4);
        valueLabel.setWidth(82);
        valueLabel.setAlignment(Align.center);
        this.addActor(valueLabel);

        Button moreButton = this.widgetFactory.createButton(this.skinUi, "settings_more", 304, y - 7, this);
        moreButton.addListener(moreListener);
    }

    public void addCheckboxSettings(LabelStyle labelStyle, String textLabel, ClickListener listener , boolean isChecked, int y) {
        this.widgetFactory.createLabel(textLabel, labelStyle, 75, y, this);

        Button checkButton = this.widgetFactory.createButton(this.skinUi, "checkbox", 302, y - 1, this);
        checkButton.setChecked(isChecked);
        checkButton.addListener(listener);
    }

    public void addSliderSettings(LabelStyle labelStyle, String textSlider, ChangeListener listener, int value, int y) {
        Label sliderLabel = new Label(textSlider, labelStyle);
        sliderLabel.setPosition(this.getWidth() / 2 - sliderLabel.getWidth() / 2, y);
        this.addActor(sliderLabel);

        Slider slider = new Slider(0, 100, 1, false, this.skinScrollbars, "default-horizontal");
        slider.setPosition(62, y - 35);
        slider.setWidth(284);
        slider.setValue(value);
        slider.addListener(listener);
        this.addActor(slider);
    }

    @Override
    public void onCancelClicked() {
        this.listener.onCancelPopupClicked();
    }

    @Override
    public void onOkClicked() {
        this.listener.onOkPopupClicked();
    }
}
