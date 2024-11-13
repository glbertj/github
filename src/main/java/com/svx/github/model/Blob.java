package com.svx.github.model;

import com.svx.github.utility.HashUtility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Blob {
    private final String id;
    private final String content;

    public Blob(String content) {
        this.content = content;
        this.id = computeHash(content);
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    private String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encodedHash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HashUtility.bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found!");
        }
    }
}
