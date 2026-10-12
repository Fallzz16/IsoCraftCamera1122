package dev.isocraft.camera;

/** Pure Java math shared with the 1.21.x prototype; no MC/Forge references. */
public final class IsometricMath {
    public static final float DEFAULT_PITCH = 35.2643897f;
    public static final float MIN_VIEW_HEIGHT = 5.0f;
    public static final float MAX_VIEW_HEIGHT = 96.0f;
    public static final float DEFAULT_VIEW_HEIGHT = 22.0f;
    public static final float MIN_DISTANCE = 6.0f;
    public static final float MAX_DISTANCE = 64.0f;
    private IsometricMath() { }

    public static float clamp(float value, float min, float max) {
        if (!Float.isFinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }

    public static float wrappedYaw(float yaw) {
        if (!Float.isFinite(yaw)) return 45.0f;
        float value = yaw % 360.0f;
        return value < 0.0f ? value + 360.0f : value;
    }

    public static float zoom(float viewHeight, float factor) {
        return clamp(viewHeight * factor, MIN_VIEW_HEIGHT, MAX_VIEW_HEIGHT);
    }

    public static float[] halfExtents(float viewHeight, int width, int height) {
        float aspect = (float) Math.max(1, width) / Math.max(1, height);
        float halfHeight = clamp(viewHeight, MIN_VIEW_HEIGHT, MAX_VIEW_HEIGHT) * 0.5f;
        return new float[] { halfHeight * aspect, halfHeight };
    }

    /** Offset from the player's eyes to the camera (backwards along its view direction). */
    public static double[] backwardOffset(float yaw, float pitch, float distance) {
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        double horizontal = Math.cos(p) * distance;
        return new double[] {Math.sin(y) * horizontal, Math.sin(p) * distance,
                             -Math.cos(y) * horizontal};
    }
}
