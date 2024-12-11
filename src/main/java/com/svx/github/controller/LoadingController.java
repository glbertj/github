package com.svx.github.controller;

import com.svx.chess.controller.ChessController;
import com.svx.github.view.LoadingView;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

public class LoadingController extends Controller<LoadingView>{

    protected LoadingController(AppController appController) {
        super(new LoadingView(), appController);
        setActions();
    }

    @Override
    protected void setActions() {
        simulateLoading(view.getProgressBar());
    }

    private void simulateLoading(ProgressBar progressBar) {
        Service<Void> service = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        for (int i = 0; i <= 300; i++) {
                            updateProgress(i, 300);
                            Thread.sleep(5);
                        }
                        return null;
                    }
                };
            }
        };

        progressBar.progressProperty().bind(service.progressProperty());

        service.setOnSucceeded(e -> {
            appController.navigatePage(new LoginController(appController));
        });

        service.start();
    }
}
