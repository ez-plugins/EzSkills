package com.github.ezplugins.ezskills.service;

import com.github.ezplugins.ezskills.ability.AbilityDefinition;
import com.github.ezplugins.ezskills.ability.AbilityDefinitionRegistry;
import com.github.ezplugins.ezskills.ability.AbilityManager;
import com.github.ezplugins.ezskills.ability.AbilityType;
import com.github.ezplugins.ezskills.api.EzSkillsService;
import com.github.ezplugins.ezskills.config.ConfigManager;
import com.github.ezplugins.ezskills.skill.SkillDefinition;
import com.github.ezplugins.ezskills.skill.SkillDefinitionRegistry;
import com.github.ezplugins.ezskills.skill.SkillManager;
import com.github.ezplugins.ezskills.skill.SkillProfile;
import com.github.ezplugins.ezskills.skill.SkillType;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Concrete implementation of {@link EzSkillsService}.
 *
 * <p>Bridges the static {@link com.github.ezplugins.ezskills.api.EzSkillsAPI} facade
 * to the plugin's runtime managers, keeping the main plugin class free of service
 * method boilerplate.</p>
 */
public final class EzSkillsServiceImpl implements EzSkillsService {

    /** Plugin instance returned by {@link #asPlugin()}. */
    private final JavaPlugin plugin;

    /** Manages skill levels and experience for all online players. */
    private final SkillManager skillManager;

    /** Manages ability states (preparation, activation, deactivation). */
    private final AbilityManager abilityManager;

    /** Handles configuration reloading. */
    private final ConfigManager configManager;

    /** Registry of all registered ability definitions. */
    private final AbilityDefinitionRegistry abilityDefinitionRegistry;

    /** Registry of all custom skill definitions registered by external plugins. */
    private final SkillDefinitionRegistry skillDefinitionRegistry;

