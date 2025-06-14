package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.GdxBaseTest;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.map.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WorldDaoTest extends GdxBaseTest {
    private final WorldDaoImpl worldDao;

    public WorldDaoTest() {
        this.worldDao = new WorldDaoImpl();
    }

    @Test
    public void testCreateWorldThreadSafe() {
        World world = this.worldDao.createWorldThreadSafe(this.gameContext);
        assertNotNull(world);
    }
}
