# EzSkills — Skill progression for any plugin

**EzSkills** is a compact RPG progression plugin that layers levelling, XP gain, and timed abilities on top of your server's gameplay. Use it standalone or integrate with plugins like [EzTree](https://www.spigotmc.org/resources/117223/), [EzMine](https://www.spigotmc.org/resources/131060/), or your own custom plugins to turn repetitive actions into a rewarding skill loop.

![Version](https://img.shields.io/badge/Latest%20version-2.0.0-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21%20%7C%2026.1-blue)
![Software](https://img.shields.io/badge/Server%20software-Bukkit%20%7C%20Spigot%20%7C%20Paper-blue)
![Java](https://img.shields.io/badge/Requires-Java%2025+-brightgreen)

[Vote in #plugin-development on Discord — tell us what EzSkills plugin comes next!](https://discord.gg/yWP95XfmBS)

---

## Features

### Four built-in skills

| Skill | Description |
|-------|-------------|
| **Woodcutting** | Award XP for felling trees |
| **Mining** | Award XP for breaking ores and stone |
| **Fishing** | Award XP for catching fish |
| **Fighting** | Award XP for combat with mobs and players |

Every source plugin that calls `EzSkillsAPI.addExperience(plugin, ...)` is subject to the per-plugin XP multiplier and enabled flag you configure centrally — no changes needed in third-party plugins.

### Timed abilities

- **Tree Feller** — the built-in Woodcutting ability. Players prepare, trigger and ride out an auto-expiring mass-fell window.
- **Custom abilities** *(new in 2.0)* — any plugin can register its own ability via `EzSkillsAPI.registerAbility(definition)`. Custom abilities appear in the `/abilities` GUI automatically.
- **Ability state GUI** *(new in 2.0)* — the `/abilities` command opens an overview showing every registered ability with its current state: Ready (green), Preparing (yellow), or Active (gold).

### Progression controls

- **Scaling XP curve** — tune `xp-base` and `xp-multiplier` per skill in `skills.yml`.
- **Per-plugin overrides** *(new in 2.0)* — in `config.yml`, set an `xp-multiplier` or `enabled: false` for any third-party plugin and any skill without touching their code.

### Storage

- **YAML** — zero-setup flat file, ideal for small servers.
- **MySQL** — recommended for production; enables full leaderboards via `/ezskills top`.

---

## Core gameplay loop

1. **Configure** XP curves in `skills.yml` and ability timings in `abilities.yml`.
2. **Earn XP** as players perform actions — any event your integration plugin reports.
3. **Level up** — `SkillLevelUpEvent` fires immediately for rewards, rank unlocks, or boss-bar announcements.
4. **Prepare and activate** an ability within its warm-up window to use a powerful timed effect.
5. **View progress** at any time with `/skills` (levels) and `/abilities` (ability states).

---

## Commands

### Player commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/skills` | Open the skill progress overview GUI | `ezskills.use` |
| `/abilities` | Open the ability state overview GUI | `ezskills.use` |

### Admin commands (`/ezskills`)

| Command | Description |
|---------|-------------|
| `get <player> <skill>` | Print a player's level and stored XP |
| `info <player>` | Print a full skill overview (works offline) |
| `addxp <player> <skill> <amount>` | Grant XP manually; fires level-up events |
| `setlevel <player> <skill> <level>` | Set a level directly, resetting XP |
| `reset <player> <skill>` | Reset one skill to level 1 |
| `resetall <player>` | Reset all skills to level 1 |
| `top <skill> [limit]` | Leaderboard by level (MySQL required for full results) |
| `ability <player> <ability>` | Force-activate an ability, bypassing preparation |
| `reload` | Reload all config files |

All `/ezskills` subcommands require the `ezskills.admin` permission.

---

## Configuration

| File | Controls |
|------|----------|
| `config.yml` | GUI titles, boss-bar notifications, `plugin-overrides` |
| `skills.yml` | XP base and multiplier per skill |
| `abilities.yml` | Preparation window, active duration, and cooldown per ability |
| `storage.yml` | Storage backend (YAML or MySQL) and connection details |

### Per-plugin XP overrides (new in 2.0)

```yaml
# config.yml
plugin-overrides:
  MyPlugin:                   # must match the exact plugin name
    woodcutting:
      enabled: true
      xp-multiplier: 1.5      # 50% bonus Woodcutting XP from MyPlugin
    mining:
      enabled: false           # MyPlugin cannot award Mining XP
```

---

## API reference

### Skills

| Method | Description |
|--------|-------------|
| `EzSkillsAPI.addExperience(playerId, skill, amount)` | Award XP directly (no multiplier applied) |
| `EzSkillsAPI.addExperience(plugin, playerId, skill, amount)` | Award XP with the server-configured multiplier and enabled check |
| `EzSkillsAPI.getSkillLevel(playerId, skill)` | Return the player's current level |
| `EzSkillsAPI.getSkillExperience(playerId, skill)` | Return the player's accumulated XP |
| `EzSkillsAPI.setSkillLevel(playerId, skill, level)` | Set a level directly, resetting XP |
| `EzSkillsAPI.getSkillProfile(playerId)` | Return the full profile object (null if not cached) |

### Abilities

| Method | Description |
|--------|-------------|
| `EzSkillsAPI.registerAbility(definition)` | Register a custom ability; appears in `/abilities` automatically |
| `EzSkillsAPI.getRegisteredAbilities()` | List all registered ability definitions |
| `EzSkillsAPI.prepareAbility(playerId, ability)` | Enter the preparation (charged) state |
| `EzSkillsAPI.activateAbility(playerId, ability)` | Activate the ability immediately |
| `EzSkillsAPI.deactivateAbility(playerId, ability)` | Cancel or end the ability early |
| `EzSkillsAPI.isAbilityActive(playerId, ability)` | Check whether the ability is currently active.

### Events

- `SkillLevelUpEvent` — fires when a player reaches a new level (carries old and new level).
- `EzSkillsAbilityPrepareEvent` — fires when an ability enters its warm-up window.
- `EzSkillsAbilityActivateEvent` — fires when an ability activates.
- `EzSkillsAbilityDeactivateEvent` — fires when an ability ends or is cancelled.

### Registering a custom ability

```java
public class NightVisionAbility implements AbilityDefinition {
    @Override public String   getName()        { return "NIGHT_VISION"; }
    @Override public String   getDisplayName() { return "Night Vision"; }
    @Override public Material getIcon()        { return Material.ENDER_EYE; } // optional — defaults to NETHER_STAR
    @Override public String   getDescription() { return "See clearly in the dark."; }
    @Override public String   getSkillName()   { return "FIGHTING"; }
    // Optional: override timing defaults (30s prep / 15s active / 120s cooldown)
}

// In onEnable:
EzSkillsAPI.registerAbility(new NightVisionAbility());
```

See the [developer documentation](https://ez-plugins.github.io/EzSkills) for the full integration guide.

---

## Compatibility

- **Minecraft**: 1.21 and 26.1+
- **Server software**: Bukkit, Spigot, or Paper
- **Java**: 25+
- **Optional**: [EzCountdown](https://github.com/ez-plugins/EzCountdown) for boss-bar level-up notifications

---

## Need help?

Drop a message on the forum thread or [join our Discord server](https://discord.gg/yWP95XfmBS) for quick support and integration tips.