    /**
     * Creates a new service implementation.
     *
     * @param plugin                    the owning plugin instance
     * @param skillManager              the skill manager
     * @param abilityManager            the ability manager
     * @param configManager             the configuration manager
     * @param abilityDefinitionRegistry the ability definition registry
     * @param skillDefinitionRegistry   the skill definition registry
     */
    public EzSkillsServiceImpl(@NotNull final JavaPlugin plugin,
                               @NotNull final SkillManager skillManager,
                               @NotNull final AbilityManager abilityManager,
                               @NotNull final ConfigManager configManager,
                               @NotNull final AbilityDefinitionRegistry abilityDefinitionRegistry,
                               @NotNull final SkillDefinitionRegistry skillDefinitionRegistry) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.abilityManager = abilityManager;
        this.configManager = configManager;
        this.abilityDefinitionRegistry = abilityDefinitionRegistry;
        this.skillDefinitionRegistry = skillDefinitionRegistry;
    }

    @Override
    public int getSkillLevel(@NotNull final UUID playerId, @NotNull final String skillName) {
        final SkillType type = SkillType.fromString(skillName);
        if (type != null) {
            return skillManager.getLevel(playerId, type);
        }
        if (skillDefinitionRegistry.isRegistered(skillName)) {
            return skillManager.getCustomLevel(playerId, skillName);
        }
        return 1;
    }

    @Override
    public int getSkillLevel(@NotNull final OfflinePlayer player, @NotNull final String skillName) {
        return getSkillLevel(player.getUniqueId(), skillName);
    }

    @Override
    public double getSkillExperience(@NotNull final UUID playerId, @NotNull final String skillName) {
        final SkillType type = SkillType.fromString(skillName);
        if (type != null) {
            return skillManager.getExperience(playerId, type);
        }
        if (skillDefinitionRegistry.isRegistered(skillName)) {
            return skillManager.getCustomExperience(playerId, skillName);
        }
        return 0.0;
    }

    @Override
    @Nullable
    public SkillProfile getSkillProfile(@NotNull final UUID playerId) {
        return skillManager.getCachedProfile(playerId);
    }

    @Override
    public void addExperience(@NotNull final UUID playerId,
                              @NotNull final String skillName,
                              final double amount) {
        final SkillType type = SkillType.fromString(skillName);
        if (type != null) {
            skillManager.addExperience(playerId, type, amount);
            return;
        }
        if (skillDefinitionRegistry.isRegistered(skillName)) {
            skillManager.addCustomExperience(playerId, skillName, amount);
        }
    }

    @Override
    public void addExperience(@NotNull final Plugin source,
                              @NotNull final UUID playerId,
                              @NotNull final String skillName,
                              final double amount) {
        if (!configManager.isSkillEnabled(source.getName(), skillName)) {
            return;
        }
        final double multiplier = configManager.getXpMultiplier(source.getName(), skillName);
        addExperience(playerId, skillName, amount * multiplier);
    }

    @Override
    public void setSkillLevel(@NotNull final UUID playerId,
                              @NotNull final String skillName,
                              final int level) {
        final SkillType type = SkillType.fromString(skillName);
        if (type != null) {
            skillManager.setLevel(playerId, type, level);
            return;
        }
        if (skillDefinitionRegistry.isRegistered(skillName)) {
            skillManager.setCustomLevel(playerId, skillName, level);
        }
    }

    @Override
    public void registerSkill(@NotNull final SkillDefinition definition) {
        skillDefinitionRegistry.register(definition);
    }

    @Override
    @NotNull
    public List<SkillDefinition> getRegisteredSkills() {
        return skillDefinitionRegistry.getAll();
    }

    @Override
    public boolean isAbilityActive(@NotNull final UUID playerId, @NotNull final String abilityName) {
        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return false;
        }
        return abilityManager.isActive(player, abilityName);
    }

    @Override
    public void prepareAbility(@NotNull final UUID playerId, @NotNull final String abilityName) {
        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        final AbilityType type = AbilityType.fromString(abilityName);
        if (type != null) {
            abilityManager.prepareAbility(player, type);
            return;
        }
        final AbilityDefinition def = findDefinition(abilityName);
        if (def != null) {
            final long windowMillis = (long) def.getPreparationWindowSeconds() * 1000L;
            abilityManager.prepareCustomAbility(player, abilityName, windowMillis);
        }
    }

    @Override
    public void activateAbility(@NotNull final UUID playerId, @NotNull final String abilityName) {
        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        final AbilityType type = AbilityType.fromString(abilityName);
        if (type != null) {
            abilityManager.activateAbility(player, type);
            return;
        }
        final AbilityDefinition def = findDefinition(abilityName);
        if (def != null) {
            final long durationTicks = (long) def.getActiveDurationSeconds() * 20L;
            abilityManager.activateCustomAbility(player, abilityName, durationTicks);
        }
    }

    @Override
    public void deactivateAbility(@NotNull final UUID playerId, @NotNull final String abilityName) {
        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        final AbilityType type = AbilityType.fromString(abilityName);
        if (type != null) {
            abilityManager.deactivateAbility(player, type);
            return;
        }
        abilityManager.deactivateCustomAbility(player, abilityName);
    }

    @Override
    public long getAbilityPreparationWindowMillis(@NotNull final String abilityName) {
        final AbilityType type = AbilityType.fromString(abilityName);
        if (type == null) {
            return 0L;
        }
        return abilityManager.getPreparationWindowMillis(type);
    }

    @Override
    public void reloadConfigs() {
        configManager.reload();
    }

    @Override
    public void registerAbility(@NotNull final AbilityDefinition definition) {
        abilityDefinitionRegistry.register(definition);
    }

    @Override
    @NotNull
    public List<AbilityDefinition> getRegisteredAbilities() {
        return abilityDefinitionRegistry.getAll();
    }

    @Override
    @NotNull
    public Plugin asPlugin() {
        return plugin;
    }

    /**
     * Looks up a registered ability definition by name (case-insensitive).
     *
     * @param name the ability name
     * @return the definition, or {@code null} if not found
     */
    @Nullable
    private AbilityDefinition findDefinition(final String name) {
        return abilityDefinitionRegistry.getAll().stream()
                .filter(d -> d.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
