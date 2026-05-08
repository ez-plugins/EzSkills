package com.github.ezplugins.ezskills.api.event;

import com.github.ezplugins.ezskills.ability.AbilityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all EzSkills ability events.
 *
 * <p>Use {@link #getAbilityName()} for a universal identifier that works for both built-in and
 * custom abilities. {@link #getAbilityType()} returns {@code null} for custom abilities that are
 * not backed by an {@link AbilityType} constant.</p>
 */
public abstract class EzSkillsAbilityEvent extends PlayerEvent {

    /** The canonical ability name (always upper-case). */
    private final String abilityName;

    /** The ability type, or {@code null} for custom abilities. */
    @Nullable
    private final AbilityType abilityType;

    /**
     * Creates an event for a built-in ability.
     *
     * @param player      the player
     * @param abilityType the built-in ability type
     */
    protected EzSkillsAbilityEvent(@NotNull Player player, @NotNull AbilityType abilityType) {
        super(player);
        this.abilityType = abilityType;
        this.abilityName = abilityType.name();
    }

    /**
     * Creates an event for a custom ability identified by name.
     *
     * @param player      the player
     * @param abilityName the ability name (stored as upper-case)
     */
    protected EzSkillsAbilityEvent(@NotNull Player player, @NotNull String abilityName) {
        super(player);
        this.abilityName = abilityName.toUpperCase();
        this.abilityType = AbilityType.fromString(abilityName);
    }

    /**
     * Returns the canonical ability name (always upper-case).
     *
     * @return ability name
     */
    @NotNull
    public String getAbilityName() {
        return abilityName;
    }

    /**
     * Returns the {@link AbilityType}, or {@code null} if this event was fired for a custom ability
     * that is not backed by a built-in constant.
     *
     * @return ability type, or {@code null}
     */
    @Nullable
    public AbilityType getAbilityType() {
        return abilityType;
    }
}
