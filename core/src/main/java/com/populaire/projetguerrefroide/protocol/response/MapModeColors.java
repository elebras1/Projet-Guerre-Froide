package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

import java.util.Arrays;

public final class MapModeColors extends Response {
    public static final int SIZE = 256 * 256;
    private final int[] colors = new int[SIZE];
    private int mode;

    public void clear(int mode) {
        this.mode = mode;
        Arrays.fill(this.colors, 0);
    }

    public void set(int provinceColor, int color) {
        int red = (provinceColor >> 24) & 0xFF;
        int green = (provinceColor >> 16) & 0xFF;
        this.colors[green * 256 + red] = color;
    }

    public int mode() {
        return this.mode;
    }

    public int[] colors() {
        return this.colors;
    }
}
