package com.github.ezplugins.ezskills.api;

import com.github.ezplugins.ezskills.ability.AbilityDefinition;
import com.github.ezplugins.ezskills.skill.SkillDefinition;
import com.github.ezplugins.ezskills.skill.SkillProfile;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service interface implemented by the EzSkills plugin.
 *
 * <p>External plugins should not depend on this interface directly; use
 * {@link EzSkillsAPI} instead for a stable static facade.</p>
 */
public interface EzSkillsService {

    /**
     * Returns the skill level for the given player and skill name.
     *
     * @param playerId  the player's unique ID
     * @param skillName the skill name (e.g. {@code "WOODCUTTING"})
     * @return the current level, or {@code 1} if the player has no data
     */
    int getSkillLevel(@NotNull UUID playerId, @NotNull String skillName);

    /**
     * Returns the skill level for the given offline player.
     *
     * @param player    the offline player
     * @param skillName the skill name
     * @return the current level
     */
    int getSkillLevel(@NotNull OfflinePlayer player, @NotNull String skillName);

    /**
     * Returns the accumulated experience for the given player and skill.
     *
     * @param playerId  the player's unique ID
     * @param skillName the skill name
     * @return the current experience
     */
    double getSkillExperience(@NotNull UUID playerId, @NotNull String skillName);

    /**
     * Returns the full skill profile for a player, or {@code null} if not loaded.
     *
     * @param playerId the player's unique ID
     * @return the profile, or {@code null}
     */
    @Nullable SkillProfile getSkillProfile(@NotNull UUID playerId);

    /**
     * Adds experience to the specified skill for the given player.
     *
     * @param playerId  the player's unique ID
     * @param skillName the skill name
     * @param amount    the amount of experience to add (must be positive)
     */
    void addExperience(@NotNull UUID playerId, @NotNull String skillName, double amount);

    /**
     * Adds experience to the specified skill for the given player, applying the XP multiplier
     * and enabled flag configured for {@code source} in EzSkills' {@code config.yml}.
     *
     * <p>If the server admin has disabled the skill for this plugin, the call is a no-op.
     * If a multiplier is configured it is applied before the amount is credited.</p>
     *
     * <p>This is the <strong>preferred method</strong> for third-party plugins, because it lets
     * server admins control every plugin's contribution centrally.</p>
     *
     * @param source    the calling plugin (used as the config key)
     * @param playerId  the player's unique ID
     * @param skillName the skill name (e.g. {@code "WOODCUTTING"})
     * @param amount    the raw amount of experience to add (multiplier is applied by EzSkills)
     */
    void addExperience(@NotNull Plugin source,
                       @NotNull UUID playerId,
                       @NotNull String skillName,
                       double amount);

    /**
     * Sets the skill level for the given player directly, resetting experience to zero.
     *
     * @param playerId  the player's unique ID
     * @param skillName the skill name
     * @param level     the new level (must be &gt;= 1)
     */
    void setSkillLevel(@NotNull UUID playerId, @NotNull String skillName, int level);

    /**
     * Returns whether the specified ability is currently active for the given player.
     *
     * @param playerId    the player's unique ID
     * @param abilityName the ability name (e.g. {@code "TREE_FELLER"})
     * @return {@code true} if the ability is active
     */
    boolean isAbilityActive(@NotNull UUID playerId, @NotNull String abilityName);

    /**
     * Puts the ability into the preparation (charged) state for the given player.
     *
     * @param playerId    the player's unique ID
     * @param abilityName the ability name
     */
    void prepareAbility(@NotNull UUID playerId, @NotNull String abilityName);

    /**
     * Activates the ability for the given player.
     *
     * @param playerId    the player's unique ID
     * @param abilityName the ability name
     */
    void activateAbility(@NotNull UUID playerId, @NotNull String abilityName);

    /**
     * Deactivates the ability for the given player.
     *
     * @param playerId    the player's unique ID
     * @param abilityName the ability name
     */
    void deactivateAbility(@NotNull UUID playerId, @NotNull String abilityName);

    /**
     * Returns the preparation window in milliseconds for the given ability.
     *
     * @param abilityName the ability name
     * @return window in milliseconds
     */
    long getAbilityPreparationWindowMillis(@NotNull String abilityName);

    /**
     * Registers a {@link SkillDefinition} so that it is tracked by EzSkills.
     *
     * <p>Call this from your plugin's {@code onEnable} <em>before</em> any players join.
     * The skill name must not clash with a built-in {@link com.github.ezplugins.ezskills.skill.SkillType}.</p>
     *
     * @param definition the definition to register
     * @throws IllegalArgumentException if the name conflicts with a built-in skill
     */
    void registerSkill(@NotNull SkillDefinition definition);

    /**
     * Returns all currently registered custom skill definitions.
     *
     * @return unmodifiable list of definitions
     */
    @NotNull
    List<SkillDefinition> getRegisteredSkills();

    /**
     * Registers an {@link AbilityDefinition} so that it appears in the ability overview GUI.
     *
     * <p>If an ability with the same name (case-insensitive) is already registered it is replaced.
     * Built-in abilities are registered automatically; do not re-register them.</p>
     *
     * @param definition the definition to register
     */
    void registerAbility(@NotNull AbilityDefinition definition);

    /**
     * Returns all currently registered ability definitions, including built-in ones.
     *
     * @return unmodifiable list of definitions
     */
    @NotNull
    List<AbilityDefinition> getRegisteredAbilities();

    /**
     * Reloads all plugin configuration files.
     */
    void reloadConfigs();

    /**
     * Returns the {@link Plugin} instance backing this service.
     *
     * @return the plugin
     */
    @NotNull Plugin asPlugin();
}
