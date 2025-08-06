package com.populaire.projetguerrefroide.lwjgl3;

import com.monstrous.gdx.webgpu.backends.desktop.WgDesktopApplication;
import com.monstrous.gdx.webgpu.backends.desktop.WgDesktopApplicationConfiguration;
import com.populaire.projetguerrefroide.ProjetGuerreFroide;

/** Launches the desktop (WebGPU) application. */
public class Launcher {
    public static void main(String[] args) {
        createApplication();
    }

    private static WgDesktopApplication createApplication() {
        return new WgDesktopApplication(new ProjetGuerreFroide(), getDefaultConfiguration());
    }

    private static WgDesktopApplicationConfiguration getDefaultConfiguration() {
        WgDesktopApplicationConfiguration configuration = new WgDesktopApplicationConfiguration();
        configuration.setWindowedMode(1080, 720);
        configuration.setTitle("ProjetGuerreFroide");
        configuration.useVsync(true);
        //configuration.setFullscreenMode(WgDesktopApplicationConfiguration.getDisplayMode());
        configuration.setWindowIcon("logo32.png");
        return configuration;
    }
}
