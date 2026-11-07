package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.configuration.Bookmark;
import com.populaire.projetguerrefroide.configuration.TutorialCategory;

import java.util.List;

public interface ConfigurationDao {
    Bookmark loadBookmark();
    List<TutorialCategory> loadTutorials();
    Settings loadSettings();
    void saveSettings(Settings settings);
}
