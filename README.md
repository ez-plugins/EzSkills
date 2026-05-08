# EzSkills

A lightweight, extensible skill progression system for Minecraft servers running Spigot 26.1+.

[![JitPack](https://jitpack.io/v/ez-plugins/EzSkills.svg)](https://jitpack.io/#ez-plugins/EzSkills)
[![GitHub release](https://img.shields.io/github/release/ez-plugins/EzSkills.svg)](https://github.com/ez-plugins/EzSkills/releases)
[![Modrinth](https://img.shields.io/modrinth/v/AwpXN7HO?label=Modrinth)](https://modrinth.com/plugin/AwpXN7HO)
[![Codecov](https://img.shields.io/codecov/c/github/ez-plugins/EzSkills)](https://codecov.io/gh/ez-plugins/EzSkills)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)

## Features

- Five built-in skills: **Woodcutting**, **Mining**, **Fishing**, **Fighting**, and **Acrobatics**
- **Custom skills API** — register your own skills at runtime via `SkillDefinition` and `EzSkillsAPI.registerSkill()`
- Asynchronous, non-blocking storage backed by [Jaloquent](https://github.com/EzFramework/Jaloquent)
- YAML flat-file storage out of the box; MySQL with a config switch
- Clean public API module (`ezskills-api`) for other plugins to integrate

## Requirements

- Java 25
- Spigot / Paper 26.1+

## Installation

Download the latest release from the [Releases page](https://github.com/ez-plugins/EzSkills/releases)
and drop the jar into your server's `plugins/` folder.

## API

### Maven - GitHub Packages

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/ez-plugins/EzSkills</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ez-plugins.EzSkills</groupId>
    <artifactId>ezskills-api</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Maven - JitPack

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ez-plugins.EzSkills</groupId>
    <artifactId>ezskills-api</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Quick start

```java
import com.github.ezplugins.ezskills.api.EzSkillsAPI;

// Get a player's woodcutting level
int level = EzSkillsAPI.getSkillLevel(player, "WOODCUTTING");

// Add experience
EzSkillsAPI.addExperience(player.getUniqueId(), "MINING", 25.0);
```

See the [docs](docs/README.md) for full documentation.

## Storage

| Backend | Description |
|---------|-------------|
| `yaml`  | Default. Stores everything in `plugins/EzSkills/players.yml`. |
| `mysql` | High-performance MySQL/MariaDB backend. Configure in `storage.yml`. |

## Contributing

Pull requests are welcome. Please run `mvn verify` before submitting.

## License

[MIT](LICENSE.md)
