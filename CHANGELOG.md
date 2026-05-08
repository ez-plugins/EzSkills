# Changelog

All notable changes to EzSkills are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [2.0.0] - 2026-05-08

### Added
- **Acrobatics skill** — tracks XP earned from jumping, falling, and sprinting (`ACROBATICS` enum constant, Feather GUI icon, `xp-base: 80.0`, `xp-multiplier: 1.4`, `max-level: 100`)
- **Custom skills API** — third-party plugins can now register their own skills at runtime via `SkillDefinition` and `EzSkillsAPI.registerSkill()`; progress is persisted in the same storage backend as built-in skills
- `SkillDefinitionRegistry` — thread-safe in-memory registry for custom `SkillDefinition` instances; rejects names that clash with built-in `SkillType` values
- `EzSkillsAPI.getRegisteredSkills()` — returns the list of all currently registered custom skill definitions
- Custom skill XP / level methods on `EzSkillsAPI`: `addExperience`, `getSkillLevel`, `getSkillExperience`, `setSkillLevel` now accept any registered custom skill name in addition to built-in `SkillType` names

### Changed
- Skill GUI (`/skills`) updated to a 36-slot (4-row) layout to accommodate five built-in skills; close button moved to slot 31
- `SkillProfile` stores custom skill progress in a separate `HashMap` alongside the built-in `EnumMap`; `getAllCustom()` exposes the full map for serialisation
- `SkillProfileModel` persists and loads custom skill columns (`<name>_level` / `<name>_experience`) automatically

### Skills

| Skill | Enum | XP base | XP multiplier | Max level |
|-------|------|---------|---------------|-----------|
| Woodcutting | `WOODCUTTING` | 100.0 | 1.5 | 100 |
| Mining | `MINING` | 100.0 | 1.5 | 100 |
| Fishing | `FISHING` | 100.0 | 1.5 | 100 |
| Fighting | `FIGHTING` | 120.0 | 1.6 | 100 |
| Acrobatics | `ACROBATICS` | 80.0 | 1.4 | 100 |

### Abilities

| Ability | Skill | Trigger |
|---------|-------|---------|
| Lumberjack | Woodcutting | Auto-smelt logs on break |
| Spelunker | Mining | Night-vision while underground |
| Angler | Fishing | Reduced fishing wait time |
| Warrior | Fighting | Temporary strength boost on kill streak |
