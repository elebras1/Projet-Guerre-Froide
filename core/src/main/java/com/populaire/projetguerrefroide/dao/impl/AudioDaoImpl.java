package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dao.AudioDao;
import com.populaire.projetguerrefroide.dao.json.SongJson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class AudioDaoImpl implements AudioDao {
    private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());
    private final String songsJsonFile = "music/songs.json";
    private final Type songsType = new TypeDefinition<Map<String, SongJson>>() {}.type;

    @Override
    @SuppressWarnings("unchecked")
    public List<String> readSongs() {
        try {
            byte[] bytes = Gdx.files.internal(this.songsJsonFile).readBytes();
            Map<String, SongJson> songs = (Map<String, SongJson>) this.dslJson.deserialize(this.songsType, bytes, bytes.length);
            List<String> files = new ObjectList<>(songs.size());
            for (SongJson song : songs.values()) {
                files.add(song.file());
            }
            return files;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
