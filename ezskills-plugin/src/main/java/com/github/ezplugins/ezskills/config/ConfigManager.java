package com.github.ezplugins.ezskills.config;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Manages loading and reloading of all EzSkills YAML configuration files.
 *
 * <p>Each config file is loaded from the plugin's data folder on demand.
 * If a file is missing, the bundled default is saved before loading.</p>
 */
public final class ConfigManager {

    /** The owning plugin instance. */
    private final JavaPlugin plugin;

    /** Loaded {@code skills.yml} configuration. */
    private FileConfiguration skillsConfig;

    /** Loaded {@code storage.yml} configuration. */
    private FileConfiguration storageConfig;

    /** Loaded {@code abilities.yml} configuration. */
    private FileConfiguration abilitiesConfig;

    /**
     * Creates a new ConfigManager for the given plugin.
     *
     * @param plugin the owning plugin
     */
    public ConfigManager(@NotNull final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads {@code config.yml} and all secondary configuration files from disk.
     */
    public void reload() {
        plugin.reloadConfig();
        skillsConfig    = loadConfig("skills.yml");
        storageConfig   = loadConfig("storage.yml");
        abilitiesConfig = loadConfig("abilities.yml");
    }

    /**
     * Returns the loaded {@code skills.yml} configuration.
     *
     * @return the skills configuration
     */
    @NotNull
    public FileConfiguration getSkillsConfig() {
        return skillsConfig;
    }

    /**
     * Returns the loaded {@code storage.yml} configuration.
     *
     * @return the storage configuration
     */
    @NotNull
    public FileConfiguration getStorageConfig() {
        return storageConfig;
    }

    /**
     * Returns the loaded {@code abilities.yml} configuration.
     *
     * @return the abilities configuration
     */
    @NotNull
    public FileConfiguration getAbilitiesConfig() {
        return abilitiesConfig;
    }

    /**
     * Returns the main {@code config.yml} through the plugin's built-in config loader.
     *
     * @return the main plugin configuration
     */
    @NotNull
    public FileConfiguration getMainConfig() {
        return plugin.getConfig();
    }

    // -------------------------------------------------------------------------
    // Plugin-override helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the XP multiplier configured for the given plugin and skill under
     * {@code plugin-overrides.<pluginName>.<skillName>.xp-multiplier} in {@code config.yml}.
     *
     * <p>Defaults to {@code 1.0} when no override is present.</p>
     *
     * @param pluginName the plugin name (case-insensitive in config lookups)
     * @param skillName  the skill name (e.g. {@code "WOODCUTTING"})
     * @return the configured multiplier, or {@code 1.0}
     */
    public double getXpMultiplier(@NotNull final String pluginName,
                                  @NotNull final String skillName) {
        final String key = "plugin-overrides." + pluginName
                + "." + skillName.toLowerCase() + ".xp-multiplier";
        return plugin.getConfig().getDouble(key, 1.0);
    }

    /**
     * Returns whether the given plugin is allowed to award XP for the given skill.
     *
     * <p>Returns {@code true} (allowed) when no override block exists for the plugin,
     * preserving backwards-compatible behaviour for unlisted plugins.</p>
     *
     * @param pluginName the plugin name
     * @param skillName  the skill name
     * @return {@code false} only when explicitly set to {@code false} in config
     */
    public boolean isSkillEnabled(@NotNull final String pluginName,
                                  @NotNull final String skillName) {
        final String key = "plugin-overrides." + pluginName
                + "." + skillName.toLowerCase() + ".enabled";
        return plugin.getConfig().getBoolean(key, true);
    }

    private FileConfiguration loadConfig(final String name) {
        final File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
