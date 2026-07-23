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

import com.mrshiehx.cmcl.modules.account.authentication.microsoft.MicrosoftAuthentication;
import com.mrshiehx.cmcl.tui.TUIManager;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.mrshiehx.cmcl.tui.Ansi.*;

public class AccountPage implements Page {
    private int selectedIndex = 0;
    private JSONArray accounts;
    private int scrollOffset = 0;

    private static final String LITTLESKIN_URL = "https://littleskin.cn/api/yggdrasil";
    private static final String[][] EXTERNAL_PRESETS = {
        {"LittleSkin", LITTLESKIN_URL},
        {"Blessing Skin", "https://skin.prinzeugen.net/api/yggdrasil"},
        {"Ely.By", "https://authserver.ely.by/api/yggdrasil"},
        {"自定地址", ""}
    };

    @Override
    public String getTitle() { return "账户管理"; }

    @Override
    public void draw(StringBuilder sb, int cx, int cy, int w, int h, TUIManager m) {
        int y = cy;
        title(sb, cx, y++, "账户管理", YELLOW);
        y++;

        accounts = m.getAccounts();

        if (accounts == null || accounts.length() == 0) {
            line(sb, cx, y++, YELLOW, "\u26A0 未保存任何账户");
            line(sb, cx, y++, WHITE, "按 L 键登录 (1=离线 2=微软 3=外置登录)");
            return;
        }

        line(sb, cx, y++, WHITE_BRIGHT, "已保存账户 (\u2191\u2193选择 Enter确认 L登录)");
        y++;

        int listH = Math.min(h - 14, accounts.length());
        if (selectedIndex >= accounts.length()) selectedIndex = accounts.length() - 1;
        if (selectedIndex < scrollOffset) scrollOffset = selectedIndex;
        if (selectedIndex >= scrollOffset + listH) scrollOffset = selectedIndex - listH + 1;

        for (int i = scrollOffset; i < accounts.length() && i < scrollOffset + listH; i++) {
            JSONObject a = accounts.optJSONObject(i);
            if (a == null) continue;
            String name = a.optString("playerName", "?");
            boolean sel = a.optBoolean("selected", false);
            boolean cur = (i == selectedIndex);
            String method = methodName(a);
            int fg = cur ? BLACK : (sel ? GREEN : WHITE);
            int bg = cur ? YELLOW : BLACK;
            String prefix = cur ? "\u25B6 " : "  ";
            String marker = sel ? " \u2605" : "";
            if (cur) { sb.append(pos(y, cx + 1)).append(fgBg(fg, bg)).append(prefix).append(name).append(marker).append(" (").append(method).append(")").append(RESET); }
            else { sb.append(pos(y, cx + 1)).append(fg(fg)).append(prefix).append(name).append(marker).append(" (").append(method).append(")").append(RESET); }
            y++;
        }

        y = cy + h - 9;
        if (y < cy + 4 + listH) y = cy + 4 + listH + 1;
        if (y >= cy + h - 2) return;

        if (selectedIndex >= 0 && selectedIndex < accounts.length()) {
            JSONObject a = accounts.optJSONObject(selectedIndex);
            if (a != null) {
                line(sb, cx, y++, YELLOW, "\u2500".repeat(w));
                line(sb, cx, y++, YELLOW_BRIGHT, "账户详情");
                line(sb, cx, y++, YELLOW, "\u2500".repeat(w));
                y++;
                info(sb, cx, y++, "名称", a.optString("playerName", "?"), CYAN);
                info(sb, cx, y++, "UUID", a.optString("uuid", "?"), CYAN);
                info(sb, cx, y++, "方式", methodName(a), CYAN);
                String server = a.optString("server", "");
                if (!server.isEmpty()) {
                    info(sb, cx, y++, "服务器", server, CYAN);
                }
            }
        }
    }

    @Override
    public boolean handleInput(int keyCode, TUIManager m) {
        if (keyCode == 'l' || keyCode == 'L') {
            doLogin(m);
            return true;
        }
        if (accounts == null || accounts.length() == 0) return false;
        if (keyCode == KEY_UP && selectedIndex > 0) { selectedIndex--; m.setStatus("\u8d26\u6237: " + selectedIndex); return true; }
        if (keyCode == KEY_DOWN && selectedIndex < accounts.length() - 1) { selectedIndex++; m.setStatus("\u8d26\u6237: " + selectedIndex); return true; }
        if (keyCode == KEY_ENTER) {
            JSONObject a = accounts.optJSONObject(selectedIndex);
            if (a != null) {
                for (int i = 0; i < accounts.length(); i++) {
                    JSONObject ac = accounts.optJSONObject(i);
                    if (ac != null) ac.put("selected", i == selectedIndex);
                }
                m.getConfig().put("accounts", accounts);
                m.saveConfig(m.getConfig());
                m.setStatus("\u5df2\u9009\u62e9: " + a.optString("playerName", "?"));
            }
            return true;
        }
        return false;
    }

    private void doLogin(TUIManager m) {
        String choice = m.prompt("\u9009\u62e9\u767b\u5f55\u65b9\u5f0f (1=\u79bb\u7ebf 2=\u5fae\u8f6f 3=\u5916\u7f6e\u767b\u5f55): ");
        if (choice.isEmpty()) { m.setStatus("\u5df2\u53d6\u6d88\u767b\u5f55"); return; }

        if ("1".equals(choice)) {
            doOfflineLogin(m);
        }
        else if ("2".equals(choice)) {
            doMicrosoftLogin(m);
        }
        else if ("3".equals(choice)) {
            doExternalLogin(m);
        }

        accounts = m.getAccounts();
    }

