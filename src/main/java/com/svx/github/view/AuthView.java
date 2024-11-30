package com.svx.github.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.util.Objects;

public abstract class AuthView extends View<StackPane> {
    protected Label titleLabel;
    protected VBox outerContainer;
    protected GridPane formContainer;
    protected GridPane lowerContainer;
    protected ImageView backgroundImage;
    protected ImageView logoImage;

    public AuthView(String title) {
        super();
        titleLabel = new Label(title);
        titleLabel.setFont(new Font(24));
        titleLabel.setId("auth-title");
    }

    protected void setupRoot() {
        root = new StackPane();
        root.setId("auth-pane");

        initializeOuterContainer();
        initializeFormContainer();
        initializeLowerContainer();
        initializeBackgroundImage();
        initializeLogoImage();

        styleReference = Objects.requireNonNull(
                getClass().getResource("/com/svx/github/style/auth.css")
        ).toExternalForm();

        outerContainer.getChildren().addAll(logoImage, titleLabel, formContainer, lowerContainer);
        root.getChildren().addAll(backgroundImage, outerContainer);
    }

    private void initializeOuterContainer() {
        outerContainer = new VBox(20);
        outerContainer.setAlignment(Pos.CENTER);
        outerContainer.setMaxWidth(400);
    }

    private void initializeFormContainer() {
        formContainer = new GridPane();
        formContainer.setMaxHeight(Region.USE_PREF_SIZE);
        formContainer.setVgap(20);
        formContainer.setHgap(10);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getStyleClass().add("container");
    }

    private void initializeLowerContainer() {
        lowerContainer = new GridPane();
        lowerContainer.setAlignment(Pos.CENTER);
        lowerContainer.setHgap(5);
        lowerContainer.getStyleClass().add("container");
    }

    private void initializeBackgroundImage() {
        backgroundImage = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource("/com/svx/github/image/auth-background.png")).toExternalForm()
        ));
        backgroundImage.setPreserveRatio(true);
        backgroundImage.fitWidthProperty().bind(root.widthProperty());
        backgroundImage.fitHeightProperty().bind(root.heightProperty());
    }

    private void initializeLogoImage() {
        logoImage = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource("/com/svx/github/image/GoaTHub-01-white.png")).toExternalForm()
        ));
        logoImage.setPreserveRatio(true);
        logoImage.setFitWidth(80);
    }

    protected TextField createTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.getStyleClass().add("text-field");
        return textField;
    }

    protected PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("limagram");
        passwordField.getStyleClass().add("password-field");
        return passwordField;
    }

    protected Button createButton(String text) {
        Button button = createAnimatedButton(text);
        button.getStyleClass().add("auth-button");
        button.getStyleClass().add("primary-button");
        button.prefWidthProperty().bind(formContainer.widthProperty());
        return button;
    }

    protected Label createLink(String text) {
        Label link = new Label(text);
        link.getStyleClass().add("auth-link");
        return link;
    }

    protected VBox createFieldBox(String labelText, Control inputField) {
        Label label = new Label(labelText);
        label.getStyleClass().add("bold");

        VBox fieldBox = new VBox(10);
        fieldBox.getChildren().addAll(label, inputField);
        return fieldBox;
    }
}

