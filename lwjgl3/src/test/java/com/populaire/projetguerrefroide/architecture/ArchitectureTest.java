package com.populaire.projetguerrefroide.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArchitectureTest {
    private static final Path SOURCES = Path.of("..", "core", "src", "main", "java", "com", "populaire", "projetguerrefroide");
    private static final String GAME_PACKAGE = "com.populaire.projetguerrefroide.";
    private static final List<String> RENDER_PACKAGES = List.of("screen", "ui", "renderer");
    private static final List<String> FORBIDDEN_IMPORTS = List.of(
        "io.github.elebras1.flecs",
        GAME_PACKAGE + "component.",
        GAME_PACKAGE + "system.",
        GAME_PACKAGE + "loader.",
        GAME_PACKAGE + "repository.",
        GAME_PACKAGE + "simulation.",
        GAME_PACKAGE + "service.EconomyService",
        GAME_PACKAGE + "dao.impl.WorldDaoImpl");
    private static final List<String> SIMULATION_FORBIDDEN_IMPORTS = List.of(
        "com.badlogic.gdx.scenes.scene2d",
        "com.badlogic.gdx.graphics.g2d",
        "com.monstrous.gdx.webgpu",
        GAME_PACKAGE + "screen.",
        GAME_PACKAGE + "ui.",
        GAME_PACKAGE + "renderer.");

    @Test
    public void renderPackagesDoNotTouchTheSimulation() throws IOException {
        List<String> violations = new ArrayList<>();
        for (String renderPackage : RENDER_PACKAGES) {
            this.collectViolations(SOURCES.resolve(renderPackage), FORBIDDEN_IMPORTS, violations);
        }
        assertTrue(violations.isEmpty(), "Render code imports simulation code: " + violations);
    }

    @Test
    public void protocolAndSimulationDoNotTouchTheRenderer() throws IOException {
        List<String> violations = new ArrayList<>();
        this.collectViolations(SOURCES.resolve("protocol"), SIMULATION_FORBIDDEN_IMPORTS, violations);
        this.collectViolations(SOURCES.resolve("protocol"), List.of("io.github.elebras1.flecs"), violations);
        this.collectViolations(SOURCES.resolve("simulation"), SIMULATION_FORBIDDEN_IMPORTS, violations);
        assertTrue(violations.isEmpty(), "Simulation or protocol code imports render code: " + violations);
    }

    private void collectViolations(Path directory, List<String> forbidden, List<String> violations) throws IOException {
        assertTrue(Files.isDirectory(directory), "Missing source directory " + directory.toAbsolutePath());
        try (Stream<Path> files = Files.walk(directory)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                for (String line : Files.readAllLines(file)) {
                    String trimmed = line.trim();
                    if (!trimmed.startsWith("import ")) {
                        continue;
                    }
                    for (String prefix : forbidden) {
                        if (trimmed.startsWith("import " + prefix) || trimmed.startsWith("import static " + prefix)) {
                            violations.add(SOURCES.relativize(file) + ": " + trimmed);
                        }
                    }
                }
            }
        }
    }
}
