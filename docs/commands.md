---
layout: default
title: Commands
parent: Server Owners
nav_order: 6
---

# Commands
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Player command

### `/skills`

Opens the skill progress overview GUI.

**Permission:** none (available to all players by default)

### `/abilities`

Opens the ability overview GUI, showing the current state (Ready / Preparing / Active) of every ability registered on the server.

**Permission:** `ezskills.use`

---

## Admin command

All admin subcommands use `/ezskills` and require the `ezskills.admin` permission.

### Inspection

| Command | Description |
|---------|-------------|
| `/ezskills get <player> <skill>` | Prints the level and XP of one skill for a player. |
| `/ezskills info <player>` | Prints a full skill overview. Supports offline players; loads from storage asynchronously. |

### Mutation

| Command | Description |
|---------|-------------|
| `/ezskills addxp <player> <skill> <amount>` | Adds the given amount of XP to a skill. Fires level-up events if thresholds are crossed. |
| `/ezskills setlevel <player> <skill> <level>` | Sets the skill level directly, resetting XP to zero. |

### Moderation

| Command | Description |
|---------|-------------|
| `/ezskills reset <player> <skill>` | Resets one skill back to level 1 with zero XP. |
| `/ezskills resetall <player>` | Resets **all** skills to level 1 with zero XP in a single save operation. |
| `/ezskills top <skill> [limit]` | Shows the top `limit` players ranked by level (default 10, max 20). Requires MySQL storage. Falls back to online-only list when using YAML. |

### Abilities

| Command | Description |
|---------|-------------|
| `/ezskills ability <player> <ability>` | Force-activates an ability for an online player, bypassing the preparation state. |

### Utilities

| Command | Description |
|---------|-------------|
| `/ezskills reload` | Reloads all config files (`config.yml`, `skills.yml`, `abilities.yml`, `storage.yml`). |

---

## Argument reference

**`<skill>`**: any of: `WOODCUTTING`, `MINING`, `FISHING`, `FIGHTING` (case-insensitive)

**`<ability>`**: any of: `LUMBERJACK`, `SPELUNKER`, `ANGLER`, `WARRIOR` (case-insensitive)

**`<player>`**: player name (online or offline)

---

## Tab completion

All arguments support tab completion, including offline-player names for `info`, `reset`, and `resetall`.
