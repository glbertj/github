package com.svx.github.manager;

import com.svx.github.model.User;
import com.svx.github.repository.UserRepository;
import com.svx.github.utility.CryptoUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SessionManager {
    private static final Path SESSION_FILE = Paths.get("C:/goathub/sessions/session.dat");

    public static void createSession(User user) {
        try {
            Files.createDirectories(SESSION_FILE.getParent());

            String sessionData = user.getId() + ":" + (System.currentTimeMillis() + 86400000);
            String encryptedData = CryptoUtility.encrypt(sessionData);
            Files.write(SESSION_FILE, encryptedData.getBytes());
        } catch (Exception e) {
            System.err.println("Failed to create session: " + e.getMessage());
        }
    }

    public static User validateSession() {
        try {
            if (Files.exists(SESSION_FILE)) {
                String encryptedData = new String(Files.readAllBytes(SESSION_FILE));
                String decryptedData = CryptoUtility.decrypt(encryptedData);

                String[] sessionInfo = decryptedData.split(":");
                String userId = sessionInfo[0];
                long expiry = Long.parseLong(sessionInfo[1]);

                if (System.currentTimeMillis() < expiry) {
                    return UserRepository.getByID(userId);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to validate session: " + e.getMessage());
        }
        return null;
    }

    public static void removeSession() {
        try {
            Files.deleteIfExists(SESSION_FILE);
        } catch (IOException e) {
            System.err.println("Failed to remove session: " + e.getMessage());
        }
    }
}

