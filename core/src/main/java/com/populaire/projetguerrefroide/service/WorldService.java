package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.data.DataManager;
import com.populaire.projetguerrefroide.map.World;

public class WorldService {
    private final DataManager dataManager;
    private World world;

    public WorldService() {
        this.dataManager = new DataManager();
    }

    public void createWorldAsync() {
        this.world = this.dataManager.createWorldAsync();
    }

    public World getWorld() {
        return this.world;
    }
}
