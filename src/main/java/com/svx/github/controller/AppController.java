package com.svx.github.controller;

import com.svx.github.manager.SessionManager;
import com.svx.github.model.User;
import com.svx.github.view.View;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppController {
    private final Stage primaryStage;

    public AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("GiThub");
        this.primaryStage.setMaximized(true);
    }

    public void startApp() {
        User currentUser = SessionManager.validateSession();
        if (currentUser != null) {
            System.out.println("User is logged in");
        } else {
            navigatePage(new LoginController(this));
        }

        primaryStage.show();
    }

    public <T extends Parent> void navigatePage(Controller<? extends View<T>> controller) {
        Scene scene = controller.getView(primaryStage.getWidth(), primaryStage.getHeight());
        primaryStage.setScene(scene);
    }
}
