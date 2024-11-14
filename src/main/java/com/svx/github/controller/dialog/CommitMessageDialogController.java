package com.svx.github.controller.dialog;

import com.svx.github.view.dialog.CommitMessageDialogView;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CommitMessageDialogController extends DialogController<CommitMessageDialogView> {
    private String commitMessage;

    public CommitMessageDialogController() {
        super(new CommitMessageDialogView());
        setActions();
    }

    @Override
    public void setActions() {
        super.setActions();

        view.getConfirmButton().setOnAction(e -> {
            commitMessage = view.getMessageTextArea().getText().trim();
            hideDialog();
        });

        view.getCancelButton().setOnAction(e -> {
            commitMessage = null;
            hideDialog();
        });
    }

    public String showAndGetCommitMessage() {
        if (view.getDialogStage() == null) {
            view.setDialogStage(new Stage());
            view.getDialogStage().initModality(Modality.APPLICATION_MODAL);
            view.getDialogStage().setTitle("Commit Message");
            view.getDialogStage().setScene(new Scene(view.getRoot()));
        }

        view.getDialogStage().showAndWait();
        return commitMessage;
    }
}

