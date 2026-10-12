package dev.isocraft.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Camera rig with no ASM coremod and no changes to player/entity rotation. */
public final class CameraEvents {
    private static final String CATEGORY = "key.categories.isocraftcamera";
    private static KeyBinding toggle, zoomIn, zoomOut, left, right, projection, foliage, cursorToggle, lookAround;
    private final CursorEvents cursor;
    public CameraEvents(CursorEvents cursor) { this.cursor = cursor; }
    private int previousPerspective = -1;
    private boolean dragPendingSave;
    private static volatile float effectiveDistance = 22.0f;
    public static float effectiveDistance() { return effectiveDistance; }

    public static void registerKeys() {
        toggle = register("key.isocraftcamera.toggle", Keyboard.KEY_F8);
        zoomIn = register("key.isocraftcamera.zoom_in", Keyboard.KEY_F7);
        zoomOut = register("key.isocraftcamera.zoom_out", Keyboard.KEY_F6);
        left = register("key.isocraftcamera.rotate_left", Keyboard.KEY_J);
        right = register("key.isocraftcamera.rotate_right", Keyboard.KEY_L);
        projection = register("key.isocraftcamera.projection", Keyboard.KEY_K);
        foliage = register("key.isocraftcamera.foliage", Keyboard.KEY_H);
        cursorToggle = register("key.isocraftcamera.cursor", Keyboard.KEY_V);
        lookAround = register("key.isocraftcamera.look", Keyboard.KEY_X);
    }

    private static KeyBinding register(String key, int keyboardCode) {
        KeyBinding binding = new KeyBinding(key, keyboardCode, CATEGORY);
        ClientRegistry.registerKeyBinding(binding);
        return binding;
    }

    public static boolean draggingCamera() {
        return lookAround != null && lookAround.isKeyDown();
    }

    private static boolean active(Minecraft mc) {
        return mc.player != null && mc.world != null && IsoCraftCamera.settings() != null
                && IsoCraftCamera.settings().enabled;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            cursor.beforeClick(Minecraft.getMinecraft());
            return;
        }
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        CameraSettings cfg = IsoCraftCamera.settings();
        if (cfg == null) return;
        FoliageRenderer.install(mc);
        FoliageRenderer renderer = mc.getBlockRendererDispatcher() instanceof FoliageRenderer
                ? (FoliageRenderer) mc.getBlockRendererDispatcher() : null;
        if (mc.player == null || mc.world == null) {
            // When leaving a world, restore the vanilla perspective if possible.
            if (previousPerspective != -1) {
                mc.gameSettings.thirdPersonView = previousPerspective;
                previousPerspective = -1;
            }
            cursor.sync(mc, false);
            if (renderer != null) renderer.update(mc, false, cfg.leavesRadius);
            return;
        }
        // Do not trigger camera shortcuts while editing text or using menus.
        if (mc.currentScreen != null) return;
        while (toggle.isPressed()) {
            cfg.enabled = !cfg.enabled;
            if (!cfg.enabled) restore(mc);
            dragPendingSave = false;
            cfg.save();
            status(mc, cfg.enabled ? "Camera isometrica: ligada" : "Camera isometrica: desligada");
        }
        if (!active(mc)) {
            cursor.sync(mc, false);
            if (renderer != null) renderer.update(mc, false, cfg.leavesRadius);
            return;
        }

        if (previousPerspective == -1) previousPerspective = mc.gameSettings.thirdPersonView;
        // Force third-person to display the player; restore original state on F8.
        mc.gameSettings.thirdPersonView = 1;

