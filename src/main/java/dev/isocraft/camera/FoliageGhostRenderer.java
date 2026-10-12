package dev.isocraft.camera;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.nio.ByteOrder;
import net.minecraft.block.BlockLeaves;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Renders the leaves omitted from the chunk mesh with their original textured
 * model at a configurable alpha. No world/block states are changed.
 * RenderWorldLastEvent runs on the render thread; the chunk mask is immutable.
 */
public final class FoliageGhostRenderer {
    private static final int MAX_GHOST_LEAVES = 384;
    private boolean failureReported;
    private World cachedWorld;
    private LeafCircle cachedCircle;
    private long lastScanTick = Long.MIN_VALUE;
    private List<BlockPos> visibleGhosts = new ArrayList<BlockPos>();

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        CameraSettings settings = IsoCraftCamera.settings();
        if (settings == null || !settings.enabled || !settings.hideLeaves
                || mc.world == null || mc.player == null || mc.renderGlobal == null) return;
        if (failureReported || !FoliageRenderer.available(mc)) return;
        try {
            renderGhostLeaves(mc, settings);
        } catch (Exception exception) {
            failureReported = true;
            IsoCraftCamera.LOGGER.error("Ghost foliage rendering failed; transparent pass disabled", exception);
            // Keep access to terrain even if a custom leaf model is incompatible.
            settings.hideLeaves = false;
            FoliageRenderer.restoreFullLeaves(mc);
        }
    }

    private void renderGhostLeaves(Minecraft mc, CameraSettings settings) {
        int cx = MathHelper.floor(mc.player.posX);
        int cz = MathHelper.floor(mc.player.posZ);
        int cy = MathHelper.floor(mc.player.posY);
        LeafCircle circle = new LeafCircle(cx, cz, cy - 1, cy + 12, settings.leavesRadius);
        float alpha = IsometricMath.clamp(settings.leafOpacity, 0.04f, 0.50f);
        refreshPositions(mc, circle);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.entityRenderer.enableLightmap();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.depthMask(false);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            buffer.setTranslation(-mc.getRenderManager().viewerPosX,
                    -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
            int alphaByte = Math.max(1, Math.min(255, Math.round(alpha * 255.0f)));
            for (BlockPos pos : visibleGhosts) {
                if (!mc.world.isBlockLoaded(pos)) continue;
                IBlockState state = mc.world.getBlockState(pos);
                if (!(state.getBlock() instanceof BlockLeaves)) continue;
                IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
                int start = buffer.getVertexCount();
                mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModelFlat(
                        mc.world, model, state, pos, buffer, true, MathHelper.getPositionRandom(pos));
                setAlpha(buffer, start, buffer.getVertexCount(), alphaByte);
            }
            buffer.setTranslation(0, 0, 0);
            tessellator.draw();
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
            mc.entityRenderer.disableLightmap();
        }
    }

    /** Refresh candidate positions at most once per five world ticks, unless the
     *  player changed leaf-circle cells. Each frame only renders the cached list. */
    private void refreshPositions(Minecraft mc, LeafCircle circle) {
        long tick = mc.world.getTotalWorldTime();
        if (mc.world == cachedWorld && circle.equals(cachedCircle)
                && tick >= lastScanTick && tick - lastScanTick < 5L) return;
        cachedWorld = mc.world;
        cachedCircle = circle;
        lastScanTick = tick;
        List<BlockPos> found = new ArrayList<BlockPos>();
        for (int y = circle.minY; y <= circle.maxY && found.size() < MAX_GHOST_LEAVES; y++) {
            if (y < 0 || y > 255) continue;
            for (int x = circle.lowX(); x <= circle.highX() && found.size() < MAX_GHOST_LEAVES; x++) {
                for (int z = circle.lowZ(); z <= circle.highZ() && found.size() < MAX_GHOST_LEAVES; z++) {
                    if (!circle.contains(x, y, z)) continue;
                    BlockPos pos = new BlockPos(x, y, z);
                    if (mc.world.isBlockLoaded(pos)
                            && mc.world.getBlockState(pos).getBlock() instanceof BlockLeaves) found.add(pos);
                }
            }
        }
        visibleGhosts = found;
    }

    /** Preserve the original foliage RGB/tint/light; change only the alpha byte. */
    private static void setAlpha(BufferBuilder buffer, int startVertex, int endVertex, int alpha) {
        ByteBuffer bytes = buffer.getByteBuffer();
        int stride = buffer.getVertexFormat().getIntegerSize() * Integer.BYTES;
        int color = buffer.getVertexFormat().getColorOffset();
        int alphaOffset = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 3 : 0;
        for (int vertex = startVertex; vertex < endVertex; vertex++) {
            bytes.put(vertex * stride + color + alphaOffset, (byte) alpha);
        }
    }
}
