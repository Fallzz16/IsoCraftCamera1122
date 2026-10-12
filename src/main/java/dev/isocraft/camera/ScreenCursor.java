package dev.isocraft.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

/** A virtual Zomboid-style cursor: the OS pointer remains grabbed inside the game. */
public final class ScreenCursor extends MouseHelper {
    private float x, y;
    private boolean initialized;
    private int dragX, dragY;

    public int getX(Minecraft mc) {
        initialize(mc);
        return Math.max(0, Math.min(mc.displayWidth - 1, (int) x));
    }

    public int getY(Minecraft mc) {
        initialize(mc);
        return Math.max(0, Math.min(mc.displayHeight - 1, (int) y));
    }

    private void initialize(Minecraft mc) {
        if (!initialized) {
            x = mc.displayWidth * 0.5f;
            y = mc.displayHeight * 0.5f;
            initialized = true;
        }
        x = Math.max(0, Math.min(Math.max(0, mc.displayWidth - 1), x));
        y = Math.max(0, Math.min(Math.max(0, mc.displayHeight - 1), y));
    }

    public int[] consumeDrag() {
        int[] result = {dragX, dragY};
        dragX = 0;
        dragY = 0;
        return result;
    }

    @Override public void mouseXYChange() {
        Minecraft mc = Minecraft.getMinecraft();
        int dx = Mouse.getDX();
        int dy = Mouse.getDY();
        if (mc.currentScreen == null && mc.inGameHasFocus && mc.world != null) {
            if (CameraEvents.draggingCamera()) {
                dragX += CameraBehavior.clampDrag(dx);
                dragY += CameraBehavior.clampDrag(dy);
                deltaX = 0;
                deltaY = 0;
                return;
            }
            if (!IsoCraftCamera.settings().cursorEnabled) {
                deltaX = dx;
                deltaY = dy;
                return;
            }
            initialize(mc);
            x += dx;
            y += dy;
            initialize(mc);
            deltaX = 0;  // Mouse no longer rotates the player while the cursor is enabled.
            deltaY = 0;
        } else {
            deltaX = dx;
            deltaY = dy;
        }
    }
}
