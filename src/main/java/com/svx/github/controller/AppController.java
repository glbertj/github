package com.svx.github.controller;

import com.svx.github.manager.SessionManager;
import com.svx.github.model.User;
import com.svx.github.view.View;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppController {
    private final Stage primaryStage;

    public AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void startApp() {
        User currentUser = SessionManager.validateSession();
        if (currentUser != null) {
            System.out.println("User is logged in");
        } else {
            navigatePage(new LoginController(), "Login Page");
        }

        primaryStage.show();
    }

    public void navigatePage(Controller<? extends View> controller, String title) {
        Scene scene = controller.getView();

        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();
    }
}
