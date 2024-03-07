package org.oleg_w570.marksman_game;

import com.google.gson.Gson;

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
    private static final String[] colors = {"#dc8a78", "#dd7878", "#ea76cb", "#8839ef", "#d20f39", "#d20f39", "#fe640b", "#df8e1d", "#40a02b", "#179299", "#04a5e5", "#209fb5", "#1e66f5", "#7287fd"};
    private static final double height = 540;
    private static final double width = 650;
    private GameState state = GameState.OFF;
    private final GameInfo gameInfo = new GameInfo(height);
    private final List<PlayerHandler> handlerList = new ArrayList<>();
    Thread nextThread;

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start(7777);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new PlayerHandler(this, clientSocket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removePlayer(PlayerHandler handler) {
        handlerList.remove(handler);
        if (handler.getPlayerInfo() != null) {
            gameInfo.playerList.remove(handler.getPlayerInfo());
            sendRemove(handler.getPlayerInfo());
            if (nextThread != null && nextThread.isAlive()) {
                nextThread.interrupt();
            } else {
                startGame();
            }
        }
    }

    private void sendRemove(PlayerInfo p) {
        Action action = new Action(Action.Type.Remove, p.nickname);
        String json = gson.toJson(action);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public boolean containsNickname(String nickname) {
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.nickname.equals(nickname)) return true;
        }
        return false;
    }

    public void addPlayer(String nickname, PlayerHandler handler) throws IOException {
        String color = colors[rand.nextInt(colors.length)];
        while (containsColor(color)) color = colors[rand.nextInt(colors.length)];

        PlayerInfo newPlayer = new PlayerInfo(nickname, color);
        gameInfo.playerList.add(newPlayer);
        handler.setPlayerInfo(newPlayer);

        sendNewPlayer(newPlayer);
        handlerList.add(handler);

        String jsonInfo = gson.toJson(gameInfo);
        handler.sendMessage(jsonInfo);
    }

    private boolean containsColor(String color) {
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.color.equals(color)) return true;
        }
        return false;
    }

    private void sendNewPlayer(PlayerInfo p) throws IOException {
        String jsonPlayer = gson.toJson(p);
        Action action = new Action(Action.Type.New, jsonPlayer);
        String json = gson.toJson(action);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public void sendWantToStart(PlayerHandler handler) {
        Action wantToStart = new Action(Action.Type.WantToStart, handler.getPlayerInfo().nickname);
        String json = gson.toJson(wantToStart);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    private boolean allWantToStart() {
        for (PlayerInfo p : gameInfo.playerList)
            if (!p.wantToStart) return false;
        return true;
    }

    private void sendState() {
        String jsonState = gson.toJson(state);
        Action action = new Action(Action.Type.State, jsonState);
        String json = gson.toJson(action);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public void startGame() {
        if (allWantToStart() && !gameInfo.playerList.isEmpty()) {
            setArrowStartY();
            state = GameState.ON;
            sendState();
            nextThread = new Thread(() -> {
                try {
                    while (!isGameOver()) {
                        if (state == GameState.PAUSE) pause();
                        next();
                        sendGameInfo(Action.Type.Update);
                        Thread.sleep(4);
                    }
                    sendWinner();
                } catch (InterruptedException e) {
                    sendStop();
                } finally {
                    resetInfo();
                    sendGameInfo(Action.Type.Reset);
                    state = GameState.OFF;
                    sendState();
                }
            });
            nextThread.setDaemon(true);
            nextThread.start();
        }
    }

    private void sendStop() {
        Action action = new Action(Action.Type.Stop, null);
        String json = gson.toJson(action);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    private PlayerInfo findWinner() {
        PlayerInfo winner = gameInfo.playerList.getFirst();
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.score > winner.score) winner = p;
        }
        return winner;
    }

    private void sendWinner() {
        PlayerInfo winner = findWinner();
        String jsonWinner = gson.toJson(winner);
        Action action = new Action(Action.Type.Winner, jsonWinner);
        String json = gson.toJson(action);
        for (PlayerHandler p : handlerList) {
            p.sendMessage(json);
        }
    }

    private void resetInfo() {
        for (PlayerInfo p : gameInfo.playerList) {
            p.score = 0;
            p.shots = 0;
            p.shooting = false;
            p.wantToPause = false;
            p.wantToStart = false;
            p.arrow.x = 5.0;
        }
        gameInfo.bigCircle.y = 0.5 * height;
        gameInfo.bigCircle.direction = 1;
        gameInfo.smallCircle.y = 0.5 * height;
        gameInfo.smallCircle.direction = 1;
    }

    private boolean isGameOver() {
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.score > 5) return true;
        }
        return false;
    }

    private void sendGameInfo(Action.Type type) {
        String jsonInfo = gson.toJson(gameInfo);
        Action action = new Action(type, jsonInfo);
        String json = gson.toJson(action);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    private void setArrowStartY() {
        final int div = gameInfo.playerList.size() / 2;
        final int mod = gameInfo.playerList.size() % 2;
        for (int i = 0; i < gameInfo.playerList.size(); ++i) {
            gameInfo.playerList.get(i).arrow.y = 0.5 * height + 50.0 * (i - div) + (1 - mod) * 25.0;
        }
    }

    private void next() {
        nextCirclePos(gameInfo.bigCircle);
        nextCirclePos(gameInfo.smallCircle);
        for (PlayerInfo p : gameInfo.playerList) {
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
        if (c.y + c.radius + c.moveSpeed > height || c.y - c.radius - c.moveSpeed < 0.0) c.direction *= -1;
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

    private boolean allWantToPause() {
        for (PlayerInfo p : gameInfo.playerList) {
            if (!p.wantToPause) return false;
        }
        return true;
    }

    private boolean allWantToResume() {
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.wantToPause) return false;
        }
        return true;
    }

    public void pauseGame() {
        switch (state) {
            case PAUSE:
                if (allWantToResume()) {
                    state = GameState.ON;
                    sendState();
                    resume();
                }
                break;
            case ON:
                if (allWantToPause()) {
                    state = GameState.PAUSE;
                    sendState();
                }
                break;
        }
    }

    public void sendWantToPause(PlayerHandler handler) {
        Action action = new Action(Action.Type.WantToPause, handler.getPlayerInfo().nickname);
        String json = gson.toJson(action);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public boolean isGameStarted() {
        return state == GameState.ON || state == GameState.PAUSE;
    }
}
