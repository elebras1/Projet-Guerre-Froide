package com.populaire.projetguerrefroide.ui.view.menubar;

import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class InGameMenu extends UiWindow {
    private final InGameMenuPanel menuPanel;
    private final SaveGameWindow menuSave;
    private final MessageSettingsWindow menuMessageSettings;
    private UiWindow current;

    public InGameMenu(UiBuilder ui, InGameMenuListener listener) {
        super(ui, 512f, 512f);
        this.menuPanel = ui.add(this, new InGameMenuPanel(ui, this, listener), 46f, 24f, UiAnchor.UPPER_LEFT);
        this.menuPanel.setVisible(false);
        this.menuSave = ui.add(this, new SaveGameWindow(ui, this::showPanel, listener::onSaveClicked), 46f, 24f, UiAnchor.UPPER_LEFT);
        this.menuSave.setVisible(false);
        this.menuMessageSettings = ui.add(this, new MessageSettingsWindow(ui, this::showPanel), 0f, 0f, UiAnchor.UPPER_LEFT);
        this.menuMessageSettings.setVisible(false);
        this.menuPanel.setVisible(true);
        this.current = this.menuPanel;
    }

    public void showPanel() {
        this.show(this.menuPanel);
    }

    void showSave() {
        this.show(this.menuSave);
    }

    void showMessageSettings() {
        this.show(this.menuMessageSettings);
    }

    private void show(UiWindow window) {
        this.current.setVisible(false);
        window.setVisible(true);
        this.current = window;
    }
}
