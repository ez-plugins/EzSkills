package com.github.ezplugins.ezskills.skill;

import com.github.ezframework.jaloquent.exception.StorageException;
import com.github.ezframework.jaloquent.model.ModelRepository;
import com.github.ezframework.javaquerybuilder.query.builder.QueryBuilder;
import com.github.ezplugins.ezskills.api.event.SkillLevelUpEvent;
import com.github.ezplugins.ezskills.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages in-memory skill profiles and dispatches async storage operations via a
 * dedicated worker thread to avoid blocking the main server thread.
 */
public final class SkillManager {

    /** Plugin instance used for logging and scheduling. */
    private final JavaPlugin plugin;

    /** Provides access to skills configuration. */
    private final ConfigManager configManager;

    /** Jaloquent repository for persistent storage. */
    private final ModelRepository<SkillProfileModel> repository;

    /** Registry of custom skill definitions; used when loading profiles and handling custom XP. */
    private final SkillDefinitionRegistry skillDefinitionRegistry;

    /** In-memory cache of loaded skill profiles, keyed by player UUID. */
    private final Map<UUID, SkillProfile> profiles = new ConcurrentHashMap<>();

    /** Active future handles for in-progress profile loads, keyed by player UUID. */
    private final Map<UUID, CompletableFuture<SkillProfile>> loadingFutures = new ConcurrentHashMap<>();

    /**
     * Set of UUIDs that have been unloaded while a load was still in flight.
     * The completing {@link LoadTask} checks this and discards the result instead of
     * putting it back into the cache (preventing a zombie profile resurrection).
     */
    private final Set<UUID> unloadedWhileLoading = ConcurrentHashMap.newKeySet();

    /** Queue of storage operations for the background worker thread. */
    private final BlockingQueue<StorageTask> taskQueue = new LinkedBlockingQueue<>();

    /** Background thread that processes all storage read/write tasks. */
    private final Thread worker;

    /** Controls the main loop of the background worker thread. */
    private volatile boolean running = true;

    public SkillManager(@NotNull JavaPlugin plugin,
                        @NotNull ConfigManager configManager,
                        @NotNull ModelRepository<SkillProfileModel> repository,
                        @NotNull SkillDefinitionRegistry skillDefinitionRegistry) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.repository = repository;
        this.skillDefinitionRegistry = skillDefinitionRegistry;
        this.worker = new Thread(this::processQueue, "EzSkills-StorageWorker");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    // -------------------------------------------------------------------------
    // Profile cache
    // -------------------------------------------------------------------------

    @Nullable
    public SkillProfile getCachedProfile(@NotNull UUID playerId) {
        return profiles.get(playerId);
    }

    /**
     * Asynchronously loads a player's profile from storage into the cache.
     * If already loaded, the returned future completes immediately.
     *
     * @param playerId the player UUID
     * @return a future that resolves to the loaded profile
     */
    @NotNull
    public CompletableFuture<SkillProfile> loadProfile(@NotNull UUID playerId) {
        final SkillProfile cached = profiles.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        final CompletableFuture<SkillProfile> future = new CompletableFuture<>();
        final CompletableFuture<SkillProfile> existing = loadingFutures.putIfAbsent(playerId, future);
        if (existing != null) {
            return existing;
        }

        taskQueue.add(new LoadTask(playerId, future));
        return future;
    }

