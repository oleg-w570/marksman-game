package org.oleg_w570.marksman_game;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import static java.lang.Math.sqrt;

public class GameClient {
    public static final Gson gson = new Gson();
    @FXML
    VBox triangleBox;
    @FXML
    VBox infoBox;
    @FXML
    Circle bigCircle;
    @FXML
    Circle smallCircle;
    @FXML
    Label scoreLabel;
    @FXML
    Label shotsLabel;
    @FXML
    Pane gamePane;
    Arrow arrow;
    final double arrowMoveSpeed = 1.0;
    State state = State.OFF;
    ServerHandler serverHandler;

    public void setConnection(Socket socket, DataInputStream dataInputStream,
                              DataOutputStream dataOutputStream) throws IOException {
        serverHandler = new ServerHandler(this, socket, dataInputStream, dataOutputStream);
    }

    public void addPlayer(PlayerInfo player) {
        Platform.runLater(() -> {
            Polygon triangle = new Polygon(0.0, 0.0, 20.0, -20.0, 0.0, -40.0);
            triangle.setFill(Color.valueOf(player.getColor()));
            if (player.isWantToStart())
                triangle.setStroke(Color.BLACK);
            triangleBox.getChildren().add(triangle);

            Label score = new Label(player.getNickname() + " score:");
            infoBox.getChildren().add(score);

            Label scoreCount = new Label(String.valueOf(player.getScore()));
            infoBox.getChildren().add(scoreCount);

            Label shots = new Label(player.getNickname() + " shots:");
            infoBox.getChildren().add(shots);

            Label shotsCount = new Label(String.valueOf(player.getShots()));
            infoBox.getChildren().add(shotsCount);
        });
    }

    public void addAllPlayers(List<PlayerInfo> gameInfo) {
        for (PlayerInfo p: gameInfo) {
            addPlayer(p);
        }
    }

    @FXML
    void onStartButtonClick() throws IOException {
        String jsonStart = gson.toJson(Action.Type.WantToStart);
        serverHandler.sendMessage(jsonStart);
    }

    @FXML
    void onPauseButtonClick() throws IOException {
        String jsonPause = gson.toJson(Action.Type.Pause);
        serverHandler.sendMessage(jsonPause);
//        if (state == State.ON) {
//            state = State.PAUSE;
//        } else if (state == State.PAUSE) {
//            state = State.ON;
//            resume();
//        }
    }

    @FXML
    void onShootButtonClick() throws IOException {
        String jsonShoot = gson.toJson(Action.Type.Shoot);
        serverHandler.sendMessage(jsonShoot);
//        if (state == State.ON && arrow == null) {
//            Platform.runLater(() -> {
//                createArrow();
//                increaseShots();
//            });
//        }
    }

    void increaseShots() {
        final int shots = Integer.parseInt(shotsLabel.getText());
        shotsLabel.setText(String.valueOf(shots + 1));
    }

    void increaseScore(final int i) {
        final int curScore = Integer.parseInt(scoreLabel.getText());
        scoreLabel.setText(String.valueOf(curScore + i));
    }

    void createArrow() {
        arrow = new Arrow(0, 0.0, 45, 0.0);
        arrow.setLayoutX(5);
        arrow.setLayoutY(gamePane.getHeight() * 0.5);
        gamePane.getChildren().add(arrow);
    }

    void removeArrow() {
        gamePane.getChildren().remove(arrow);
        arrow = null;
    }

    boolean arrowHit(Circle c) {
        final double aX = arrow.getLayoutX() + 45.0;
        final double aY = arrow.getLayoutY();
        final double cX = c.getLayoutX();
        final double cY = c.getLayoutY();

        return sqrt((aX - cX) * (aX - cX) + (aY - cY) * (aY - cY)) < c.getRadius();
    }

    public void setPlayerWantToStart(String color) {
        for (Node c: triangleBox.getChildren()) {
            if (c instanceof Polygon) {
                if (((Polygon) c).getFill().equals(Color.valueOf(color)))
                    ((Polygon) c).setStroke(Color.BLACK);
            }
        }
    }

    public void updatePos(PositionInfo posInfo) {
        Platform.runLater(() -> {
            bigCircle.setLayoutY(posInfo.bigPos());
            smallCircle.setLayoutY(posInfo.smallPos());
        });
    }
}