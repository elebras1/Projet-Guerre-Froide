package com.populaire.projetguerrefroide.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class ColorGenerator {

    public static int getWhiteRGBA() {
        return (220 << 24) | (220 << 16) | (220 << 8) | 0xFF;
    }

    public static int getLightBlueRGBA() {
        return (0 << 24) | (191 << 16) | (255 << 8) | 0xFF;
    }

    public static int getGreyRGBA() {
        return (128 << 24) | (128 << 16) | (128 << 8) | 0xFF;
    }

    public static int getRedToGreenRGBA(int value, int range) {
        float normalized = (value + range) / (2f * range);
        normalized = Math.max(0f, Math.min(1f, normalized));

        int red   = (int)(255 * (1 - normalized));
        int green = (int)(255 * normalized);
        int blue  = 25;
        int alpha = 255;

        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }


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
