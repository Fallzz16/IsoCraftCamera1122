package dev.isocraft.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Client-side visual yaw only. Never changes rotationYaw (movement/network aim). */
public final class PlayerFacingCursor {
    private final CursorEvents cursor;
    private float displayedYaw;
    private boolean hasYaw;
    private boolean stored;
    private float oldBody, oldPrevBody, oldHead, oldPrevHead;

    public PlayerFacingCursor(CursorEvents cursor) { this.cursor = cursor; }

    @SubscribeEvent
    public void renderPre(RenderPlayerEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        CameraSettings cfg = IsoCraftCamera.settings();
        EntityPlayer player = event.getEntityPlayer();
        if (cfg == null || !cfg.enabled || !cfg.cursorEnabled || !cfg.faceCursor
                || mc.player == null || player != mc.player || mc.world == null
                || mc.currentScreen != null || CameraEvents.draggingCamera()) {
            hasYaw = false;
            return;
        }
        Vec3d point = target(mc, cfg);
        if (point == null) return;
        float wanted = CameraBehavior.faceTarget(player.posX, player.posZ, point.x, point.z,
                hasYaw ? displayedYaw : player.renderYawOffset);
        if (!hasYaw) { displayedYaw = wanted; hasYaw = true; }
        else displayedYaw = CameraBehavior.approachYaw(displayedYaw, wanted, 16.0f);
        // This scope is intentionally just a single local-player render.
        oldBody = player.renderYawOffset;
        oldPrevBody = player.prevRenderYawOffset;
        oldHead = player.rotationYawHead;
        oldPrevHead = player.prevRotationYawHead;
        stored = true;
        player.renderYawOffset = displayedYaw;
        player.prevRenderYawOffset = displayedYaw;
        player.rotationYawHead = displayedYaw;
        player.prevRotationYawHead = displayedYaw;
    }

    @SubscribeEvent
    public void renderPost(RenderPlayerEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!stored || mc.player == null || event.getEntityPlayer() != mc.player) return;
        EntityPlayer p = event.getEntityPlayer();
        p.renderYawOffset = oldBody;
        p.prevRenderYawOffset = oldPrevBody;
        p.rotationYawHead = oldHead;
        p.prevRotationYawHead = oldPrevHead;
        stored = false;
    }

    private Vec3d target(Minecraft mc, CameraSettings cfg) {
        RayTraceResult selected = cursor.getSelected();
        if (selected != null && selected.hitVec != null) return selected.hitVec;
        Vec3d eyes = mc.player.getPositionEyes(1.0f);
        ScreenRayMath.Ray ray = ScreenRayMath.ray(new double[] {eyes.x, eyes.y, eyes.z},
                cfg.yaw, cfg.pitch, CameraEvents.effectiveDistance(), cfg.viewHeight,
                mc.gameSettings.fovSetting, cfg.orthographic, mc.displayWidth, mc.displayHeight,
                cursor.getVirtualMouse().getX(mc), cursor.getVirtualMouse().getY(mc));
        double dy = ray.direction[1];
        if (Math.abs(dy) < 0.001) return null;
        double t = (mc.player.posY + 0.65 - ray.origin[1]) / dy;
        if (t < 0.0 || t > 256.0) return null;
        return new Vec3d(ray.origin[0] + t * ray.direction[0],
                ray.origin[1] + t * ray.direction[1], ray.origin[2] + t * ray.direction[2]);
    }
}
