package dev.isocraft.camera;

/** Pure geometry: finite horizontal disk centered on the player, limited vertically. */
public final class LeafCircle {
    public final int centerX, centerZ, minY, maxY;
    public final float radius;

    public LeafCircle(int centerX, int centerZ, int minY, int maxY, float radius) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.minY = minY;
        this.maxY = maxY;
        this.radius = radius;
    }

    public boolean contains(int x, int y, int z) {
        if (y < minY || y > maxY) return false;
        double dx = (x + 0.5) - (centerX + 0.5);
        double dz = (z + 0.5) - (centerZ + 0.5);
        return dx * dx + dz * dz <= radius * radius;
    }

    public int lowX() { return centerX - (int) Math.ceil(radius) - 1; }
    public int highX() { return centerX + (int) Math.ceil(radius) + 1; }
    public int lowZ() { return centerZ - (int) Math.ceil(radius) - 1; }
    public int highZ() { return centerZ + (int) Math.ceil(radius) + 1; }

    @Override public boolean equals(Object candidate) {
        if (!(candidate instanceof LeafCircle)) return false;
        LeafCircle c = (LeafCircle) candidate;
        return centerX == c.centerX && centerZ == c.centerZ && minY == c.minY
                && maxY == c.maxY && Float.compare(radius, c.radius) == 0;
    }

    @Override public int hashCode() {
        int h = centerX * 31 + centerZ;
        h = h * 31 + minY;
        h = h * 31 + maxY;
        return h * 31 + Float.floatToIntBits(radius);
    }
}
