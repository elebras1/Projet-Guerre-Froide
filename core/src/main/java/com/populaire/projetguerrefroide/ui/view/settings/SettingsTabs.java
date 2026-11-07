package com.populaire.projetguerrefroide.ui.view.settings;

final class SettingsTabs {

    private SettingsTabs() {
    }

    static int step(String[] values, String current, int step) {
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                index = i;
            }
        }
        return Math.floorMod(index + step, values.length);
    }

    static int step(short[] values, short current, int step) {
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                index = i;
            }
        }
        return Math.floorMod(index + step, values.length);
    }
}
