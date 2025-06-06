package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.entity.Bookmark;

public interface ConfigurationDao {
    Bookmark loadBookmark();
    Settings loadSettings();
    void saveSettings(Settings settings);
}
