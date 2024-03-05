package org.oleg_w570.marksman_game;

import javafx.scene.shape.Polygon;

public class PlayerInfo {
    public String nickname;
    public String color;
    public int shots = 0;
    public int score = 0;
    public boolean wantToStop = false;
    public boolean wantToStart = false;

    public PlayerInfo(String nickname, String color) {
        this.nickname = nickname;
        this.color = color;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isWantToStart() {
        return wantToStart;
    }

    public void setWantToStart(boolean wantToStart) {
        this.wantToStart = wantToStart;
    }

    public boolean isWantToStop() {
        return wantToStop;
    }

    public void setWantToStop(boolean wantToStop) {
        this.wantToStop = wantToStop;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getShots() {
        return shots;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
