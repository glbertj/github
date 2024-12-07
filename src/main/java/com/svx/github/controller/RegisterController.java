package com.svx.github.controller;

import com.svx.github.controller.dialog.StartGameDialogController;
import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.NotificationBox;
import com.svx.github.model.User;
import com.svx.github.repository.UserRepository;
import com.svx.github.utility.CryptoUtility;
import com.svx.github.view.RegisterView;

import java.sql.SQLException;
import java.util.UUID;

public class RegisterController extends Controller<RegisterView> {

    public RegisterController(AppController appController) {
        super(new RegisterView(), appController);
        setActions();
    }

    @Override
    protected void setActions() {
        view.getRegisterButton().setOnAction(e -> handleRegister());
        view.getToLoginButton().setOnMouseClicked(e -> navigateToLogin());
    }

    private void handleRegister() {
        if (ConnectionManager.isNotOnline()) {
            appController.openDialog(new StartGameDialogController(appController));
        }

        String username = view.getUsernameField().getText().trim();
        String email = view.getEmailField().getText().trim();
        String password = view.getPasswordField().getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            appController.showNotification("All fields are required.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            appController.showNotification("Username must only contain letters, numbers, and underscores.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            return;
        }

        if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            appController.showNotification("Invalid email format.", NotificationBox.NotificationType.ERROR, "far-envelope");
            return;
        }

        if (password.length() < 8) {
            appController.showNotification("Password must be at least 8 characters long.", NotificationBox.NotificationType.ERROR, "fas-fingerprint");
            return;
        }

        User newUser = new User(UUID.randomUUID(), username, email, CryptoUtility.hashPassword(password));
        boolean registerSuccessful = false;
        try {
            registerSuccessful = UserRepository.registerUser(newUser);
        } catch (SQLException e) {
            appController.showNotification("Registration failed. Please try again.", NotificationBox.NotificationType.ERROR, "fas-exclamation-circle");
        }
        if (registerSuccessful) {
            appController.showNotification("Successfully registered.", NotificationBox.NotificationType.SUCCESS, "fas-check-circle");
            appController.navigatePage(new LoginController(appController));
        } else {
            appController.showNotification("Registration failed. Username or email may already be in use.", NotificationBox.NotificationType.ERROR, "fas-copy");
        }
    }

    private void navigateToLogin() {
        appController.navigatePage(new LoginController(appController));
    }
}