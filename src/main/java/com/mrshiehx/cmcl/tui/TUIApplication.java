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

import com.mrshiehx.cmcl.tui.page.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.mrshiehx.cmcl.tui.Ansi.*;

public class TUIApplication {
    private Terminal terminal;
    private TUIManager manager;
    private List<Page> pages;
    private int currentPageIndex;
    private boolean running;
    private static final int SIDEBAR_W = 24;

    private static final int[] MENU_COLORS = {GREEN, CYAN, YELLOW, MAGENTA, WHITE};

    public void start() {
        try {
            terminal = TerminalBuilder.builder().system(true).build();
            terminal.enterRawMode();
            System.out.print(HIDE_CURSOR);

            manager = new TUIManager();
            manager.setInputProvider(this::promptUser);
            manager.setTaskRunner(this::runExternalTask);

            pages = Arrays.asList(
                    new DashboardPage(), new VersionPage(), new AccountPage(),
                    new ModPage(), new ConfigPage()
            );
            currentPageIndex = 0;
            manager.setExitCallback(this::stop);
            pages.get(currentPageIndex).onSelected(manager);

            running = true;
            while (running) {
                drawFrame();
                int code = readKey();
                handleKey(code);
            }
        } catch (Exception e) {
            System.err.println("TUI Error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void stop() { running = false; }

    private void cleanup() {
        System.out.print(SHOW_CURSOR);
        System.out.print(RESET);
        System.out.flush();
        try { if (terminal != null) terminal.close(); } catch (IOException ignored) {}
    }

    private String promptUser(String prompt) {
        int width = terminal.getWidth();
        int height = terminal.getHeight();
        StringBuilder sb = new StringBuilder();
        sb.append(pos(height, 1));
        sb.append(fgBgBright(BLACK, WHITE));
        sb.append(fillLine(width, ' '));
        sb.append(pos(height, 1));
        sb.append(prompt);
        sb.append(SHOW_CURSOR);
        sb.append(RESET);
        System.out.print(sb);
        System.out.flush();

        StringBuilder input = new StringBuilder();
        try {
            while (true) {
                int c = terminal.reader().read();
                if (c == '\n' || c == '\r') break;
                if (c == 127 || c == '\b') {
                    if (input.length() > 0) {
                        input.deleteCharAt(input.length() - 1);
                        System.out.print("\b \b");
                    }
                } else if (c >= 32) {
                    input.append((char) c);
                    System.out.print((char) c);
                }
                System.out.flush();
            }
        } catch (IOException ignored) {}
        System.out.print(HIDE_CURSOR);
        return input.toString();
    }

    private void runExternalTask(Runnable task) {
        System.out.print(SHOW_CURSOR);
        System.out.print(pos(1, 1));
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
        try { if (terminal != null) terminal.close(); } catch (IOException ignored) {}
        task.run();
        try {
            terminal = TerminalBuilder.builder().system(true).build();
            terminal.enterRawMode();
            System.out.print(HIDE_CURSOR);
        } catch (IOException e) {
            running = false;
        }
    }

    private int readKey() throws IOException {
        int c = terminal.reader().read();
        if (c != 27) return c;
        int c2 = terminal.reader().read(50);
        if (c2 < 0) return 27;
        if (c2 == '[') {
            int c3 = terminal.reader().read(50);
            if (c3 < 0) return 27;
            if (c3 == 'A') return KEY_UP;
            if (c3 == 'B') return KEY_DOWN;
            if (c3 == 'C') return KEY_RIGHT;
            if (c3 == 'D') return KEY_LEFT;
            if (c3 == 'Z') return KEY_BACKTAB;
            if (c3 >= '1' && c3 <= '9') {
                int c4 = terminal.reader().read(50);
                if (c4 < 0) return 27;
                if (c4 == '~') {
                    if (c3 == '1') { terminal.reader().read(50); return KEY_F1; }
                    if (c3 == '2') { terminal.reader().read(50); return KEY_F2; }
                    if (c3 == '3') { terminal.reader().read(50); return KEY_F3; }
                    if (c3 == '4') { terminal.reader().read(50); return KEY_F4; }
                    if (c3 == '5') { terminal.reader().read(50); return KEY_F5; }
                }
            }
            return 27;
        }
        if (c2 == 'O') {
            int c3 = terminal.reader().read(50);
            if (c3 < 0) return 27;
            if (c3 == 'P') return KEY_F1;
            if (c3 == 'Q') return KEY_F2;
            if (c3 == 'R') return KEY_F3;
            if (c3 == 'S') return KEY_F4;
            if (c3 == 'T') return KEY_F5;
            return 27;
        }
        return 27;
    }

    private void handleKey(int code) {
        Page page = pages.get(currentPageIndex);
        if (code == KEY_ESC) {
            if (page.handleInput(KEY_ESC, manager)) return;
            stop(); return;
        }
        if (code == 'q' || code == 'Q') { stop(); return; }
        if (code == KEY_TAB) { if (page.handleInput(KEY_TAB, manager)) return; switchPage((currentPageIndex + 1) % pages.size()); return; }
        if (code == KEY_BACKTAB) { if (page.handleInput(KEY_BACKTAB, manager)) return; switchPage((currentPageIndex - 1 + pages.size()) % pages.size()); return; }
        if (code == KEY_UP) { page.handleInput(KEY_UP, manager); return; }
        if (code == KEY_DOWN) { page.handleInput(KEY_DOWN, manager); return; }
        if (code == KEY_LEFT) { page.handleInput(KEY_LEFT, manager); return; }
        if (code == KEY_RIGHT) { page.handleInput(KEY_RIGHT, manager); return; }
        if (code >= '1' && code <= '5') { switchPage(code - '1'); return; }
        if (code == KEY_F1) { switchPage(0); return; }
        if (code == KEY_F2) { switchPage(1); return; }
        if (code == KEY_F3) { switchPage(2); return; }
        if (code == KEY_F4) { switchPage(3); return; }
        if (code == KEY_F5) { switchPage(4); return; }
        page.handleInput(code, manager);
    }

    private void switchPage(int index) {
        if (index == currentPageIndex || index < 0 || index >= pages.size()) return;
        pages.get(currentPageIndex).onDeselected();
        currentPageIndex = index;
        pages.get(currentPageIndex).onSelected(manager);
    }

    private void drawFrame() {
        int width = terminal.getWidth();
        int height = terminal.getHeight();
        if (width < 50 || height < 14) return;
        StringBuilder sb = new StringBuilder(4096);
        sb.append(CLEAR_SCREEN).append(pos(1, 1));
        drawHeader(sb, width);
        drawSidebar(sb, width, height);
        drawContent(sb, width, height);
        drawStatus(sb, width, height);
        System.out.print(sb);
        System.out.flush();
    }

    private void drawHeader(StringBuilder sb, int width) {
        String text = " Console Minecraft Launcher v" + manager.getAppVersion() + " ";
        sb.append(pos(1, 1));
        sb.append(fgBgBright(BLACK, CYAN));
        sb.append(fillLine(width, ' '));
        sb.append(pos(1, 1));
        sb.append(text);
        sb.append(RESET);
    }

    private void drawSidebar(StringBuilder sb, int width, int height) {
        int startY = 2, endY = height - 1;
        for (int i = 0; i < endY - startY; i++) {
            int row = startY + i;
            sb.append(pos(row, 1)).append(fg(WHITE)).append(fillLine(SIDEBAR_W, ' ')).append(RESET);
            sb.append(pos(row, SIDEBAR_W + 1)).append(fg(CYAN)).append('\u2502').append(RESET);
        }
        String[][] menu = {{" 1 ","首页"},{" 2 ","版本管理"},{" 3 ","账户管理"},{" 4 ","Mod 管理"},{" 5 ","系统设置"}};
        for (int m = 0; m < menu.length; m++) {
            int row = startY + 2 + m * 2;
            if (row >= endY) break;
            boolean active = (m == currentPageIndex);
            sb.append(pos(row, 3));
            if (active) sb.append(fgBgBright(BLACK, MENU_COLORS[m]));
            else sb.append(fgBright(MENU_COLORS[m]));
            sb.append(" ").append(menu[m][0]).append(" ").append(menu[m][1]);
            sb.append(RESET);
        }
        sb.append(pos(endY - 1, 3)).append(fg(WHITE)).append("F1-F5/Tab").append(RESET);
        sb.append(pos(endY - 1, SIDEBAR_W + 1)).append(fg(CYAN)).append('\u2502').append(RESET);
    }

    private void drawContent(StringBuilder sb, int width, int height) {
        int cx = SIDEBAR_W + 3, cy = 2, cw = width - cx - 1, ch = height - cy - 1;
        if (cw < 10 || ch < 3) return;
        for (int i = 0; i < ch; i++) {
            sb.append(pos(cy + i, cx)).append(fg(WHITE)).append(fillLine(cw, ' ')).append(RESET);
        }
        pages.get(currentPageIndex).draw(sb, cx, cy, cw, ch, manager);
    }

    private void drawStatus(StringBuilder sb, int width, int height) {
        int row = height;
        if (row <= 0) return;
        sb.append(pos(row, 1)).append(fgBgBright(WHITE, BLUE)).append(fillLine(width, ' '));
        sb.append(pos(row, 2)).append(manager.getStatus()).append(RESET);
    }
}
