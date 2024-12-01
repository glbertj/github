package com.svx.github.controller;

import com.svx.chess.controller.ChessController;
import com.svx.github.controller.dialog.DialogController;
import com.svx.github.manager.ConnectionManager;
import com.svx.github.manager.SessionManager;
import com.svx.github.model.NotificationBox;
import com.svx.github.model.UserSingleton;
import com.svx.github.view.View;
import com.svx.github.view.dialog.DialogView;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Objects;

public class AppController {
    private final Stage primaryStage;
    private final StackPane rootPane;
    private final Scene appScene;
    private final NotificationBox notificationBox;
    private final Pane darkenOverlay;

    public AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;

        rootPane = new StackPane();
        rootPane.getStyleClass().add("root-pane");
        appScene = new Scene(rootPane, 1000, 700);
        this.primaryStage.setTitle("GiThub");
        this.primaryStage.setMinWidth(1000);
        this.primaryStage.setMinHeight(700);
        this.primaryStage.setMaximized(true);

        darkenOverlay = new Pane();
        darkenOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        darkenOverlay.setVisible(true);
        darkenOverlay.setMouseTransparent(false);

        notificationBox = new NotificationBox();
        rootPane.getChildren().addAll(notificationBox);
        StackPane.setAlignment(notificationBox, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(notificationBox, new Insets(0, 20, 20, 0));

        ConnectionManager.setAppController(this);
    }

    public void startApp() {
        this.primaryStage.setScene(appScene);
//        User currentUser = SessionManager.validateSession();
//        if (currentUser != null) {
//            UserSingleton.setCurrentUser(currentUser);
//            navigatePage(new MainLayoutController(this));
//            showNotification("Valid session found!", NotificationBox.NotificationType.SUCCESS, "fas-sign-in-alt");
//        } else {
//            navigatePage(new LoginController(this));
//        }

        navigatePage(new ChessController(this));

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

        showOverlay();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setScene(dialogController.getScene());
        dialogStage.showAndWait();
    }

    public void showNotification(String message, NotificationBox.NotificationType type, String iconCode) {
        notificationBox.show(message, type, iconCode);
    }

    public void showOverlay() {
        rootPane.getChildren().addLast(darkenOverlay);
    }

    public void hideOverlay() {
        rootPane.getChildren().removeLast();
    }

    public void logout() {
        navigatePage(new LoginController(this));
        SessionManager.removeSession();
        UserSingleton.clearCurrentUser();
    }

    public void exitApp() {
        primaryStage.close();
    }

    public void toggleFullScreen() {
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
    }

    public ReadOnlyBooleanProperty getFocusedProperty() {
        return primaryStage.focusedProperty();
    }
}