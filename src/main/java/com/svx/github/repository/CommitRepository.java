package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.Commit;
import com.svx.github.model.Repository;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class CommitRepository {

    public static void saveToDatabase(Commit commit) {
        String query = "INSERT INTO commits (commit_id, repository_id, tree_id, parent_commit_id, message, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, commit.getDatabaseUuid());
            stmt.setString(2, String.valueOf(commit.getRepository().id()));
            stmt.setString(3, commit.getTreeId());
            stmt.setString(4, commit.getParentId());
            stmt.setString(5, commit.getMessage());
            stmt.setTimestamp(6, Timestamp.valueOf(commit.getTimestamp()));
            stmt.executeUpdate();
            System.out.println("Commit saved to database: " + commit.getDatabaseUuid());
        } catch (SQLException e) {
            System.out.println("Error saving commit to database: " + e.getMessage());
        }
    }

    public static Commit loadById(String commitId, Repository repository) {
        String query = "SELECT * FROM commits WHERE commit_id = ?";
        try (Connection conn = ConnectionManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, commitId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String treeId = rs.getString("tree_id");
                String parentId = rs.getString("parent_commit_id");
                String message = rs.getString("message");
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                return new Commit(UUID.fromString(commitId), treeId, parentId, message, timestamp, commitId, repository);
            }
        } catch (SQLException e) {
            System.out.println("Error loading commit: " + e.getMessage());
        }
        return null;
    }
}
