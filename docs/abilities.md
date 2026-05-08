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
| **Preparing** | Charged — the player has `preparation-time` seconds to trigger activation |
| **Active** | Live for `active-time` seconds; fires `EzSkillsAbilityActivateEvent` |
| **Cooldown** | Must wait `cooldown` seconds before preparing again |

---

## Timing configuration

Each ability is configured in `abilities.yml`:

```yaml
woodcutting:               # one section per skill
  preparation-time: 30     # seconds the ability stays "prepared"
  active-time:      15     # seconds the ability remains active
  cooldown:         120    # cooldown after expiry
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `preparation-time` | `int` | `30` | Preparation window (seconds) |
| `active-time` | `int` | `15` | Active duration (seconds) |
| `cooldown` | `int` | `120` | Post-expiry cooldown (seconds) |

---

## Force-activating (admin)

Skip the preparation state and activate an ability directly:

```
/ezskills ability <player> <ability>
```

See [Commands](commands) for the full admin reference.
