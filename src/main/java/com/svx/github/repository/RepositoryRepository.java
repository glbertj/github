package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RepositoryRepository {

    public static void save(Repository repository) {
        String query = "INSERT INTO repositories (owner_id, name, head_commit_id) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE head_commit_id = VALUES(head_commit_id)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, repository.getOwnerId().toString());
            stmt.setString(2, repository.getName());
            stmt.setString(3, repository.getLatestCommitId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving repository to database: " + e.getMessage());
        }
    }

    public static String getLatestCommitId(Repository repository) {
        String query = "SELECT head_commit_id FROM repositories WHERE owner_id = ? AND name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, repository.getOwnerId().toString());
            stmt.setString(2, repository.getName());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("head_commit_id");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching latest commit ID: " + e.getMessage());
        }

        return null;
    }

    public static List<Repository> loadAllUserRepositories(UUID ownerId) {
        String query = "SELECT name, head_commit_id FROM repositories WHERE owner_id = ?";
        List<Repository> repositories = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ownerId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String headCommitId = rs.getString("head_commit_id");

                Repository repository = new Repository(name, headCommitId, ownerId);
                repositories.add(repository);
            }
        } catch (SQLException e) {
            System.out.println("Error loading repositories: " + e.getMessage());
        }

        return repositories;
    }
}