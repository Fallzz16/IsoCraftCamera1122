package dev.isocraft.camera;

import java.lang.reflect.Field;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * A renderer-only leaf mask. No world states, lighting, items, collisions or
 * server packets are changed. The snapshot is safe for chunk-builder threads.
 */
public final class FoliageRenderer extends BlockRendererDispatcher {
    private final BlockRendererDispatcher original;
    private static volatile LeafCircle mask;
    private static Field dispatcherField;
    private static boolean hookFailed;
    private World lastWorld;
    private LeafCircle previous;
    private int pendingTicks;

    private FoliageRenderer(BlockRendererDispatcher original, BlockColors colors) {
        super(original.getBlockModelShapes(), colors);
        this.original = original;
    }

    public static void install(Minecraft mc) {
        if (hookFailed || mc.getBlockRendererDispatcher() instanceof FoliageRenderer) return;
        try {
            if (dispatcherField == null) {
                for (Field f : Minecraft.class.getDeclaredFields()) {
                    if (f.getType() == BlockRendererDispatcher.class) {
                        dispatcherField = f;
                        dispatcherField.setAccessible(true);
                        break;
                    }
                }
            }
            if (dispatcherField == null) throw new NoSuchFieldException("BlockRendererDispatcher field");
            dispatcherField.set(mc, new FoliageRenderer(mc.getBlockRendererDispatcher(), mc.getBlockColors()));
            IsoCraftCamera.LOGGER.info("Visual-only foliage dispatcher installed");
        } catch (Exception ex) {
            hookFailed = true; // Avoid repeating reflection failures 20 times per second.
            IsoCraftCamera.LOGGER.error("Foliage masking disabled: renderer hook unavailable", ex);
        }
    }

    /** Recovers the original chunk meshes if a ghost pass fails. */
    public static boolean available(Minecraft mc) {
        return mc.getBlockRendererDispatcher() instanceof FoliageRenderer;
    }

    public static void restoreFullLeaves(Minecraft mc) {
        if (mc.getBlockRendererDispatcher() instanceof FoliageRenderer) {
            ((FoliageRenderer) mc.getBlockRendererDispatcher()).update(mc, false, 3.5f);
        }
    }

    public static boolean hidden(IBlockState state, BlockPos position) {
        LeafCircle snapshot = mask;
        return snapshot != null && state != null && state.getBlock() instanceof BlockLeaves
                && snapshot.contains(position.getX(), position.getY(), position.getZ());
    }

    @Override public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess access, BufferBuilder buffer) {
        return !hidden(state, pos) && original.renderBlock(state, pos, access, buffer);
    }

    @Override public BlockModelShapes getBlockModelShapes() { return original.getBlockModelShapes(); }
    @Override public BlockModelRenderer getBlockModelRenderer() { return original.getBlockModelRenderer(); }
    @Override public IBakedModel getModelForState(IBlockState state) { return original.getModelForState(state); }
    @Override public void renderBlockBrightness(IBlockState state, float brightness) { original.renderBlockBrightness(state, brightness); }
    @Override public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture,
                                             IBlockAccess world) { original.renderBlockDamage(state, pos, texture, world); }
    @Override public void onResourceManagerReload(IResourceManager manager) { original.onResourceManagerReload(manager); }

    /** Rebuild only the former and current small regions; never rebuild the entire world. */
    public void update(Minecraft mc, boolean enabled, float radius) {
        World world = mc.world;
        LeafCircle next = null;
        if (enabled && mc.player != null && world != null) {
            BlockPos player = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
            next = new LeafCircle(player.getX(), player.getZ(), player.getY() - 1,
                    player.getY() + 12, radius);
        }
        if (world != lastWorld) {
            // Switching worlds discards the old snapshot and its chunks.
            lastWorld = world;
            previous = null;
            mask = null;
            pendingTicks = 0;
        }
        if ((previous == null && next == null) || (previous != null && previous.equals(next))) {
            pendingTicks = 0;
            return;
        }
        // Rebuilding both sides of the mask can starve chunk rebuild workers if
        // the player repeatedly crosses block boundaries. Coalesce updates.
        if (previous != null && next != null && ++pendingTicks < 3) return;
        pendingTicks = 0;
        LeafCircle before = previous;
        // Newly compiling chunks will read one atomic immutable snapshot.
        mask = next;
        previous = next;
        if (world == null) return;
        if (before != null && next != null) {
            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    Math.min(before.lowX(), next.lowX()), Math.min(before.minY, next.minY),
                    Math.min(before.lowZ(), next.lowZ()), Math.max(before.highX(), next.highX()),
                    Math.max(before.maxY, next.maxY), Math.max(before.highZ(), next.highZ()));
        } else {
            dirty(mc, before == null ? next : before);
        }
    }

    private static void dirty(Minecraft mc, LeafCircle region) {
        if (region == null) return;
        mc.renderGlobal.markBlockRangeForRenderUpdate(region.lowX(), region.minY, region.lowZ(),
                region.highX(), region.maxY, region.highZ());
    }
}
