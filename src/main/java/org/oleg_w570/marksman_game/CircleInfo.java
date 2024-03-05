package org.oleg_w570.marksman_game;

public class CircleInfo {
    private double y;
    private final double radius;
    private final double moveSpeed;
    private int direction = 1;

    public CircleInfo(double startPos, double radius, double moveSpeed) {
        this.y = startPos;
        this.radius = radius;
        this.moveSpeed = moveSpeed;
    }

    public double nextPos(final double height) {
        if (y + radius + moveSpeed > height || y - radius - moveSpeed < 0.0)
            direction *= -1;
        y += direction * moveSpeed;
        return y;
    }
}
