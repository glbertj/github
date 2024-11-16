package com.svx.github.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtility {

    public static void saveToDisk(String id, String content, Path objectsPath) throws IOException {
        Path objectDir = objectsPath.resolve(id.substring(0, 2));
        Path objectPath = objectDir.resolve(id.substring(2));

        Files.createDirectories(objectDir);

        byte[] compressedContent = CompressionUtility.compress(content);
        Files.write(objectPath, compressedContent);
    }

    public static String loadFromDisk(String id, Path objectsPath) throws IOException {
        Path objectPath = objectsPath.resolve(id.substring(0, 2)).resolve(id.substring(2));

        byte[] compressedContent = Files.readAllBytes(objectPath);
        return CompressionUtility.decompress(compressedContent);
    }
}
