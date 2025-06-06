package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.GdxBaseTest;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.entity.GameEntities;
import com.populaire.projetguerrefroide.map.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WorldDaoTest extends GdxBaseTest {
    private final WorldDaoImpl worldDao;

    public WorldDaoTest() {
        this.worldDao = new WorldDaoImpl("1946.1.1");
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
