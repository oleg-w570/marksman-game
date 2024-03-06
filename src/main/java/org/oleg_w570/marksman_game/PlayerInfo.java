package org.oleg_w570.marksman_game;

public class PlayerInfo {
    public String nickname;
    public String color;
    public ArrowInfo arrow = new ArrowInfo();
    public int shots = 0;
    public int score = 0;
    public boolean wantToPause = false;
    public boolean wantToStart = false;
    public boolean shooting = false;

    public PlayerInfo(String nickname, String color) {
        this.nickname = nickname;
        this.color = color;
    }
}
