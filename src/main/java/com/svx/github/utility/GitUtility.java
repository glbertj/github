package com.svx.github.utility;

import com.svx.github.view.dialog.DialogView;
import javafx.scene.Parent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class GitUtility {

    public static void scanDirectory(String directoryPath) {
        Path startPath = Paths.get(directoryPath);
        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName().toString().equals(".git")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    processFile(file, directoryPath);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("Error scanning directory: " + directoryPath);
        }
    }

    private static void processFile(Path file, String directoryPath) {
        try {
            byte[] fileContent = Files.readAllBytes(file);
            String hash = computeSHA1(fileContent);

            String directory = directoryPath + "/.git/objects/" + hash.substring(0, 2);
            String filename = hash.substring(2);
            Path blobPath = Paths.get(directory, filename);

            if (Files.exists(blobPath)) {
                System.out.println("Blob already exists for: " + file);
                return;
            }

            byte[] compressedContent = compress(fileContent);
            Files.createDirectories(blobPath.getParent());
            Files.write(blobPath, compressedContent);

            System.out.println("Created blob for: " + file);
        } catch (Exception e) {
            System.out.println("Error processing file: " + e.getMessage());
        }
    }

    private static String computeSHA1(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = digest.digest(content);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] compress(byte[] content) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(content);
        deflater.finish();
        byte[] buffer = new byte[1024];
        int length;
        try (var outputStream = new java.io.ByteArrayOutputStream()) {
            while (!deflater.finished()) {
                length = deflater.deflate(buffer);
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }

    public static byte[] decompress(byte[] compressedData) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length)) {
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int bytesDecompressed = inflater.inflate(buffer);
                outputStream.write(buffer, 0, bytesDecompressed);
            }
            return outputStream.toByteArray();
        } finally {
            inflater.end();
        }
    }

    public static boolean hasRepository(String gitPath, DialogView<? extends Parent> view) {
        if (gitPath.isBlank()) {
            view.getErrorLabel().setText("");
            view.getConfirmButton().setDisable(true);
            return false;
        }

        File configFile = new File(gitPath.trim(), ".git/config");

        return configFile.exists() && configFile.isFile();
    }
}
