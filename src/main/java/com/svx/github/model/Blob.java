package com.svx.github.model;

import com.svx.github.utility.HashUtility;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Blob {
    private final String id;
    private final String content;
    private final Repository repository;

    public Blob(String content, Repository repository) {
        this.content = content;
        this.id = HashUtility.computeSHA1(content);
        this.repository = repository;
        saveBlobToDisk();
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    private void saveBlobToDisk() {
        Path objectDir = repository.getObjectsPath().resolve(id.substring(0, 2));
        Path objectPath = objectDir.resolve(id.substring(2));

        try {
            Files.createDirectories(objectDir);

            try (FileOutputStream fos = new FileOutputStream(objectPath.toFile());
                 DeflaterOutputStream dos = new DeflaterOutputStream(fos)) {
                dos.write(content.getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("Blob saved to disk with ID: " + id);
        } catch (IOException e) {
            System.out.println("Error saving blob to disk: " + e.getMessage());
        }
    }

    public static String computeBlobId(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return HashUtility.computeSHA1(content);
    }

    public static Blob loadFromDisk(String id, Repository repository) throws IOException {
        Path objectPath = repository.getObjectsPath().resolve(id.substring(0, 2)).resolve(id.substring(2));

        try (FileInputStream fis = new FileInputStream(objectPath.toFile());
             InflaterInputStream iis = new InflaterInputStream(fis);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = iis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            String content = baos.toString(StandardCharsets.UTF_8);
            return new Blob(content, repository);
        }
    }
}
