---
layout: default
title: Storage
parent: Server Owners
nav_order: 3
---

# Storage
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Overview

EzSkills supports two storage backends configured in `plugins/EzSkills/storage.yml`.
All reads and writes are processed on a dedicated **background worker thread**, keeping the main server thread free.

If the MySQL backend fails to connect on startup, EzSkills automatically falls back to YAML and logs a warning.

---

## YAML storage

The default backend. Player data is written to `plugins/EzSkills/players.yml`.

```yaml
storage:
  type: yaml
```

No additional configuration is required.

{: .note }
The YAML backend does not support server-side leaderboard queries (`/ezskills top`).
Use the MySQL backend for full leaderboard support.

---

## MySQL storage

Stores player data in a MySQL (or MariaDB) database using a **HikariCP** connection pool.

```yaml
storage:
  type: mysql

  mysql:
    host:     localhost
    port:     3306
    database: ezskills
    username: root
    password: ""

    # Optional prefix for table names (e.g. "ez_" → ez_player_skills)
    table-prefix: ""

    # SSL options (optional)
    ssl:      false           # Set to true to require SSL for the connection
    # ssl-mode: DISABLED      # SSL mode: DISABLED, PREFERRED, or REQUIRED

    pool:
      max-size: 10   # Maximum number of pooled connections
      min-idle: 2    # Minimum idle connections kept alive
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `host` | `string` | `localhost` | MySQL server hostname. |
| `port` | `int` | `3306` | MySQL server port. |
| `database` | `string` | `ezskills` | Target database. |
| `username` | `string` | `root` | Database user. |
| `password` | `string` | `""` | Database password. |
| `table-prefix` | `string` | `""` | Prefix prepended to all table names. |
| `ssl` | `boolean` | `false` | Enable SSL for the MySQL connection. |
| `ssl-mode` | `string` | `DISABLED` | SSL mode: `DISABLED`, `PREFERRED`, or `REQUIRED`. |
| `pool.max-size` | `int` | `10` | Maximum HikariCP pool size. |
| `pool.min-idle` | `int` | `2` | Minimum idle connections in the pool. |

---

## Table structure

EzSkills automatically creates the `player_skills` table (respecting any configured prefix) on first run using `QueryBuilder.createTable()` - no manual schema setup is needed.

| Column | Type | Description |
|--------|------|-------------|
| `id` | `VARCHAR(36) PK` | Player UUID string. |
| `woodcutting_level` | `INT` | Woodcutting level (default 1). |
| `woodcutting_experience` | `DOUBLE` | Accumulated Woodcutting XP. |
| `mining_level` | `INT` | Mining level. |
| `mining_experience` | `DOUBLE` | Accumulated Mining XP. |
| `fishing_level` | `INT` | Fishing level. |
| `fishing_experience` | `DOUBLE` | Accumulated Fishing XP. |
| `fighting_level` | `INT` | Fighting level. |
| `fighting_experience` | `DOUBLE` | Accumulated Fighting XP. |

---

## Zero raw SQL

EzSkills uses [JavaQueryBuilder](https://github.com/EzFramework/JavaQueryBuilder) for all SQL generation and [Jaloquent](https://github.com/EzFramework/Jaloquent) for ORM operations. There are **no raw SQL strings** anywhere in the plugin source; all queries are built programmatically.
