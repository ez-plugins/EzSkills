package com.github.ezplugins.ezskills.storage;

import com.github.ezframework.jaloquent.model.ModelRepository;
import com.github.ezframework.jaloquent.model.TableRegistry;
import com.github.ezframework.jaloquent.store.DataStore;
import com.github.ezframework.jaloquent.store.sql.DataSourceJdbcStore;
import com.github.ezframework.javaquerybuilder.query.builder.CreateBuilder;
import com.github.ezframework.javaquerybuilder.query.builder.QueryBuilder;
import com.github.ezframework.javaquerybuilder.query.sql.SqlResult;
import com.github.ezplugins.ezskills.config.ConfigManager;
import com.github.ezplugins.ezskills.skill.SkillProfileModel;
import com.github.ezplugins.ezskills.skill.SkillType;
import com.github.ezplugins.ezskills.skill.YamlDataStore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages storage initialisation and teardown for EzSkills.
 *
 * <p>Builds the appropriate {@link DataStore} (YAML or MySQL) based on the configured
 * storage type, creates the Jaloquent {@link ModelRepository}, and owns the HikariCP
 * connection pool lifecycle when MySQL is in use.</p>
 */
public final class StorageManager {

    /** The owning plugin instance. */
    private final JavaPlugin plugin;

    /** Configuration manager used to read storage settings. */
    private final ConfigManager configManager;

    /** HikariCP connection pool; {@code null} when using YAML storage. */
    @Nullable
    private HikariDataSource hikariDataSource;

    /** Jaloquent repository used for all skill profile persistence. */
    private ModelRepository<SkillProfileModel> repository;

    /**
     * Creates a new StorageManager.
     *
     * @param plugin        the owning plugin
     * @param configManager the config manager providing storage settings
     */
    public StorageManager(@NotNull final JavaPlugin plugin,
                          @NotNull final ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Initialises the data store and model repository.
     *
     * <p>Attempts to connect to MySQL if configured; falls back to YAML on failure.</p>
     */
    public void initialise() {
        final DataStore dataStore = createDataStore();
        this.repository = createRepository(dataStore);
    }

    /**
     * Returns the model repository created during initialisation.
     *
     * @return the skill profile model repository
     */
    @NotNull
    public ModelRepository<SkillProfileModel> getRepository() {
        return repository;
    }

    /**
     * Closes the HikariCP connection pool if one is open.
     */
    public void shutdown() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
        }
    }

    private DataStore createDataStore() {
        final String type = configManager.getStorageConfig().getString("storage.type", "yaml");
        if ("mysql".equalsIgnoreCase(type)) {
            try {
                return createMySqlStore();
            }
            catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to connect to MySQL, falling back to YAML storage.", e);
            }
        }
        else if (!"yaml".equalsIgnoreCase(type)) {
            plugin.getLogger().warning("Unknown storage type '" + type + "', using yaml.");
        }
        return new YamlDataStore(new File(plugin.getDataFolder(), "players.yml"));
    }

    private DataStore createMySqlStore() throws Exception {
        final FileConfiguration storageCfg = configManager.getStorageConfig();
        final String host     = storageCfg.getString("storage.mysql.host", "localhost");
        final int    port     = storageCfg.getInt("storage.mysql.port", 3306);
        final String database = storageCfg.getString("storage.mysql.database", "ezskills");
        final String username = storageCfg.getString("storage.mysql.username", "root");
        final String password = storageCfg.getString("storage.mysql.password", "");
        final String prefix   = storageCfg.getString("storage.mysql.table-prefix", "");
        final boolean ssl     = storageCfg.getBoolean("storage.mysql.ssl", false);
        final String sslMode  = storageCfg.getString("storage.mysql.ssl-mode",
                ssl ? "PREFERRED" : "DISABLED");

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + ssl
                + "&sslMode=" + sslMode
                + "&serverTimezone=UTC&characterEncoding=utf8"
                + "&useUnicode=true&allowPublicKeyRetrieval=true");
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("EzSkills-Pool");
        config.setMaximumPoolSize(storageCfg.getInt("storage.mysql.pool.max-size", 10));
        config.setMinimumIdle(storageCfg.getInt("storage.mysql.pool.min-idle", 2));
        config.setConnectionTimeout(30_000L);

        this.hikariDataSource = new HikariDataSource(config);

        final String table = prefix + "player_skills";
        final DataSourceJdbcStore store = new DataSourceJdbcStore(hikariDataSource);

        final SqlResult createSql = buildCreateTableQuery(table);
        store.executeUpdate(createSql.getSql(), createSql.getParameters());

        TableRegistry.register(table, table, buildColumnDefinitions());

        return store;
    }

    private SqlResult buildCreateTableQuery(final String tableName) {
        CreateBuilder create = QueryBuilder.createTable(tableName)
                .ifNotExists()
                .column("id", "VARCHAR(36) NOT NULL");
        for (final SkillType type : SkillType.values()) {
            final String key = type.name().toLowerCase();
            create = create
                    .column(key + "_level", "INT NOT NULL DEFAULT 1")
                    .column(key + "_experience", "DOUBLE NOT NULL DEFAULT 0");
        }
        return create.primaryKey("id").build();
    }

    private Map<String, String> buildColumnDefinitions() {
        final Map<String, String> cols = new LinkedHashMap<>();
        cols.put("id", "VARCHAR(36) NOT NULL");
        for (final SkillType type : SkillType.values()) {
            final String key = type.name().toLowerCase();
            cols.put(key + "_level", "INT NOT NULL DEFAULT 1");
            cols.put(key + "_experience", "DOUBLE NOT NULL DEFAULT 0");
        }
        return cols;
    }

    private ModelRepository<SkillProfileModel> createRepository(final DataStore store) {
        final String tablePrefix = (store instanceof DataSourceJdbcStore)
                ? configManager.getStorageConfig().getString("storage.mysql.table-prefix", "")
                : "";
        return new ModelRepository<>(store, tablePrefix + "player_skills", (id, data) -> {
            final SkillProfileModel m = new SkillProfileModel(id);
            m.fromMap(data);
            return m;
        });
    }
}
