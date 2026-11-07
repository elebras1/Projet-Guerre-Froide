package com.populaire.projetguerrefroide.ui.view.lobby;

import com.populaire.projetguerrefroide.configuration.Bookmark;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import java.util.List;

public class LobbyBookmarks extends UiWindow {
    private final UiList<BookmarkEntry> bookmarksList;

    public LobbyBookmarks(UiBuilder ui) {
        super(ui, 280f, 768f);
        this.bookmarksList = ui.list(this, 4f, 45f, 270f, 268f, UiAnchor.UPPER_LEFT, 2f, 0f, 0f, BookmarkEntry::new);
    }

    public void setBookmarks(List<Bookmark> bookmarks) {
        this.bookmarksList.setRowCount(bookmarks.size());
        for (int i = 0; i < bookmarks.size(); i++) {
            this.bookmarksList.row(i).bind(bookmarks.get(i));
        }
    }
}
