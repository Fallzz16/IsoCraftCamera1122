import dev.isocraft.camera.ScreenRayMath;

public final class ScreenRayMathTest {
    private static int count;
    private static void check(boolean condition, String reason) {
        ++count;
        if (!condition) throw new AssertionError(reason);
    }
    private static boolean near(double a, double b) { return Math.abs(a - b) < 1e-6; }
    private static double dot(double[] a, double[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }
    private static double norm(double[] d) { return Math.sqrt(dot(d,d)); }
    public static void main(String[] args) {
        double[] target = {100, 70, -200};
        ScreenRayMath.Ray center = ScreenRayMath.ray(target, 45, 35.2643897f, 22, 22, 70,
                true, 1921, 1081, 960, 540);
        check(near(norm(center.direction), 1.0), "normalized");
        check(near(center.origin[0] + center.direction[0] * 22, target[0]), "center horizontal");
        check(near(center.origin[1] + center.direction[1] * 22, target[1]), "center vertical");
        check(near(center.origin[2] + center.direction[2] * 22, target[2]), "center depth");
        ScreenRayMath.Ray left = ScreenRayMath.ray(target, 45, 35.2643897f, 22, 22, 70,
                true, 1921, 1081, 0, 540);
        ScreenRayMath.Ray right = ScreenRayMath.ray(target, 45, 35.2643897f, 22, 22, 70,
                true, 1921, 1081, 1920, 540);
        check(left.origin[0] > right.origin[0], "right means world negative X");
        check(left.origin[2] > right.origin[2], "right means world negative Z");
        check(near(dot(center.direction, left.direction), 1), "ortho rays parallel");
        check(near(dot(center.direction, right.direction), 1), "ortho rays parallel both sides");
        ScreenRayMath.Ray top = ScreenRayMath.ray(target, 45, 35.2643897f, 22, 22, 70,
                true, 1921, 1081, 960, 1080);
        check(top.origin[1] > center.origin[1], "upwards cursor is higher");
        ScreenRayMath.Ray p = ScreenRayMath.ray(target, 45, 35.2643897f, 22, 22, 70,
                false, 1921, 1081, 960, 540);
        check(near(norm(p.direction), 1), "perspective ray normalized");
        check(near(dot(center.direction, p.direction), 1), "center ray matches perspective");
        ScreenRayMath.Ray pR = ScreenRayMath.ray(target, 45, 35.2643897f, 22, 22, 70,
                false, 1921, 1081, 1920, 540);
        check(dot(center.direction, pR.direction) < 1, "perspective converges");
        check(near(pR.origin[0], p.origin[0]), "perspective all rays same camera origin");
        check(near(pR.origin[1], p.origin[1]), "perspective same camera Y");
        System.out.println("OK: " + count + " tests for screen-to-world cursor rays");
    }
}
