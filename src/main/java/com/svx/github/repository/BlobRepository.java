package com.svx.github.repository;

import com.svx.github.manager.ConnectionManager;
import com.svx.github.model.Blob;
import com.svx.github.model.Repository;
import com.svx.github.utility.CompressionUtility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlobRepository {

    public static void save(Blob blob) throws Exception {
        String query = "INSERT INTO blobs (id, content) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            byte[] compressedContent = CompressionUtility.compress(blob.getContent());

            stmt.setString(1, blob.getId()); // SHA1 ID
            stmt.setBytes(2, compressedContent);
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new Exception();
        }
    }

    public static Blob load(String blobId, Repository repository) throws Exception {
        String query = "SELECT content FROM blobs WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, blobId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] compressedContent = rs.getBytes("content");
                String content = CompressionUtility.decompress(compressedContent);
                return new Blob(content, repository);
            }
        } catch (SQLException | IOException e) {
            throw new Exception();
        }
        return null;
    }
}