package com.github.ezplugins.ezskills;

import com.github.ezplugins.ezskills.ability.AbilityDefinitionRegistry;
import com.github.ezplugins.ezskills.ability.AbilityManager;
import com.github.ezplugins.ezskills.config.ConfigManager;
import com.github.ezplugins.ezskills.skill.SkillDefinitionRegistry;
import com.github.ezplugins.ezskills.skill.SkillManager;
import com.github.ezplugins.ezskills.storage.StorageManager;
import org.jetbrains.annotations.NotNull;

/**
 * Central registry that holds every live plugin component instance.
 *
 * <p>Populated by {@link Bootstrap#start()} and cleared by {@link Bootstrap#stop()}.
 * External callers (commands, listeners, etc.) may obtain components via
 * {@link #get()} after the plugin has been enabled.</p>
 */
public final class Registry {

    /** Singleton; non-null only while the plugin is enabled. */
    private static Registry instance;

    /** Manages configuration files for the plugin. */
    private final ConfigManager configManager;

    /** Manages the storage backend (YAML or MySQL). */
    private final StorageManager storageManager;

    /** Manages skill profiles and XP/level operations. */
    private final SkillManager skillManager;

    /** Registry of all registered ability definitions (built-in and custom). */
    private final AbilityDefinitionRegistry abilityDefinitionRegistry;

    /** Manages per-player ability state (preparing, active, cooldown). */
    private final AbilityManager abilityManager;

    /** Registry of custom skill definitions registered by external plugins. */
    private final SkillDefinitionRegistry skillDefinitionRegistry;

    Registry(@NotNull final ConfigManager configManager,
             @NotNull final StorageManager storageManager,
             @NotNull final SkillManager skillManager,
             @NotNull final AbilityDefinitionRegistry abilityDefinitionRegistry,
             @NotNull final AbilityManager abilityManager,
             @NotNull final SkillDefinitionRegistry skillDefinitionRegistry) {
        this.configManager = configManager;
        this.storageManager = storageManager;
        this.skillManager = skillManager;
        this.abilityDefinitionRegistry = abilityDefinitionRegistry;
        this.abilityManager = abilityManager;
        this.skillDefinitionRegistry = skillDefinitionRegistry;
    }

    // -------------------------------------------------------------------------
    // Lifecycle (package-private — only Bootstrap touches these)
    // -------------------------------------------------------------------------

    static void register(@NotNull final Registry registry) {
        instance = registry;
    }

    static void unregister() {
        instance = null;
    }

    // -------------------------------------------------------------------------
    // Access
    // -------------------------------------------------------------------------

    /**
     * Returns the active {@link Registry} instance.
     *
     * @return the registry
     * @throws IllegalStateException if EzSkills is not currently enabled
     */
    @NotNull
    public static Registry get() {
        if (instance == null) {
            throw new IllegalStateException("EzSkills is not enabled on this server.");
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Component getters
    // -------------------------------------------------------------------------

    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @NotNull
    public StorageManager getStorageManager() {
        return storageManager;
    }

    @NotNull
    public SkillManager getSkillManager() {
        return skillManager;
    }

    @NotNull
    public AbilityDefinitionRegistry getAbilityDefinitionRegistry() {
        return abilityDefinitionRegistry;
    }

    @NotNull
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    @NotNull
    public SkillDefinitionRegistry getSkillDefinitionRegistry() {
        return skillDefinitionRegistry;
    }
}
