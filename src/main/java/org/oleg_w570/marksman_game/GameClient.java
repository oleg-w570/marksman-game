package org.oleg_w570.marksman_game;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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
    VBox labelBox;
    @FXML
    Circle bigCircle;
    @FXML
    Circle smallCircle;
    @FXML
    Pane gamePane;
    ServerHandler serverHandler;
    GameState state = GameState.OFF;

    public void setConnection(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        serverHandler = new ServerHandler(this, socket, dataInputStream, dataOutputStream);
    }

    @FXML
    void onStartButtonClick() throws IOException {
        if (state == GameState.OFF) {
            String jsonStart = gson.toJson(Action.Type.WantToStart);
            serverHandler.sendMessage(jsonStart);
        }
    }

    @FXML
    void onPauseButtonClick() throws IOException {
        if (state != GameState.OFF) {
            String json = gson.toJson(Action.Type.WantToPause);
            serverHandler.sendMessage(json);
        }
    }

    @FXML
    void onShootButtonClick() throws IOException {
        if (state == GameState.ON) {
            String json = gson.toJson(Action.Type.Shoot);
            serverHandler.sendMessage(json);
        }
    }


    public void setGameInfo(GameInfo gameInfo) {
        for (PlayerInfo p : gameInfo.playerList) {
            addPlayer(p);
        }
    }

    public void addPlayer(PlayerInfo p) {
        Platform.runLater(() -> {
            Polygon triangle = new Polygon(0.0, 0.0, 20.0, -20.0, 0.0, -40.0);
            triangle.setId(p.nickname + "Triangle");
            triangle.setFill(Color.valueOf(p.color));
            if (p.wantToStart) triangle.setStroke(Color.BLACK);
            triangleBox.getChildren().add(triangle);

            Label score = new Label(p.nickname + " score:");
            score.setId(p.nickname+"Score");
            labelBox.getChildren().add(score);

            Label scoreCount = new Label(String.valueOf(p.score));
            score.setId(p.nickname+"ScoreCount");
            labelBox.getChildren().add(scoreCount);

            Label shots = new Label(p.nickname + " shots:");
            score.setId(p.nickname+"Shots");
            labelBox.getChildren().add(shots);

            Label shotsCount = new Label(String.valueOf(p.shots));
            score.setId(p.nickname+"ShotsCount");
            labelBox.getChildren().add(shotsCount);
        });
    }

    private Polygon findTriangleByColor(String color) {
        for (Node c : triangleBox.getChildren()) {
            if (((Polygon) c).getFill().equals(Color.valueOf(color))) return (Polygon) c;
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
            for (PlayerInfo p : gameInfo.playerList) {
                Arrow playerArrow = findArrow(p.arrow.y);
                if (p.shooting) {
                    if (playerArrow == null) {
                        playerArrow = createArrow(p);
                        setShots(p);
                    }
                    playerArrow.setLayoutX(p.arrow.x);
                } else if (playerArrow != null) {
                    removeArrow(playerArrow);
                    setScore(p);
                }
            }
        });
    }

    private Arrow findArrow(double y) {
        for (Node c : gamePane.getChildren()) {
            if (c instanceof Arrow && Math.abs(c.getLayoutY() - y) < 0.000001d) {
                return (Arrow) c;
            }
        }
        return null;
    }

    private Arrow createArrow(final PlayerInfo p) {
        Arrow arrow = new Arrow(0, 0.0, 45, 0.0);
        arrow.setLayoutX(5);
        arrow.setLayoutY(p.arrow.y);
        arrow.setId(p.nickname+"Arrow");
        gamePane.getChildren().add(arrow);
        return arrow;
    }

    private void removeArrow(final Arrow arrow) {
        gamePane.getChildren().remove(arrow);
    }

    private Label findScoreCountLabel(final String nickname) {
        for (int i = 0; i < labelBox.getChildren().size(); ++i) {
            if (((Label) labelBox.getChildren().get(i)).getText().equals(nickname + " score:")) {
                return (Label) labelBox.getChildren().get(i + 1);
            }
        }
        return null;
    }

    private void setScore(final PlayerInfo p) {
        final Label scoreLabel = findScoreCountLabel(p.nickname);
        scoreLabel.setText(String.valueOf(p.score));
    }

    private Label findShotsCountLabel(final String nickname) {
        for (int i = 0; i < labelBox.getChildren().size(); ++i) {
            if (((Label) labelBox.getChildren().get(i)).getText().equals(nickname + " shots:")) {
                return (Label) labelBox.getChildren().get(i + 1);
            }
        }
        return null;
    }

    public void setShots(final PlayerInfo playerInfo) {
        Platform.runLater(() -> {
            Label shotsLabel = findShotsCountLabel(playerInfo.nickname);
            shotsLabel.setText(String.valueOf(playerInfo.shots));
        });
    }

    public void updatePlayerWantToStart(final String playerColor) {
        Polygon playerTriangle = findTriangleByColor(playerColor);
        if (playerTriangle.getStroke() == Color.BLACK) playerTriangle.setStroke(Color.RED);
        else playerTriangle.setStroke(Color.BLACK);
    }

    public void setState(final GameState state) {
        this.state = state;
    }

    public void showWinner(PlayerInfo p) {
        Platform.runLater(() -> {
            String info = "Congratulations to " + p.nickname + "!\n" + p.nickname + " won with " + p.score + " score.";
            Alert alert = new Alert(Alert.AlertType.INFORMATION, info);
            alert.show();
        });
    }

    public void resetGameInfo(final GameInfo gameInfo) {
        Platform.runLater(() -> {
            bigCircle.setLayoutY(gameInfo.bigCircle.y);
            smallCircle.setLayoutY(gameInfo.smallCircle.y);
            for (PlayerInfo p : gameInfo.playerList) {
                setShots(p);
                setScore(p);
                gamePane.getChildren().remove(findArrow(p.arrow.y));
                findTriangleByColor(p.color).setStroke(Color.TRANSPARENT);
            }
        });
    }

    public void removePlayer(PlayerInfo p) {
        Platform.runLater(() -> {
            gamePane.getChildren().remove(findArrow(p.arrow.y));
            triangleBox.getChildren().remove(findTriangleByColor(p.color));
            labelBox.getChildren().remove(findShotsCountLabel(p.nickname));
            labelBox.getChildren().remove(findScoreCountLabel(p.nickname));

        });
    }

//    private void findShotsLabel()
}