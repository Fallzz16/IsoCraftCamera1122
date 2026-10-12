package dev.isocraft.camera;

/** Pure-Java rules: deterministic, independent of Minecraft rendering. */
public final class CameraBehavior {
    public static final float DRAG_SENSITIVITY_YAW = 0.23f;
    public static final float DRAG_SENSITIVITY_PITCH = 0.18f;
    // The orthographic camera does not need to be far away to achieve a wide view.
    // Keeping it near the player reduces 1.12.2 third-person chunk culling mismatch.
    public static final float MAX_ORTHO_DISTANCE = 11.5f;
    private CameraBehavior() { }

    public static float renderedDistance(float configured, boolean orthographic) {
        float safe = IsometricMath.clamp(configured, IsometricMath.MIN_DISTANCE, IsometricMath.MAX_DISTANCE);
        return orthographic ? Math.min(safe, MAX_ORTHO_DISTANCE) : safe;
    }

    public static float dragYaw(float initial, int dx) {
        return IsometricMath.wrappedYaw(initial + clampDrag(dx) * DRAG_SENSITIVITY_YAW);
    }

    public static float dragPitch(float initial, int dy) {
        return IsometricMath.clamp(initial + clampDrag(dy) * DRAG_SENSITIVITY_PITCH, -8.0f, 85.0f);
    }

    public static int clampDrag(int drag) {
        return Math.max(-160, Math.min(160, drag));
    }

    /** Minecraft yaw: 0 = +Z, -90 = +X, 90 = -X. */
    public static float faceTarget(double playerX, double playerZ, double targetX, double targetZ, float fallback) {
        double dx = targetX - playerX;
        double dz = targetZ - playerZ;
        if (dx * dx + dz * dz < 0.04) return fallback;
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    public static float approachYaw(float current, float goal, float maxStep) {
        float delta = (float) Math.IEEEremainder(goal - current, 360.0);
        return IsometricMath.wrappedYaw(current + IsometricMath.clamp(delta, -maxStep, maxStep));
    }
}
