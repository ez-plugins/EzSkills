package com.github.ezplugins.ezskills.ability;

import com.github.ezplugins.ezskills.api.event.EzSkillsAbilityActivateEvent;
import com.github.ezplugins.ezskills.api.event.EzSkillsAbilityDeactivateEvent;
import com.github.ezplugins.ezskills.api.event.EzSkillsAbilityPrepareEvent;
import com.github.ezplugins.ezskills.config.ConfigManager;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Manages the preparation and activation state of abilities for all online players.
 */
public final class AbilityManager {

    /** Plugin instance used for scheduling tasks. */
    private final JavaPlugin plugin;

    /** Provides access to abilities configuration. */
    private final ConfigManager configManager;

    /** Tracks ability states per player and per ability type. */
    private final Map<UUID, Map<AbilityType, AbilityState>> states = new ConcurrentHashMap<>();

    /** Tracks ability states for custom (non-enum) abilities, keyed by upper-case ability name. */
    private final Map<UUID, Map<String, AbilityState>> customStates = new ConcurrentHashMap<>();

    /**
     * Tracks the last deactivation time for built-in abilities, used to enforce cooldowns.
     * Key: player UUID → ability type → deactivation instant.
     */
    private final Map<UUID, Map<AbilityType, Instant>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Tracks the last deactivation time for custom abilities, used to enforce cooldowns.
     * Key: player UUID → upper-case ability name → deactivation instant.
     */
    private final Map<UUID, Map<String, Instant>> customCooldowns = new ConcurrentHashMap<>();

