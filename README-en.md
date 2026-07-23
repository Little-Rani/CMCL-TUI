# CMCL - Console Minecraft Launcher

A Minecraft Java Edition launcher for the terminal, with TUI interface.

## Features

- **TUI Interface** - Interactive terminal UI with keyboard navigation
- **CLI Commands** - Full command-line interface for scripting
- **Version Management** - Browse/install/delete Minecraft versions (release/snapshot/old/april fools)
- **Mod Loaders** - Fabric, Forge, Quilt, LiteLoader, OptiFine support
- **Multi-Account** - Offline / Microsoft / External login (with LittleSkin preset)
- **Download Mirrors** - Official source / BMCLAPI mirror
- **Mod Search** - CurseForge, Modrinth mod search and download
- **Modpack Install** - CurseForge, Modrinth, MCBBS, MultiMC modpacks

## Installation

### Prerequisites

- Java 8 or higher

### Download

Get the latest release from [Releases](https://github.com/Little-Rani/CMCL-TUI):

- `cmcl.jar` - Universal JAR
- `cmcl` - Linux/macOS executable
- `cmcl.exe` - Windows executable

### Run

```shell
java -jar cmcl.jar
```

Or run the executable directly (Linux/macOS):

```shell
./cmcl
```

## Quick Start

### TUI Mode

Run the launcher without arguments to enter TUI mode:

```shell
cmcl
```

| Key | Action |
|-----|--------|
| `Tab` | Switch page |
| `↑ ↓` | Navigate list |
| `Enter` | Confirm |
| `I` | Install version (Version page) |
| `L` | Login account (Account page) |
| `1-5` / `F1-F5` | Jump to page |

### CLI Mode

```shell
# Install a version
cmcl install 1.21 --select

# Install version + Forge
cmcl install 1.20.1 --forge --select

# List available versions
cmcl install --show=release

# Launch game
cmcl 1.21

# Select version
cmcl -s 1.21
```

## Build

```shell
git clone https://github.com/Little-Rani/CMCL-TUI
cd CMCL-TUI
./gradlew jar
```

Output is in `build/libs/`.

## Configuration

Configuration file `cmcl.json` is located in the working directory (Linux/macOS: `~/.config/cmcl/cmcl.json`).

Common options:

| Name | Type | Description |
|------|------|-------------|
| `downloadSource` | Integer | Download source: 0=Official, 1=BMCLAPI |
| `maxMemory` | Integer | Max memory (MB) |
| `javaPath` | Text | Java executable path |
| `gameDir` | Text | Game directory |
| `language` | Text | Language: zh/en/cantonese |
| `selectedVersion` | Text | Currently selected version |

## Disclaimer

Minecraft is copyright Mojang Studios and Microsoft. This launcher is not affiliated with Mojang Studios or Microsoft.

## License

[GPL v3](LICENSE)
