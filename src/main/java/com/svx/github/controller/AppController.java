package com.svx.github.controller;

import com.svx.github.controller.dialog.DialogController;
import com.svx.github.manager.SessionManager;
import com.svx.github.model.NotificationBox;
import com.svx.github.model.User;
import com.svx.github.model.UserSingleton;
import com.svx.github.view.View;
import com.svx.github.view.dialog.DialogView;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class AppController {
    private final Stage primaryStage;
    private final StackPane rootPane;
    private final NotificationBox notificationBox;

    public AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;

        rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: #1e1e1e;");
        Scene primaryScene = new Scene(rootPane, 1000, 700);
        primaryStage.setScene(primaryScene);

        primaryStage.setTitle("GiThub");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        notificationBox = new NotificationBox();
        rootPane.getChildren().add(notificationBox);
        StackPane.setAlignment(notificationBox, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(notificationBox, new Insets(0, 20, 20, 0));
    }

    public void startApp() {
        User currentUser = SessionManager.validateSession();
        if (currentUser != null) {
            UserSingleton.setCurrentUser(currentUser);
            navigatePage(new MainLayoutController(this));
        } else {
            navigatePage(new LoginController(this));
        }

        primaryStage.show();
    }

    public <T extends Parent> void navigatePage(Controller<? extends View<T>> controller) {
        Parent newRoot = controller.getView().getRoot();

        rootPane.getStylesheets().clear();
        rootPane.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/com/svx/github/style/styles.css")
        ).toExternalForm());

        if (controller.getView().getStyleReference() != null) {
            rootPane.getStylesheets().add(controller.getView().getStyleReference());
        }

        if (rootPane.getChildren().size() > 1) {
            rootPane.getChildren().removeFirst();
        }
        rootPane.getChildren().addFirst(newRoot);
    }


    public <T extends Parent> void openDialog(DialogController<? extends DialogView<T>> dialogController) {
        dialogController.getView().setDialogStage(new Stage());
        Stage dialogStage = dialogController.getView().getDialogStage();

        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(dialogController.getScene());
        dialogStage.initOwner(primaryStage);
        dialogStage.showAndWait();
    }

    public void showNotification(String message, NotificationBox.NotificationType type, String iconCode) {
        notificationBox.show(message, type, iconCode);
    }

    public void logout() {
        navigatePage(new LoginController(this));
        SessionManager.removeSession();
        UserSingleton.clearCurrentUser();
    }

    public void exitApp() {
        primaryStage.close();
    }

    public ReadOnlyBooleanProperty getFocusedProperty() {
        return primaryStage.focusedProperty();
    }
}
