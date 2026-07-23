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

import com.mrshiehx.cmcl.functions.mod.CurseForgeSearcher;
import com.mrshiehx.cmcl.functions.mod.ModFunction;
import com.mrshiehx.cmcl.functions.mod.ModrinthSearcher;
import com.mrshiehx.cmcl.modSources.curseforge.CurseForgeModManager;
import com.mrshiehx.cmcl.modSources.curseforge.CurseForgeManager;
import com.mrshiehx.cmcl.modSources.modrinth.ModrinthManager;
import com.mrshiehx.cmcl.modSources.modrinth.ModrinthModManager;
import com.mrshiehx.cmcl.tui.TUIManager;
import com.mrshiehx.cmcl.utils.FileUtils;
import com.mrshiehx.cmcl.utils.cmcl.version.VersionUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import static com.mrshiehx.cmcl.tui.Ansi.*;

public class ModPage implements Page {
    private List<String> mods;
    private int selectedIndex = 0;
    private String currentVersion;
    private int scrollOffset = 0;
    private String searchQuery = "";

    @Override
    public String getTitle() { return "Mod 管理"; }

    @Override
    public void draw(StringBuilder sb, int cx, int cy, int w, int h, TUIManager m) {
        int y = cy;
        title(sb, cx, y++, "Mod 管理", MAGENTA);
        y++;

        currentVersion = m.getSelectedVersion();
        if (currentVersion == null || currentVersion.isEmpty()) {
            line(sb, cx, y++, YELLOW, "\u26A0 未选择版本");
            line(sb, cx, y++, WHITE, "请在版本管理中选择一个版本");
            return;
        }

        mods = m.getModsForVersion(currentVersion);
        String modDir = new File(m.getGameDir(), "mods").getAbsolutePath();

        line(sb, cx, y++, CYAN_BRIGHT, "\u7248\u672c: " + currentVersion);
        y++;

        if (mods.isEmpty()) {
            line(sb, cx, y++, YELLOW, "\u26A0 未安装 Mod");
            line(sb, cx, y++, WHITE, "按键: S=搜索CurseForge  M=搜索Modrinth");
            return;
        }

        line(sb, cx, y++, WHITE_BRIGHT, "已安装 Mod (\u2191\u2193导航 S=CF M=MR D=删除)");
        y++;

        int listH = Math.min(h - 7, mods.size());
        if (selectedIndex >= mods.size()) selectedIndex = mods.size() - 1;
        if (selectedIndex < scrollOffset) scrollOffset = selectedIndex;
        if (selectedIndex >= scrollOffset + listH) scrollOffset = selectedIndex - listH + 1;

        for (int i = scrollOffset; i < mods.size() && i < scrollOffset + listH; i++) {
            String mod = mods.get(i);
            boolean cur = (i == selectedIndex);
            int fgC = cur ? BLACK : WHITE;
            int bgC = cur ? MAGENTA : BLACK;
            if (cur) { sb.append(pos(y, cx + 1)).append(fgBg(fgC, bgC)).append("\u25B6 ").append(mod).append(RESET); }
            else { sb.append(pos(y, cx + 1)).append(fg(fgC)).append("  ").append(mod).append(RESET); }
            y++;
        }

        y = cy + h - 4;
        if (y < cy + 4 + listH) y = cy + 4 + listH + 1;
        if (y >= cy + h - 2) return;

        line(sb, cx, y++, WHITE, "\u2500".repeat(w));
        line(sb, cx, y++, GREEN, "CF=CurseForge  MR=Modrinth");
        line(sb, cx, y++, GREEN, "方向键\u2191\u2193切换  S:CF搜索  M:MR搜索  D:删除");
    }

    @Override
    public boolean handleInput(int keyCode, TUIManager m) {
        if (currentVersion == null || currentVersion.isEmpty()) {
            if (keyCode == 's' || keyCode == 'S' || keyCode == 'm' || keyCode == 'M') {
                m.setStatus("\u8bf7\u5148\u5728\u7248\u672c\u7ba1\u7406\u4e2d\u9009\u62e9\u4e00\u4e2a\u7248\u672c");
                return true;
            }
            return false;
        }

        if (keyCode == 's' || keyCode == 'S') { doSearchCF(m); return true; }
        if (keyCode == 'm' || keyCode == 'M') { doSearchMR(m); return true; }
        if (keyCode == 'd' || keyCode == 'D') { doDelete(m); return true; }

        if (mods == null || mods.isEmpty()) return false;
        if (keyCode == KEY_UP && selectedIndex > 0) { selectedIndex--; m.setStatus("Mod: " + mods.get(selectedIndex)); return true; }
        if (keyCode == KEY_DOWN && selectedIndex < mods.size() - 1) { selectedIndex++; m.setStatus("Mod: " + mods.get(selectedIndex)); return true; }
        return false;
    }

