package com.svx.github.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

import java.util.Objects;

public class RegisterView extends View<GridPane> {

    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private Button registerButton;
    private Button toLoginButton;

    public RegisterView() {
        super();
    }

    @Override
    public void initializeView() {
        root = new GridPane();
        root.setId("login-register-pane");
        root.setVgap(10);
        root.setHgap(10);

        styleReference = Objects.requireNonNull(getClass().getResource("/com/svx/github/style/login-register.css")).toExternalForm();

        Label titleLabel = new Label("Login");
        titleLabel.setFont(new Font(24));
        root.add(titleLabel, 0, 0, 2, 1);

        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        root.add(usernameLabel, 0, 1);
        root.add(usernameField, 1, 1);

        Label emailLabel = new Label("Email:");
        emailField = new TextField();
        root.add(emailLabel, 0, 2);
        root.add(emailField, 1, 2);

        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        root.add(passwordLabel, 0, 3);
        root.add(passwordField, 1, 3);

        registerButton = new Button("Register");
        root.add(registerButton, 0, 4);
        GridPane.setHgrow(registerButton, Priority.ALWAYS);

        toLoginButton = new Button("To Login");
        root.add(toLoginButton, 1, 4);
        GridPane.setHgrow(toLoginButton, Priority.ALWAYS);
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getToLoginButton() {
        return toLoginButton;
    }
}
