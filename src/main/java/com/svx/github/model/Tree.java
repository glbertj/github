package com.svx.github.model;

import com.svx.github.utility.HashUtility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Tree {
    private final Map<String, Blob> entries;
    private final String id;

    public Tree() {
        this.entries = new HashMap<>();
        this.id = computeHash(entries.toString());
    }

    public void addEntry(String filename, Blob blob) {
        entries.put(filename, blob);
    }

    public Map<String, Blob> getEntries() {
        return entries;
    }

    public Blob getBlob(String filename) {
        return entries.get(filename);
    }

    public String getId() {
        return id;
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
