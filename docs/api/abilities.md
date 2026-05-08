---
nav_exclude: true
---

# Abilities API

## Built-in ability types

| Enum value    | Description                           |
|---------------|---------------------------------------|
| `TREE_FELLER` | Instantly fells an entire tree at once |

---

## Registering a custom ability

Any plugin can register its own abilities. Custom abilities automatically appear in the `/abilities` GUI alongside built-in ones, showing their current state (Ready / Preparing / Active) for every player.

### Step 1: Implement `AbilityDefinition`

Create a class that implements `com.github.ezplugins.ezskills.ability.AbilityDefinition`. The interface describes everything EzSkills needs to display and time the ability. **Read timing values from your own config** so server admins can adjust them:

```java
import com.github.ezplugins.ezskills.ability.AbilityDefinition;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public final class NightVisionAbility implements AbilityDefinition {

    private final FileConfiguration config;

    public NightVisionAbility(FileConfiguration config) {
        this.config = config;
    }

    @Override public String   getName()        { return "NIGHT_VISION"; }
    @Override public String   getDisplayName() { return "Night Vision"; }
    @Override public Material getIcon()        { return Material.ENDER_EYE; }
    @Override public String   getDescription() { return "See clearly in the dark."; }

    // Optional: associate with a skill - shown in the GUI lore
    @Override public String   getSkillName()   { return "FIGHTING"; }

    // Timing values read from your plugin's config.yml
    @Override public int getPreparationWindowSeconds() {
        return config.getInt("abilities.night_vision.preparation-seconds", 30);
    }

    @Override public int getActiveDurationSeconds() {
        return config.getInt("abilities.night_vision.active-seconds", 15);
    }

    @Override public int getCooldownSeconds() {
        return config.getInt("abilities.night_vision.cooldown-seconds", 120);
    }
}
```

Add the matching section to your `config.yml`:

```yaml
# config.yml (your plugin)
abilities:
  night_vision:
    preparation-seconds: 30   # how long the charge lasts before expiring
    active-seconds: 15        # how long the effect stays active
    cooldown-seconds: 120     # cooldown after the effect expires
```

### Step 2: Register in `onEnable`

Always check that EzSkills is present before calling the API:

```java
@Override
public void onEnable() {
    if (getServer().getPluginManager().getPlugin("EzSkills") != null) {
        EzSkillsAPI.registerAbility(new NightVisionAbility(getConfig()));
    }
}
```

Add EzSkills as a soft dependency so your plugin loads after it:

```yaml
# plugin.yml
softdepend:
  - EzSkills
```

### Step 3: Apply the effect

Listen to `EzSkillsAbilityActivateEvent` to apply the actual game effect when the ability fires. Use the ability name to target only your own ability:

```java
import com.github.ezplugins.ezskills.api.event.EzSkillsAbilityActivateEvent;
import com.github.ezplugins.ezskills.api.event.EzSkillsAbilityDeactivateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class NightVisionListener implements Listener {

    @EventHandler
    public void onActivate(EzSkillsAbilityActivateEvent event) {
        if (!"NIGHT_VISION".equals(event.getAbilityName())) return;

        final int durationTicks = 20 * 20; // 20 seconds
        event.getPlayer().addPotionEffect(
            new PotionEffect(PotionEffectType.NIGHT_VISION, durationTicks, 0));
    }

    @EventHandler
    public void onDeactivate(EzSkillsAbilityDeactivateEvent event) {
        if (!"NIGHT_VISION".equals(event.getAbilityName())) return;

        event.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
}
```

### Step 4: Trigger the ability lifecycle

Use your own game events (e.g. a right-click, sneak, or any action) to move the ability through its states:

```java
import com.github.ezplugins.ezskills.api.EzSkillsAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class NightVisionTriggerListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final var player = event.getPlayer();

        if (EzSkillsAPI.isAbilityActive(player, "NIGHT_VISION")) {
            // Already active - do nothing
            return;
        }

        if (!EzSkillsAPI.isAbilityActive(player, "NIGHT_VISION")) {
            // Charge it first; player must interact again within the window to activate
            EzSkillsAPI.prepareAbility(player, "NIGHT_VISION");
        } else {
            // Second interaction within the window - activate!
            EzSkillsAPI.activateAbility(player, "NIGHT_VISION");
        }
    }
}
```

---

## `AbilityDefinition` method reference

| Method | Required | Description |
|--------|----------|-------------|
| `getName()` | yes | Unique identifier, e.g. `"NIGHT_VISION"`. Case-insensitive. |
| `getDisplayName()` | yes | Title shown on the GUI item |
| `getIcon()` | yes | `Material` used for the GUI item |
| `getDescription()` | yes | Short lore line shown in the GUI |
| `getSkillName()` | no | Associated skill name shown in lore; `null` = no association |
| `getPreparationWindowSeconds()` | no | Default: `30` |
| `getActiveDurationSeconds()` | no | Default: `15` |
| `getCooldownSeconds()` | no | Default: `120` |

---

## Listing all registered abilities

```java
List<AbilityDefinition> all = EzSkillsAPI.getRegisteredAbilities();
```

---

## Lifecycle

```
[Inactive] → prepareAbility() → [Prepared] → activateAbility() → [Active] → deactivateAbility() / timer → [Inactive]
```

| State | GUI colour | Description |
|-------|------------|-------------|
| Inactive | Green | Ready; no cooldown |
| Preparing | Yellow | Charged; waiting for activation |
| Active | Gold (glowing) | Effect is live |

## Checking state at runtime

```java
boolean active  = EzSkillsAPI.isAbilityActive(player, "NIGHT_VISION");
long windowMs   = EzSkillsAPI.getAbilityPreparationWindowMillis("NIGHT_VISION");
```

## Direct control

```java
EzSkillsAPI.prepareAbility(player, "NIGHT_VISION");    // enter preparing state
EzSkillsAPI.activateAbility(player, "NIGHT_VISION");   // enter active state
EzSkillsAPI.deactivateAbility(player, "NIGHT_VISION"); // cancel / end early
```

---

## Configuration (`abilities.yml` - EzSkills)

Built-in abilities are configured in EzSkills' own `abilities.yml`. Custom abilities use the config of **your own plugin** as shown above.

```yaml
tree_feller:
  preparation-window-seconds: 3.0
  duration-ticks: 100
```
