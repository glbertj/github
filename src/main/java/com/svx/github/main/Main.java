package com.svx.github.main;

import com.svx.github.controller.AppController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppController appController = new AppController(primaryStage);
        appController.startApp();
    }

    public static void main(String[] args) {
        launch(args);
    }
}