        boolean changed = false;
        while (foliage.isPressed()) {
            cfg.hideLeaves = !cfg.hideLeaves;
            status(mc, "Folhas ocultas: " + (cfg.hideLeaves ? "ligadas" : "desligadas"));
            changed = true;
        }
        while (cursorToggle.isPressed()) {
            cfg.cursorEnabled = !cfg.cursorEnabled;
            status(mc, "Cursor livre: " + (cfg.cursorEnabled ? "ligado" : "desligado"));
            changed = true;
        }
        cursor.sync(mc, true);
        if (renderer != null) renderer.update(mc, cfg.hideLeaves, cfg.leavesRadius);
        // No collision-based zoom. X-drag rotates only the rig, without changing
        // player yaw or altering movement directions.
        int[] drag = cursor.consumeCameraDrag();
        if (drag[0] != 0 || drag[1] != 0) {
            cfg.yaw = CameraBehavior.dragYaw(cfg.yaw, drag[0]);
            cfg.pitch = CameraBehavior.dragPitch(cfg.pitch, drag[1]);
            changed = true;
        }
        while (zoomIn.isPressed()) {
            cfg.viewHeight = IsometricMath.zoom(cfg.viewHeight, 0.85f);
            cfg.distance = IsometricMath.clamp(cfg.distance * 0.85f,
                    IsometricMath.MIN_DISTANCE, IsometricMath.MAX_DISTANCE);
            changed = true;
        }
        while (zoomOut.isPressed()) {
            cfg.viewHeight = IsometricMath.zoom(cfg.viewHeight, 1.0f / 0.85f);
            cfg.distance = IsometricMath.clamp(cfg.distance / 0.85f,
                    IsometricMath.MIN_DISTANCE, IsometricMath.MAX_DISTANCE);
            changed = true;
        }
        while (left.isPressed()) { cfg.yaw = IsometricMath.wrappedYaw(cfg.yaw - 90.0f); changed = true; }
        while (right.isPressed()) { cfg.yaw = IsometricMath.wrappedYaw(cfg.yaw + 90.0f); changed = true; }
        while (projection.isPressed()) {
            cfg.orthographic = !cfg.orthographic;
            changed = true;
            status(mc, cfg.orthographic ? "Projecao: ortografica" : "Projecao: perspectiva");
        }
        // Avoid writing JSON twenty times per second while X is held.
        if (changed) {
            if (draggingCamera()) dragPendingSave = true;
            else cfg.save();
        }
        if (!draggingCamera() && dragPendingSave) {
            dragPendingSave = false;
            cfg.save();
        }
    }

    private void restore(Minecraft mc) {
        if (previousPerspective != -1) {
            mc.gameSettings.thirdPersonView = previousPerspective;
            previousPerspective = -1;
        }
    }

    private static void status(Minecraft mc, String message) {
        mc.player.sendStatusMessage(new TextComponentString("[IsoCraft] " + message), true);
    }

    @SubscribeEvent
    public void onCamera(EntityViewRenderEvent.CameraSetup event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!active(mc) || event.getEntity() != mc.getRenderViewEntity()) return;
        CameraSettings cfg = IsoCraftCamera.settings();
        Entity cameraEntity = event.getEntity();
        if (cameraEntity == null) return;

        // Fixed camera distance: previous wall-ray clamp made the rig bounce when
        // blocks entered/leaved the ray. Orthographic scale depends on viewHeight,
        // not distance; a shorter offset also reduces vanilla 1.12 culling artifacts.
        float distance = CameraBehavior.renderedDistance(cfg.distance, cfg.orthographic);

        effectiveDistance = distance;
        // Forge posts CameraSetup *inside* orientCamera, after vanilla setupCameraTransform
        // has established its projection and the third-person modelview transforms.
        // Replacing the two matrices here ensures a deterministic camera independent
        // from the player's aiming direction, without touching player rotation.
        if (cfg.orthographic) {
            float[] e = IsometricMath.halfExtents(cfg.viewHeight, mc.displayWidth, mc.displayHeight);
            float far = Math.max(512.0f, mc.gameSettings.renderDistanceChunks * 32.0f + distance + 64.0f);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(-e[0], e[0], -e[1], e[1], 0.05, far);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0f, 0.0f, -distance);

        // Vanilla applies these angles AFTER posting the Forge CameraSetup event.
        event.setYaw(180.0f + cfg.yaw);
        event.setPitch(cfg.pitch);
        event.setRoll(0.0f);
    }
}
