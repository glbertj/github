package com.svx.github.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionSingleton {
    private static Connection connection = null;

    private ConnectionSingleton() {}

    public static synchronized Connection getInstance() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/github",
                        "root",
                        ""
                );
                System.out.println("Connected to database");
            } catch (SQLException e) {
                System.err.println("Error connecting to database: " + e.getMessage());
            }
        }
        return connection;
    }
}
