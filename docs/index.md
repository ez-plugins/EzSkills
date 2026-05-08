---
layout: home
title: EzSkills
nav_order: 1
description: "A modular, async skill system for Spigot/Paper servers."
permalink: /
---

# EzSkills

A modular, asynchronous skill system for **Spigot / Paper** servers.
Built on [Jaloquent](https://github.com/EzFramework/Jaloquent) and
[JavaQueryBuilder](https://github.com/EzFramework/JavaQueryBuilder) — **zero raw SQL** in the plugin itself.

---

## Features

- **Four built-in skills** — Woodcutting, Mining, Fishing, Fighting
- **Per-skill abilities** — configurable preparation windows, activation durations, and cooldowns
- **Async storage** — all reads and writes happen off the main thread via a dedicated worker
- **Dual storage backends** — YAML (flat file) or MySQL (HikariCP connection pool)
- **Rich player GUI** — 27-slot skill overview with Unicode progress bars
- **Admin commands** — `get`, `info`, `addxp`, `setlevel`, `reset`, `resetall`, `top`, `reload`
- **Developer API** — `EzSkillsAPI`, `SkillLevelUpEvent`, and more event hooks

---

## Quick start

1. Drop `EzSkills.jar` into your `plugins/` folder and restart the server.
2. Edit `plugins/EzSkills/config.yml` to customise the GUI title.
3. Edit `plugins/EzSkills/skills.yml` to tune XP curves per skill.
4. Edit `plugins/EzSkills/storage.yml` to switch to MySQL if desired.
5. Run `/skills` in-game to see the skill overview.

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Java        | 17+     |
| Spigot / Paper | 1.20+ |
| MySQL (optional) | 8.0+ |

---

{: .note }
EzSkills is distributed under the [MIT License](https://github.com/ez-plugins/EzSkills/blob/main/LICENSE).
