---
nav_exclude: true
---

# Skills API

## Built-in skill types

| Enum value    | Description          |
|---------------|----------------------|
| `WOODCUTTING` | Chopping trees       |
| `MINING`      | Breaking ores/stone  |
| `FISHING`     | Catching fish        |
| `FIGHTING`    | Combat with mobs/players |

Skill names are matched case-insensitively wherever the API accepts a `String` skill name.

---

## Integrating with skills from your plugin

EzSkills does not know about game events on its own — it exposes an XP API that **you** call from your plugin's event listeners.

### 1 — Depend on EzSkills

Add EzSkills as a soft-depend in your `plugin.yml` so your plugin loads after it:

```yaml
# plugin.yml
name: MyPlugin
...
softdepend:
  - EzSkills
```

And add the API artifact to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.ez-plugins.EzSkills</groupId>
    <artifactId>ezskills-api</artifactId>
    <version>2.0.0</version>
    <scope>provided</scope>
</dependency>
```

### 2 — Award XP using the source-aware method

Pass `this` (your plugin instance) as the first argument to `addExperience`. EzSkills will then automatically apply the XP multiplier and enabled/disabled flag that the **server admin** has configured for your plugin in EzSkills' `config.yml`:

```java
import com.github.ezplugins.ezskills.api.EzSkillsAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;

public final class SkillListener implements Listener {

    private final MyPlugin plugin;

    public SkillListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (getServer().getPluginManager().getPlugin("EzSkills") == null) return;
        // EzSkills applies the configured multiplier and enabled flag for "MyPlugin"
        EzSkillsAPI.addExperience(plugin, event.getPlayer().getUniqueId(), "WOODCUTTING", 10.0);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (getServer().getPluginManager().getPlugin("EzSkills") == null) return;
        EzSkillsAPI.addExperience(plugin, event.getPlayer().getUniqueId(), "FISHING", 20.0);
    }
}
```

### 3 — Server-admin configuration (EzSkills' `config.yml`)

Server admins configure your plugin's XP contribution in EzSkills' own `config.yml`, under `plugin-overrides`. No code changes in your plugin are needed — EzSkills applies these settings automatically:

```yaml
# EzSkills/config.yml
plugin-overrides:
  MyPlugin:                 # must match your plugin's exact name
    woodcutting:
      enabled: true
      xp-multiplier: 1.5   # players get 15 XP per log instead of 10
    mining:
      enabled: false        # MyPlugin cannot award Mining XP on this server
    fishing:
      enabled: true
      xp-multiplier: 0.5   # halve Fishing XP from MyPlugin
```

| Key | Default | Description |
|-----|---------|-------------|
| `enabled` | `true` | When `false`, all XP calls from this plugin for this skill are silently ignored |
| `xp-multiplier` | `1.0` | Multiplied against the raw amount passed to `addExperience` |

Plugins not listed under `plugin-overrides` are unaffected — they always award the exact amount passed to the API.

### 4 — React to level-ups

Listen to `SkillLevelUpEvent` to trigger custom rewards:

```java
import com.github.ezplugins.ezskills.api.event.SkillLevelUpEvent;
import com.github.ezplugins.ezskills.skill.SkillType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class LevelRewardListener implements Listener {

    @EventHandler
    public void onLevelUp(SkillLevelUpEvent event) {
        if (event.getSkillType() == SkillType.WOODCUTTING && event.getNewLevel() == 10) {
            event.getPlayer().sendMessage("§6You reached Woodcutting level 10!");
        }
    }
}
```

---

## Reading skill data

```java
// By UUID
int level = EzSkillsAPI.getSkillLevel(player.getUniqueId(), "WOODCUTTING");
double xp  = EzSkillsAPI.getSkillExperience(player.getUniqueId(), "MINING");

// By OfflinePlayer
int level = EzSkillsAPI.getSkillLevel(offlinePlayer, "FISHING");

// Full profile (null if player is offline / not cached)
SkillProfile profile = EzSkillsAPI.getSkillProfile(player.getUniqueId());
if (profile != null) {
    SkillProgress woodcutting = profile.getProgress(SkillType.WOODCUTTING);
    int level = woodcutting.getLevel();
    double xp  = woodcutting.getExperience();
}
```

## Modifying skill data

```java
// Add experience (fires SkillLevelUpEvent if a threshold is crossed)
EzSkillsAPI.addExperience(player.getUniqueId(), "WOODCUTTING", 50.0);

// Set level directly (resets XP to 0)
EzSkillsAPI.setSkillLevel(player.getUniqueId(), "MINING", 10);
```

---

## XP formula

$$
\text{xpRequired}(level) = \text{base} \times \text{multiplier}^{(level - 1)}
$$

Configured per skill in `skills.yml` (EzSkills' own config):

```yaml
woodcutting:
  xp-base: 100.0       # XP required to reach level 2
  xp-multiplier: 1.5   # curve steepness

mining:
  xp-base: 100.0
  xp-multiplier: 1.5
```
