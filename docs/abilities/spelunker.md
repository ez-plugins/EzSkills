---
layout: default
title: Spelunker
parent: Abilities
grand_parent: Server Owners
nav_order: 2
---

# Spelunker
{: .no_toc }

Spelunker is the ability associated with the [Mining](../skills/mining) skill.

When triggered, EzSkills marks the player as having Spelunker active and fires `EzSkillsAbilityActivateEvent`. The actual in-game effect - such as a temporary haste buff or vein mining - is implemented by your server's integration or companion plugin.

- **Skill:** [Mining](../skills/mining)
- **Enum constant:** `SPELUNKER`

---

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Lifecycle

```
Inactive → Preparing → Active → Cooldown → Inactive
```

| State | Description |
|-------|-------------|
| **Inactive** | Ready; no cooldown active |
| **Preparing** | Charged: the player has `preparation-window-seconds` seconds to trigger activation |
| **Active** | Live for `duration-ticks` ticks; integration effects should fire now |
| **Cooldown** | Must wait `cooldown-seconds` seconds before preparing again |

---

## Default configuration

```yaml
# abilities.yml
spelunker:
  preparation-window-seconds: 3.0    # seconds the ability stays prepared
  duration-ticks:             100    # ticks the ability remains active (20 ticks = 1 s)
  cooldown-seconds:           120.0  # cooldown after expiry
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `preparation-window-seconds` | `double` | `3.0` | Window in which the player can trigger activation |
| `duration-ticks` | `int` | `100` | How long the ability stays active (in ticks) |
| `cooldown-seconds` | `double` | `120.0` | Post-expiry cooldown in seconds |

---

## Force-activating (admin)

Bypass preparation and activate Spelunker for a player immediately:

```
/ezskills ability <player> SPELUNKER
```

---

## Developer integration

See [Events](../../api/events) for listening to `EzSkillsAbilityActivateEvent` and implementing custom effects.
