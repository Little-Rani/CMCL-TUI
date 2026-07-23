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
package com.mrshiehx.cmcl.tui.page;

import com.mrshiehx.cmcl.tui.Ansi;
import com.mrshiehx.cmcl.tui.TUIManager;
import com.mrshiehx.cmcl.utils.cmcl.ArgumentsUtils;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import static com.mrshiehx.cmcl.tui.Ansi.*;

public class ConfigPage implements Page {
    private int selectedIndex = 0;

    private static final int ITEM_COUNT = 6;

    @Override
    public String getTitle() {
        return "系统设置";
    }

    @Override
    public void draw(StringBuilder sb, int cx, int cy, int w, int h, TUIManager m) {
        int y = cy;
        sb.append(pos(y++, cx + 1));
        sb.append(BOLD).append(fg(WHITE_BRIGHT)).append("\u25B6 系统设置").append(RESET);
        sb.append(pos(y++, cx + 1));
        sb.append(fg(WHITE)).append(fillLine(w, '\u2500')).append(RESET);
        y++;

        JSONObject config = m.getConfig();

        String[] labels = {"最大内存", "窗口宽度", "窗口高度", "下载源", "Java 路径", "游戏目录"};
        String[] values = {
            m.getMaxMemory() + " MB",
            m.getWindowWidth() + " px",
            m.getWindowHeight() + " px",
            m.getDownloadSource() == 0 ? "官方 (Mojang)" : "BMCLAPI 镜像",
            shorten(m.getJavaPath(), w - 10),
            shorten(config.optString("gameDir", m.getGameDir().getAbsolutePath()), w - 10)
        };

        for (int i = 0; i < ITEM_COUNT; i++) {
            boolean cur = (i == selectedIndex);
            int fgC = cur ? BLACK : WHITE;
            int bgC = cur ? WHITE : BLACK;

            sb.append(pos(y, cx + 1));
            if (cur) sb.append(fgBg(fgC, bgC));
            else sb.append(fg(fgC));
            sb.append(cur ? "\u25B6 " : "  ");
            sb.append(labels[i]).append(": ").append(values[i]);
            sb.append(RESET);
            y++;
        }
        y++;

        sb.append(pos(y++, cx + 1));
        sb.append(fg(CYAN)).append(fillLine(w, '\u2500')).append(RESET);
        sb.append(pos(y++, cx + 1));
        sb.append(fgBright(CYAN)).append("启动参数").append(RESET);
        List<String> jvmArgs = ArgumentsUtils.parseJVMArgs(config.optJSONArray("jvmArgs"));
        Map<String, String> gameArgs = ArgumentsUtils.parseGameArgs(config.optJSONObject("gameArgs"));
        sb.append(pos(y++, cx + 2));
        sb.append(fg(WHITE)).append("JVM: ").append(fg(CYAN)).append(jvmArgs.size()).append(fg(WHITE)).append(" 个参数").append(RESET);
        sb.append(pos(y++, cx + 2));
        sb.append(fg(WHITE)).append("游戏: ").append(fg(CYAN)).append(gameArgs.size()).append(fg(WHITE)).append(" 个参数").append(RESET);
        y++;

        sb.append(pos(cy + h - 2, cx + 1));
        sb.append(fg(GREEN)).append("\u2191\u2193\u6d4f\u89c8  Enter\u5207\u6362  J\u8f93\u5165\u8def\u5f84").append(RESET);
    }

    @Override
    public boolean handleInput(int keyCode, TUIManager m) {
        if (keyCode == 'j' || keyCode == 'J') {
            if (selectedIndex == 4 || selectedIndex == 5) {
                promptPath(m);
            }
            return true;
        }
        if (keyCode == KEY_UP) { if (selectedIndex > 0) { selectedIndex--; m.setStatus("设置: " + selectedIndex); } return true; }
        if (keyCode == KEY_DOWN) { if (selectedIndex < ITEM_COUNT - 1) { selectedIndex++; m.setStatus("设置: " + selectedIndex); } return true; }
        if (keyCode == KEY_ENTER) { toggle(m); return true; }
        return false;
    }

    private void promptPath(TUIManager m) {
        JSONObject config = m.getConfig();
        String key = selectedIndex == 4 ? "javaPath" : "gameDir";
        String label = selectedIndex == 4 ? "Java 路径" : "游戏目录";
        String current = selectedIndex == 4 ? m.getJavaPath() : config.optString("gameDir", m.getGameDir().getAbsolutePath());
        String input = m.prompt("\u8f93\u5165" + label + " (\u5f53\u524d: " + current + "): ");
        if (!input.isEmpty()) {
            config.put(key, input);
            m.saveConfig(config);
            m.setStatus(label + " \u5df2\u8bbe\u7f6e: " + input);
        } else {
            m.setStatus("\u5df2\u53d6\u6d88");
        }
    }

    private void toggle(TUIManager m) {
        JSONObject config = m.getConfig();
        switch (selectedIndex) {
            case 0: {
                int[] vs = {512, 1024, 2048, 4096};
                int cur = m.getMaxMemory();
                int next = vs[0];
                for (int i = 0; i < vs.length; i++) { if (vs[i] == cur) { next = vs[(i + 1) % vs.length]; break; } }
                config.put("maxMemory", next);
                m.saveConfig(config);
                m.setStatus("最大内存: " + next + " MB");
                break;
            }
            case 1: {
                int[] vs = {854, 1280, 1600, 1920};
                int cur = m.getWindowWidth();
                int next = vs[0];
                for (int i = 0; i < vs.length; i++) { if (vs[i] == cur) { next = vs[(i + 1) % vs.length]; break; } }
                config.put("windowSizeWidth", next);
                m.saveConfig(config);
                m.setStatus("窗口宽度: " + next);
                break;
            }
            case 2: {
                int[] vs = {480, 600, 720, 1080};
                int cur = m.getWindowHeight();
                int next = vs[0];
                for (int i = 0; i < vs.length; i++) { if (vs[i] == cur) { next = vs[(i + 1) % vs.length]; break; } }
                config.put("windowSizeHeight", next);
                m.saveConfig(config);
                m.setStatus("窗口高度: " + next);
                break;
            }
            case 3: {
                int cur = m.getDownloadSource();
                int next = cur == 0 ? 1 : 0;
                config.put("downloadSource", next);
                m.saveConfig(config);
                m.setStatus("下载源: " + (next == 0 ? "官方" : "BMCLAPI 镜像"));
                break;
            }
            case 4: {
                promptPath(m);
                break;
            }
            case 5: {
                promptPath(m);
                break;
            }
        }
    }

    @Override
    public void onSelected(TUIManager m) {
        m.setStatus("系统设置 - Enter 切换  J:输入路径");
    }

    @Override
    public void onDeselected() {}

    private String shorten(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return "..." + s.substring(s.length() - max + 3);
    }
}
