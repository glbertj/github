package com.svx.github.controller;

import com.svx.github.controller.dialog.DialogController;
import com.svx.github.manager.RepositoryManager;
import com.svx.github.manager.SessionManager;
import com.svx.github.model.User;
import com.svx.github.model.VersionControl;
import com.svx.github.view.View;
import com.svx.github.view.dialog.DialogView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AppController {
    private final Stage primaryStage;

    public AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("GiThub");
        this.primaryStage.setMinWidth(1000);
        this.primaryStage.setMinHeight(700);
    }

    public void startApp() {
        User currentUser = SessionManager.validateSession();
        if (currentUser != null) {
            navigatePage(new MainLayoutController(this));
        } else {
            navigatePage(new LoginController(this));
        }

        primaryStage.show();
    }

    public <T extends Parent> void navigatePage(Controller<? extends View<T>> controller) {
        Scene scene = controller.getScene(primaryStage.getWidth(), primaryStage.getHeight());
        primaryStage.setScene(scene);
    }

    public <T extends Parent> void openDialog(DialogController<? extends DialogView<T>> dialogController) {
        dialogController.getView().setDialogStage(new Stage());
        Stage dialogStage = dialogController.getView().getDialogStage();

        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(dialogController.getScene());
        dialogStage.initOwner(primaryStage);
        dialogStage.showAndWait();
    }

    public void exitApp() {
        primaryStage.close();
    }
}
