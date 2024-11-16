package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.Repository;
import com.svx.github.model.Tree;
import com.svx.github.utility.CompressionUtility;
import com.svx.github.utility.JsonUtility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class TreeRepository {

    public static boolean save(Tree tree, String ownerId) {
        String query = "INSERT INTO trees (tree_id, owner_id, entries) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String entriesJson = JsonUtility.serialize(tree.getEntries());
            byte[] compressedEntries = CompressionUtility.compress(entriesJson);

            stmt.setString(1, tree.getDatabaseUuid());
            stmt.setString(2, ownerId);
            stmt.setBytes(3, compressedEntries);
            stmt.executeUpdate();
            return true;
        } catch (SQLException | IOException e) {
            System.out.println("Error saving tree to database: " + e.getMessage());
            return false;
        }
    }

    public static Tree load(String treeId, Repository repository) {
        String query = "SELECT entries, tree_id FROM trees WHERE tree_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, treeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] compressedEntries = rs.getBytes("entries");
                String entriesJson = CompressionUtility.decompress(compressedEntries);
                Map<String, String> entries = JsonUtility.deserialize(entriesJson);

                String databaseUuid = rs.getString("tree_id");
                return new Tree(treeId, entries, databaseUuid, repository);
            }
        } catch (SQLException | IOException e) {
            System.out.println("Error loading tree from database: " + e.getMessage());
        }
        return null;
    }
}
