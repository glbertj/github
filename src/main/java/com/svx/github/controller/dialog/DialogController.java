package com.svx.github.controller.dialog;

import com.svx.github.controller.AppController;
import com.svx.github.view.dialog.DialogView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.util.Objects;

public abstract class DialogController<T extends DialogView<? extends Parent>> {
    protected final T view;
    protected final AppController appController;

    public DialogController(T view, AppController appController) {
        this.view = view;
        this.appController = appController;
        view.initializeView();
        setActions();
    }

    public void setActions() {
        view.getCancelButton().setOnAction(e -> hideDialog());
        view.getCloseIcon().setOnMouseClicked(e -> {
            view.getDialogStage().close();
            appController.hideOverlay();
        });

        view.getCancelButton().setOnAction(e -> {
            view.getDialogStage().close();
            appController.hideOverlay();
        });
    }

    public Scene getScene() {
        Scene scene = new Scene(view.getRoot());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/svx/github/style/styles.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/svx/github/style/dialog-style.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        if (view.getStyleReference() != null) {
            scene.getStylesheets().add(view.getStyleReference());
        }

        return scene;
    }

    public T getView() {
        return view;
    }

    public void hideDialog() {
        view.getDialogStage().close();
    }
}

