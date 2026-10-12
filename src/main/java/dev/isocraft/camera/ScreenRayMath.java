package dev.isocraft.camera;

/**
 * Builds a world-space cursor ray without depending on stale OpenGL matrices.
 * The camera convention matches CameraSetup yaw=180+isometricYaw / pitch.
 * Pixel coordinates are bottom-left-origin, as reported by LWJGL Mouse.
 * Pure Java, so this calculation is testable without starting Minecraft.
 */
public final class ScreenRayMath {
    private ScreenRayMath() { }

    public static final class Ray {
        public final double[] origin;
        public final double[] direction;
        private Ray(double[] origin, double[] direction) {
            this.origin = origin;
            this.direction = direction;
        }
    }

    public static Ray ray(double[] target, float yaw, float pitch, float distance,
                          float viewHeight, float verticalFov, boolean orthographic,
                          int viewportWidth, int viewportHeight, int mouseX, int mouseY) {
        if (target == null || target.length != 3) throw new IllegalArgumentException("target XYZ required");
        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);
        double s = Math.sin(radYaw), c = Math.cos(radYaw);
        double sp = Math.sin(radPitch), cp = Math.cos(radPitch);
        double[] backward = {s * cp, sp, -c * cp};
        double[] right = {-c, 0, -s};
        double[] up = {-s * sp, cp, c * sp};
        int w = Math.max(1, viewportWidth), h = Math.max(1, viewportHeight);
        double x = 2.0 * (mouseX + 0.5) / w - 1.0;
        double y = 2.0 * (mouseY + 0.5) / h - 1.0;
        double aspect = (double) w / h;
        double[] origin = new double[3];
        for (int i = 0; i < 3; i++) origin[i] = target[i] + backward[i] * distance;
        if (orthographic) {
            double halfH = Math.max(0.5, viewHeight / 2.0);
            for (int i = 0; i < 3; i++) {
                origin[i] += right[i] * x * halfH * aspect + up[i] * y * halfH;
            }
            return new Ray(origin, new double[] {-backward[0], -backward[1], -backward[2]});
        }
        // Perspective projection with the chosen user FOV. Minecraft's sprint /
        // potion-based temporary FOV changes may cause a small visual mismatch.
        double scale = Math.tan(Math.toRadians(IsometricMath.clamp(verticalFov, 30f, 110f)) / 2.0);
        double[] direction = new double[3];
        double norm = 0.0;
        for (int i = 0; i < 3; i++) {
            direction[i] = -backward[i] + x * aspect * scale * right[i] + y * scale * up[i];
            norm += direction[i] * direction[i];
        }
        norm = Math.sqrt(norm);
        for (int i = 0; i < 3; i++) direction[i] /= norm;
        return new Ray(origin, direction);
    }
}
