package com.github.ezplugins.ezskills.skill;

import com.github.ezplugins.ezskills.ability.AbilityManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Preloads skill profiles on join and unloads them on disconnect.
 *
 * <p>{@link PlayerQuitEvent} is fired for both normal logouts and kicks, so there
 * is no need to also handle {@code PlayerKickEvent} (which would cause a double-unload).</p>
 */
public final class SkillProfileListener implements Listener {

    /** Manages skill profile loading and unloading. */
    private final SkillManager skillManager;

    /** Manages ability states that must be cleared on disconnect. */
    private final AbilityManager abilityManager;

    public SkillProfileListener(@NotNull SkillManager skillManager,
                                @NotNull AbilityManager abilityManager) {
        this.skillManager = skillManager;
        this.abilityManager = abilityManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        skillManager.loadProfile(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        skillManager.unloadProfile(event.getPlayer().getUniqueId());
        abilityManager.removePlayer(event.getPlayer().getUniqueId());
    }
}
