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
            triangle.setFill(Color.valueOf(player.color));
            if (player.wantToStart)
                triangle.setStroke(Color.BLACK);
            triangleBox.getChildren().add(triangle);

            Label score = new Label(player.nickname + " score:");
            infoBox.getChildren().add(score);

            Label scoreCount = new Label(String.valueOf(player.score));
            infoBox.getChildren().add(scoreCount);

            Label shots = new Label(player.nickname + " shots:");
            infoBox.getChildren().add(shots);

            Label shotsCount = new Label(String.valueOf(player.shots));
            infoBox.getChildren().add(shotsCount);
        });
    }

    public void setGameInfo(GameInfo gameInfo) {
        for (PlayerInfo p : gameInfo.allPlayers) {
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
        String json = gson.toJson(Action.Type.Pause);
        serverHandler.sendMessage(json);
    }

    @FXML
    void onShootButtonClick() throws IOException {
        String json = gson.toJson(Action.Type.Shoot);
        serverHandler.sendMessage(json);
    }

    private Polygon findTriangleByColor(String color) {
        for (Node c : triangleBox.getChildren()) {
            if (((Polygon) c).getFill().equals(Color.valueOf(color)))
                return (Polygon) c;
        }
        return null;
    }

    public void setPlayerWantToStart(String playerColor) {
        Polygon playerTriangle = findTriangleByColor(playerColor);
        playerTriangle.setStroke(Color.BLACK);
    }

    public void updateGameInfo(GameInfo gameInfo) {
        Platform.runLater(() -> {
            bigCircle.setLayoutY(gameInfo.bigCircle.y);
            smallCircle.setLayoutY(gameInfo.smallCircle.y);
            for (PlayerInfo p : gameInfo.allPlayers) {
                Arrow playerArrow = findArrowByY(p.arrow.y);
                if (p.shooting) {
                    if (playerArrow == null) {
                        playerArrow = createArrow(p.arrow.y);
                        increaseShots(p);
                    }
                    playerArrow.setLayoutX(p.arrow.x);
                } else if (playerArrow != null) {
                    removeArrow(playerArrow);
                    increaseScore(p);
                }
            }
        });
    }

    private Arrow findArrowByY(double y) {
        for (Node c : gamePane.getChildren()) {
            if (c instanceof Arrow && Math.abs(c.getLayoutY() - y) < 0.000001d) {
                return (Arrow) c;
            }
        }
        return null;
    }

    private Arrow createArrow(final double y) {
        arrow = new Arrow(0, 0.0, 45, 0.0);
        arrow.setLayoutX(5);
        arrow.setLayoutY(y);
        gamePane.getChildren().add(arrow);
        return arrow;
    }

    private void removeArrow(final Arrow arrow) {
        gamePane.getChildren().remove(arrow);
    }

    private Label findScoreLabel(final String nickname) {
        for (int i = 0; i < infoBox.getChildren().size(); ++i) {
            if (((Label) infoBox.getChildren().get(i)).getText().equals(nickname + " score:")) {
                return (Label) infoBox.getChildren().get(i+1);
            }
        }
        return null;
    }

    private void increaseScore(final PlayerInfo p) {
        final Label scoreLabel = findScoreLabel(p.nickname);
        scoreLabel.setText(String.valueOf(p.score));
    }

    private Label findShotsLabel(String nickname) {
        for (int i = 0; i < infoBox.getChildren().size(); ++i) {
            if (((Label) infoBox.getChildren().get(i)).getText().equals(nickname + " shots:")) {
                return (Label) infoBox.getChildren().get(i+1);
            }
        }
        return null;
    }

    public void increaseShots(PlayerInfo playerInfo) {
        Label shotsLabel = findShotsLabel(playerInfo.nickname);
        Platform.runLater(() -> {
            shotsLabel.setText(String.valueOf(playerInfo.shots));
        });
    }
}