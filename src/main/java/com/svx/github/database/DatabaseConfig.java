package com.svx.github.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Was thinking of making a .env-like file. Not sure if I'll pick this up later again though.

public class DatabaseConfig {

    private static final Properties properties = new Properties();

    private DatabaseConfig() {}

    public static Properties getInstance() {
        if (properties.isEmpty()) {
            try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Error reading properties file: " + e.getMessage());
            }
        }
        return properties;
    }

    public String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password");
    }
}
