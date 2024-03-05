package org.oleg_w570.marksman_game;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.util.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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
    private static final double height = 540;
    private CircleInfo bigCircle = new CircleInfo(0.5 * height, 50.0, 0.75);
    private CircleInfo smallCircle = new CircleInfo(0.5 * height, 25.0, 1.5);

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
        for (PlayerHandler p: handlerList) {
            if (p.getPlayerInfo() != null && p.getPlayerInfo().getNickname().equals(nickname))
                return true;
        }
        return false;
    }

    public void removePlayer(PlayerHandler player) {
        handlerList.remove(player);
    }

    public List<PlayerInfo> getAllPlayers() {
        List<PlayerInfo> allPlayers = new ArrayList<>();
        for (PlayerHandler h: handlerList) {
            allPlayers.add(h.getPlayerInfo());
        }
        return allPlayers;
    }

    public void addPlayer(String nickname, PlayerHandler handler) throws IOException {
        String color = colors[rand.nextInt(colors.length)];
        while (containsColor(color))
            color = colors[rand.nextInt(colors.length)];

        PlayerInfo newPlayer = new PlayerInfo(nickname, color);
        handler.setPlayerInfo(newPlayer);

        String jsonPlayer = gson.toJson(newPlayer);
        Action action = new Action(Action.Type.New, jsonPlayer);
        String json = gson.toJson(action);
        for (PlayerHandler p: handlerList) {
            p.sendMessage(json);
        }

        handlerList.add(handler);
        String jsonAllPlayers = gson.toJson(getAllPlayers());
        handler.sendMessage(jsonAllPlayers);
    }

    private boolean containsColor(String color) {
        for (PlayerHandler h: handlerList) {
            if (h.getPlayerInfo().getColor().equals(color))
                return true;
        }
        return false;
    }

    public void updateWantToStart(PlayerHandler handler) throws IOException {
        Action wantToStart = new Action(Action.Type.WantToStart, handler.getPlayerInfo().getColor());
        String json = gson.toJson(wantToStart);
        for (PlayerHandler h: handlerList) {
            h.sendMessage(json);
        }
    }

    public boolean allWantToStart() {
        for (PlayerHandler h: handlerList)
            if (!h.getPlayerInfo().isWantToStart())
                return false;
        return true;
    }

    public void startGame() throws IOException {
        if (allWantToStart()) {
            Thread t = new Thread(() -> {
                try {
                    while (true) {
//                        if (state == State.PAUSE)
//                            pause();
                        PositionInfo nextPos = nextPos();
                        String jsonPos = gson.toJson(nextPos);
                        Action action = new Action(Action.Type.UpdatePos, jsonPos);
                        String json = gson.toJson(action);
                        for (PlayerHandler h: handlerList) {
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

    private PositionInfo nextPos() {
        double bigNextPos = bigCircle.nextPos(height);
        double smallNextPos = smallCircle.nextPos(height);
        return new PositionInfo(bigNextPos, smallNextPos);
    }

    void next() {
//        if (arrow != null) {
//            arrow.setLayoutX(arrow.getLayoutX() + arrowMoveSpeed);
//            if (arrowHit(bigCircle)) {
//                increaseScore(1);
//                removeArrow();
//            } else if (arrowHit(smallCircle)) {
//                increaseScore(2);
//                removeArrow();
//            } else if (arrow.getLayoutX() + 45.0 > gamePane.getWidth()) {
//                removeArrow();
//            }
//        }
    }

    synchronized void resume() {
        this.notifyAll();
    }

    synchronized void pause() throws InterruptedException {
        this.wait();
    }
}
