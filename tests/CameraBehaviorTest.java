import dev.isocraft.camera.CameraBehavior;

public final class CameraBehaviorTest {
    private static int checks;
    private static void near(double actual, double expected, double tolerance, String name) {
        if (Math.abs(actual - expected) > tolerance) throw new AssertionError(name + ": " + actual + " != " + expected);
        checks++;
    }
    private static void yes(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        checks++;
    }
    public static void main(String[] args) {
        near(CameraBehavior.renderedDistance(22f, true), 11.5, 0.001, "ortho distance cap");
        near(CameraBehavior.renderedDistance(9f, true), 9, 0.001, "ortho no cap under limit");
        near(CameraBehavior.renderedDistance(22f, false), 22, 0.001, "perspective uses full distance");
        near(CameraBehavior.renderedDistance(100f, false), 64, 0.001, "max distance");
        near(CameraBehavior.dragYaw(45, 0), 45, 0.001, "neutral yaw");
        near(CameraBehavior.dragYaw(45, 10), 47.3, 0.005, "drag yaw");
        near(CameraBehavior.dragYaw(358, 20), 2.6, 0.005, "yaw wrapping");
        near(CameraBehavior.dragPitch(35, 10), 36.8, 0.005, "drag pitch");
        near(CameraBehavior.dragPitch(-6, -30), -8, 0.001, "pitch min");
        near(CameraBehavior.dragPitch(84, 30), 85, 0.001, "pitch max");
        near(CameraBehavior.dragPitch(20, 1000), 48.8, 0.005, "drag clamped");
        near(CameraBehavior.dragYaw(0, 1000), 36.8, 0.005, "yaw mouse spike clamped");
        near(CameraBehavior.faceTarget(0,0,0,10,999),0,0.001,"face +Z");
        near(CameraBehavior.faceTarget(0,0,10,0,999),-90,0.001,"face +X");
        near(CameraBehavior.faceTarget(0,0,-10,0,999),90,0.001,"face -X");
        near(Math.abs(CameraBehavior.faceTarget(0,0,0,-10,999)),180,0.001,"face -Z");
        near(CameraBehavior.faceTarget(0,0,0,0,73),73,0.001,"no spin when cursor too close");
        near(CameraBehavior.approachYaw(350,10,16),6,0.001,"shortest angle wrapping");
        near(CameraBehavior.approachYaw(10,350,16),354,0.001,"shortest angle reverse");
        near(CameraBehavior.approachYaw(45,70,16),61,0.001,"bounded angular speed");
        yes(CameraBehavior.clampDrag(900) == 160,"spike max");
        yes(CameraBehavior.clampDrag(-900) == -160,"spike min");
        yes(CameraBehavior.clampDrag(2) == 2,"normal drag");
        System.out.println("CameraBehaviorTest: PASS " + checks + " checks");
    }
}
