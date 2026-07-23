/*
 * Console Minecraft Launcher
 * Copyright (C) 2021-2024  MrShiehX
 * Copyright (C) 2024  Little-Rani
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.mrshiehx.cmcl.tui;

public class Ansi {
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String HIDE_CURSOR = "\033[?25l";
    public static final String SHOW_CURSOR = "\033[?25h";
    public static final String CLEAR_SCREEN = "\033[2J";
    public static final String CLEAR_LINE = "\033[K";

    public static final int BLACK = 0;
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;
    public static final int BLUE = 4;
    public static final int MAGENTA = 5;
    public static final int CYAN = 6;
    public static final int WHITE = 7;

    public static final int BLACK_BRIGHT = BLACK;
    public static final int RED_BRIGHT = RED;
    public static final int GREEN_BRIGHT = GREEN;
    public static final int YELLOW_BRIGHT = YELLOW;
    public static final int BLUE_BRIGHT = BLUE;
    public static final int MAGENTA_BRIGHT = MAGENTA;
    public static final int CYAN_BRIGHT = CYAN;
    public static final int WHITE_BRIGHT = WHITE;

    public static String pos(int row, int col) {
        return "\033[" + row + ";" + col + "H";
    }

    public static String fg(int color) {
        return "\033[3" + color + "m";
    }

    public static String fgBright(int color) {
        return "\033[1;3" + color + "m";
    }

    public static String bg(int color) {
        return "\033[4" + color + "m";
    }

    public static String fgBg(int fgColor, int bgColor) {
        return "\033[3" + fgColor + ";4" + bgColor + "m";
    }

    public static String fgBgBright(int fgColor, int bgColor) {
        return "\033[1;3" + fgColor + ";4" + bgColor + "m";
    }

    public static String fillLine(int width, char ch) {
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < width; i++) sb.append(ch);
        return sb.toString();
    }

    public static String padRight(String text, int width) {
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        for (int i = 0; i < width; i++) {
            sb.append(i < len ? text.charAt(i) : ' ');
        }
        return sb.toString();
    }

    public static void drawLine(StringBuilder sb, int row, int col, int width, int fg, int bg, boolean bold, String text) {
        sb.append(pos(row, col));
        if (bold) sb.append(BOLD);
        sb.append(fgBg(fg, bg));
        sb.append(padRight(text, width));
        sb.append(RESET);
    }

    public static void drawLineBright(StringBuilder sb, int row, int col, int width, int fg, int bg, String text) {
        drawLine(sb, row, col, width, fg, bg, true, text);
    }

    public static void drawTitle(StringBuilder sb, int row, int col, int width, int color, String text) {
        sb.append(pos(row, col));
        sb.append(BOLD).append(fg(color));
        String line = fillLine(width, '\u2501');
        sb.append(line);
        sb.append(RESET);

        sb.append(pos(row + 1, col));
        sb.append(fg(color)).append('\u2502').append(' ').append(text);
        sb.append(RESET);

        sb.append(pos(row + 2, col));
        sb.append(BOLD).append(fg(color));
        sb.append(fillLine(width, '\u2501'));
        sb.append(RESET);
    }

    // Key codes for navigation
    public static final int KEY_UP = -1000;
    public static final int KEY_DOWN = -1001;
    public static final int KEY_LEFT = -1002;
    public static final int KEY_RIGHT = -1003;
    public static final int KEY_F1 = -1004;
    public static final int KEY_F2 = -1005;
    public static final int KEY_F3 = -1006;
    public static final int KEY_F4 = -1007;
    public static final int KEY_F5 = -1008;
    public static final int KEY_ENTER = 13;
    public static final int KEY_ESC = 27;
    public static final int KEY_TAB = 9;
    public static final int KEY_BACKTAB = -1009;
}
