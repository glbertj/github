package com.svx.github.view;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Objects;

public class LoadingView extends View<BorderPane>{

    private ProgressBar progressBar;

    @Override
    public void initializeView() {
        root = new BorderPane();

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource("/com/svx/github/image/name.png")).toExternalForm()
        ));
        logoImage.setPreserveRatio(true);
        logoImage.setFitWidth(500);
//        logoImage.fitWidthProperty().bind(root.widthProperty().divide(5));

        Label goat = new Label("GREATEST OF ALL TIME");
        goat.getStyleClass().addAll("primary-text", "goat-text");

        progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.prefWidthProperty().bind(root.widthProperty().divide(2));

        container.getChildren().addAll(logoImage, goat, progressBar);

        BorderPane.setAlignment(container, Pos.CENTER);
        root.setCenter(container);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
