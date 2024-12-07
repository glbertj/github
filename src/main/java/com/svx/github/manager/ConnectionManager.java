package com.svx.github.manager;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {

    private static final String URL = "jdbc:mysql://localhost:3306/github";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final BooleanProperty isOnlineProperty = new SimpleBooleanProperty(true);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void startConnectionMonitor() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                boolean isOnline = getConnection() != null;

                Platform.runLater(() -> isOnlineProperty.set(isOnline));

            } catch (SQLException e) {
                Platform.runLater(() -> isOnlineProperty.set(false));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void stopConnectionMonitor() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    public static BooleanProperty isOnlineProperty() { return isOnlineProperty; }

    public static boolean isNotOnline() { return !isOnlineProperty.get(); }
}
