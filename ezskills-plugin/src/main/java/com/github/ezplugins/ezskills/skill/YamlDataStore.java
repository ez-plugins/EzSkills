package com.github.ezplugins.ezskills.skill;

import com.github.ezframework.jaloquent.store.DataStore;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * A Jaloquent {@link DataStore} backed by a Bukkit YAML configuration file.
 *
 * <p>Storage paths follow the Jaloquent convention of {@code "prefix/id"}, which
 * are mapped to nested YAML sections using {@code '.'} as the separator
 * (e.g. {@code "player_skills/uuid"} → YAML path {@code "player_skills.uuid"}).</p>
 *
 * <p>All write operations flush to disk immediately. For high-throughput scenarios
 * prefer MySQL via {@link com.github.ezframework.jaloquent.store.DataSourceJdbcStore}.</p>
 */
public final class YamlDataStore implements DataStore {

    /** The underlying YAML file on disk. */
    private final File file;

    /** The in-memory representation of the YAML file. */
    private YamlConfiguration config;

    public YamlDataStore(@NotNull File file) {
        this.file = file;
        reload();
    }

    /**
     * Reloads the YAML file from disk.
     */
    public void reload() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    // -------------------------------------------------------------------------
    // DataStore implementation
    // -------------------------------------------------------------------------

    @Override
    public void save(@NotNull String path, @NotNull Map<String, Object> data) throws Exception {
        final String yamlPath = toYamlPath(path);
        config.createSection(yamlPath, data);
        saveFile();
    }

    @Override
    @NotNull
    public Optional<Map<String, Object>> load(@NotNull String path) throws Exception {
        final String yamlPath = toYamlPath(path);
        final ConfigurationSection section = config.getConfigurationSection(yamlPath);
        if (section == null) {
            return Optional.empty();
        }
        return Optional.of(sectionToMap(section));
    }

    @Override
    public void delete(@NotNull String path) throws Exception {
        final String yamlPath = toYamlPath(path);
        config.set(yamlPath, null);
        saveFile();
    }

    @Override
    public boolean exists(@NotNull String path) throws Exception {
        final String yamlPath = toYamlPath(path);
        return config.isConfigurationSection(yamlPath) || config.isSet(yamlPath);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a Jaloquent storage path ({@code "prefix/id"}) to a Bukkit YAML path
     * ({@code "prefix.id"}).
     *
     * @param jaloquentPath the storage path (e.g. {@code "player_skills/uuid"})
     * @return the equivalent YAML path (e.g. {@code "player_skills.uuid"})
     */
    private static String toYamlPath(@NotNull String jaloquentPath) {
        return jaloquentPath.replace('/', '.');
    }

    private static Map<String, Object> sectionToMap(@NotNull ConfigurationSection section) {
        final Map<String, Object> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            final Object value = section.get(key);
            if (value instanceof ConfigurationSection nested) {
                map.put(key, sectionToMap(nested));
            }
            else {
                map.put(key, value);
            }
        }
        return map;
    }

    private void saveFile() throws Exception {
        try {
            config.save(file);
        }
        catch (IOException e) {
            throw new Exception("Failed to save YAML data store to " + file.getPath(), e);
        }
    }
}
