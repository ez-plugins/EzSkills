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

When triggered, EzSkills marks the player as having Spelunker active and fires `EzSkillsAbilityActivateEvent`. The actual in-game effect — such as a temporary haste buff or vein mining — is implemented by your server's integration or companion plugin.

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
| **Preparing** | Charged — the player has `preparation-time` seconds to trigger activation |
| **Active** | Live for `active-time` seconds; integration effects should fire now |
| **Cooldown** | Must wait `cooldown` seconds before preparing again |

---

## Default configuration

```yaml
# abilities.yml
mining:
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

Bypass preparation and activate Spelunker for a player immediately:

```
/ezskills ability <player> SPELUNKER
```

---

## Developer integration

See [Events](../../api/events) for listening to `EzSkillsAbilityActivateEvent` and implementing custom effects.
