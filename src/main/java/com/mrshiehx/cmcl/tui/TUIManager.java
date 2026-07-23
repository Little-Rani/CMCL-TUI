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

import com.mrshiehx.cmcl.CMCL;
import com.mrshiehx.cmcl.constants.Constants;
import com.mrshiehx.cmcl.functions.AccountFunction;
import com.mrshiehx.cmcl.functions.InstallFunction;
import com.mrshiehx.cmcl.utils.Utils;
import com.mrshiehx.cmcl.utils.cmcl.AccountUtils;
import com.mrshiehx.cmcl.utils.system.SystemUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TUIManager {
    private String statusMessage = "就绪";
    private Runnable exitCallback;
    private Function<String, String> inputProvider;
    private Consumer<Runnable> taskRunner;

    public void setExitCallback(Runnable exitCallback) { this.exitCallback = exitCallback; }
    public void setInputProvider(Function<String, String> provider) { this.inputProvider = provider; }
    public void setTaskRunner(Consumer<Runnable> runner) { this.taskRunner = runner; }

    public void exit() { if (exitCallback != null) exitCallback.run(); }

    public String prompt(String promptText) {
        if (inputProvider != null) return inputProvider.apply(promptText);
        return "";
    }

    public void runTask(Runnable task) {
        if (taskRunner != null) taskRunner.accept(task);
    }

    public void setStatus(String message) { this.statusMessage = message; }
    public String getStatus() { return statusMessage; }

    public JSONObject getConfig() { return CMCL.getConfig(); }
    public File getGameDir() { return CMCL.gameDir; }
    public File getVersionsDir() { return CMCL.versionsDir; }
    public String getJavaPath() { return CMCL.javaPath; }

    public List<String> listVersions() {
        File dir = CMCL.versionsDir;
        if (dir == null || !dir.exists()) return new ArrayList<>();
        File[] files = dir.listFiles(File::isDirectory);
        if (files == null) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (File f : files) {
            if (new File(f, f.getName() + ".json").exists()) result.add(f.getName());
        }
        return result;
    }

    public String getSelectedVersion() { return getConfig().optString("selectedVersion", ""); }

    public String getSelectedAccountName() {
        try {
            JSONObject a = AccountUtils.getSelectedAccount(getConfig(), false);
            if (a != null) return a.optString("playerName", "?");
        } catch (Exception ignored) {}
        return null;
    }

    public String getSelectedAccountType() {
        try {
            JSONObject a = AccountUtils.getSelectedAccount(getConfig(), false);
            if (a != null) {
                JSONObject li = a.optJSONObject("loginInformation");
                return li != null ? li.optString("method", "offline") : "offline";
            }
        } catch (Exception ignored) {}
        return null;
    }

    public JSONArray getAccounts() { return getConfig().optJSONArray("accounts"); }

    public int getMaxMemory() { return getConfig().optInt("maxMemory", (int) SystemUtils.getDefaultMemory()); }

    public boolean saveConfig(JSONObject config) {
        try { CMCL.saveConfig(config); return true; } catch (Exception e) { return false; }
    }

    public String getAppVersion() { return Constants.CMCL_VERSION_NAME; }

    public List<String> getModsForVersion(String version) {
        File modDir = new File(CMCL.gameDir, "versions/" + version + "/mods");
        if (!modDir.exists()) modDir = new File(CMCL.gameDir, "mods");
        if (!modDir.exists()) return new ArrayList<>();
        File[] files = modDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) return new ArrayList<>();
        List<String> mods = new ArrayList<>();
        for (File f : files) mods.add(f.getName());
        return mods;
    }

    public int getWindowWidth() { return getConfig().optInt("windowSizeWidth", 854); }
    public int getWindowHeight() { return getConfig().optInt("windowSizeHeight", 480); }
    public int getDownloadSource() { return getConfig().optInt("downloadSource", 0); }
    public String tr(String key) { return CMCL.getString(key); }
}
