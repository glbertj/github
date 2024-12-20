package com.svx.github.controller;

import com.svx.github.controller.dialog.StartGameDialogController;
import com.svx.github.manager.ConnectionManager;
import com.svx.github.manager.SessionManager;
import com.svx.github.model.NotificationBox;
import com.svx.github.model.User;
import com.svx.github.model.UserSingleton;
import com.svx.github.repository.UserRepository;
import com.svx.github.utility.CryptoUtility;
import com.svx.github.view.LoginView;
import java.sql.SQLException;

public class LoginController extends Controller<LoginView> {

    public LoginController(AppController appController) {
        super(new LoginView(), appController);
        setActions();
    }

    @Override
    protected void setActions() {
        view.getLoginButton().setOnAction(e -> handleLogin());
        view.getRegisterButton().setOnMouseClicked(e -> navigateToRegister());
    }

    private void handleLogin() {
        if (ConnectionManager.isNotOnline()) {
            appController.openDialog(new StartGameDialogController(appController));
            return;
        }

        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText();

        if (username.isEmpty() || password.isEmpty()) {
            appController.showNotification("All fields must be filled.", NotificationBox.NotificationType.ERROR, "fas-times-circle");
            return;
        }

        if (ConnectionManager.isNotOnline()) {
            appController.openDialog(new StartGameDialogController(appController));
            return;
        }

        User authenticatedUser;
        try {
            authenticatedUser = username.contains("@") ? UserRepository.getByEmail(username) : UserRepository.getByUsername(username);
        } catch (SQLException e) {
            appController.showNotification("Something went wrong in the database.", NotificationBox.NotificationType.ERROR, "fas-lock");
            return;
        }

        if (authenticatedUser != null && CryptoUtility.verifyPassword(password, authenticatedUser.getPassword())) {
            SessionManager.createSession(authenticatedUser);
            UserSingleton.setCurrentUser(authenticatedUser);

            appController.showNotification("Logged in.", NotificationBox.NotificationType.SUCCESS, "fas-lock-open");
            appController.navigatePage(new MainLayoutController(appController));
        } else {
            appController.showNotification("Invalid username or password.", NotificationBox.NotificationType.ERROR, "fas-lock");
        }
    }

    private void navigateToRegister() {
        appController.navigatePage(new RegisterController(appController));
    }
}
