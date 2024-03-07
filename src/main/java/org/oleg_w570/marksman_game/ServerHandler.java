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
        while (true) {
            String msg = in.readUTF();
            Action action = gson.fromJson(msg, Action.class);
            switch (action.type()) {
                case New -> gameClient.addPlayer(gson.fromJson(action.info(), PlayerInfo.class));
                case WantToStart -> gameClient.setPlayerWantToStart(action.info());
                case State -> gameClient.setState(gson.fromJson(action.info(), GameState.class));
                case Update -> gameClient.updateGameInfo(gson.fromJson(action.info(), GameInfo.class));
                case WantToPause -> gameClient.updatePlayerWantToPause(action.info());
                case Winner -> gameClient.showWinner(gson.fromJson(action.info(), PlayerInfo.class));
                case Reset -> gameClient.resetGameInfo(gson.fromJson(action.info(), GameInfo.class));
                case Remove -> gameClient.removePlayer(action.info());
                case Stop -> gameClient.showStop();
            }
        }
    }

    private void downHandler() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
