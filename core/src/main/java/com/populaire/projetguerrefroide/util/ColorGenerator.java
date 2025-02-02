package com.populaire.projetguerrefroide.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class ColorGenerator {

    public static int getDeterministicRGBA(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            int red = hashBytes[0] & 0xFF;
            int green = hashBytes[1] & 0xFF;
            int blue = hashBytes[2] & 0xFF;

            return (red << 24) | (green << 16) | (blue << 8) | 0xFF;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such SHA-256", e);
        }
    }
}