    private void doSearchCF(TUIManager m) {
        String query = m.prompt("\u8f93\u5165 CurseForge \u641c\u7d22\u5173\u952e\u8bcd: ");
        if (query.isEmpty()) { m.setStatus("\u5df2\u53d6\u6d88"); return; }

        m.setStatus("\u6b63\u5728 CurseForge \u641c\u7d22: " + query);
        m.runTask(() -> {
            try {
                CurseForgeModManager cf = new CurseForgeModManager();
                JSONObject mod = CurseForgeSearcher.search(cf, query, null, 10);
                if (mod == null) {
                    System.out.println("\n\u672a\u9009\u62e9\u4efb\u4f55 mod. \u6309 Enter \u8fd4\u56de TUI...");
                } else {
                    int modId = mod.getInt("id");
                    String modName = mod.getString("name");
                    System.out.println("\u5df2\u9009\u62e9: " + modName + " (ID: " + modId + ")");
                    String downloadUrl = cf.getDownloadLink(String.valueOf(modId), modName, currentVersion, null, false, null);
                    if (downloadUrl != null && !downloadUrl.isEmpty()) {
                        ModFunction.downloadMod(downloadUrl);
                        System.out.println("\n\u4e0b\u8f7d\u5b8c\u6210! \u6309 Enter \u8fd4\u56de TUI...");
                    } else {
                        System.out.println("\n\u65e0\u6cd5\u83b7\u53d6\u4e0b\u8f7d\u94fe\u63a5. \u6309 Enter \u8fd4\u56de TUI...");
                    }
                }
            } catch (Exception e) {
                System.out.println("\n\u9519\u8bef: " + e.getMessage() + ". \u6309 Enter \u8fd4\u56de TUI...");
                e.printStackTrace();
            }
            try { System.in.read(); } catch (Exception ignored) {}
        });
        mods = m.getModsForVersion(currentVersion);
    }

    private void doSearchMR(TUIManager m) {
        String query = m.prompt("\u8f93\u5165 Modrinth \u641c\u7d22\u5173\u952e\u8bcd: ");
        if (query.isEmpty()) { m.setStatus("\u5df2\u53d6\u6d88"); return; }

        m.setStatus("\u6b63\u5728 Modrinth \u641c\u7d22: " + query);
        m.runTask(() -> {
            try {
                ModrinthModManager mr = new ModrinthModManager();
                ModrinthSearcher.Result result = ModrinthSearcher.search(mr, query, null, 10);
                if (result == null || result.modID == null) {
                    System.out.println("\n\u672a\u9009\u62e9\u4efb\u4f55 mod. \u6309 Enter \u8fd4\u56de TUI...");
                } else {
                    System.out.println("\u5df2\u9009\u62e9: " + result.modName + " (ID: " + result.modID + ")");
                    String downloadUrl = mr.getDownloadLink(result.modID, result.modName, currentVersion, null, false, null);
                    if (downloadUrl != null && !downloadUrl.isEmpty()) {
                        ModFunction.downloadMod(downloadUrl);
                        System.out.println("\n\u4e0b\u8f7d\u5b8c\u6210! \u6309 Enter \u8fd4\u56de TUI...");
                    } else {
                        System.out.println("\n\u65e0\u6cd5\u83b7\u53d6\u4e0b\u8f7d\u94fe\u63a5. \u6309 Enter \u8fd4\u56de TUI...");
                    }
                }
            } catch (Exception e) {
                System.out.println("\n\u9519\u8bef: " + e.getMessage() + ". \u6309 Enter \u8fd4\u56de TUI...");
                e.printStackTrace();
            }
            try { System.in.read(); } catch (Exception ignored) {}
        });
        mods = m.getModsForVersion(currentVersion);
    }

    private void doDelete(TUIManager m) {
        if (mods == null || mods.isEmpty() || selectedIndex < 0 || selectedIndex >= mods.size()) {
            m.setStatus("\u6ca1\u6709\u53ef\u5220\u9664\u7684 mod");
            return;
        }

        String modName = mods.get(selectedIndex);
        String confirm = m.prompt("\u786e\u5b9a\u5220\u9664 " + modName + "? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            m.setStatus("\u5df2\u53d6\u6d88\u5220\u9664");
            return;
        }

        File modDir = new File(m.getGameDir(), "versions/" + currentVersion + "/mods");
        if (!modDir.exists()) modDir = new File(m.getGameDir(), "mods");

        File modFile = new File(modDir, modName);
        if (modFile.exists() && modFile.delete()) {
            m.setStatus("\u5df2\u5220\u9664: " + modName);
        } else {
            m.setStatus("\u5220\u9664\u5931\u8d25: " + modName);
        }
        mods = m.getModsForVersion(currentVersion);
    }

    @Override
    public void onSelected(TUIManager m) {
        currentVersion = m.getSelectedVersion();
        if (currentVersion != null && !currentVersion.isEmpty()) mods = m.getModsForVersion(currentVersion);
        m.setStatus("Mod 管理 - S:CurseForge M:Modrinth D:删除");
    }

    @Override
    public void onDeselected() {}

    private void title(StringBuilder sb, int cx, int y, String text, int color) {
        sb.append(pos(y, cx + 1)).append(BOLD).append(fgBright(color)).append("\u25B6 ").append(text).append(RESET);
    }
    private void line(StringBuilder sb, int cx, int y, int color, String text) {
        sb.append(pos(y, cx + 1)).append(fg(color)).append(text).append(RESET);
    }
}
