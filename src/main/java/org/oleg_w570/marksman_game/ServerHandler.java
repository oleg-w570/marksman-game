package org.oleg_w570.marksman_game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.oleg_w570.marksman_game.GameClient.gson;

public class ServerHandler extends Thread {
    GameClient gameClient;
    private final Socket clientSocket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public ServerHandler(GameClient gameClient, Socket socket,
                         DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.gameClient = gameClient;
        clientSocket = socket;
        in = dataInputStream;
        out = dataOutputStream;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        try {
            requestGameInfo();
            handlingMessage();
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            downHandler();
        }
    }

    private void requestGameInfo() throws IOException {
        String jsonInfo = in.readUTF();
        GameInfo gameInfo = gson.fromJson(jsonInfo, GameInfo.class);
        gameClient.setGameInfo(gameInfo);
    }

    private void handlingMessage() throws IOException {
        loop:
        while (true) {
            String msg = in.readUTF();
            Action action = gson.fromJson(msg, Action.class);
            switch (action.type()) {
                case New:
                    gameClient.addPlayer(gson.fromJson(action.info(), PlayerInfo.class));
                    break;
                case WantToStart:
                    gameClient.setPlayerWantToStart(action.info());
                    break;
                case Update:
                    GameInfo gameInfo = gson.fromJson(action.info(), GameInfo.class);
                    gameClient.updateGameInfo(gameInfo);
                    break;
                case IncreaseShots:
                    gameClient.increaseShots(gson.fromJson(action.info(), PlayerInfo.class));
                    break;
                case Exit:
                    break loop;
            }
        }
    }

    private void downHandler() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            interrupt();
        }
    }

    public void sendMessage(String msg) throws IOException {
        out.writeUTF(msg);
    }
}
