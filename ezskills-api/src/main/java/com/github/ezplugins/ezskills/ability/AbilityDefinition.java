package com.github.ezplugins.ezskills.ability;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes an ability that can be registered with EzSkills.
 *
 * <p>Implement this interface and call
 * {@link com.github.ezplugins.ezskills.api.EzSkillsAPI#registerAbility(AbilityDefinition)} from
 * your plugin's {@code onEnable} to add a custom ability to the EzSkills ability overview GUI.</p>
 *
 * <p>All timing defaults (30 s preparation, 15 s active, 120 s cooldown) can be overridden by
 * implementing the corresponding default methods.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * EzSkillsAPI.registerAbility(new AbilityDefinition() {
 *     public String getName()        { return "NIGHT_VISION"; }
 *     public String getDisplayName() { return "Night Vision"; }
 *     public Material getIcon()      { return Material.ENDER_EYE; }
 *     public String getDescription() { return "See clearly in the dark."; }
 *     public String getSkillName()   { return null; }
 * });
 * }</pre>
 */
public interface AbilityDefinition {

    /**
     * Returns the unique identifier for this ability (e.g. {@code "NIGHT_VISION"}).
     *
     * <p>Names are stored and compared case-insensitively. Built-in abilities use the name of their
     * {@link AbilityType} constant.</p>
     *
     * @return ability name
     */
    @NotNull
    String getName();

    /**
     * Returns the human-readable display name shown as the GUI item title.
     *
     * @return display name
     */
    @NotNull
    String getDisplayName();

    /**
     * Returns the {@link Material} used as the ability icon in the GUI.
     *
     * <p>Override this to customise the icon. The default fallback is
     * {@link Material#NETHER_STAR}.</p>
     *
     * @return icon material
     */
    @NotNull
    default Material getIcon() {
        return Material.NETHER_STAR;
    }

    /**
     * Returns a short description shown in the GUI item lore.
     *
     * @return description text
     */
    @NotNull
    String getDescription();

    /**
     * Returns the name of the associated skill (e.g. {@code "WOODCUTTING"}), or {@code null} if
     * this ability is not tied to a specific skill.
     *
     * @return associated skill name, or {@code null}
     */
    @Nullable
    String getSkillName();

    /**
     * Returns the preparation window in seconds — how long after entering the preparing state the
     * player has before the charge expires.
     *
     * @return preparation window in seconds (default 30)
     */
    default int getPreparationWindowSeconds() {
        return 30;
    }

    /**
     * Returns how many seconds the ability stays active once triggered.
     *
     * @return active duration in seconds (default 15)
     */
    default int getActiveDurationSeconds() {
        return 15;
    }

    /**
     * Returns the cooldown in seconds after the ability expires before it can be prepared again.
     *
     * @return cooldown in seconds (default 120)
     */
    default int getCooldownSeconds() {
        return 120;
    }
}
