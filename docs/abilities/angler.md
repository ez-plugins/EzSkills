---
layout: default
title: Angler
parent: Abilities
grand_parent: Server Owners
nav_order: 3
---

# Angler
{: .no_toc }

Angler is the ability associated with the [Fishing](../skills/fishing) skill.

When triggered, EzSkills marks the player as having Angler active and fires `EzSkillsAbilityActivateEvent`. The actual in-game effect — such as double loot from fishing — is implemented by your server's integration or companion plugin.

- **Skill:** [Fishing](../skills/fishing)
- **Enum constant:** `ANGLER`

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
fishing:
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

Bypass preparation and activate Angler for a player immediately:

```
/ezskills ability <player> ANGLER
```

---

## Developer integration

See [Events](../../api/events) for listening to `EzSkillsAbilityActivateEvent` and implementing custom effects.
