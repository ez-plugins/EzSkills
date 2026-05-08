---
nav_exclude: true
---

# MySQL Storage

## Configuration (`storage.yml`)

```yaml
storage:
  type: mysql

  mysql:
    host: localhost
    port: 3306
    database: ezskills
    username: root
    password: "yourpassword"
    table-prefix: ""
    pool:
      max-size: 10
      min-idle: 2
```

## Table schema

EzSkills creates the table automatically on first start:

```sql
CREATE TABLE IF NOT EXISTS `player_skills` (
    `id`                     VARCHAR(36)  NOT NULL,
    `woodcutting_level`      INT          NOT NULL DEFAULT 1,
    `woodcutting_experience` DOUBLE       NOT NULL DEFAULT 0,
    `mining_level`           INT          NOT NULL DEFAULT 1,
    `mining_experience`      DOUBLE       NOT NULL DEFAULT 0,
    `fishing_level`          INT          NOT NULL DEFAULT 1,
    `fishing_experience`     DOUBLE       NOT NULL DEFAULT 0,
    `fighting_level`         INT          NOT NULL DEFAULT 1,
    `fighting_experience`    DOUBLE       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Requirements

- MySQL 8.0+ or MariaDB 10.5+
- The database must exist before starting EzSkills.
