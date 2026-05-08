---
layout: default
title: API Reference
parent: Developer Docs
nav_order: 2
---

# API Reference
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Dependency setup

Add the `ezskills-api` artifact to your project — see [Installation](installation.md) for Maven and Gradle snippets.

Declare EzSkills as a (soft) dependency in `plugin.yml` so the API is available when your plugin loads:

```yaml
depend:
  - EzSkills
```

---

## EzSkillsAPI

The `EzSkillsAPI` class is a static facade that delegates to `EzSkillsService`, the interface implemented by the main plugin class.

### Obtaining the instance

```java
// Throws IllegalStateException if EzSkills is not loaded
EzSkillsService api = EzSkillsAPI.get();
```

### Reading skill data

```java
UUID id = player.getUniqueId();

int level = api.getSkillLevel(id, "WOODCUTTING");
double xp  = api.getSkillExperience(id, "MINING");
```

Both methods accept either a `UUID` or an `OfflinePlayer`.

### Modifying skill data

```java
// These operate on the in-memory cache and queue an async save.
// They are safe to call from the main thread.
api.addExperience(id, "FISHING", 50.0);
api.setSkillLevel(id, "FIGHTING", 10);
```

---

## Events

All events extend `org.bukkit.event.Event` and are fired on the **main thread**.

### SkillLevelUpEvent

Fired when a player's skill advances to a new level.

```java
import com.github.ezplugins.ezskills.api.event.SkillLevelUpEvent;

@EventHandler
public void onLevelUp(SkillLevelUpEvent event) {
    Player player   = event.getPlayer();
    SkillType skill = event.getSkillType();
    int oldLevel    = event.getOldLevel();
    int newLevel    = event.getNewLevel();
}
```

### EzSkillsAbilityPrepareEvent

Fired when an ability enters the *preparing* state.

```java
import com.github.ezplugins.ezskills.api.event.EzSkillsAbilityPrepareEvent;

@EventHandler
public void onPrepare(EzSkillsAbilityPrepareEvent event) {
    Player player       = event.getPlayer();
    AbilityType ability = event.getAbilityType();
    // event.setCancelled(true) to prevent preparation
}
```

### EzSkillsAbilityActivateEvent

Fired when an ability becomes *active*.

```java
import com.github.ezplugins.ezskills.api.event.EzSkillsAbilityActivateEvent;

@EventHandler
public void onActivate(EzSkillsAbilityActivateEvent event) {
    // Apply custom buff logic here
}
```

### EzSkillsAbilityDeactivateEvent

Fired when an ability leaves the *active* state (expired or cancelled).

```java
import com.github.ezplugins.ezskills.api.event.EzSkillsAbilityDeactivateEvent;

@EventHandler
public void onDeactivate(EzSkillsAbilityDeactivateEvent event) {
    // Remove custom buff logic here
}
```

---

## SkillType enum

```java
import com.github.ezplugins.ezskills.skill.SkillType;

SkillType type = SkillType.fromString("woodcutting"); // null-safe, case-insensitive
```

Constants: `WOODCUTTING`, `MINING`, `FISHING`, `FIGHTING`

---

## AbilityType enum

```java
import com.github.ezplugins.ezskills.ability.AbilityType;

AbilityType ability = AbilityType.fromString("lumberjack");
```

Constants: `LUMBERJACK`, `SPELUNKER`, `ANGLER`, `WARRIOR`

---

## Thread safety

- `EzSkillsAPI` methods that read or mutate skill data are safe to call from the **main thread**.
- All actual storage I/O happens on the dedicated `EzSkills-StorageWorker` thread.
- Do **not** call EzSkills API methods from asynchronous Bukkit events or other plugin worker threads without synchronising back to the main thread first.
