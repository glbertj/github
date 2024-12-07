package com.svx.github.utility;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class CryptoUtility {
    private static final String SECRET_KEY = "big_black_niggay";

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String hashPassword(String password) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = null;
        byte[] hashedPassword;
        try {
            factory = SecretKeyFactory.getInstance(ALGORITHM);
            hashedPassword = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        byte[] saltAndHash = new byte[salt.length + hashedPassword.length];
        System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
        System.arraycopy(hashedPassword, 0, saltAndHash, salt.length, hashedPassword.length);

        return Base64.getEncoder().encodeToString(saltAndHash);
    }

    public static boolean verifyPassword(String password, String storedHash) {
        byte[] saltAndHash = Base64.getDecoder().decode(storedHash);

        byte[] salt = new byte[16];
        System.arraycopy(saltAndHash, 0, salt, 0, salt.length);

        byte[] storedHashBytes = new byte[saltAndHash.length - salt.length];
        System.arraycopy(saltAndHash, salt.length, storedHashBytes, 0, storedHashBytes.length);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory;
        byte[] hashedPassword;
        try {
            factory = SecretKeyFactory.getInstance(ALGORITHM);
            hashedPassword = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < storedHashBytes.length; i++) {
            if (storedHashBytes[i] != hashedPassword[i]) {
                return false;
            }
        }
        return true;
    }

    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
    }
}
