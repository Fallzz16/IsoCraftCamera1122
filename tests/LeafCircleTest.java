import dev.isocraft.camera.LeafCircle;

public final class LeafCircleTest {
    private static int count;
    private static void check(boolean value, String description) {
        count++;
        if (!value) throw new AssertionError(description);
    }
    public static void main(String[] args) {
        LeafCircle c = new LeafCircle(5, -8, 63, 76, 3.5f);
        check(c.contains(5, 65, -8), "center");
        check(c.contains(8, 75, -8), "edge");
        check(!c.contains(9, 65, -8), "outside radius");
        check(!c.contains(5, 62, -8), "below");
        check(!c.contains(5, 77, -8), "above");
        check(c.lowX() < 2 && c.highX() > 8, "dirty bounds");
        check(c.equals(new LeafCircle(5, -8, 63, 76, 3.5f)), "stable mask");
        check(!c.equals(new LeafCircle(6, -8, 63, 76, 3.5f)), "moving forces rebuild");
        check(c.hashCode() == new LeafCircle(5, -8, 63, 76, 3.5f).hashCode(), "hash contract");
        System.out.println("OK: " + count + " testes de mascara de folhas");
    }
}
