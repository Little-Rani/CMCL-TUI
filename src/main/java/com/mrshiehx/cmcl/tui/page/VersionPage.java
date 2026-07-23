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

import com.mrshiehx.cmcl.bean.arguments.Arguments;
import com.mrshiehx.cmcl.functions.InstallFunction;
import com.mrshiehx.cmcl.tui.TUIManager;
import com.mrshiehx.cmcl.utils.FileUtils;
import com.mrshiehx.cmcl.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mrshiehx.cmcl.tui.Ansi.*;

public class VersionPage implements Page {
    private static final int STATE_BROWSE = 0;
    private static final int STATE_BROWSE_REMOTE = 1;
    private static final int STATE_LOADER_SELECT = 2;

    private static final int TAB_RELEASE = 0;
    private static final int TAB_PRERELEASE = 1;
    private static final int TAB_OLD = 2;

    private int state = STATE_BROWSE;
    private int selectedIndex = 0;
    private int remoteSelectedIndex = 0;
    private int loaderSelectedIndex = 0;
    private int scrollOffset = 0;
    private int remoteScrollOffset = 0;
    private int remoteTab = TAB_RELEASE;

    private List<String> versions;
    private List<RemoteVersion> remoteVersions;
    private List<RemoteVersion> filteredVersions;
    private String selectedRemoteVersion;
    private String loaderVersion = "";

    private static final String[] TAB_NAMES = {"正式版", "Pre版/测试版", "远古版"};
    private static final String[] LOADER_NAMES = {"无", "Fabric", "Forge", "Quilt", "LiteLoader", "OptiFine"};
    private static final String[] LOADER_ARGS = {"", "--fabric", "--forge", "--quilt", "--liteloader", "--optifine"};

    private static final Set<String> APRIL_FOOLS = new HashSet<>(Arrays.asList(
        "1.5", "2.0", "15w14a", "1.RV-Pre1",
        "3D Shareware v1.34", "20w14infinite",
        "21w13a", "22w13oneBlockAtATime",
        "23w13a_or_b", "24w14potato"
    ));

    private static final String[][] LOADER_CONFLICTS = {
        {},
        {"Forge", "Quilt", "LiteLoader", "OptiFine"},
        {"Fabric", "Quilt"},
        {"Fabric", "Forge", "LiteLoader", "OptiFine"},
        {"Fabric", "Quilt"},
        {"Fabric", "Quilt"}
    };

    @Override
    public String getTitle() { return "版本管理"; }

    @Override
    public void draw(StringBuilder sb, int cx, int cy, int w, int h, TUIManager m) {
        if (state == STATE_BROWSE_REMOTE) {
            drawRemoteBrowser(sb, cx, cy, w, h, m);
        } else if (state == STATE_LOADER_SELECT) {
            drawLoaderSelect(sb, cx, cy, w, h, m);
        } else {
            drawBrowse(sb, cx, cy, w, h, m);
        }
    }

