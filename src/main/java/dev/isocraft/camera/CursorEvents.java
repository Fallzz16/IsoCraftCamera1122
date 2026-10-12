package dev.isocraft.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Draw the virtual cursor and replace vanilla crosshair raycast while enabled. */
public final class CursorEvents {
    private final SelectionRaycaster raycaster = new SelectionRaycaster();
    private final ScreenCursor virtualMouse = new ScreenCursor();
    private net.minecraft.util.MouseHelper previousMouse;
    private RayTraceResult selected;

    public void sync(Minecraft mc, boolean active) {
        boolean desired = active; // X-drag works even with the free cursor toggled off
        if (desired && previousMouse == null) {
            previousMouse = mc.mouseHelper;
            mc.mouseHelper = virtualMouse;
            selected = null;

        } else if (!desired && previousMouse != null) {
            mc.mouseHelper = previousMouse;
            previousMouse = null;
            selected = null;

        }
    }

    public void reset() { selected = null; }

    public RayTraceResult getSelected() { return selected; }

    public int[] consumeCameraDrag() { return virtualMouse.consumeDrag(); }

    public ScreenCursor getVirtualMouse() { return virtualMouse; }

    public void beforeClick(Minecraft mc) {
        if (enabled(mc)) apply(mc);
    }

    private boolean enabled(Minecraft mc) {
        return previousMouse != null && mc.world != null && mc.player != null
                && mc.currentScreen == null && IsoCraftCamera.settings().enabled
                && IsoCraftCamera.settings().cursorEnabled;
    }

    private void apply(Minecraft mc) {
        selected = raycaster.pick(mc, virtualMouse);
        if (selected != null) {
            mc.objectMouseOver = selected;
            mc.pointedEntity = selected.typeOfHit == RayTraceResult.Type.ENTITY ? selected.entityHit : null;
        } else {
            mc.objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS,
                    mc.player.getPositionEyes(1f), net.minecraft.util.EnumFacing.UP,
                    mc.player.getPosition());
            mc.pointedEntity = null;
        }
    }

    @SubscribeEvent public void worldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled(mc)) return;
        apply(mc);
        if (selected != null && selected.typeOfHit == RayTraceResult.Type.BLOCK && selected.getBlockPos() != null) {
            // Extra selection outline to match the custom ray, not vanilla's center crosshair.
            AxisAlignedBB bb = mc.world.getBlockState(selected.getBlockPos())
                    .getSelectedBoundingBox(mc.world, selected.getBlockPos()).grow(0.002)
                    .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY,
                            -mc.getRenderManager().viewerPosZ);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            RenderGlobal.drawSelectionBoundingBox(bb, 0.38f, 0.9f, 0.66f, 0.6f);
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    @SubscribeEvent public void noCenterCrosshair(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS && enabled(Minecraft.getMinecraft()))
            event.setCanceled(true);
    }

    @SubscribeEvent public void drawCursor(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !enabled(mc)) return;
        ScaledResolution scaled = event.getResolution();
        int x = virtualMouse.getX(mc) * scaled.getScaledWidth() / Math.max(1, mc.displayWidth);
        int y = (mc.displayHeight - virtualMouse.getY(mc)) * scaled.getScaledHeight() / Math.max(1, mc.displayHeight);
        boolean target = selected != null;
        int tone = !target ? 0xffdddddd : selected.typeOfHit == RayTraceResult.Type.ENTITY ? 0xffffc96a : 0xff8affbf;
        // Five-part reticle, black outline for contrast on terrain / leaves.
        Gui.drawRect(x - 1, y - 1, x + 2, y + 2, 0xff11151a);
        Gui.drawRect(x, y, x + 1, y + 1, tone);
        Gui.drawRect(x - 7, y - 1, x - 3, y + 1, 0xff11151a);
        Gui.drawRect(x + 4, y - 1, x + 8, y + 1, 0xff11151a);
        Gui.drawRect(x - 1, y - 7, x + 1, y - 3, 0xff11151a);
        Gui.drawRect(x - 1, y + 4, x + 1, y + 8, 0xff11151a);
        Gui.drawRect(x - 6, y, x - 4, y + 1, tone);
        Gui.drawRect(x + 5, y, x + 7, y + 1, tone);
        Gui.drawRect(x, y - 6, x + 1, y - 4, tone);
        Gui.drawRect(x, y + 5, x + 1, y + 7, tone);
    }
}