    public AbilityManager(@NotNull JavaPlugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    // -------------------------------------------------------------------------
    // State access
    // -------------------------------------------------------------------------

    private AbilityState getOrCreate(UUID playerId, AbilityType type) {
        return states
                .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(type, k -> new AbilityState());
    }

    /**
     * Removes all ability state for the given player.
     *
     * @param playerId the player's unique ID
     */
    public void removePlayer(UUID playerId) {
        states.remove(playerId);
        customStates.remove(playerId);
        cooldowns.remove(playerId);
        customCooldowns.remove(playerId);
    }

    /**
     * Returns whether the given ability is currently active for the player.
     *
     * <p>Handles both built-in ({@link AbilityType}) and custom (string-named) abilities.</p>
     *
     * @param player      the player
     * @param abilityName the ability name
     * @return {@code true} if active
     */
    public boolean isActive(@NotNull Player player, @NotNull String abilityName) {
        final AbilityType type = AbilityType.fromString(abilityName);
        if (type != null) {
            return isActive(player, type);
        }
        final Map<String, AbilityState> cs = customStates.get(player.getUniqueId());
        if (cs == null) {
            return false;
        }
        final AbilityState state = cs.get(abilityName.toUpperCase());
        return state != null && state.isActive();
    }

    /**
     * Returns whether the given ability is currently in the preparing (charged) state for the
     * player, using the supplied window length for custom abilities.
     *
     * @param player      the player
     * @param abilityName the ability name
     * @param windowMillis the preparation window in milliseconds (used for custom abilities only;
     *                     built-in abilities read from configuration)
     * @return {@code true} if preparing
     */
    public boolean isPreparing(@NotNull Player player,
                               @NotNull String abilityName,
                               long windowMillis) {
        final AbilityType type = AbilityType.fromString(abilityName);
        if (type != null) {
            return isPrepared(player, type);
        }
        final Map<String, AbilityState> cs = customStates.get(player.getUniqueId());
        if (cs == null) {
            return false;
        }
        final AbilityState state = cs.get(abilityName.toUpperCase());
        return state != null && state.isPrepared(windowMillis);
    }

    /**
     * Puts a custom ability into the preparing state for the given player.
     *
     * @param player      the player
     * @param abilityName the custom ability name
     * @param windowMillis preparation window in milliseconds
     */
    public void prepareCustomAbility(@NotNull Player player,
                                     @NotNull String abilityName,
                                     long windowMillis) {
        final String key = abilityName.toUpperCase();
        final AbilityState state = customStates
                .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(key, k -> new AbilityState());
        state.setPreparedAt(Instant.now());

        final EzSkillsAbilityPrepareEvent event = new EzSkillsAbilityPrepareEvent(player, abilityName);
        Bukkit.getPluginManager().callEvent(event);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state.isPrepared(windowMillis)) {
                state.setPreparedAt(null);
            }
        }, windowMillis / 50L + 1L);
    }

    /**
     * Activates a custom ability for the given player.
     *
     * @param player        the player
     * @param abilityName   the custom ability name
     * @param durationTicks how many server ticks the ability should stay active
     */
    public void activateCustomAbility(@NotNull Player player,
                                      @NotNull String abilityName,
                                      long durationTicks) {
        final String key = abilityName.toUpperCase();
        final AbilityState state = customStates
                .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(key, k -> new AbilityState());
        state.setActiveDurationTicks(durationTicks);
        state.setPreparedAt(null);

        final EzSkillsAbilityActivateEvent event =
                new EzSkillsAbilityActivateEvent(player, abilityName);
        Bukkit.getPluginManager().callEvent(event);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state.isActive()) {
                deactivateCustomAbility(player, abilityName);
            }
        }, durationTicks);
    }

    /**
     * Deactivates a custom ability for the given player.
     *
     * @param player      the player
     * @param abilityName the custom ability name
     */
    public void deactivateCustomAbility(@NotNull Player player, @NotNull String abilityName) {
        final Map<String, AbilityState> cs = customStates.get(player.getUniqueId());
        if (cs == null) {
            return;
        }
        final AbilityState state = cs.get(abilityName.toUpperCase());
        if (state == null) {
            return;
        }
        state.clear();
        // Record the deactivation time for the custom cooldown.
        customCooldowns
                .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(abilityName.toUpperCase(), Instant.now());

        final EzSkillsAbilityDeactivateEvent event =
                new EzSkillsAbilityDeactivateEvent(player, abilityName);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Returns whether the given built-in ability is on cooldown for the player.
     *
     * @param player the player
     * @param type   the built-in ability type
     * @return {@code true} if still on cooldown
     */
    public boolean isOnCooldown(@NotNull Player player, @NotNull AbilityType type) {
        final Map<AbilityType, Instant> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return false;
        }
        final Instant last = playerCooldowns.get(type);
        if (last == null) {
            return false;
        }
        final long cooldownMillis = getCooldownMillis(type);
        return Instant.now().toEpochMilli() - last.toEpochMilli() < cooldownMillis;
    }

    /**
     * Returns the remaining cooldown in whole seconds for the given built-in ability,
     * or {@code 0} when the ability is ready.
     *
     * @param player the player
     * @param type   the built-in ability type
     * @return remaining cooldown seconds, {@code 0} if ready
     */
    public long remainingCooldownSeconds(@NotNull Player player, @NotNull AbilityType type) {
        final Map<AbilityType, Instant> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0L;
        }
        final Instant last = playerCooldowns.get(type);
        if (last == null) {
            return 0L;
        }
        final long cooldownMillis = getCooldownMillis(type);
        final long elapsedMillis = Instant.now().toEpochMilli() - last.toEpochMilli();
        final long remainingMillis = cooldownMillis - elapsedMillis;
        return remainingMillis > 0 ? (remainingMillis + 999L) / 1000L : 0L;
    }

    // -------------------------------------------------------------------------
    // Built-in ability queries (typed)
    // -------------------------------------------------------------------------

    /**
     * Returns whether the given built-in ability is currently active for the player.
     *
     * @param player the player
     * @param type   the built-in ability type
     * @return {@code true} if active
     */
    public boolean isActive(@NotNull Player player, @NotNull AbilityType type) {
        final Map<AbilityType, AbilityState> playerStates = states.get(player.getUniqueId());
        if (playerStates == null) {
            return false;
        }
        final AbilityState state = playerStates.get(type);
        return state != null && state.isActive();
    }

    /**
     * Returns whether the given built-in ability is currently in the preparing state.
     *
     * @param player the player
     * @param type   the built-in ability type
     * @return {@code true} if preparing
     */
    public boolean isPrepared(@NotNull Player player, @NotNull AbilityType type) {
        final Map<AbilityType, AbilityState> playerStates = states.get(player.getUniqueId());
        if (playerStates == null) {
            return false;
        }
        final AbilityState state = playerStates.get(type);
        if (state == null) {
            return false;
        }
        final long windowMillis = getPreparationWindowMillis(type);
        return state.isPrepared(windowMillis);
    }

    // -------------------------------------------------------------------------
    // Mutations
    // -------------------------------------------------------------------------

    public void prepareAbility(@NotNull Player player, @NotNull AbilityType type) {
        if (isOnCooldown(player, type)) {
            return;
        }
        final AbilityState state = getOrCreate(player.getUniqueId(), type);
        state.setPreparedAt(Instant.now());

        final EzSkillsAbilityPrepareEvent event = new EzSkillsAbilityPrepareEvent(player, type);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void activateAbility(@NotNull Player player, @NotNull AbilityType type) {
        final AbilityState state = getOrCreate(player.getUniqueId(), type);
        final long durationTicks = getActiveDurationTicks(type);
        state.setActiveDurationTicks(durationTicks);
        state.setPreparedAt(null);

        final EzSkillsAbilityActivateEvent event = new EzSkillsAbilityActivateEvent(player, type);
        Bukkit.getPluginManager().callEvent(event);

        // Schedule auto-deactivate task
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state.isActive()) {
                deactivateAbility(player, type);
            }
        }, durationTicks);
    }

    public void deactivateAbility(@NotNull Player player, @NotNull AbilityType type) {
        final Map<AbilityType, AbilityState> playerStates = states.get(player.getUniqueId());
        if (playerStates == null) {
            return;
        }
        final AbilityState state = playerStates.get(type);
        if (state == null) {
            return;
        }
        state.clear();
        // Record the deactivation time so the cooldown can be enforced.
        cooldowns
                .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(type, Instant.now());

        final EzSkillsAbilityDeactivateEvent event = new EzSkillsAbilityDeactivateEvent(player, type);
        Bukkit.getPluginManager().callEvent(event);
    }

    // -------------------------------------------------------------------------
    // Config helpers
    // -------------------------------------------------------------------------

    public long getPreparationWindowMillis(@NotNull AbilityType type) {
        final String key = type.name().toLowerCase() + ".preparation-window-seconds";
        final double seconds = configManager.getAbilitiesConfig().getDouble(key, 3.0);
        return (long) (seconds * 1000L);
    }

    private long getActiveDurationTicks(@NotNull AbilityType type) {
        final String key = type.name().toLowerCase() + ".duration-ticks";
        return configManager.getAbilitiesConfig().getLong(key, 100L);
    }

    /**
     * Returns the cooldown length in milliseconds for the given built-in ability type.
     *
     * @param type the ability type
     * @return cooldown in milliseconds
     */
    public long getCooldownMillis(@NotNull AbilityType type) {
        final String key = type.name().toLowerCase() + ".cooldown-seconds";
        final double seconds = configManager.getAbilitiesConfig().getDouble(key, 0.0);
        return (long) (seconds * 1000L);
    }
}
