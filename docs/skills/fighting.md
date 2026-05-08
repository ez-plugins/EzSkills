---
layout: default
title: Fighting
parent: Skills
grand_parent: Server Owners
nav_order: 4
---

# Fighting
{: .no_toc }

Fighting tracks the experience a player earns from engaging in combat.

- **Enum constant:** `FIGHTING`
- **GUI icon:** Iron Sword
- **Associated ability:** [Warrior](../abilities/warrior)

---

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## XP curve

Default values in `skills.yml`:

```yaml
fighting:
  xp-base:       120.0
  xp-multiplier: 1.6
```

XP required to advance *from* the listed level to the next:

| Level | XP to next level |
|------:|-----------------:|
| 1 | 120 |
| 2 | 192 |
| 3 | 307 |
| 4 | 492 |
| 5 | 786 |
| 6 | 1,258 |
| 7 | 2,013 |
| 8 | 3,221 |
| 9 | 5,154 |
| 10 | 8,246 |
| 15 | 134,218 |
| 20 | 2,181,958 |

Formula: `xp = xp-base × xp-multiplier^(level − 1)`

{: .warning }
Fighting's steeper multiplier (`1.6`) makes high levels significantly harder than other skills. Reduce `xp-multiplier` in `skills.yml` if progression feels too slow for your server.

---

## Awarding XP

**Via command:**

```
/ezskills addxp <player> FIGHTING <amount>
```

**Via API:** see [Developer Docs](../../developer).

---

## Configuration reference

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `xp-base` | `double` | `120.0` | XP needed to advance from level 1 to 2 |
| `xp-multiplier` | `double` | `1.6` | Growth factor applied each subsequent level |
