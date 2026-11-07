package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ChatMessage extends UiWindow {
    private final Label chatMemberText;

    public ChatMessage(UiBuilder ui) {
        super(ui, 512f, 22f);
        this.chatMemberText = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 1f, 1f, 612f, 22f, Align.topLeft, UiAnchor.UPPER_LEFT, 10f, 1f);
    }

    public void setText(String text) {
        this.chatMemberText.setText(text);
    }
}
