package org.oleg_w570.marksman_game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.oleg_w570.marksman_game.GameServer.gson;

public class PlayerHandler extends Thread {
    private final GameServer gameServer;
    private final Socket clientSocket;
    private final DataInputStream in;
    private DataOutputStream out;
    private PlayerInfo playerInfo;


    public PlayerHandler(GameServer server, Socket socket) throws IOException {
        gameServer = server;
        clientSocket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        try {
            String nickname = requestNickname();
            gameServer.addPlayer(nickname, this);
            handlingMessage();
        } catch (IOException e) {
            stopConnection();
        }
    }

    private void handlingMessage() throws IOException {
        while (true) {
            String msg = in.readUTF();
            Action.Type actionType = gson.fromJson(msg, Action.Type.class);
            switch (actionType) {
                case WantToStart:
                    playerInfo.wantToStart = true;
                    gameServer.updateWantToStart(this);
                    gameServer.startGame();
                    break;
                case Shoot:
                    playerInfo.shooting = true;
                    ++playerInfo.shots;
                    break;
                case WantToPause:
                    playerInfo.wantToPause = !playerInfo.wantToPause;
                    gameServer.sendWantToPause(this);
                    gameServer.pauseGame();
                    break;
            }
        }
    }

    private String requestNickname() throws IOException {
        String nickname = in.readUTF();
        while (gameServer.containsNickname(nickname)) {
            out.writeUTF("Nickname is taken");
            nickname = in.readUTF();
        }
        out.writeUTF("OK");
        return nickname;
    }

    private void stopConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            gameServer.removePlayer(this);
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            stopConnection();
        }
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }
}
