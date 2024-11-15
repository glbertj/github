package com.svx.github.model;

import com.svx.github.utility.CompressionUtility;
import com.svx.github.utility.HashUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class Blob {
    private final String id;
    private final String databaseUuid;
    private final String content;
    private final Repository repository;

    public Blob(String content, Repository repository) throws IOException {
        this.content = content;
        this.id = HashUtility.computeSHA1(content);
        this.databaseUuid = UUID.randomUUID().toString();
        this.repository = repository;
        saveToDisk();
    }

    public Blob(String content, String databaseUuid, Repository repository) {
        this.content = content;
        this.id = HashUtility.computeSHA1(content);
        this.databaseUuid = databaseUuid;
        this.repository = repository;
    }

    public String getId() {
        return id;
    }

    public String getDatabaseUuid() {
        return databaseUuid;
    }

    public String getContent() {
        return content;
    }

    public void saveToDisk() throws IOException {
        Path blobPath = repository.getObjectsPath().resolve(id);
        byte[] compressedContent = CompressionUtility.compress(content);
        Files.write(blobPath, compressedContent);
    }

    public static Blob loadFromDisk(String blobId, Repository repository) throws IOException {
        Path blobPath = repository.getObjectsPath().resolve(blobId);
        byte[] compressedContent = Files.readAllBytes(blobPath);
        String content = CompressionUtility.decompress(compressedContent);
        return new Blob(content, blobId, repository);
    }

    public static String computeBlobId(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return HashUtility.computeSHA1(content);
    }

    public Repository getRepository() {
        return repository;
    }
}
