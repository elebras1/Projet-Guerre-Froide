package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GL31;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.util.MeshMultiDrawIndirect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class GdxBaseTest {
    private MockedConstruction<MeshMultiDrawIndirect> mockedMeshMultiDrawIndirect;
    protected AssetManager assetManager;
    protected GameContext gameContext;

    @BeforeAll
    public void beforeAll() {
        GdxNativesLoader.load();
        Gdx.files = new Lwjgl3Files();
        Gdx.gl = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);
        Gdx.gl30 = Mockito.mock(GL30.class);
        Gdx.gl31 = Mockito.mock(GL31.class);
        Gdx.gl32 = Mockito.mock(GL32.class);
        Gdx.graphics = Mockito.mock(Graphics.class);
        Application mockApp = Mockito.mock(Application.class);
        Gdx.app = mockApp;
        Mockito.doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(mockApp).postRunnable(any(Runnable.class));
        mockedMeshMultiDrawIndirect = mockConstruction(MeshMultiDrawIndirect.class);

        this.assetManager = new AssetManager();
        this.assetManager.load("ui/ui_skin.json", Skin.class);
        this.assetManager.load("ui/fonts/fonts_skin.json", Skin.class);
        this.assetManager.finishLoading();

        this.gameContext = new GameContext(this.assetManager);
        this.gameContext.putAllLocalisation(this.gameContext.getLocalisationDao().readCountriesCsv());
        this.gameContext.putAllLocalisation(this.gameContext.getLocalisationDao().readPoliticsCsv());
        this.gameContext.putAllLocalisation(this.gameContext.getLocalisationDao().readMainMenuInGameCsv());
        this.gameContext.putAllLocalisation(this.gameContext.getLocalisationDao().readPopupCsv());
        this.gameContext.putAllLocalisation(this.gameContext.getLocalisationDao().readProvincesCsv());
        this.gameContext.putAllLocalisation(this.gameContext.getLocalisationDao().readRegionsCsv());
        this.gameContext.putAllLocalisation(this.gameContext.getLocalisationDao().readLanguageCsv());
        this.gameContext.putAllLocalisation(this.gameContext.getLocalisationDao().readInterfaceCsv());
    }

    @AfterAll
    public void afterAll() {
        if (this.assetManager != null) {
            this.assetManager.dispose();
            this.assetManager = null;
        }
        this.mockedMeshMultiDrawIndirect.close();
        Gdx.app.exit();
    }
}
