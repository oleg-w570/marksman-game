package org.oleg_w570.marksman_game;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class GameClient {
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

    public void connectServer(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        serverHandler = new ServerHandler(this, socket, dataInputStream, dataOutputStream);
    }

    @FXML
    void onStartButtonClick() {
        if (state == GameState.OFF) {
            serverHandler.sendAction(Action.Type.WantToStart);
        }
    }

    @FXML
    void onPauseButtonClick() {
        if (state != GameState.OFF) {
            serverHandler.sendAction(Action.Type.WantToPause);
        }
    }

    @FXML
    void onShootButtonClick() {
        if (state == GameState.ON) {
            serverHandler.sendAction(Action.Type.Shoot);
        }
    }


    public void setGameInfo(final GameInfo gameInfo) {
        for (PlayerInfo p : gameInfo.playerList) {
            addPlayer(p);
        }
    }

    public void addPlayer(final PlayerInfo p) {
        Platform.runLater(() -> {
            Polygon triangle = new Polygon(0.0, 0.0, 20.0, -20.0, 0.0, -40.0);
            triangle.setId(p.nickname + "Triangle");
            triangle.setFill(Color.valueOf(p.color));
            if (p.wantToStart) triangle.setStroke(Color.BLACK);
            triangleBox.getChildren().add(triangle);

            Label score = new Label(p.nickname + " score:");
            score.setTextFill(Color.valueOf("#4c4f69"));
            score.setId(p.nickname + "Score");

            Label scoreCount = new Label(String.valueOf(p.score));
            scoreCount.setTextFill(Color.valueOf("#4c4f69"));
            scoreCount.setId(p.nickname + "ScoreCount");

            Label shots = new Label(p.nickname + " shots:");
            shots.setTextFill(Color.valueOf("#4c4f69"));
            shots.setId(p.nickname + "Shots");

            Label shotsCount = new Label(String.valueOf(p.shots));
            shotsCount.setTextFill(Color.valueOf("#4c4f69"));
            shotsCount.setId(p.nickname + "ShotsCount");

            VBox vbox = new VBox(0.0d, score, scoreCount, shots, shotsCount);
            vbox.setBorder(Border.stroke(Color.valueOf("#4c4f69")));
            vbox.setAlignment(Pos.CENTER);
            vbox.setId(p.nickname + "VBox");
            labelBox.getChildren().add(vbox);
        });
    }

    private Polygon findTriangle(final String nickname) {
        return (Polygon) gamePane.getScene().lookup("#" + nickname + "Triangle");
    }

    public void setPlayerWantToStart(final String nickname) {
        Polygon playerTriangle = findTriangle(nickname);
        playerTriangle.setStroke(Color.BLACK);
    }

    public void updateGameInfo(final GameInfo gameInfo) {
        Platform.runLater(() -> {
            bigCircle.setLayoutY(gameInfo.bigCircle.y);
            smallCircle.setLayoutY(gameInfo.smallCircle.y);
            for (PlayerInfo p : gameInfo.playerList) {
                Arrow playerArrow = findArrow(p.nickname);
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

    private Arrow createArrow(final PlayerInfo p) {
        Arrow arrow = new Arrow(0, 0.0, 45, 0.0);
        arrow.setLayoutX(5);
        arrow.setLayoutY(p.arrow.y);
        arrow.setId(p.nickname + "Arrow");
        gamePane.getChildren().add(arrow);
        return arrow;
    }

    private Arrow findArrow(final String nickname) {
        return (Arrow) gamePane.getScene().lookup("#" + nickname + "Arrow");
    }

    private void removeArrow(final Arrow arrow) {
        gamePane.getChildren().remove(arrow);
    }

    private Label findScoreCountLabel(final String nickname) {
        return (Label) gamePane.getScene().lookup("#" + nickname + "ScoreCount");
    }

    private void setScore(final PlayerInfo p) {
        final Label scoreLabel = findScoreCountLabel(p.nickname);
        scoreLabel.setText(String.valueOf(p.score));
    }

    private Label findShotsCountLabel(final String nickname) {
        return (Label) gamePane.getScene().lookup("#" + nickname + "ShotsCount");
    }

    public void setShots(final PlayerInfo playerInfo) {
        Label shotsLabel = findShotsCountLabel(playerInfo.nickname);
        shotsLabel.setText(String.valueOf(playerInfo.shots));
    }

    public void updatePlayerWantToPause(final String nickname) {
        Polygon playerTriangle = findTriangle(nickname);
        if (playerTriangle.getStroke() == Color.BLACK) playerTriangle.setStroke(Color.WHITESMOKE);
        else playerTriangle.setStroke(Color.BLACK);
    }

    public void setState(final GameState state) {
        this.state = state;
    }

    public void showWinner(final PlayerInfo p) {
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
                gamePane.getChildren().remove(findArrow(p.nickname));
                findTriangle(p.nickname).setStroke(Color.TRANSPARENT);
            }
        });
    }

    public void removePlayer(final String nickname) {
        Platform.runLater(() -> {
            gamePane.getChildren().remove(findArrow(nickname));
            triangleBox.getChildren().remove(findTriangle(nickname));
            labelBox.getChildren().remove(findVBox(nickname));
        });
    }

    private VBox findVBox(final String nickname) {
        return (VBox) gamePane.getScene().lookup("#" + nickname + "VBox");
    }

    public void showStop() {
        Platform.runLater(() -> {
            String info = "The game was urgently stopped due to the player's exit.";
            Alert alert = new Alert(Alert.AlertType.WARNING, info);
            alert.show();
        });
    }
}