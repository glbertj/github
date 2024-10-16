package com.svx.github.controller.dialog;

import com.svx.github.view.dialog.DialogView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public abstract class DialogController<T extends DialogView<? extends Parent>> {
    protected T view;

    public DialogController(T view) {
        this.view = view;
        view.initializeView();
        setActions();
    }

    public void setActions() {
        view.getCancelButton().setOnAction(e -> hideDialog());
    }

    public Scene getScene() {
        Scene scene = new Scene(view.getRoot(), 400, 480);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/svx/github/style/dialog-style.css")).toExternalForm());

        if (view.getStyleReference() != null) {
            scene.getStylesheets().add(view.getStyleReference());
        } else {
            System.out.println("No style reference found for " + view.getClass().getSimpleName());
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

