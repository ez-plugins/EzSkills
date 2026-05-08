---
layout: default
title: Configuration
parent: Server Owners
nav_order: 2
---

# Configuration
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## config.yml

General plugin settings.

```yaml
gui:
  title: "&6★ Skills"         # Title of the /skills inventory GUI
  abilities-title: "&6Abilities"  # Title of the /abilities inventory GUI

notifications:
  bossbar:
    enabled: true             # Requires EzCountdown to be installed
    duration: 5               # Seconds the boss-bar is shown
    message: "&6⬆ {player} reached {skill} Level {level}!"

# Per-plugin XP overrides — see Plugin Overrides section below
plugin-overrides: {}
```

---

## Plugin overrides

The `plugin-overrides` section in `config.yml` lets server admins control how much XP each third-party plugin is allowed to award per skill, without touching the plugin's own files.

```yaml
plugin-overrides:
  MyPlugin:                   # exact plugin name (case-sensitive)
    woodcutting:
      enabled: true
      xp-multiplier: 1.5      # award 50 % more Woodcutting XP from this plugin
    mining:
      enabled: false           # MyPlugin cannot award Mining XP on this server
    fishing:
      enabled: true
      xp-multiplier: 0.5      # halve Fishing XP from MyPlugin
  AnotherPlugin:
    fighting:
      enabled: true
      xp-multiplier: 2.0
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | `boolean` | `true` | When `false`, all XP calls from this plugin for this skill are silently ignored |
| `xp-multiplier` | `double` | `1.0` | Multiplied against the raw amount the plugin passes to `addExperience` |

Plugins not listed in `plugin-overrides` are unaffected — they always award the exact amount they pass to the API. This feature only takes effect when third-party plugins use `EzSkillsAPI.addExperience(plugin, ...)`.

{: .note }
After editing `config.yml` run `/ezskills reload` to apply changes without restarting the server.

---

## skills.yml

Controls the XP formula for each skill.
XP required to advance from level *n* to *n+1* is calculated as:

$$\text{xp} = \text{base} \times \text{multiplier}^{n-1}$$

```yaml
woodcutting:
  xp-base:       100.0   # XP needed to reach level 2
  xp-multiplier: 1.5     # Exponential growth factor

mining:
  xp-base:       100.0
  xp-multiplier: 1.5

fishing:
  xp-base:       80.0
  xp-multiplier: 1.4

fighting:
  xp-base:       120.0
  xp-multiplier: 1.6
```

| Key | Type | Description |
|-----|------|-------------|
| `xp-base` | `double` | XP required for level 1 → 2. |
| `xp-multiplier` | `double` | Multiplier applied each subsequent level. |

---

## abilities.yml

Configures per-skill ability timing.

```yaml
woodcutting:
  preparation-time: 30    # Seconds the ability stays "prepared" after trigger
  active-time:      15    # Seconds the ability remains active
  cooldown:         120   # Cooldown in seconds after the ability expires

mining:
  preparation-time: 30
  active-time:      15
  cooldown:         120

fishing:
  preparation-time: 30
  active-time:      15
  cooldown:         120

fighting:
  preparation-time: 30
  active-time:      15
  cooldown:         120
```

| Key | Type | Description |
|-----|------|-------------|
| `preparation-time` | `int` | Window (seconds) in which the player can trigger the ability. |
| `active-time` | `int` | Duration (seconds) the ability stays active once triggered. |
| `cooldown` | `int` | Cooldown (seconds) before the ability can be prepared again. |

---

## storage.yml

See the [Storage](storage.md) page for the full reference.
