package com.github.ezplugins.ezskills.api.event;

import com.github.ezplugins.ezskills.skill.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player's skill increases by one level.
 */
public final class SkillLevelUpEvent extends PlayerEvent {

    /** Bukkit handler list used for event dispatch. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The type of skill that levelled up. */
    private final SkillType skillType;

    /** The level before the level-up. */
    private final int oldLevel;

    /** The level after the level-up. */
    private final int newLevel;

    public SkillLevelUpEvent(@NotNull Player player,
                             @NotNull SkillType skillType,
                             int oldLevel,
                             int newLevel) {
        super(player);
        this.skillType = skillType;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    @NotNull
    public SkillType getSkillType() {
        return skillType;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
