package com.populaire.projetguerrefroide.util;

public class IntegerUtils {
    public static int toInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
            ((bytes[offset + 1] & 0xFF) << 16) |
            ((bytes[offset + 2] & 0xFF) << 8) |
            (bytes[offset + 3] & 0xFF);
    }
}
