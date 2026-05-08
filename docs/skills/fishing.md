---
layout: default
title: Fishing
parent: Skills
grand_parent: Server Owners
nav_order: 3
---

# Fishing
{: .no_toc }

Fishing tracks the experience a player earns from catching fish and treasure.

- **Enum constant:** `FISHING`
- **GUI icon:** Cod
- **Associated ability:** [Angler](../abilities/angler)

---

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## XP curve

Default values in `skills.yml`:

```yaml
fishing:
  xp-base:       80.0
  xp-multiplier: 1.4
```

XP required to advance *from* the listed level to the next:

| Level | XP to next level |
|------:|-----------------:|
| 1 | 80 |
| 2 | 112 |
| 3 | 157 |
| 4 | 220 |
| 5 | 307 |
| 6 | 430 |
| 7 | 602 |
| 8 | 843 |
| 9 | 1,180 |
| 10 | 1,653 |
| 15 | 8,987 |
| 20 | 47,810 |

Formula: `xp = xp-base × xp-multiplier^(level − 1)`

---

## Awarding XP

**Via command:**

```
/ezskills addxp <player> FISHING <amount>
```

**Via API:** see [Developer Docs](../../developer).

---

## Configuration reference

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `xp-base` | `double` | `80.0` | XP needed to advance from level 1 to 2 |
| `xp-multiplier` | `double` | `1.4` | Growth factor applied each subsequent level |
