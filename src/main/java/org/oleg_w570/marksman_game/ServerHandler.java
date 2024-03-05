package org.oleg_w570.marksman_game;

import com.google.gson.reflect.TypeToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
            requestAllInfo();
            handlingMessage();
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            downHandler();
        }
    }

    private void requestAllInfo() throws IOException {
        String jsonAllPlayers = in.readUTF();
        Type listOfPlayers = new TypeToken<ArrayList<PlayerInfo>>(){}.getType();
        List<PlayerInfo> allPlayers = gson.fromJson(jsonAllPlayers, listOfPlayers);
        gameClient.addAllPlayers(allPlayers);
    }

    private void handlingMessage() throws IOException {
        loop:
        while (true) {
            String msg = in.readUTF();
            Action action = gson.fromJson(msg, Action.class);
            switch (action.type()) {
                case New:
                    PlayerInfo playerInfo = gson.fromJson(action.info(), PlayerInfo.class);
                    gameClient.addPlayer(playerInfo);
                    break;
                case WantToStart:
                    gameClient.setPlayerWantToStart(action.info());
                    break;
                case UpdatePos:
                    PositionInfo posInfo = gson.fromJson(action.info(), PositionInfo.class);
                    gameClient.updatePos(posInfo);
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
