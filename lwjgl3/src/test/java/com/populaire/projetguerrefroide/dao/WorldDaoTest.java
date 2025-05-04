package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.GdxBaseTest;
import com.populaire.projetguerrefroide.entity.GameEntities;
import com.populaire.projetguerrefroide.map.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WorldDaoTest extends GdxBaseTest {
    private final WorldDao worldDao;

    public WorldDaoTest() {
        this.worldDao = new WorldDao();
    }

    @Test
    public void testCreateGameEntities() {
        GameEntities gameEntities = this.worldDao.createGameEntities();
        assertNotNull(gameEntities);
    }

    @Test
    public void testCreateWorldThreadSafe() {
        GameEntities gameEntities = this.worldDao.createGameEntities();
        World world = this.worldDao.createWorldThreadSafe(gameEntities, this.gameContext);
        assertNotNull(world);
    }
}
