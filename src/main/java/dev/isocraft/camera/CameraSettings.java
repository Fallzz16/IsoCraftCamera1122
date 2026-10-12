package dev.isocraft.camera;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/** Client-only settings. The 1.12.2 config is separate from the 1.21.x mod. */
public final class CameraSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public boolean enabled = false;
    public boolean orthographic = true;
    public float yaw = 45.0f;
    public float pitch = IsometricMath.DEFAULT_PITCH;
    public float viewHeight = IsometricMath.DEFAULT_VIEW_HEIGHT;
    public float distance = 18.0f;
    public boolean avoidWalls = false; // legacy flag; automatic collision zoom is disabled in v0.3
    public boolean hideLeaves = true;
    public float leavesRadius = 3.5f;
    public float leafOpacity = 0.12f; // 12% visible
    public boolean faceCursor = true;
    public boolean cursorEnabled = true;

    public void normalize() {
        yaw = IsometricMath.wrappedYaw(yaw);
        pitch = IsometricMath.clamp(pitch, -8.0f, 85.0f);
        viewHeight = IsometricMath.clamp(viewHeight,
                IsometricMath.MIN_VIEW_HEIGHT, IsometricMath.MAX_VIEW_HEIGHT);
        leavesRadius = IsometricMath.clamp(leavesRadius, 1.0f, 8.0f);
        leafOpacity = IsometricMath.clamp(leafOpacity, 0.04f, 0.50f);
        avoidWalls = false; // migration: ignore old true value to stop oscillating zoom
        distance = IsometricMath.clamp(distance,
                IsometricMath.MIN_DISTANCE, IsometricMath.MAX_DISTANCE);
    }

    public static CameraSettings load(File directory) {
        configFile = new File(directory, "isocraftcamera-1122.json");
        if (configFile.isFile()) {
            try (Reader input = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                CameraSettings result = GSON.fromJson(input, CameraSettings.class);
                if (result != null) { result.normalize(); return result; }
            } catch (Exception e) {
                IsoCraftCamera.LOGGER.warn("Configuracao invalida. Valores padrao utilizados.", e);
            }
        }
        return new CameraSettings();
    }

    public void save() {
        if (configFile == null) return;
        normalize();
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs())
                throw new IllegalStateException("Nao foi possivel criar: " + parent);
            try (Writer output = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                GSON.toJson(this, output);
            }
        } catch (Exception e) {
            IsoCraftCamera.LOGGER.error("Falha ao salvar configuracao", e);
        }
    }
}
