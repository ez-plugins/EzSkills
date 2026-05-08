---
layout: default
title: Lumberjack
parent: Abilities
grand_parent: Server Owners
nav_order: 1
---

# Lumberjack
{: .no_toc }

Lumberjack is the ability associated with the [Woodcutting](../skills/woodcutting) skill.

When triggered, EzSkills marks the player as having Lumberjack active and fires `EzSkillsAbilityActivateEvent`. The actual in-game effect — such as breaking an entire tree at once — is implemented by your server's integration or companion plugin.

- **Skill:** [Woodcutting](../skills/woodcutting)
- **Enum constant:** `LUMBERJACK`

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
| **Preparing** | Charged — the player has `preparation-time` seconds to trigger activation |
| **Active** | Live for `active-time` seconds; integration effects should fire now |
| **Cooldown** | Must wait `cooldown` seconds before preparing again |

---

## Default configuration

```yaml
# abilities.yml
woodcutting:
  preparation-time: 30    # seconds the ability stays prepared
  active-time:      15    # seconds the ability remains active
  cooldown:         120   # cooldown after expiry
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `preparation-time` | `int` | `30` | Window in which the player can trigger activation |
| `active-time` | `int` | `15` | How long the ability stays active |
| `cooldown` | `int` | `120` | Post-expiry cooldown in seconds |

---

## Force-activating (admin)

Bypass preparation and activate Lumberjack for a player immediately:

```
/ezskills ability <player> LUMBERJACK
```

---

## Developer integration

See [Events](../../api/events) for listening to `EzSkillsAbilityActivateEvent` and implementing custom effects.
