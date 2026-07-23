# CMCL - Console Minecraft Launcher

终端里的 Minecraft Java 版启动器，带 TUI 界面

## 特性

- **TUI 图形界面** - 终端内交互式界面，键盘操作
- **CLI 命令行** - 完整命令行功能，适合脚本集成
- **版本管理** - 浏览/安装/删除 Minecraft 版本（正式版/快照版/远古版/愚人节版）
- **模组加载器** - 支持 Fabric、Forge、Quilt、LiteLoader、OptiFine
- **多账户** - 离线登录 / 微软登录 / 外置登录（含 LittleSkin 预设）
- **下载镜像** - 官方源 / BMCLAPI 镜像源
- **模组搜索** - CurseForge、Modrinth 模组搜索与下载
- **整合包安装** - CurseForge、Modrinth、MCBBS、MultiMC 整合包

## 安装

### 前提条件

- Java 8 或更高版本

### 下载

从 [Releases](https://github.com/kinich/console-minecraft-launcher/releases) 下载最新版本：

- `cmcl.jar` - 通用 JAR 包
- `cmcl` - Linux/macOS 可执行文件
- `cmcl.exe` - Windows 可执行文件

### 运行

```shell
java -jar cmcl.jar
```

或直接执行可执行文件（Linux/macOS）：

```shell
./cmcl
```

## 快速开始

### TUI 模式

直接运行启动器进入 TUI 界面：

```shell
cmcl
```

| 按键 | 功能 |
|------|------|
| `Tab` | 切换页面 |
| `↑ ↓` | 列表导航 |
| `Enter` | 确认选择 |
| `I` | 安装新版本（版本管理页） |
| `L` | 登录账户（账户管理页） |
| `1-5` / `F1-F5` | 直接切换页面 |

### CLI 模式

```shell
# 安装版本
cmcl install 1.21 --select

# 安装版本 + Forge
cmcl install 1.20.1 --forge --select

# 列出可安装版本
cmcl install --show=release

# 启动游戏
cmcl 1.21

# 选择版本
cmcl -s 1.21
```

## 构建

```shell
git clone https://github.com/kinich/console-minecraft-launcher.git
cd console-minecraft-launcher
./gradlew jar
```

构建产物在 `build/libs/` 目录下。

## 配置

配置文件 `cmcl.json` 位于程序运行目录（Linux/macOS：`~/.config/cmcl/cmcl.json`）。

常用配置项：

| 配置名 | 类型 | 说明 |
|--------|------|------|
| `downloadSource` | 整数 | 下载源：0=官方，1=BMCLAPI |
| `maxMemory` | 整数 | 最大内存（MB） |
| `javaPath` | 文本 | Java 路径 |
| `gameDir` | 文本 | 游戏目录 |
| `language` | 文本 | 语言：zh/en/cantonese |
| `selectedVersion` | 文本 | 当前选择的版本 |

## 免责声明

Minecraft 版权归 Mojang Studios 与 Microsoft 所有。本启动器与 Mojang Studios、Microsoft 无任何关联。

## 许可证

[GPL v3](LICENSE)
