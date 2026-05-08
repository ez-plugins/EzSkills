package com.github.ezplugins.ezskills;

import com.github.ezplugins.ezskills.ability.AbilityDefinitionRegistry;
import com.github.ezplugins.ezskills.ability.AbilityManager;
import com.github.ezplugins.ezskills.api.EzSkillsAPI;
import com.github.ezplugins.ezskills.command.AbilitiesCommand;
import com.github.ezplugins.ezskills.command.EzSkillsCommand;
import com.github.ezplugins.ezskills.command.EzSkillsTabCompleter;
import com.github.ezplugins.ezskills.command.SkillsCommand;
import com.github.ezplugins.ezskills.config.ConfigManager;
import com.github.ezplugins.ezskills.gui.AbilityMenuListener;
import com.github.ezplugins.ezskills.gui.AbilityOverviewMenu;
import com.github.ezplugins.ezskills.gui.SkillMenuListener;
import com.github.ezplugins.ezskills.gui.SkillProgressMenu;
import com.github.ezplugins.ezskills.notification.LevelUpNotifier;
import com.github.ezplugins.ezskills.service.EzSkillsServiceImpl;
import com.github.ezplugins.ezskills.skill.SkillDefinitionRegistry;
import com.github.ezplugins.ezskills.skill.SkillManager;
import com.github.ezplugins.ezskills.skill.SkillProfileListener;
import com.github.ezplugins.ezskills.storage.StorageManager;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the complete plugin startup and shutdown sequence.
 *
 * <p>On {@link #start()} all components are constructed, wired together, registered
 * with Bukkit and stored in {@link Registry}. On {@link #stop()} the reverse happens:
 * the API facade is cleared, managers are shut down and the registry is emptied.</p>
 */
public final class Bootstrap {

    /** The owning plugin instance. */
    private final JavaPlugin plugin;

    /**
     * Creates a new Bootstrap for the given plugin.
     *
     * @param plugin the owning plugin instance
     */
    Bootstrap(@NotNull final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Runs the full startup sequence.
     */
    void start() {
        saveDefaults();

        final ConfigManager configManager = new ConfigManager(plugin);
        configManager.reload();

        final StorageManager storageManager = new StorageManager(plugin, configManager);
        storageManager.initialise();

        final SkillDefinitionRegistry skillDefinitionRegistry = new SkillDefinitionRegistry();

        final SkillManager skillManager =
                new SkillManager(plugin, configManager, storageManager.getRepository(),
                        skillDefinitionRegistry);

        final AbilityDefinitionRegistry abilityDefinitionRegistry = new AbilityDefinitionRegistry();
        abilityDefinitionRegistry.registerBuiltIns();

        final AbilityManager abilityManager = new AbilityManager(plugin, configManager);

        Registry.register(new Registry(
                configManager, storageManager, skillManager,
                abilityDefinitionRegistry, abilityManager, skillDefinitionRegistry));

        EzSkillsAPI.init(new EzSkillsServiceImpl(
                plugin, skillManager, abilityManager, configManager, abilityDefinitionRegistry,
                skillDefinitionRegistry));

        registerListeners(skillManager, abilityManager, configManager);
        registerCommands(skillManager, abilityManager, configManager,
                abilityDefinitionRegistry, abilityOverviewMenu(abilityDefinitionRegistry, abilityManager),
                skillProgressMenu(skillManager, configManager));
        registerOptionalIntegrations(configManager);

        plugin.getLogger().info("EzSkills enabled.");
    }

    /**
     * Runs the full shutdown sequence.
     */
    void stop() {
        final Registry registry = registryIfPresent();
        if (registry != null) {
            registry.getSkillManager().shutdown();
        }
        EzSkillsAPI.shutdown();
        if (registry != null) {
            registry.getStorageManager().shutdown();
        }
        Registry.unregister();
        plugin.getLogger().info("EzSkills disabled.");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void saveDefaults() {
        plugin.saveDefaultConfig();
        plugin.saveResource("skills.yml", false);
        plugin.saveResource("storage.yml", false);
        plugin.saveResource("abilities.yml", false);
    }

    private void registerListeners(@NotNull final SkillManager skillManager,
                                   @NotNull final AbilityManager abilityManager,
                                   @NotNull final ConfigManager configManager) {
        final var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new SkillProfileListener(skillManager, abilityManager), plugin);
        pm.registerEvents(new SkillMenuListener(), plugin);
        pm.registerEvents(new AbilityMenuListener(), plugin);
    }

    private void registerCommands(@NotNull final SkillManager skillManager,
                                  @NotNull final AbilityManager abilityManager,
                                  @NotNull final ConfigManager configManager,
                                  @NotNull final AbilityDefinitionRegistry abilityDefinitionRegistry,
                                  @NotNull final AbilityOverviewMenu abilityOverviewMenu,
                                  @NotNull final SkillProgressMenu skillProgressMenu) {
        Objects.requireNonNull(plugin.getCommand("ezskills"))
                .setExecutor(new EzSkillsCommand(skillManager, abilityManager, configManager, plugin));
        Objects.requireNonNull(plugin.getCommand("ezskills"))
                .setTabCompleter(new EzSkillsTabCompleter(abilityDefinitionRegistry));
        Objects.requireNonNull(plugin.getCommand("skills"))
                .setExecutor(new SkillsCommand(skillManager, skillProgressMenu));
        Objects.requireNonNull(plugin.getCommand("abilities"))
                .setExecutor(new AbilitiesCommand(abilityOverviewMenu));
    }

    private void registerOptionalIntegrations(@NotNull final ConfigManager configManager) {
        if (plugin.getServer().getPluginManager().getPlugin("EzCountdown") != null) {
            final LevelUpNotifier notifier = LevelUpNotifier.tryCreate(configManager);
            if (notifier != null) {
                plugin.getServer().getPluginManager().registerEvents(notifier, plugin);
                plugin.getLogger().info(
                        "EzCountdown detected — bossbar level-up notifications enabled.");
            }
        }
    }

    @NotNull
    private SkillProgressMenu skillProgressMenu(@NotNull final SkillManager skillManager,
                                                @NotNull final ConfigManager configManager) {
        return new SkillProgressMenu(skillManager, configManager);
    }

    @NotNull
    private AbilityOverviewMenu abilityOverviewMenu(
            @NotNull final AbilityDefinitionRegistry abilityDefinitionRegistry,
            @NotNull final AbilityManager abilityManager) {
        return new AbilityOverviewMenu(abilityDefinitionRegistry, abilityManager);
    }

    /**
     * Returns the registry if the plugin is active, or {@code null} if it was never started.
     *
     * @return the active {@link Registry}, or {@code null}
     */
    private Registry registryIfPresent() {
        try {
            return Registry.get();
        }
        catch (IllegalStateException ignored) {
            return null;
        }
    }
}
