package com.brandex.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


// This utility class was created using the assistance of an AI language model, and is intended for educational purposes. It provides basic password hashing functionality using SHA-256.
public class PasswordHasher {

    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainText.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String plainText, String hash) {
        return hash(plainText).equals(hash);
    }
}
