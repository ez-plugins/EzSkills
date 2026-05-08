package com.github.ezplugins.ezskills;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for EzSkills.
 *
 * <p>Intentionally kept as a thin entry-point. All startup logic is in
 * {@link Bootstrap} and all runtime state is held in {@link Registry}.</p>
 */
public final class EzSkills extends JavaPlugin {

    /** Bootstrap instance that manages startup and shutdown. */
    private Bootstrap bootstrap;

    @Override
    public void onEnable() {
        bootstrap = new Bootstrap(this);
        bootstrap.start();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.stop();
        }
    }
}

