package dev.isocraft.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Cursor picking, recalculated on every interaction tick; never reads GL state. */
public final class SelectionRaycaster {
    public RayTraceResult pick(Minecraft mc, ScreenCursor cursor) {
        CameraSettings settings = IsoCraftCamera.settings();
        if (settings == null || mc.world == null || mc.player == null) return null;
        Vec3d eyes = mc.player.getPositionEyes(1.0f);
        ScreenRayMath.Ray ray = ScreenRayMath.ray(
                new double[] {eyes.x, eyes.y, eyes.z}, settings.yaw, settings.pitch,
                CameraEvents.effectiveDistance(), settings.viewHeight,
                mc.gameSettings.fovSetting, settings.orthographic,
                mc.displayWidth, mc.displayHeight, cursor.getX(mc), cursor.getY(mc));
        Vec3d from = new Vec3d(ray.origin[0], ray.origin[1], ray.origin[2]);
        Vec3d direction = new Vec3d(ray.direction[0], ray.direction[1], ray.direction[2]);
        Vec3d to = from.add(direction.scale(128.0));
        RayTraceResult block = tracePastHiddenLeaves(mc.world, from, to);
        if (block != null && block.typeOfHit != RayTraceResult.Type.BLOCK) block = null;
        double reach = mc.playerController.getBlockReachDistance();
        if (block != null && block.typeOfHit == RayTraceResult.Type.BLOCK
                && (block.hitVec == null || eyes.squareDistanceTo(block.hitVec) > reach * reach + 0.01
                || obstructed(mc.world, eyes, block.hitVec, block.getBlockPos()))) block = null;

        double entityReach = mc.playerController.extendedReach() ? 6.0 : 3.0;
        Entity best = null;
        Vec3d bestHit = null;
        double distance = Double.POSITIVE_INFINITY;
        AxisAlignedBB nearby = mc.player.getEntityBoundingBox().grow(entityReach + 1.0);
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, nearby)) {
            if (!entity.canBeCollidedWith()) continue;
            AxisAlignedBB bb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            RayTraceResult intersection = bb.calculateIntercept(from, to);
            if (intersection == null || intersection.hitVec == null) continue;
            double eyeSq = eyes.squareDistanceTo(intersection.hitVec);
            double cameraSq = from.squareDistanceTo(intersection.hitVec);
            if (eyeSq > entityReach * entityReach || cameraSq >= distance) continue;
            if (block != null && from.squareDistanceTo(block.hitVec) < cameraSq) continue;
            if (obstructed(mc.world, eyes, intersection.hitVec, null)) continue;
            distance = cameraSq;
            best = entity;
            bestHit = intersection.hitVec;
        }
        return best != null ? new RayTraceResult(best, bestHit) : block;
    }

    public static RayTraceResult tracePastHiddenLeaves(World world, Vec3d start, Vec3d end) {
        Vec3d direction = end.subtract(start).normalize();
        Vec3d cursor = start;
        for (int i = 0; i < 64; ++i) {
            RayTraceResult hit = world.rayTraceBlocks(cursor, end, false, true, false);
            if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || hit.hitVec == null) return hit;
            BlockPos pos = hit.getBlockPos();
            if (pos == null || !FoliageRenderer.hidden(world.getBlockState(pos), pos)) return hit;
            AxisAlignedBB bounds = new AxisAlignedBB(pos);
            Vec3d after = hit.hitVec.add(direction.scale(0.015));
            for (int j = 0; j < 100 && bounds.contains(after); ++j) after = after.add(direction.scale(0.04));
            if (after.squareDistanceTo(cursor) < 0.00001 || after.squareDistanceTo(start) > end.squareDistanceTo(start)) return null;
            cursor = after;
        }
        return null;
    }

    private static boolean obstructed(World world, Vec3d eye, Vec3d target, BlockPos targetPos) {
        if (eye.squareDistanceTo(target) < 0.0001) return false;
        Vec3d destination = target.add(target.subtract(eye).normalize().scale(0.06));
        RayTraceResult sight = tracePastHiddenLeaves(world, eye, destination);
        return sight != null && sight.typeOfHit == RayTraceResult.Type.BLOCK
                && (targetPos == null || !targetPos.equals(sight.getBlockPos()));
    }
}
