---
layout: default
title: Abilities
parent: Server Owners
nav_order: 5
has_children: true
---

# Abilities
{: .no_toc }

Each skill has one associated ability. Click an ability for its individual timing and configuration.

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Built-in abilities

| Ability | Enum | Skill |
|---------|------|-------|
| [Lumberjack](abilities/lumberjack) | `LUMBERJACK` | [Woodcutting](skills/woodcutting) |
| [Spelunker](abilities/spelunker) | `SPELUNKER` | [Mining](skills/mining) |
| [Angler](abilities/angler) | `ANGLER` | [Fishing](skills/fishing) |
| [Warrior](abilities/warrior) | `WARRIOR` | [Fighting](skills/fighting) |

{: .note }
Abilities fire Bukkit events when they activate. The actual in-game effect is applied by your server's integration or companion plugin. See [Developer Docs](../developer) for details.

---

## Ability lifecycle

```
Inactive → Preparing → Active → Cooldown → Inactive
```

| State | Description |
|-------|-------------|
| **Inactive** | Ready; no cooldown active |
| **Preparing** | Charged: the player has `preparation-window-seconds` seconds to trigger activation |
| **Active** | Live for `duration-ticks` ticks; fires `EzSkillsAbilityActivateEvent` |
| **Cooldown** | Must wait `cooldown-seconds` seconds before preparing again |

---

## Timing configuration

Each ability is configured in `abilities.yml`:

```yaml
tree_feller:                          # ability enum name in lower_snake_case
  preparation-window-seconds: 3.0    # seconds the ability stays "prepared"
  duration-ticks:             100    # ticks the ability remains active (20 ticks = 1 s)
  cooldown-seconds:           120.0  # cooldown in seconds after expiry
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `preparation-window-seconds` | `double` | `3.0` | Preparation window in seconds |
| `duration-ticks` | `int` | `100` | Active duration in ticks (20 ticks = 1 s) |
| `cooldown-seconds` | `double` | `120.0` | Post-expiry cooldown in seconds |

---

## Force-activating (admin)

Skip the preparation state and activate an ability directly:

```
/ezskills ability <player> <ability>
```

See [Commands](commands) for the full admin reference.
