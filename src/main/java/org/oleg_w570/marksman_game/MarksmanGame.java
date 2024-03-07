package org.oleg_w570.marksman_game;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MarksmanGame extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MarksmanGame.class.getResource("welcome-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.setTitle("Marksman");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}