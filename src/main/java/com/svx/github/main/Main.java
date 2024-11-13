package com.svx.github.main;

import com.svx.github.controller.AppController;
import com.svx.github.controller.MainLayoutController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.model.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Map;

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