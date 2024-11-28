package com.svx.github.manager;

import com.svx.github.controller.AppController;
import com.svx.github.controller.dialog.StartGameDialogController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private static final String URL = "jdbc:mysql://localhost:3306/github";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static AppController appController;

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            appController.openDialog(new StartGameDialogController(appController));
            throw new SQLException();
        }
    }

    public static void setAppController(AppController appController) {
        ConnectionManager.appController = appController;
    }
}
