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

# Per-plugin XP overrides - see Plugin Overrides section below
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

Plugins not listed in `plugin-overrides` are unaffected; they always award the exact amount they pass to the API. This feature only takes effect when third-party plugins use `EzSkillsAPI.addExperience(plugin, ...)`.

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
  max-level:     100     # Maximum attainable level

mining:
  xp-base:       100.0
  xp-multiplier: 1.5
  max-level:     100

fishing:
  xp-base:       80.0
  xp-multiplier: 1.4
  max-level:     100

fighting:
  xp-base:       120.0
  xp-multiplier: 1.6
  max-level:     100

acrobatics:
  xp-base:       80.0
  xp-multiplier: 1.4
  max-level:     100
```

| Key | Type | Description |
|-----|------|-------------|
| `xp-base` | `double` | XP required for level 1 → 2. |
| `xp-multiplier` | `double` | Multiplier applied each subsequent level. |
| `max-level` | `int` | Maximum attainable level. Defaults to `100` if omitted. |

---

## abilities.yml

Configures per-skill ability timing.

```yaml
tree_feller:
  preparation-window-seconds: 3.0    # Seconds the ability stays "prepared" after trigger
  duration-ticks:             100    # Ticks the ability remains active (20 ticks = 1 s)
  cooldown-seconds:           120.0  # Cooldown in seconds after the ability expires

spelunker:
  preparation-window-seconds: 3.0
  duration-ticks:             100
  cooldown-seconds:           120.0

angler:
  preparation-window-seconds: 3.0
  duration-ticks:             100
  cooldown-seconds:           120.0

warrior:
  preparation-window-seconds: 3.0
  duration-ticks:             100
  cooldown-seconds:           120.0
```

| Key | Type | Description |
|-----|------|-------------|
| `preparation-window-seconds` | `double` | Window (seconds) in which the player can trigger the ability. |
| `duration-ticks` | `int` | Duration (ticks) the ability stays active once triggered (20 ticks = 1 s). |
| `cooldown-seconds` | `double` | Cooldown (seconds) before the ability can be prepared again. |

---

## storage.yml

See the [Storage](storage.md) page for the full reference.
