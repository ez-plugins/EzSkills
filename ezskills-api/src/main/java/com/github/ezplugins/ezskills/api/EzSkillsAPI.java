package com.github.ezplugins.ezskills.api;

import com.github.ezplugins.ezskills.ability.AbilityDefinition;
import com.github.ezplugins.ezskills.skill.SkillProfile;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Static facade providing access to EzSkills functionality for external plugins.
 *
 * <p>All methods throw {@link IllegalStateException} if EzSkills has not been
 * enabled on the server.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * int level = EzSkillsAPI.getSkillLevel(player, "WOODCUTTING");
 * EzSkillsAPI.addExperience(player.getUniqueId(), "MINING", 10.0);
 * }</pre>
 */
public final class EzSkillsAPI {

    /** The active service implementation; {@code null} when EzSkills is not loaded. */
    private static EzSkillsService service;

    private EzSkillsAPI() { }

    /**
     * Registers the service implementation. Called by the EzSkills plugin on enable.
     *
     * @param impl the service implementation
     */
    public static void init(@NotNull EzSkillsService impl) {
        service = impl;
    }

    /**
     * Clears the service registration. Called by the EzSkills plugin on disable.
     */
    public static void shutdown() {
        service = null;
    }

    @NotNull
    private static EzSkillsService service() {
        if (service == null) {
            throw new IllegalStateException("EzSkills is not enabled on this server.");
        }
        return service;
    }

    // -------------------------------------------------------------------------
    // Skills
    // -------------------------------------------------------------------------

    /**
     * Returns the skill level for the given player and skill name.
     *
     * @param playerId  the player's unique ID
     * @param skillName the skill name (e.g. {@code "WOODCUTTING"})
     * @return the current level
     */
    public static int getSkillLevel(@NotNull UUID playerId, @NotNull String skillName) {
        return service().getSkillLevel(playerId, skillName);
    }

    /**
     * Returns the skill level for the given player.
     *
     * @param player    the player
     * @param skillName the skill name
     * @return the current level
     */
    public static int getSkillLevel(@NotNull OfflinePlayer player, @NotNull String skillName) {
        return service().getSkillLevel(player, skillName);
    }

    /**
     * Returns the accumulated experience for the given player and skill.
     *
     * @param playerId  the player's unique ID
     * @param skillName the skill name
     * @return the current experience
     */
    public static double getSkillExperience(@NotNull UUID playerId, @NotNull String skillName) {
        return service().getSkillExperience(playerId, skillName);
    }

    /**
     * Returns the full skill profile for the given player, or {@code null} if not cached.
     *
     * @param playerId the player's unique ID
     * @return the profile, or {@code null}
     */
    @Nullable
    public static SkillProfile getSkillProfile(@NotNull UUID playerId) {
        return service().getSkillProfile(playerId);
    }

    /**
     * Adds experience to the specified skill for the given player.
     *
     * @param playerId  the player's unique ID
     * @param skillName the skill name
     * @param amount    the amount of experience to add
     */
    public static void addExperience(@NotNull UUID playerId, @NotNull String skillName, double amount) {
        service().addExperience(playerId, skillName, amount);
    }

    /**
     * Adds experience to the specified skill for the given player, applying the XP multiplier
     * and enabled flag that the server admin configured for {@code source} in EzSkills'
     * {@code config.yml}.
     *
     * <p>This is the <strong>preferred method</strong> for third-party plugins: it delegates all
     * balancing decisions to EzSkills' central configuration.</p>
     *
     * @param source    the calling plugin
     * @param playerId  the player's unique ID
     * @param skillName the skill name (e.g. {@code "WOODCUTTING"})
     * @param amount    the raw XP amount before the configured multiplier is applied
     */
    public static void addExperience(@NotNull Plugin source,
                                     @NotNull UUID playerId,
                                     @NotNull String skillName,
                                     double amount) {
        service().addExperience(source, playerId, skillName, amount);
    }

    /**
     * Sets the skill level for the given player, resetting experience to zero.
     *
     * @param playerId  the player's unique ID
     * @param skillName the skill name
     * @param level     the new level
     */
    public static void setSkillLevel(@NotNull UUID playerId, @NotNull String skillName, int level) {
        service().setSkillLevel(playerId, skillName, level);
    }

    // -------------------------------------------------------------------------
    // Abilities
    // -------------------------------------------------------------------------

    /**
     * Returns whether the specified ability is currently active for the given player.
     *
     * @param playerId    the player's unique ID
     * @param abilityName the ability name (e.g. {@code "TREE_FELLER"})
     * @return {@code true} if the ability is active
     */
    public static boolean isAbilityActive(@NotNull UUID playerId, @NotNull String abilityName) {
        return service().isAbilityActive(playerId, abilityName);
    }

    /**
     * Puts the ability into the preparation state for the given player.
     *
     * @param playerId    the player's unique ID
     * @param abilityName the ability name
     */
    public static void prepareAbility(@NotNull UUID playerId, @NotNull String abilityName) {
        service().prepareAbility(playerId, abilityName);
    }

    /**
     * Activates the ability for the given player.
     *
     * @param playerId    the player's unique ID
     * @param abilityName the ability name
     */
    public static void activateAbility(@NotNull UUID playerId, @NotNull String abilityName) {
        service().activateAbility(playerId, abilityName);
    }

    /**
     * Deactivates the ability for the given player.
     *
     * @param playerId    the player's unique ID
     * @param abilityName the ability name
     */
    public static void deactivateAbility(@NotNull UUID playerId, @NotNull String abilityName) {
        service().deactivateAbility(playerId, abilityName);
    }

    /**
     * Returns the preparation window in milliseconds for the given ability.
     *
     * @param abilityName the ability name
     * @return window in milliseconds
     */
    public static long getAbilityPreparationWindowMillis(@NotNull String abilityName) {
        return service().getAbilityPreparationWindowMillis(abilityName);
    }

    /**
     * Registers a custom {@link AbilityDefinition} so it appears in the ability overview GUI.
     *
     * <p>Call this from your plugin's {@code onEnable}, after verifying that EzSkills is present.</p>
     *
     * @param definition the definition to register
     */
    public static void registerAbility(@NotNull AbilityDefinition definition) {
        service().registerAbility(definition);
    }

    /**
     * Returns all currently registered ability definitions, including built-in ones.
     *
     * @return unmodifiable list of definitions
     */
    @NotNull
    public static List<AbilityDefinition> getRegisteredAbilities() {
        return service().getRegisteredAbilities();
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    /**
     * Reloads all EzSkills configuration files.
     */
    public static void reloadConfigs() {
        service().reloadConfigs();
    }

    /**
     * Returns the underlying {@link Plugin} instance.
     *
     * @return the plugin
     */
    @NotNull
    public static Plugin getPlugin() {
        return service().asPlugin();
    }
}
