---
layout: default
title: Getting Started
parent: Developer Docs
nav_order: 1
---

# Getting Started

## Adding EzSkills as a dependency

### JitPack

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
    <version>2.0.2</version>
    <scope>provided</scope>
</dependency>
```

### GitHub Packages

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
    <version>2.0.2</version>
    <scope>provided</scope>
</dependency>
```

## plugin.yml

Declare EzSkills as a soft or hard dependency:

```yaml
depend: [ EzSkills ]     # hard dependency
# or
softdepend: [ EzSkills ] # soft dependency
```

## First call

```java
import com.github.ezplugins.ezskills.api.EzSkillsAPI;

// In your command or event handler:
int level = EzSkillsAPI.getSkillLevel(player, "WOODCUTTING");
player.sendMessage("Your woodcutting level is " + level);
```

> All `EzSkillsAPI` methods throw `IllegalStateException` if EzSkills is not loaded.
> Guard with `Bukkit.getPluginManager().isPluginEnabled("EzSkills")` when using soft-depend.