    private void drawBrowse(StringBuilder sb, int cx, int cy, int w, int h, TUIManager m) {
        int y = cy;
        title(sb, cx, y++, "版本管理", CYAN);
        y++;

        versions = m.listVersions();
        String sel = m.getSelectedVersion();

        if (versions.isEmpty()) {
            line(sb, cx, y++, YELLOW, "\u26A0 未安装任何版本");
            line(sb, cx, y++, WHITE, "按 I 键安装新版本  R 刷新");
            return;
        }

        line(sb, cx, y++, WHITE_BRIGHT, "\u2191\u2193\u9009\u62e9 Enter\u786e\u8ba4 I\u5b89\u88c5 R\u5237\u65b0 D\u5220\u9664");
        y++;

        int listH = Math.min(h - 14, versions.size());
        if (selectedIndex >= versions.size()) selectedIndex = versions.size() - 1;
        if (selectedIndex < scrollOffset) scrollOffset = selectedIndex;
        if (selectedIndex >= scrollOffset + listH) scrollOffset = selectedIndex - listH + 1;

        for (int i = scrollOffset; i < versions.size() && i < scrollOffset + listH; i++) {
            String v = versions.get(i);
            boolean isSel = v.equals(sel);
            boolean isCur = (i == selectedIndex);
            int fgC = isCur ? BLACK : (isSel ? GREEN : WHITE);
            int bgC = isCur ? CYAN : BLACK;
            String prefix = isCur ? "\u25B6 " : "  ";
            String marker = isSel ? " \u2605" : "";
            if (isCur) { sb.append(pos(y, cx + 1)).append(fgBg(fgC, bgC)).append(prefix).append(v).append(marker).append(RESET); }
            else { sb.append(pos(y, cx + 1)).append(fg(fgC)).append(prefix).append(v).append(marker).append(RESET); }
            y++;
        }

        y = cy + h - 9;
        if (y < cy + 4 + listH) y = cy + 4 + listH + 1;
        if (y >= cy + h - 2) return;

        if (selectedIndex >= 0 && selectedIndex < versions.size()) {
            String v = versions.get(selectedIndex);
            line(sb, cx, y++, CYAN, "\u2500".repeat(w));
            line(sb, cx, y++, CYAN_BRIGHT, v + " \u8be6\u60c5");
            line(sb, cx, y++, CYAN, "\u2500".repeat(w));
            y++;

            try {
                File jf = new File(m.getVersionsDir(), v + "/" + v + ".json");
                if (jf.exists()) {
                    JSONObject j = new JSONObject(FileUtils.readFileContent(jf));
                    info(sb, cx, y++, "\u7c7b\u578b", j.optString("type", "?"), CYAN);
                    info(sb, cx, y++, "\u53d1\u5e03", j.optString("releaseTime", "?"), CYAN);
                    JSONObject jv = j.optJSONObject("javaVersion");
                    if (jv != null) info(sb, cx, y++, "Java\u9700\u6c42", String.valueOf(jv.optInt("majorVersion", 0)), CYAN);
                }
            } catch (Exception ignored) {}
        }

        int ds = m.getDownloadSource();
        String dsName = ds == 0 ? "\u5b98\u65b9\u6e90" : "BMCLAPI \u955c\u50cf";
        sb.append(pos(cy + h - 2, cx + 1)).append(fg(WHITE)).append("\u4e0b\u8f7d\u6e90: ").append(fg(GREEN)).append(dsName).append(RESET);
    }

