---
nav_exclude: true
---

# Storage Overview

EzSkills uses [Jaloquent](https://github.com/EzFramework/Jaloquent) as its ORM layer.
Player skill data is stored as a flat record per player (one row / one YAML section per UUID).

## Choosing a backend

Set `storage.type` in `storage.yml`:

| Value   | Description |
|---------|-------------|
| `yaml`  | Default. Single-file, zero setup. Good for small servers. |
| `mysql` | Recommended for production. Configure connection details below. |

## Data model

Each player record contains:

| Column / Key              | Type   | Default |
|---------------------------|--------|---------|
| `id`                      | UUID   | —       |
| `woodcutting_level`       | int    | 1       |
| `woodcutting_experience`  | double | 0       |
| `mining_level`            | int    | 1       |
| `mining_experience`       | double | 0       |
| `fishing_level`           | int    | 1       |
| `fishing_experience`      | double | 0       |
| `fighting_level`          | int    | 1       |
| `fighting_experience`     | double | 0       |
