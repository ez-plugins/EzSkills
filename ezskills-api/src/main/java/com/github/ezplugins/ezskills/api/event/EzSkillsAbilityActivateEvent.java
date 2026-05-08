package com.github.ezplugins.ezskills.api.event;

import com.github.ezplugins.ezskills.ability.AbilityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player's ability is activated.
 */
public final class EzSkillsAbilityActivateEvent extends EzSkillsAbilityEvent {

    /** Bukkit handler list used for event dispatch. */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates an activate event for a built-in ability.
     *
     * @param player      the player
     * @param abilityType the built-in ability type
     */
    public EzSkillsAbilityActivateEvent(@NotNull Player player, @NotNull AbilityType abilityType) {
        super(player, abilityType);
    }

    /**
     * Creates an activate event for a custom ability identified by name.
     *
     * @param player      the player
     * @param abilityName the ability name
     */
    public EzSkillsAbilityActivateEvent(@NotNull Player player, @NotNull String abilityName) {
        super(player, abilityName);
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
