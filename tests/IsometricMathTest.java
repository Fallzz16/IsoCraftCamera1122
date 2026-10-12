import dev.isocraft.camera.IsometricMath;

public final class IsometricMathTest {
    private static int tests;
    private static void check(boolean condition, String explanation) {
        ++tests;
        if (!condition) throw new AssertionError(explanation);
    }
    private static boolean near(double a, double b) { return Math.abs(a - b) < 0.0001; }
    public static void main(String[] args) {
        check(near(IsometricMath.DEFAULT_PITCH, 35.2643897), "true isometric angle");
        check(near(IsometricMath.wrappedYaw(-90), 270), "wrap negative yaw");
        check(near(IsometricMath.wrappedYaw(450), 90), "wrap positive yaw");
        check(near(IsometricMath.zoom(5, .5f), 5), "zoom minimum");
        check(near(IsometricMath.zoom(96, 2), 96), "zoom maximum");
        float[] e = IsometricMath.halfExtents(22, 1920, 1080);
        check(near(e[1], 11), "height = 22 blocks");
        check(near(e[0] / e[1], 1920.0 / 1080.0), "correct viewport aspect");
        check(near(IsometricMath.clamp(Float.NaN, 0, 1), 0), "invalid config safe");
        double[] offset = IsometricMath.backwardOffset(45, IsometricMath.DEFAULT_PITCH, 22);
        check(near(Math.sqrt(offset[0]*offset[0] + offset[1]*offset[1] + offset[2]*offset[2]), 22), "distance normalized");
        check(offset[1] > 0 && offset[0] > 0 && offset[2] < 0, "camera above and behind player");
        double[] offset2 = IsometricMath.backwardOffset(135, IsometricMath.DEFAULT_PITCH, 22);
        check(offset2[0] > 0 && offset2[2] > 0, "quarter-turn changes quadrant");
        check(IsometricMath.wrappedYaw(Float.NaN) == 45, "invalid yaw safe");
        System.out.println("OK: " + tests + " matematicos");
    }
}
