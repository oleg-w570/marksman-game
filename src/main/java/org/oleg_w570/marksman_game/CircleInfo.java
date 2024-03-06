package org.oleg_w570.marksman_game;

public class CircleInfo {
    public double x;
    public double y;
    public final double radius;
    public final double moveSpeed;
    public int direction = 1;

    public CircleInfo(double x, double y, double radius, double moveSpeed) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.moveSpeed = moveSpeed;
    }
}
