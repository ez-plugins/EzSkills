---
layout: default
title: Mining
parent: Skills
grand_parent: Server Owners
nav_order: 2
---

# Mining
{: .no_toc }

Mining tracks the experience a player earns from excavating ores and stone.

- **Enum constant:** `MINING`
- **GUI icon:** Diamond Pickaxe
- **Associated ability:** [Spelunker](../abilities/spelunker)

---

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## XP curve

Default values in `skills.yml`:

```yaml
mining:
  xp-base:       100.0
  xp-multiplier: 1.5
```

XP required to advance *from* the listed level to the next:

| Level | XP to next level |
|------:|-----------------:|
| 1 | 100 |
| 2 | 150 |
| 3 | 225 |
| 4 | 338 |
| 5 | 506 |
| 6 | 759 |
| 7 | 1,139 |
| 8 | 1,709 |
| 9 | 2,563 |
| 10 | 3,844 |
| 15 | 29,193 |
| 20 | 221,684 |

Formula: `xp = xp-base × xp-multiplier^(level − 1)`

---

## Awarding XP

**Via command:**

```
/ezskills addxp <player> MINING <amount>
```

**Via API:** see [Developer Docs](../../developer).

---

## Configuration reference

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `xp-base` | `double` | `100.0` | XP needed to advance from level 1 to 2 |
| `xp-multiplier` | `double` | `1.5` | Growth factor applied each subsequent level |
