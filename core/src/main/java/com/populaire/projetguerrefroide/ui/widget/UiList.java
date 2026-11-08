package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.ds.ObjectList;

public class UiList<T extends UiWindow> extends HoverScrollPane {
    public interface RowFactory<T extends UiWindow> {
        T create(UiBuilder ui);
    }

    private final UiBuilder ui;
    private final RowFactory<T> rowFactory;
    private final Table content;
    private final ObjectList<T> rows;
    private final float spacing;
    private int rowCount;
    private UiWindow header;

    public UiList(UiBuilder ui, Skin skin, String style, float width, float height, float spacing, float borderWidth, float borderHeight, RowFactory<T> rowFactory) {
        super(new Table(), skin, style);
        this.ui = ui;
        this.rowFactory = rowFactory;
        this.spacing = spacing;
        this.rows = new ObjectList<>();
        this.content = (Table) this.getActor();
        this.content.top().left();
        this.content.pad(borderHeight, borderWidth, borderHeight, borderWidth);
        float scrollbarWidth = this.getStyle().vScrollKnob != null ? this.getStyle().vScrollKnob.getMinWidth() : 0f;
        this.setSize(width + scrollbarWidth, height);
        this.setScrollingDisabled(true, false);
        this.setOverscroll(false, false);
    }

    public void setHeader(UiWindow header) {
        this.header = header;
        this.rebuild();
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public void setRowCount(int count) {
        while (this.rows.size() < count) {
            this.rows.add(this.rowFactory.create(this.ui));
        }
        if (count == this.rowCount) {
            return;
        }
        this.rowCount = count;
        this.rebuild();
    }

    public T row(int index) {
        return this.rows.get(index);
    }

    private void rebuild() {
        this.content.clearChildren();
        if (this.header != null) {
            this.content.add(this.header).align(Align.left).padBottom(this.spacing).row();
        }
        for (int i = 0; i < this.rowCount; i++) {
            this.content.add(this.rows.get(i)).align(Align.left).padBottom(this.spacing).row();
        }
        this.layout();
    }
}
