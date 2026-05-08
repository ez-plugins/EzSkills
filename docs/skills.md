---
layout: default
title: Skills
parent: Server Owners
nav_order: 4
has_children: true
---

# Skills
{: .no_toc }

EzSkills ships with five built-in skills. Click a skill to view its individual XP curve and configuration.

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Built-in skills

| Skill | Enum | GUI icon | Ability |
|-------|------|----------|---------|
| [Woodcutting](skills/woodcutting) | `WOODCUTTING` | Oak Log | [Lumberjack](abilities/lumberjack) |
| [Mining](skills/mining) | `MINING` | Diamond Pickaxe | [Spelunker](abilities/spelunker) |
| [Fishing](skills/fishing) | `FISHING` | Cod | [Angler](abilities/angler) |
| [Fighting](skills/fighting) | `FIGHTING` | Iron Sword | [Warrior](abilities/warrior) |
| [Acrobatics](skills/acrobatics) | `ACROBATICS` | Feather | --- |

---

## XP formula

XP required to advance from level *n* to *n+1* follows an exponential curve:

$$\text{xp}(n) = \text{base} \times \text{multiplier}^{n-1}$$

`xp-base` and `xp-multiplier` are configured independently per skill in `skills.yml`. See each skill page for its default values and a worked level table.

---

## Awarding XP

**In-game command:**

```
/ezskills addxp <player> <skill> <amount>
```

**Via integration:** see [Developer Docs](../developer) for API-based XP grants that can hook into Bukkit events (e.g. award Woodcutting XP when a player breaks a log).

---

## Player GUI

Players open `/skills` to see a 27-slot inventory overview. Each skill card shows:

- Current level
- Current XP / XP to next level
- A 12-character Unicode progress bar (`█░`)
- XP remaining to next level-up

The GUI title is set in `config.yml` → `gui.title`.

---

## Level-up notifications

When a player levels up, EzSkills can display a boss bar via [EzCountdown](https://github.com/ez-plugins/EzCountdown). Configure the message in `config.yml`:

```yaml
notifications:
  bossbar:
    enabled: true
    duration: 5
    message: "&6⬆ {player} reached {skill} Level {level}!"
```

| Placeholder | Value |
|-------------|-------|
| `{player}` | Player name |
| `{skill}` | Skill name |
| `{level}` | New level |