    private void doOfflineLogin(TUIManager m) {
        String name = m.prompt("\u8f93\u5165\u73a9\u5bb6\u540d\u79f0: ");
        if (name.isEmpty()) { m.setStatus("\u5df2\u53d6\u6d88"); return; }

        JSONObject account = new JSONObject();
        account.put("playerName", name);
        account.put("selected", true);
        account.put("loginMethod", 0);
        addAccount(m, account);
        m.setStatus("\u5df2\u767b\u5f55\u79bb\u7ebf\u8d26\u6237: " + name);
    }

    private void doMicrosoftLogin(TUIManager m) {
        m.setStatus("\u6b63\u5728\u6253\u5f00\u6d4f\u89c8\u5668\u8fdb\u884c\u5fae\u8f6f\u767b\u5f55...");
        m.runTask(() -> {
            try {
                JSONObject account = MicrosoftAuthentication.loginMicrosoftAccount();
                if (account != null) {
                    addAccount(m, account);
                    System.out.println("\n\u767b\u5f55\u6210\u529f! \u6309 Enter \u8fd4\u56de TUI...");
                } else {
                    System.out.println("\n\u767b\u5f55\u53d6\u6d88\u6216\u5931\u8d25. \u6309 Enter \u8fd4\u56de TUI...");
                }
            } catch (Exception e) {
                System.out.println("\n\u767b\u5f55\u5931\u8d25: " + e.getMessage() + ". \u6309 Enter \u8fd4\u56de TUI...");
            }
            try { System.in.read(); } catch (Exception ignored) {}
        });
    }

    private void doExternalLogin(TUIManager m) {
        String presetChoice = m.prompt("\u9009\u62e9\u5916\u7f6e\u767b\u5f55\u670d\u52a1\u5668 (1=LittleSkin 2=Blessing Skin 3=Ely.By 4=\u81ea\u5b9a\u4e49): ");
        if (presetChoice.isEmpty()) { m.setStatus("\u5df2\u53d6\u6d88"); return; }

        int idx;
        try {
            idx = Integer.parseInt(presetChoice) - 1;
        } catch (NumberFormatException e) {
            m.setStatus("\u65e0\u6548\u9009\u62e9");
            return;
        }
        if (idx < 0 || idx >= EXTERNAL_PRESETS.length) {
            m.setStatus("\u65e0\u6548\u9009\u62e9");
            return;
        }

        String address;
        if (idx == EXTERNAL_PRESETS.length - 1) {
            address = m.prompt("\u8f93\u5165\u5916\u7f6e\u767b\u5f55\u670d\u52a1\u5668\u5730\u5740: ");
        } else {
            address = EXTERNAL_PRESETS[idx][1];
            m.setStatus("\u5df2\u9009\u62e9: " + EXTERNAL_PRESETS[idx][0] + " (" + address + ")");
        }
        if (address.isEmpty()) { m.setStatus("\u5df2\u53d6\u6d88"); return; }

        String username = m.prompt("\u8f93\u5165\u7528\u6237\u540d: ");
        if (username.isEmpty()) { m.setStatus("\u5df2\u53d6\u6d88"); return; }

        String finalAddress = address;
        String finalUsername = username;
        m.setStatus("\u6b63\u5728\u767b\u5f55 " + finalAddress + " ...");
        m.runTask(() -> {
            try {
                JSONObject account = com.mrshiehx.cmcl.modules.account.authentication.yggdrasil.authlib.AuthlibInjectorAuthentication.authlibInjectorLogin(finalAddress, finalUsername, true);
                if (account != null) {
                    account.put("server", finalAddress);
                    addAccount(m, account);
                    System.out.println("\n\u767b\u5f55\u6210\u529f! \u6309 Enter \u8fd4\u56de TUI...");
                } else {
                    System.out.println("\n\u767b\u5f55\u5931\u8d25. \u6309 Enter \u8fd4\u56de TUI...");
                }
            } catch (Exception e) {
                System.out.println("\n\u767b\u5f55\u5931\u8d25: " + e.getMessage() + ". \u6309 Enter \u8fd4\u56de TUI...");
            }
            try { System.in.read(); } catch (Exception ignored) {}
        });
    }

    private void addAccount(TUIManager m, JSONObject account) {
        JSONObject config = m.getConfig();
        JSONArray accs = config.optJSONArray("accounts");
        if (accs == null) accs = new JSONArray();
        account.put("selected", true);
        for (int i = 0; i < accs.length(); i++) {
            JSONObject a = accs.optJSONObject(i);
            if (a != null) a.put("selected", false);
        }
        accs.put(account);
        config.put("accounts", accs);
        m.saveConfig(config);
    }

    private String methodName(JSONObject a) {
        int lm = a.optInt("loginMethod", -1);
        switch (lm) {
            case 0: return "离线";
            case 1: return "外置登录";
            case 2: return "微软";
            case 3: return "Nide8Auth";
            default: return "未知";
        }
    }

    @Override
    public void onSelected(TUIManager m) {
        accounts = m.getAccounts();
        if (accounts != null && accounts.length() > 0) {
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject a = accounts.optJSONObject(i);
                if (a != null && a.optBoolean("selected", false)) { selectedIndex = i; break; }
            }
        }
        m.setStatus("账户管理 - " + (accounts != null ? accounts.length() : 0) + " 个账户  L:登录");
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
}
