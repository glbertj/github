package com.svx.github.controller;

import com.svx.github.controller.dialog.AddRepositoryDialogController;
import com.svx.github.controller.dialog.CreateRepositoryDialogController;
import com.svx.github.utility.GitUtility;
import com.svx.github.view.MainLayoutView;

public class MainLayoutController extends Controller<MainLayoutView> {

    public MainLayoutController(AppController appController) {
        super(new MainLayoutView(), appController);
        setActions();
    }

    @Override
    protected void setActions() {
        setMenuActions();
        setSidebarActions();
    }

    private void setMenuActions() {
        view.getCreateRepositoryMenu().setOnAction(e -> appController.openDialog(new CreateRepositoryDialogController()));

        view.getAddRepositoryMenu().setOnAction(e -> appController.openDialog(new AddRepositoryDialogController()));

        view.getExitMenu().setOnAction(e -> appController.exitApp());
    }

    private void setSidebarActions() {
        view.getChangesButton().setOnAction(e -> view.showChangesTab());
        view.getHistoryButton().setOnAction(e -> view.showHistoryTab());

        view.getCommitButton().setOnAction(e -> {
            GitUtility.scanDirectory(view.getRepositoryDropdown().getValue().path());
        });
    }
}
