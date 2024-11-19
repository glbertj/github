package com.svx.github.controller;

import com.svx.github.model.User;
import com.svx.github.repository.UserRepository;
import com.svx.github.utility.CryptoUtility;
import com.svx.github.view.RegisterView;
import java.util.UUID;

public class RegisterController extends Controller<RegisterView> {

    public RegisterController(AppController appController) {
        super(new RegisterView(), appController);
    }

    @Override
    protected void setActions() {
        view.getRegisterButton().setOnAction(e -> handleRegister());
        view.getToLoginButton().setOnAction(e -> navigateToLogin());
    }

    private void handleRegister() {
        String username = view.getUsernameField().getText().trim();
        String email = view.getEmailField().getText().trim();
        String password = view.getPasswordField().getText();

        clearError();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            showError("Invalid email format.");
            return;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters long.");
            return;
        }

        User newUser = new User(UUID.randomUUID(), username, email, CryptoUtility.hashPassword(password));
        boolean registerSuccessful = UserRepository.registerUser(newUser);
        if (registerSuccessful) {
            showSuccess();
            appController.navigatePage(new LoginController(appController));
        } else {
            showError("Registration failed. Username or email may already be in use.");
        }
    }

    private void navigateToLogin() {
        appController.navigatePage(new LoginController(appController));
    }

    private void showError(String message) {
        view.getErrorLabel().setText(message);
        view.getErrorLabel().setStyle("-fx-text-fill: #d73a49;");
        view.getErrorLabel().setVisible(true);
    }

    private void showSuccess() {
        view.getErrorLabel().setText("Registration successful! Redirecting to login...");
        view.getErrorLabel().setStyle("-fx-text-fill: #28a745;");
        view.getErrorLabel().setVisible(true);
    }

    private void clearError() {
        view.getErrorLabel().setVisible(false);
        view.getErrorLabel().setText("");
    }
}