    /**
     * Saves the cached profile for the given player to storage asynchronously.
     * Uses a fire-and-forget queue.
     *
     * @param playerId the player UUID
     */
    public void saveProfile(@NotNull UUID playerId) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile != null) {
            taskQueue.add(new SaveTask(playerId, profile));
        }
    }

    /**
     * Saves and unloads a player profile from the in-memory cache.
     *
     * @param playerId the player UUID
     */
    public void unloadProfile(@NotNull UUID playerId) {
        saveProfile(playerId);
        profiles.remove(playerId);
        // If a LoadTask is still in the queue for this player, mark it so it
        // won't put a stale profile back into the cache when it completes.
        if (loadingFutures.remove(playerId) != null) {
            unloadedWhileLoading.add(playerId);
        }
    }

    // -------------------------------------------------------------------------
    // Skill mutations
    // -------------------------------------------------------------------------

    /**
     * Returns the skill level for the given player, or {@code 1} if not cached.
     *
     * @param playerId the player UUID
     * @param type     the skill type
     * @return the current level, or {@code 1} if the profile is not loaded
     */
    public int getLevel(@NotNull UUID playerId, @NotNull SkillType type) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return 1;
        }
        return profile.getProgress(type).getLevel();
    }

    /**
     * Returns the accumulated experience for the given player, or {@code 0} if not cached.
     *
     * @param playerId the player UUID
     * @param type     the skill type
     * @return the current experience, or {@code 0.0} if the profile is not loaded
     */
    public double getExperience(@NotNull UUID playerId, @NotNull SkillType type) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return 0.0;
        }
        return profile.getProgress(type).getExperience();
    }

    /**
     * Adds experience to the player's skill and checks for level-ups.
     *
     * @param playerId the player UUID
     * @param type     the skill type
     * @param amount   the amount to add (must be positive)
     */
    public void addExperience(@NotNull UUID playerId, @NotNull SkillType type, double amount) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return;
        }

        final SkillProgress progress = profile.getProgress(type);
        progress.setExperience(progress.getExperience() + amount);

        checkLevelUp(playerId, type, profile);
        taskQueue.add(new SaveTask(playerId, profile));
    }

    /**
     * Sets the skill level directly, resetting experience to zero.
     *
     * @param playerId the player UUID
     * @param type     the skill type
     * @param level    the new level (must be &gt;= 1)
     */
    public void setLevel(@NotNull UUID playerId, @NotNull SkillType type, int level) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return;
        }

        final SkillProgress progress = profile.getProgress(type);
        progress.setLevel(Math.max(1, level));
        progress.setExperience(0.0);
        taskQueue.add(new SaveTask(playerId, profile));
    }

    /**
     * Resets one skill to level 1 with zero experience.
     *
     * @param playerId the player UUID
     * @param type     the skill type to reset
     */
    public void resetSkill(@NotNull UUID playerId, @NotNull SkillType type) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return;
        }
        final SkillProgress progress = profile.getProgress(type);
        progress.setLevel(1);
        progress.setExperience(0.0);
        taskQueue.add(new SaveTask(playerId, profile));
    }

    /**
     * Resets all skills to level 1 with zero experience in a single save operation.
     *
     * @param playerId the player UUID
     */
    public void resetAllSkills(@NotNull UUID playerId) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return;
        }
        for (final SkillType type : SkillType.values()) {
            final SkillProgress progress = profile.getProgress(type);
            progress.setLevel(1);
            progress.setExperience(0.0);
        }
        taskQueue.add(new SaveTask(playerId, profile));
    }

    // -------------------------------------------------------------------------
    // Custom skill mutations (string-based, for externally registered skills)
    // -------------------------------------------------------------------------

    /**
     * Returns the level for a custom skill, or {@code 1} if the profile is not loaded or
     * the skill has no entry yet.
     *
     * @param playerId  the player UUID
     * @param skillName the custom skill name (case-insensitive)
     * @return the current level
     */
    public int getCustomLevel(@NotNull UUID playerId, @NotNull String skillName) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return 1;
        }
        return profile.getCustomProgress(skillName).getLevel();
    }

    /**
     * Returns the accumulated experience for a custom skill, or {@code 0.0} if not loaded.
     *
     * @param playerId  the player UUID
     * @param skillName the custom skill name (case-insensitive)
     * @return the current experience
     */
    public double getCustomExperience(@NotNull UUID playerId, @NotNull String skillName) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return 0.0;
        }
        return profile.getCustomProgress(skillName).getExperience();
    }

    /**
     * Adds experience to a custom skill and checks for level-ups.
     *
     * @param playerId  the player UUID
     * @param skillName the custom skill name (case-insensitive)
     * @param amount    the amount to add (must be positive)
     */
    public void addCustomExperience(@NotNull UUID playerId,
                                    @NotNull String skillName,
                                    double amount) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return;
        }
        final SkillProgress progress = profile.getCustomProgress(skillName);
        progress.setExperience(progress.getExperience() + amount);
        checkCustomLevelUp(playerId, skillName, profile);
        taskQueue.add(new SaveTask(playerId, profile));
    }

    /**
     * Sets the level for a custom skill directly, resetting experience to zero.
     *
     * @param playerId  the player UUID
     * @param skillName the custom skill name (case-insensitive)
     * @param level     the new level (must be &gt;= 1)
     */
    public void setCustomLevel(@NotNull UUID playerId,
                               @NotNull String skillName,
                               int level) {
        final SkillProfile profile = profiles.get(playerId);
        if (profile == null) {
            return;
        }
        final SkillProgress progress = profile.getCustomProgress(skillName);
        progress.setLevel(Math.max(1, level));
        progress.setExperience(0.0);
        taskQueue.add(new SaveTask(playerId, profile));
    }

    /**
     * Queries the top {@code limit} players by level for the given skill type.
     * Requires a SQL-backed store; throws {@link StorageException} for flat-map stores.
     *
     * @param type  the skill type
     * @param limit maximum number of results
     * @return ordered list of profile models (highest level first)
     * @throws StorageException if the underlying store does not support queries
     */
    @NotNull
    public List<SkillProfileModel> queryLeaderboard(@NotNull SkillType type, int limit)
            throws StorageException {
        return repository.query(
                new QueryBuilder()
                        .orderBy(type.name().toLowerCase() + "_level", false)
                        .limit(limit)
                        .build()
        );
    }

    /**
     * Returns all currently cached (online) player profiles sorted by level
     * descending for the given skill type.  Used as a fallback leaderboard when
     * the store does not support {@link #queryLeaderboard}.
     *
     * @param type  the skill type
     * @param limit maximum number of results
     * @return list of (UUID, SkillProfile) entries, highest level first
     */
    @NotNull
    public List<Map.Entry<UUID, SkillProfile>> getOnlineLeaderboard(
            @NotNull SkillType type, int limit) {
        final List<Map.Entry<UUID, SkillProfile>> entries = new ArrayList<>(profiles.entrySet());
        entries.sort((a, b) ->
                b.getValue().getProgress(type).getLevel()
                - a.getValue().getProgress(type).getLevel());
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    // -------------------------------------------------------------------------
    // Shutdown
    // -------------------------------------------------------------------------

    /**
     * Gracefully shuts down the storage worker, flushing all pending tasks and
     * force-saving all cached profiles before returning.
     */
    public void shutdown() {
        running = false;
        worker.interrupt();

        // Drain queued tasks
        while (!taskQueue.isEmpty()) {
            final StorageTask task = taskQueue.poll();
            if (task != null) {
                task.run(this);
            }
        }

        // Force-save all remaining cached profiles
        for (Map.Entry<UUID, SkillProfile> entry : profiles.entrySet()) {
            try {
                final SkillProfileModel model = SkillProfileModel.fromSkillProfile(entry.getKey(), entry.getValue());
                repository.save(model);
            }
            catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to save profile for " + entry.getKey() + " on shutdown", e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void checkLevelUp(UUID playerId, SkillType type, SkillProfile profile) {
        final SkillProgress progress = profile.getProgress(type);
        final int maxLevel = getMaxLevel(type);
        double xpForNext = xpForNextLevel(type, progress.getLevel());

        while (progress.getExperience() >= xpForNext && progress.getLevel() < maxLevel) {
            final int oldLevel = progress.getLevel();
            progress.setExperience(progress.getExperience() - xpForNext);
            progress.setLevel(oldLevel + 1);
            xpForNext = xpForNextLevel(type, progress.getLevel());

            final int newLevel = progress.getLevel();
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                final SkillLevelUpEvent event = new SkillLevelUpEvent(player, type, oldLevel, newLevel);
                Bukkit.getPluginManager().callEvent(event);
            }
        }

        // At max level, stop accumulating overflow XP.
        if (progress.getLevel() >= maxLevel) {
            progress.setExperience(0);
        }
    }

    private double xpForNextLevel(SkillType type, int currentLevel) {
        final String key = type.name().toLowerCase();
        final double base = configManager.getSkillsConfig().getDouble(key + ".xp-base", 100.0);
        final double multiplier = configManager.getSkillsConfig().getDouble(key + ".xp-multiplier", 1.5);
        return base * Math.pow(multiplier, currentLevel - 1);
    }

    private int getMaxLevel(SkillType type) {
        final String key = type.name().toLowerCase() + ".max-level";
        return configManager.getSkillsConfig().getInt(key, 100);
    }

    private void checkCustomLevelUp(UUID playerId, String skillName, SkillProfile profile) {
        final SkillProgress progress = profile.getCustomProgress(skillName);
        final int maxLevel = getCustomMaxLevel(skillName);
        double xpForNext = xpForCustomNextLevel(skillName, progress.getLevel());

        while (progress.getExperience() >= xpForNext && progress.getLevel() < maxLevel) {
            progress.setExperience(progress.getExperience() - xpForNext);
            progress.setLevel(progress.getLevel() + 1);
            xpForNext = xpForCustomNextLevel(skillName, progress.getLevel());
        }

        if (progress.getLevel() >= maxLevel) {
            progress.setExperience(0);
        }
    }

    private double xpForCustomNextLevel(String skillName, int currentLevel) {
        final String key = skillName.toLowerCase();
        final double defaultBase = skillDefinitionRegistry.find(skillName)
                .map(SkillDefinition::getDefaultXpBase).orElse(100.0);
        final double defaultMultiplier = skillDefinitionRegistry.find(skillName)
                .map(SkillDefinition::getDefaultXpMultiplier).orElse(1.5);
        final double base = configManager.getSkillsConfig().getDouble(key + ".xp-base", defaultBase);
        final double multiplier = configManager.getSkillsConfig()
                .getDouble(key + ".xp-multiplier", defaultMultiplier);
        return base * Math.pow(multiplier, currentLevel - 1);
    }

    private int getCustomMaxLevel(String skillName) {
        final String key = skillName.toLowerCase() + ".max-level";
        final int defaultMax = skillDefinitionRegistry.find(skillName)
                .map(SkillDefinition::getDefaultMaxLevel).orElse(100);
        return configManager.getSkillsConfig().getInt(key, defaultMax);
    }

    private void processQueue() {
        while (running || !taskQueue.isEmpty()) {
            try {
                final StorageTask task = taskQueue.poll(500, TimeUnit.MILLISECONDS);
                if (task != null) {
                    task.run(this);
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Inner task types
    // -------------------------------------------------------------------------

    private interface StorageTask {
        void run(SkillManager manager);
    }

    private record LoadTask(UUID playerId, CompletableFuture<SkillProfile> future) implements StorageTask {
        @Override
        public void run(SkillManager manager) {
            SkillProfile loaded;
            try {
                final List<String> customNames = manager.skillDefinitionRegistry.getNames();
                final Optional<SkillProfileModel> opt = manager.repository.find(playerId.toString());
                loaded = opt.map(m -> m.toSkillProfile(customNames)).orElseGet(SkillProfile::new);
            }
            catch (Exception e) {
                manager.plugin.getLogger().log(Level.SEVERE,
                        "Failed to load skill profile for " + playerId, e);
                loaded = new SkillProfile();
            }

            // If the player disconnected before the load finished, discard the result
            // so we don't put a zombie profile back into the live cache.
            if (manager.unloadedWhileLoading.remove(playerId)) {
                future.complete(loaded);
                return;
            }

            manager.profiles.put(playerId, loaded);
            manager.loadingFutures.remove(playerId, future);
            future.complete(loaded);
        }
    }

    private record SaveTask(UUID playerId, SkillProfile profile) implements StorageTask {
        @Override
        public void run(SkillManager manager) {
            try {
                final SkillProfileModel model = SkillProfileModel.fromSkillProfile(playerId, profile);
                manager.repository.save(model);
            }
            catch (Exception e) {
                manager.plugin.getLogger().log(Level.SEVERE,
                        "Failed to save skill profile for " + playerId, e);
            }
        }
    }
}
