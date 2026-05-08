---
layout: default
title: Acrobatics
parent: Skills
grand_parent: Server Owners
nav_order: 5
---

# Acrobatics
{: .no_toc }

Acrobatics tracks the experience a player earns from physical movement such as jumping, falling, and sprinting.

- **Enum constant:** `ACROBATICS`
- **GUI icon:** Feather
- **Associated ability:** None (built-in)

---

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## XP curve

Default values in `skills.yml`:

```yaml
acrobatics:
  xp-base:       80.0
  xp-multiplier: 1.4
```

XP required to advance *from* the listed level to the next:

| Level | XP to next level |
|------:|-----------------:|
| 1 | 80 |
| 2 | 112 |
| 3 | 157 |
| 4 | 219 |
| 5 | 307 |
| 6 | 430 |
| 7 | 602 |
| 8 | 843 |
| 9 | 1,180 |
| 10 | 1,652 |
| 15 | 11,998 |
| 20 | 87,120 |

Formula: `xp = xp-base × xp-multiplier^(level − 1)`

---

## Awarding XP

**Via command:**

```
/ezskills addxp <player> ACROBATICS <amount>
```

**Via API:** see [Developer Docs](../../developer).

---

## Configuration reference

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `xp-base` | `double` | `80.0` | XP needed to advance from level 1 to 2 |
| `xp-multiplier` | `double` | `1.4` | Growth factor applied each subsequent level |
