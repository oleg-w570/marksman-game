package org.oleg_w570.marksman_game;

import com.google.gson.Gson;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.sqrt;


public class GameServer {
    public static final Gson gson = new Gson();
    private static final Random rand = new Random();
    private static final String[] colors = {
            "#dc8a78", "#dd7878", "#ea76cb", "#8839ef",
            "#d20f39", "#d20f39", "#fe640b", "#df8e1d",
            "#40a02b", "#179299", "#04a5e5", "#209fb5",
            "#1e66f5", "#7287fd"
    };
    private final List<PlayerHandler> handlerList = new ArrayList<>();
    private ServerSocket serverSocket;
    private final GameInfo gameInfo = new GameInfo(height);
    private static final double height = 540;
    private static final double width = 650;

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start(7777);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new PlayerHandler(this, clientSocket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean containsNickname(String nickname) {
        for (PlayerInfo p : gameInfo.allPlayers) {
            if (p.nickname.equals(nickname))
                return true;
        }
        return false;
    }

    public void removePlayer(PlayerHandler player) {
        handlerList.remove(player);
        gameInfo.allPlayers.remove(player.getPlayerInfo());
    }

    public void addPlayer(String nickname, PlayerHandler handler) throws IOException {
        String color = colors[rand.nextInt(colors.length)];
        while (containsColor(color))
            color = colors[rand.nextInt(colors.length)];

        PlayerInfo newPlayer = new PlayerInfo(nickname, color);
        gameInfo.allPlayers.add(newPlayer);
        handler.setPlayerInfo(newPlayer);

        String jsonPlayer = gson.toJson(newPlayer);
        Action action = new Action(Action.Type.New, jsonPlayer);
        String json = gson.toJson(action);
        for (PlayerHandler p : handlerList) {
            p.sendMessage(json);
        }
        handlerList.add(handler);

        String jsonInfo = gson.toJson(gameInfo);
        handler.sendMessage(jsonInfo);
    }

    private boolean containsColor(String color) {
        for (PlayerInfo p : gameInfo.allPlayers) {
            if (p.color.equals(color))
                return true;
        }
        return false;
    }

    public void updateWantToStart(PlayerHandler handler) throws IOException {
        Action wantToStart = new Action(Action.Type.WantToStart, handler.getPlayerInfo().color);
        String json = gson.toJson(wantToStart);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public boolean allWantToStart() {
        for (PlayerInfo p : gameInfo.allPlayers)
            if (!p.wantToStart)
                return false;
        return true;
    }

    public void startGame() throws IOException {
        if (allWantToStart()) {
            setArrowStartY();
            Thread t = new Thread(() -> {
                try {
                    while (true) {
//                        if (state == State.PAUSE)
//                            pause();
                        next();
                        String jsonInfo = gson.toJson(gameInfo);
                        Action action = new Action(Action.Type.Update, jsonInfo);
                        String json = gson.toJson(action);
                        for (PlayerHandler h : handlerList) {
                            h.sendMessage(json);
                        }
                        Thread.sleep(4);
                    }
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    private void setArrowStartY() {
        final int div = gameInfo.allPlayers.size() / 2;
        final int mod = gameInfo.allPlayers.size() % 2;
        for (int i = 0; i < gameInfo.allPlayers.size(); ++i) {
            gameInfo.allPlayers.get(i).arrow.y = 0.5 * height + 50.0 * (i - div) + (1 - mod) * 25.0;
        }
    }

    private void next() {
        nextCirclePos(gameInfo.bigCircle);
        nextCirclePos(gameInfo.smallCircle);
        for (PlayerInfo p : gameInfo.allPlayers) {
            if (p.shooting) {
                p.arrow.x += p.arrow.moveSpeed;
                if (hit(p.arrow, gameInfo.bigCircle)) {
                    ++p.score;
                    p.shooting = false;
                    p.arrow.x = 5.0;
                } else if (hit(p.arrow, gameInfo.smallCircle)) {
                    p.score += 2;
                    p.shooting = false;
                    p.arrow.x = 5.0;
                } else if (p.arrow.x + 45.0 > width) {
                    p.shooting = false;
                    p.arrow.x = 5.0;
                }
            }
        }
    }

    private void nextCirclePos(CircleInfo c) {
        if (c.y + c.radius + c.moveSpeed > height || c.y - c.radius - c.moveSpeed < 0.0)
            c.direction *= -1;
        c.y += c.direction * c.moveSpeed;
    }

    boolean hit(ArrowInfo a, CircleInfo c) {
        return sqrt((a.x + 45.0 - c.x) * (a.x + 45.0 - c.x) + (a.y - c.y) * (a.y - c.y)) < c.radius;
    }

    synchronized void resume() {
        this.notifyAll();
    }

    synchronized void pause() throws InterruptedException {
        this.wait();
    }
}
