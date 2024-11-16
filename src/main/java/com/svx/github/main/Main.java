package com.svx.github.main;

import com.svx.github.controller.AppController;
import javafx.application.Application;
import javafx.stage.Stage;
import com.svx.github.repository.*;
import com.svx.github.model.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppController appController = new AppController(primaryStage);
        appController.startApp();
    }

    public static void main(String[] args) {
//        launch(args);
    }
}