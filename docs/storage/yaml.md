---
nav_exclude: true
---

# YAML Storage

YAML storage is the default backend. Data is written to
`plugins/EzSkills/players.yml`.

## Format

```yaml
player_skills:
  550e8400-e29b-41d4-a716-446655440000:
    woodcutting_level: 5
    woodcutting_experience: 43.0
    mining_level: 3
    mining_experience: 12.5
    fishing_level: 1
    fishing_experience: 0.0
    fighting_level: 2
    fighting_experience: 8.0
```

## Notes

- All writes go to the async storage worker thread and are flushed to disk immediately.
- For servers with many players prefer [MySQL](mysql.md).