    private void drawTabs(StringBuilder sb, int cx, int y, int w) {
        int tabX = cx;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            boolean active = (i == remoteTab);
            String tab = " " + TAB_NAMES[i] + " ";
            if (active) {
                sb.append(pos(y, tabX)).append(fgBg(BLACK, CYAN)).append(tab).append(RESET);
            } else {
                sb.append(pos(y, tabX)).append(fg(CYAN)).append(tab).append(RESET);
            }
            tabX += tab.length();
        }
    }

    private void drawRemoteBrowser(StringBuilder sb, int cx, int cy, int w, int h, TUIManager m) {
        int y = cy;
        sb.append(pos(y++, cx + 1)).append(BOLD).append(fgBright(CYAN)).append("\u25B6 \u5b89\u88c5\u65b0\u7248\u672c").append(RESET);

        drawTabs(sb, cx + 2, y++, w);

        if (filteredVersions == null || filteredVersions.isEmpty()) {
            line(sb, cx, y++, YELLOW, "\u26A0 \u52a0\u8f7d\u7248\u672c\u5217\u8868\u5931\u8d25");
            line(sb, cx, y++, WHITE, "ESC \u8fd4\u56de");
            return;
        }

        line(sb, cx, y++, WHITE_BRIGHT, "\u4e0b\u8f7d\u6e90: " + fg(GREEN) + (m.getDownloadSource() == 0 ? "\u5b98\u65b9" : "BMCLAPI") + RESET);
        y++;

        line(sb, cx, y++, WHITE_BRIGHT, "Tab\u5207\u6362\u6807\u7b7e  \u2191\u2193\u6d4f\u89c8 Enter\u9009\u62e9  ESC\u8fd4\u56de");
        y++;

        int listH = Math.min(h - y - 7, filteredVersions.size());
        if (remoteSelectedIndex >= filteredVersions.size()) remoteSelectedIndex = filteredVersions.size() - 1;
        if (remoteSelectedIndex < remoteScrollOffset) remoteScrollOffset = remoteSelectedIndex;
        if (remoteSelectedIndex >= remoteScrollOffset + listH) remoteScrollOffset = remoteSelectedIndex - listH + 1;

        for (int i = remoteScrollOffset; i < filteredVersions.size() && i < remoteScrollOffset + listH; i++) {
            RemoteVersion rv = filteredVersions.get(i);
            boolean cur = (i == remoteSelectedIndex);
            int fgC = cur ? BLACK : WHITE;
            int bgC = cur ? CYAN : BLACK;
            String prefix = cur ? "\u25B6 " : "  ";
            boolean isAF = APRIL_FOOLS.contains(rv.id);
            String afTag = isAF ? fg(RED) + " (\u611a\u4eba\u8282)" + RESET + fg(fgC) : "";
            if (cur) { sb.append(pos(y, cx + 1)).append(fgBg(fgC, bgC)).append(prefix).append(rv.id).append(afTag).append(RESET); }
            else { sb.append(pos(y, cx + 1)).append(fg(fgC)).append(prefix).append(rv.id).append(afTag).append(RESET); }
            y++;
        }

        if (remoteSelectedIndex >= 0 && remoteSelectedIndex < filteredVersions.size()) {
            RemoteVersion rv = filteredVersions.get(remoteSelectedIndex);
            y = cy + h - 5;
            int minY = cy + (y - cy < 6 ? cy + 6 : y);
            y = minY;
            if (y >= cy + h - 1) return;

            line(sb, cx, y++, CYAN, "\u2500".repeat(w));
            info(sb, cx, y++, "\u7248\u672c", rv.id, CYAN_BRIGHT);
            info(sb, cx, y++, "\u7c7b\u578b", rv.type, CYAN);
            info(sb, cx, y++, "\u53d1\u5e03\u65f6\u95f4", rv.releaseTime, CYAN);
        }
    }

    private void drawLoaderSelect(StringBuilder sb, int cx, int cy, int w, int h, TUIManager m) {
        int y = cy;
        String verInfo = selectedRemoteVersion;
        boolean isAF = APRIL_FOOLS.contains(selectedRemoteVersion);
        if (isAF) verInfo += " " + fg(RED) + "(\u611a\u4eba\u8282\u7248)" + RESET;

        sb.append(pos(y++, cx + 1)).append(BOLD).append(fgBright(CYAN)).append("\u25B6 \u5b89\u88c5 ").append(verInfo).append(RESET);
        sb.append(pos(y++, cx + 1)).append(fg(CYAN)).append(fillLine(w, '\u2500')).append(RESET);
        y++;

        line(sb, cx, y++, WHITE_BRIGHT, "\u9009\u62e9\u6a21\u7ec4\u88c5\u8f7d\u5668 (\u2191\u2193\u6d4f\u89c8 Enter\u786e\u8ba4 ESC\u53d6\u6d88)");
        y++;

        for (int i = 0; i < LOADER_NAMES.length; i++) {
            boolean cur = (i == loaderSelectedIndex);
            boolean hasVer = !LOADER_ARGS[i].isEmpty();
            int fgC = cur ? BLACK : WHITE;
            int bgC = cur ? CYAN : BLACK;
            String prefix = cur ? "\u25B6 " : "  ";
            String versionInfo = "";
            if (hasVer && i == loaderSelectedIndex && !loaderVersion.isEmpty()) {
                versionInfo = " [" + loaderVersion + "]";
            } else if (hasVer && i == loaderSelectedIndex) {
                versionInfo = " (\u53ef\u6309V\u8f93\u5165\u7248\u672c)";
            }
            if (cur) { sb.append(pos(y, cx + 1)).append(fgBg(fgC, bgC)).append(prefix).append(LOADER_NAMES[i]).append(versionInfo).append(RESET); }
            else { sb.append(pos(y, cx + 1)).append(fg(fgC)).append(prefix).append(LOADER_NAMES[i]).append(RESET); }
            y++;
        }
        y++;

        String[] conflicts = LOADER_CONFLICTS[loaderSelectedIndex];
        if (conflicts.length > 0) {
            String conflictStr = String.join(", ", conflicts);
            line(sb, cx, y++, YELLOW, "\u26A0 \u4e0d\u517c\u5bb9: " + conflictStr);
            y++;
        }

        line(sb, cx, y++, WHITE, "\u4e0b\u8f7d\u6e90: " + fg(GREEN) + (m.getDownloadSource() == 0 ? "\u5b98\u65b9" : "BMCLAPI \u955c\u50cf") + RESET);
        line(sb, cx, y++, WHITE, "Enter: \u5f00\u59cb\u5b89\u88c5  V: \u8f93\u5165\u88c5\u8f7d\u5668\u7248\u672c  ESC: \u53d6\u6d88");
    }

    @Override
    public boolean handleInput(int keyCode, TUIManager m) {
        if (state == STATE_BROWSE_REMOTE) {
            return handleRemoteInput(keyCode, m);
        } else if (state == STATE_LOADER_SELECT) {
            return handleLoaderInput(keyCode, m);
        } else {
            return handleBrowseInput(keyCode, m);
        }
    }

    private boolean handleBrowseInput(int keyCode, TUIManager m) {
        if (keyCode == 'i' || keyCode == 'I') {
            loadRemoteVersions(m);
            return true;
        }
        if (keyCode == 'r' || keyCode == 'R') {
            versions = m.listVersions();
            m.setStatus("\u5df2\u5237\u65b0, " + versions.size() + " \u4e2a\u7248\u672c");
            return true;
        }
        if (keyCode == 'd' || keyCode == 'D') {
            doDelete(m);
            return true;
        }
        if (versions == null || versions.isEmpty()) return false;
        if (keyCode == KEY_UP && selectedIndex > 0) { selectedIndex--; m.setStatus("\u7248\u672c: " + versions.get(selectedIndex)); return true; }
        if (keyCode == KEY_DOWN && selectedIndex < versions.size() - 1) { selectedIndex++; m.setStatus("\u7248\u672c: " + versions.get(selectedIndex)); return true; }
        if (keyCode == KEY_ENTER) {
            m.getConfig().put("selectedVersion", versions.get(selectedIndex));
            m.saveConfig(m.getConfig());
            m.setStatus("\u5df2\u9009\u62e9: " + versions.get(selectedIndex));
            return true;
        }
        return false;
    }

    private boolean handleRemoteInput(int keyCode, TUIManager m) {
        if (keyCode == KEY_ESC) {
            remoteVersions = null;
            filteredVersions = null;
            state = STATE_BROWSE;
            m.setStatus("\u5df2\u53d6\u6d88\u5b89\u88c5");
            return true;
        }
        if (keyCode == KEY_TAB) {
            remoteTab = (remoteTab + 1) % TAB_NAMES.length;
            applyFilter();
            remoteSelectedIndex = 0;
            remoteScrollOffset = 0;
            m.setStatus("\u6807\u7b7e: " + TAB_NAMES[remoteTab]);
            return true;
        }
        if (keyCode == KEY_BACKTAB) {
            remoteTab = (remoteTab - 1 + TAB_NAMES.length) % TAB_NAMES.length;
            applyFilter();
            remoteSelectedIndex = 0;
            remoteScrollOffset = 0;
            m.setStatus("\u6807\u7b7e: " + TAB_NAMES[remoteTab]);
            return true;
        }
        if (keyCode == KEY_LEFT) {
            remoteTab = (remoteTab - 1 + TAB_NAMES.length) % TAB_NAMES.length;
            applyFilter();
            remoteSelectedIndex = 0;
            remoteScrollOffset = 0;
            m.setStatus("\u6807\u7b7e: " + TAB_NAMES[remoteTab]);
            return true;
        }
        if (keyCode == KEY_RIGHT) {
            remoteTab = (remoteTab + 1) % TAB_NAMES.length;
            applyFilter();
            remoteSelectedIndex = 0;
            remoteScrollOffset = 0;
            m.setStatus("\u6807\u7b7e: " + TAB_NAMES[remoteTab]);
            return true;
        }
        if (filteredVersions == null || filteredVersions.isEmpty()) return false;
        if (keyCode == KEY_UP && remoteSelectedIndex > 0) { remoteSelectedIndex--; m.setStatus(filteredVersions.get(remoteSelectedIndex).id); return true; }
        if (keyCode == KEY_DOWN && remoteSelectedIndex < filteredVersions.size() - 1) { remoteSelectedIndex++; m.setStatus(filteredVersions.get(remoteSelectedIndex).id); return true; }
        if (keyCode == KEY_ENTER) {
            if (remoteSelectedIndex >= 0 && remoteSelectedIndex < filteredVersions.size()) {
                selectedRemoteVersion = filteredVersions.get(remoteSelectedIndex).id;
                state = STATE_LOADER_SELECT;
                loaderSelectedIndex = 0;
                loaderVersion = "";
                m.setStatus("\u7248\u672c: " + selectedRemoteVersion + " - \u9009\u62e9\u6a21\u7ec4\u88c5\u8f7d\u5668");
            }
            return true;
        }
        return false;
    }

    private boolean handleLoaderInput(int keyCode, TUIManager m) {
        if (keyCode == KEY_ESC) {
            state = STATE_BROWSE_REMOTE;
            m.setStatus("\u5df2\u53d6\u6d88");
            return true;
        }
        if (keyCode == KEY_TAB || keyCode == KEY_BACKTAB) return true;
        if (keyCode == KEY_UP && loaderSelectedIndex > 0) { loaderSelectedIndex--; m.setStatus(LOADER_NAMES[loaderSelectedIndex]); return true; }
        if (keyCode == KEY_DOWN && loaderSelectedIndex < LOADER_NAMES.length - 1) { loaderSelectedIndex++; m.setStatus(LOADER_NAMES[loaderSelectedIndex]); return true; }
        if (keyCode == 'v' || keyCode == 'V') {
            if (loaderSelectedIndex > 0) {
                loaderVersion = m.prompt("\u8f93\u5165 " + LOADER_NAMES[loaderSelectedIndex] + " \u7248\u672c (\u56de\u8f66\u4e3a\u81ea\u52a8\u9009\u62e9): ");
                m.setStatus("\u7248\u672c: " + (loaderVersion.isEmpty() ? "\u81ea\u52a8" : loaderVersion));
            }
            return true;
        }
        if (keyCode == KEY_ENTER) {
            doInstall(m);
            return true;
        }
        return false;
    }

    private void loadRemoteVersions(TUIManager m) {
        m.setStatus("\u6b63\u5728\u52a0\u8f7d\u7248\u672c\u5217\u8868...");
        try {
            File versionsFile = Utils.downloadVersionsFile();
            JSONArray arr = new JSONObject(FileUtils.readFileContent(versionsFile)).optJSONArray("versions");
            remoteVersions = new ArrayList<>();
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject jo = arr.optJSONObject(i);
                    if (jo != null) {
                        remoteVersions.add(new RemoteVersion(
                            jo.optString("id"),
                            jo.optString("type"),
                            jo.optString("releaseTime"),
                            jo.optString("url")
                        ));
                    }
                }
            }
            remoteTab = TAB_RELEASE;
            applyFilter();
            remoteSelectedIndex = 0;
            remoteScrollOffset = 0;
            state = STATE_BROWSE_REMOTE;
            m.setStatus("\u53ef\u5b89\u88c5\u7248\u672c: " + filteredVersions.size() + " \u4e2a  Tab\u5207\u6362\u6807\u7b7e");
        } catch (Exception e) {
            m.setStatus("\u52a0\u8f7d\u5931\u8d25: " + e.getMessage());
        }
    }

    private void applyFilter() {
        if (remoteVersions == null) { filteredVersions = null; return; }
        filteredVersions = new ArrayList<>();
        for (RemoteVersion rv : remoteVersions) {
            switch (remoteTab) {
                case TAB_RELEASE:
                    if ("release".equals(rv.type)) {
                        filteredVersions.add(rv);
                    }
                    break;
                case TAB_PRERELEASE:
                    if ("snapshot".equals(rv.type) || "old_alpha".equals(rv.type) || "old_beta".equals(rv.type) || APRIL_FOOLS.contains(rv.id)) {
                        filteredVersions.add(rv);
                    }
                    break;
                case TAB_OLD:
                    if (("old_alpha".equals(rv.type) || "old_beta".equals(rv.type)) && !APRIL_FOOLS.contains(rv.id)) {
                        filteredVersions.add(rv);
                    }
                    break;
            }
        }
    }

    private void doInstall(TUIManager m) {
        if (selectedRemoteVersion == null || selectedRemoteVersion.isEmpty()) {
            m.setStatus("\u672a\u9009\u62e9\u7248\u672c");
            return;
        }

        m.setStatus("\u6b63\u5728\u5b89\u88c5 " + selectedRemoteVersion + " ...");

        String loaderArg = LOADER_ARGS[loaderSelectedIndex];
        if (loaderSelectedIndex > 0 && !loaderVersion.isEmpty()) {
            loaderArg = "--" + LOADER_NAMES[loaderSelectedIndex].toLowerCase() + "=" + loaderVersion;
        }

        String finalLoaderArg = loaderArg;
        m.runTask(() -> {
            try {
                String[] cmdArgs;
                if (finalLoaderArg.isEmpty()) {
                    cmdArgs = new String[]{selectedRemoteVersion, "--select"};
                } else {
                    cmdArgs = new String[]{selectedRemoteVersion, finalLoaderArg, "--select"};
                }
                new InstallFunction().execute(new Arguments(cmdArgs, true));
                System.out.println("\n\u5b89\u88c5\u5b8c\u6210! \u6309 Enter \u8fd4\u56de TUI...");
            } catch (Exception e) {
                System.out.println("\n\u5b89\u88c5\u5931\u8d25: " + e.getMessage());
                System.out.println("\n\u6309 Enter \u8fd4\u56de TUI...");
            }
            try { System.in.read(); } catch (Exception ignored) {}
        });

        versions = m.listVersions();
        state = STATE_BROWSE;
        m.setStatus("\u5b89\u88c5\u5b8c\u6210");
    }

    private void doDelete(TUIManager m) {
        if (versions == null || versions.isEmpty() || selectedIndex < 0 || selectedIndex >= versions.size()) {
            m.setStatus("\u6ca1\u6709\u53ef\u5220\u9664\u7684\u7248\u672c");
            return;
        }

        String v = versions.get(selectedIndex);
        String confirm = m.prompt("\u786e\u5b9a\u5220\u9664 " + v + "? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            m.setStatus("\u5df2\u53d6\u6d88\u5220\u9664");
            return;
        }

        File versionDir = new File(m.getVersionsDir(), v);
        if (versionDir.exists()) {
            com.mrshiehx.cmcl.utils.FileUtils.deleteDirectory(versionDir);
            m.setStatus("\u5df2\u5220\u9664: " + v);
        } else {
            m.setStatus("\u7248\u672c\u76ee\u5f55\u4e0d\u5b58\u5728: " + v);
        }
        versions = m.listVersions();
    }

    @Override
    public void onSelected(TUIManager m) {
        state = STATE_BROWSE;
        versions = m.listVersions();
        if (!versions.isEmpty()) {
            int idx = versions.indexOf(m.getSelectedVersion());
            if (idx >= 0) selectedIndex = idx;
        }
        m.setStatus("\u7248\u672c\u7ba1\u7406 - " + (versions != null ? versions.size() : 0) + " \u4e2a\u7248\u672c  I:\u5b89\u88c5  R:\u5237\u65b0");
    }

    @Override
    public void onDeselected() {}

    private void title(StringBuilder sb, int cx, int y, String text, int color) {
        sb.append(pos(y, cx + 1)).append(BOLD).append(fgBright(color)).append("\u25B6 ").append(text).append(RESET);
    }
    private void line(StringBuilder sb, int cx, int y, int color, String text) {
        sb.append(pos(y, cx + 1)).append(fg(color)).append(text).append(RESET);
    }
    private void info(StringBuilder sb, int cx, int y, String label, String value, int color) {
        sb.append(pos(y, cx + 2)).append(fg(WHITE)).append(label).append(": ").append(fg(color)).append(value).append(RESET);
    }

    private static class RemoteVersion {
        final String id;
        final String type;
        final String releaseTime;
        final String url;

        RemoteVersion(String id, String type, String releaseTime, String url) {
            this.id = id;
            this.type = type;
            this.releaseTime = releaseTime;
            this.url = url;
        }
    }
}
