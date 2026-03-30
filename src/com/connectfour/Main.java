package com.connectfour;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameController controller = new GameController();
        Scene scene = new Scene(controller.getRootPane(), 600, 600);
        
        primaryStage.setTitle("Connect Four");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
