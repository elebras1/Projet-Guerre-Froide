package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.TestGdxFiles;
import com.populaire.projetguerrefroide.dao.impl.LocalisationDaoImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalisationDaoImplTest {

    @BeforeAll
    public void setUp() {
        TestGdxFiles.install();
    }

    @Test
    public void readsOrganisationLocalisation() {
        LocalisationDao localisationDao = new LocalisationDaoImpl();

        localisationDao.setLanguage("ENGLISH");
        Map<String, String> english = localisationDao.readOrganisations();
        assertEquals("Organisations", english.get("ORGANISATIONS"));
        assertEquals("United Nations", english.get("UN"));
        assertEquals("UN", english.get("UN_short"));
        assertEquals("NATO", english.get("NATO_short"));
        assertEquals("Warsaw Pact", english.get("WP"));
        assertEquals("Commonwealth of Nations", english.get("COMMONWEALTH"));
        assertEquals("French Union", english.get("FRENCH_UNION"));
        assertEquals("League of Arab States", english.get("ARAB_LEAGUE"));
        assertEquals("Organization of American States", english.get("OAS"));

        localisationDao.setLanguage("FRENCH");
        Map<String, String> french = localisationDao.readOrganisations();
        assertEquals("Organisations", french.get("ORGANISATIONS"));
        assertEquals("Nations unies", french.get("UN"));
        assertEquals("ONU", french.get("UN_short"));
        assertEquals("OTAN", french.get("NATO_short"));
        assertEquals("Pacte de Varsovie", french.get("WP"));
        assertEquals("Commonwealth des nations", french.get("COMMONWEALTH"));
        assertEquals("Union française", french.get("FRENCH_UNION"));
        assertEquals("Ligue des États arabes", french.get("ARAB_LEAGUE"));
        assertEquals("Organisation des États américains", french.get("OAS"));
    }
}
