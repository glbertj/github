package com.svx.github.controller.dialog;

import com.svx.github.controller.AppController;
import com.svx.github.controller.GameController;
import com.svx.github.view.dialog.StartGameDialogView;

public class StartGameDialogController extends DialogController<StartGameDialogView> {

    public StartGameDialogController(AppController appController) {
        super(new StartGameDialogView(), appController);
        setActions();
    }

    @Override
    public void setActions() {
        super.setActions();

        view.getCancelButton().setOnAction(e -> {
            appController.logout();
            hideDialog();
        });

        view.getConfirmButton().setOnAction(e -> {
            appController.logout();
            appController.navigatePage(new GameController(appController));
            hideDialog();
        });
    }
}
