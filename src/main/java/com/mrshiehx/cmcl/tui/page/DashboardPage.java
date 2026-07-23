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
import org.json.JSONArray;

import static com.mrshiehx.cmcl.tui.Ansi.*;

public class DashboardPage implements Page {
    @Override
    public String getTitle() {
        return "首页";
    }

    @Override
    public void draw(StringBuilder sb, int cx, int cy, int w, int h, TUIManager m) {
        int y = cy;
        drawTitleLine(sb, cx, y++, GREEN_BRIGHT, "\u2605 仪表盘");
        y++;

        String ver = m.getSelectedVersion();
        String name = m.getSelectedAccountName();
        String type = m.getSelectedAccountType();

        if (!ver.isEmpty()) {
            drawInfo(sb, cx, y++, "版本", ver, GREEN);
        } else {
            drawWarning(sb, cx, y++, "未选择版本");
        }
        if (name != null) {
            drawInfo(sb, cx, y++, "账户", name + " (" + type + ")", CYAN);
        } else {
            drawWarning(sb, cx, y++, "未选择账户");
        }
        y++;

        drawSection(sb, cx, y++, "\u2699 系统信息", MAGENTA);
        drawInfo(sb, cx, y++, "Java", shorten(m.getJavaPath(), w - 6), WHITE);
        drawInfo(sb, cx, y++, "内存", m.getMaxMemory() + " MB", WHITE);
        drawInfo(sb, cx, y++, "窗口", m.getWindowWidth() + "x" + m.getWindowHeight(), WHITE);
        drawInfo(sb, cx, y++, "目录", shorten(m.getGameDir().getAbsolutePath(), w - 6), WHITE);
        y++;

        int vc = m.listVersions().size();
        JSONArray acc = m.getAccounts();
        int ac = acc != null ? acc.length() : 0;
        String ds = m.getDownloadSource() == 0 ? "官方源" : "BMCLAPI 镜像";

        drawSection(sb, cx, y++, "\u2726 统计信息", YELLOW);
        drawInfo(sb, cx, y++, "版本", vc + " 个", WHITE);
        drawInfo(sb, cx, y++, "账户", ac + " 个", WHITE);
        drawInfo(sb, cx, y++, "下载源", ds, WHITE);

        int hintY = cy + h - 2;
        sb.append(pos(hintY, cx + 1));
        sb.append(fg(GREEN));
        sb.append("1-5/F1-F5:切换页面  Tab:下一页");
        sb.append(RESET);
    }

    @Override
    public boolean handleInput(int keyCode, TUIManager manager) {
        return false;
    }

    @Override
    public void onSelected(TUIManager m) {
        m.setStatus("首页 - 启动器概览");
    }

    @Override
    public void onDeselected() {}

    private void drawTitleLine(StringBuilder sb, int cx, int y, int color, String text) {
        sb.append(pos(y, cx + 1));
        sb.append(BOLD).append(fg(color));
        sb.append(text);
        sb.append(RESET);
    }

    private void drawSection(StringBuilder sb, int cx, int y, String text, int color) {
        sb.append(pos(y, cx + 1));
        sb.append(fgBright(color));
        sb.append(text);
        sb.append(RESET);
    }

    private void drawInfo(StringBuilder sb, int cx, int y, String label, String value, int color) {
        sb.append(pos(y, cx + 1));
        sb.append(fg(WHITE));
        sb.append("\u2502 ").append(label).append(": ");
        sb.append(fg(color));
        sb.append(value);
        sb.append(RESET);
    }

    private void drawWarning(StringBuilder sb, int cx, int y, String text) {
        sb.append(pos(y, cx + 1));
        sb.append(fg(YELLOW));
        sb.append("\u26A0 ").append(text);
        sb.append(RESET);
    }

    private String shorten(String s, int max) {
        if (s.length() <= max) return s;
        return "..." + s.substring(s.length() - max + 3);
    }
}
