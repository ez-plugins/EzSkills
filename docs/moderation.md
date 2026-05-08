---
layout: default
title: Moderation
parent: Server Owners
nav_order: 7
---

# Moderation
{: .no_toc }

This page covers permissions, managing player skill data, and common situations you'll encounter as a server admin.

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `ezskills.use` | Everyone | Open the `/skills` GUI |
| `ezskills.admin` | OP only | All `/ezskills` subcommands |

Grant `ezskills.admin` to a staff role in your permissions plugin (e.g. LuckPerms) to let moderators run admin commands without full OP status:

```
/lp group moderator permission set ezskills.admin true
```

---

## Inspecting a player

Check what level and XP a player has before taking action:

```
/ezskills info <player>
```

Works for **offline players**; data is loaded from storage asynchronously and printed to your chat when ready.

To check a single skill:

```
/ezskills get <player> <skill>
```

---

## Correcting skill data

### Adjusting XP

Add (or effectively subtract using a negative value) XP without disrupting the level:

```
/ezskills addxp <player> <skill> <amount>
```

{: .note }
XP cannot go below zero. Adding negative XP stops at 0 without going into negative values.

### Overriding level

Set a player directly to a specific level (XP is reset to 0):

```
/ezskills setlevel <player> <skill> <level>
```

---

## Resetting skill data

### Reset a single skill

Resets one skill to level 1 with zero XP:

```
/ezskills reset <player> <skill>
```

### Reset all skills

Resets every skill in a single save operation:

```
/ezskills resetall <player>
```

{: .warning }
Both reset commands are **irreversible**. There is no built-in undo. Take a backup of your storage before resetting a contested player's data (see [Backups](#backups) below).

---

## Leaderboards

See the top players for a skill:

```
/ezskills top <skill> [limit]
```

`limit` defaults to 10, maximum 20.

{: .note }
`/ezskills top` requires **MySQL** storage to query across all players. With YAML storage it returns online players only.

---

## Ability management

Force-activate an ability for a player (bypasses the preparation state):

```
/ezskills ability <player> <ability>
```

Useful for verifying ability behaviour in tests or restoring a player whose ability got stuck.

---

## Reloading config

Apply changes to `config.yml`, `skills.yml`, `abilities.yml`, or `storage.yml` without restarting:

```
/ezskills reload
```

{: .warning }
Reload does **not** reconnect the storage backend. If you change `storage.yml` (e.g. switch from YAML to MySQL), a full server restart is required.

---

## Backups

EzSkills does not bundle a built-in backup tool. Recommended approach depends on your storage backend:

### YAML storage

Player data lives in `plugins/EzSkills/data/`. Copy the folder before any bulk reset:

```bash
cp -r plugins/EzSkills/data plugins/EzSkills/data.bak
```

### MySQL storage

Use `mysqldump` before destructive operations:

```bash
mysqldump -u <user> -p <database> ezskills_profiles > backup.sql
```

Restore with:

```bash
mysql -u <user> -p <database> < backup.sql
```
