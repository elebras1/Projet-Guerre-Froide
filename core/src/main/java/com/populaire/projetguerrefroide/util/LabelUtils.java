package com.populaire.projetguerrefroide.util;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class LabelUtils {
    private static final GlyphLayout layout = new GlyphLayout();

    public static void truncateLabel(Label label, float maxWidth) {
        BitmapFont font = label.getStyle().font;
        String text = label.getText().toString();

        layout.setText(font, text);
        if (layout.width <= maxWidth) {
            return;
        }

        String ellipsis = "...";
        layout.setText(font, ellipsis);
        float ellipsisWidth = layout.width;

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(text.charAt(i));
            layout.setText(font, result + ellipsis);
            if (layout.width + ellipsisWidth > maxWidth) {
                result.setLength(result.length() - 1);
                break;
            }
        }

        label.setText(result + ellipsis);
    }
}
