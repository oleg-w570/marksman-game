package org.oleg_w570.marksman_game;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import static java.lang.Math.sqrt;

public class GameController {
    @FXML
    Circle bigCircle;
    final double bigMoveSpeed = 0.75;
    int bigDirection = 1;
    @FXML
    Circle smallCircle;
    final double smallMoveSpeed = 1.5;
    int smallDirection = 1;
    @FXML
    Label scoreLabel;
    @FXML
    Label shotsLabel;
    @FXML
    Pane gamePane;
    Line arrow;
    final double arrowMoveSpeed = 1.0;
    State state = State.OFF;

    @FXML
    void onStartButtonClick() {
        if (state == State.OFF) {
            state = State.ON;
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        if (state == State.PAUSE)
                            pause();
                        Platform.runLater(this::next);
                        Thread.sleep(4);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    @FXML
    void onPauseButtonClick() {
        if (state == State.ON) {
            state = State.PAUSE;
        } else if (state == State.PAUSE) {
            resume();
        }
    }

    @FXML
    void onShootButtonClick() {
        if (state == State.ON && arrow == null) {
            Platform.runLater(() -> {
                createArrow();
                increaseShots();
            });
        }
    }

    synchronized void resume() {
        state = State.ON;
        this.notifyAll();
    }

    synchronized void pause() throws InterruptedException {
        this.wait();
    }

    void next() {
        if (bigCircle.getLayoutY() + bigCircle.getRadius() + bigMoveSpeed > gamePane.getHeight()
                || bigCircle.getLayoutY() - bigCircle.getRadius() - bigMoveSpeed < 0.0)
            bigDirection *= -1;
        bigCircle.setLayoutY(bigCircle.getLayoutY() + bigDirection * bigMoveSpeed);

        if (smallCircle.getLayoutY() + smallCircle.getRadius() + smallMoveSpeed > gamePane.getHeight()
                || smallCircle.getLayoutY() - smallCircle.getRadius() - smallMoveSpeed < 0.0)
            smallDirection *= -1;
        smallCircle.setLayoutY(smallCircle.getLayoutY() + smallDirection * smallMoveSpeed);

        if (arrow != null) {
            arrow.setLayoutX(arrow.getLayoutX() + arrowMoveSpeed);
            if (arrowHit(bigCircle)) {
                increaseScore(1);
                removeArrow();
            } else if (arrowHit(smallCircle)) {
                increaseScore(2);
                removeArrow();
            } else if (arrow.getLayoutX() + (arrow.getEndX() - arrow.getStartX()) > gamePane.getWidth()) {
                removeArrow();
            }
        }
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
        arrow = new Line(0, 0.0, 45, 0.0);
        arrow.setLayoutX(5);
        arrow.setLayoutY(gamePane.getHeight() * 0.5);
        gamePane.getChildren().add(arrow);
    }

    void removeArrow() {
        gamePane.getChildren().remove(arrow);
        arrow = null;
    }

    boolean arrowHit(Circle circle) {
        final double arrowX = arrow.getLayoutX() + (arrow.getEndX() - arrow.getStartX());
        final double arrowY = arrow.getLayoutY();
        final double circleX = circle.getLayoutX();
        final double circleY = circle.getLayoutY();

        return sqrt((arrowX - circleX) * (arrowX - circleX) + (arrowY - circleY) * (arrowY - circleY)) < circle.getRadius();
    }
}