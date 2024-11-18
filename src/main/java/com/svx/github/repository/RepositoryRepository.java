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

    public static boolean save(Repository repository) {
        String query = "INSERT INTO repositories (id, owner_id, name, head_commit_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, repository.getId().toString());
            stmt.setString(2, repository.getOwnerId().toString());
            stmt.setString(3, repository.getName());
            stmt.setString(4, repository.getLatestCommitId());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving repository to database: " + e.getMessage());
            return false;
        }
    }

    public static List<Repository> loadAllUserRepositories(UUID ownerId) {
        String query = "SELECT id, name, head_commit_id FROM repositories WHERE owner_id = ?";
        List<Repository> repositories = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ownerId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                String name = rs.getString("name");
                String headCommitId = rs.getString("head_commit_id");

                Repository repository = new Repository(id, name, headCommitId, ownerId);
                repositories.add(repository);
            }
        } catch (SQLException e) {
            System.out.println("Error loading repositories: " + e.getMessage());
        }
        return repositories;
    }

    public static void updateHead(UUID repositoryId, String commitId) {
        String query = "UPDATE repositories SET head_commit_id = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, commitId);
            stmt.setString(2, repositoryId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating repository's latest commit ID: " + e.getMessage());
        }
    }
}

