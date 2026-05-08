package com.github.ezplugins.ezskills.skill;

import org.jetbrains.annotations.NotNull;

/**
 * Describes a skill that can be registered with EzSkills.
 *
 * <p>Implement this interface and call
 * {@link com.github.ezplugins.ezskills.api.EzSkillsAPI#registerSkill(SkillDefinition)} from
 * your plugin's {@code onEnable} to add a custom skill to EzSkills. The skill name must be
 * unique (case-insensitive) and must not clash with any built-in {@link SkillType} name.</p>
 *
 * <p>XP base, multiplier, and max level can be overridden by the server admin in
 * {@code skills.yml}; the values returned by this interface are the defaults used
 * when no config entry is present.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * EzSkillsAPI.registerSkill(new SkillDefinition() {
 *     public String getName()        { return "ALCHEMY"; }
 *     public String getDisplayName() { return "Alchemy"; }
 * });
 * }</pre>
 */
public interface SkillDefinition {

    /**
     * Returns the unique identifier for this skill (e.g. {@code "ALCHEMY"}).
     *
     * <p>Names are stored and compared case-insensitively. Must not match any
     * {@link SkillType} enum constant name.</p>
     *
     * @return skill name (upper-case recommended)
     */
    @NotNull
    String getName();

    /**
     * Returns the human-readable display name shown in the GUI and notifications.
     *
     * @return display name
     */
    @NotNull
    String getDisplayName();

    /**
     * Returns the default XP required to reach level 2 from level 1.
     *
     * <p>The server admin can override this in {@code skills.yml} under
     * {@code <skillname>.xp-base}.</p>
     *
     * @return default XP base (default 100.0)
     */
    default double getDefaultXpBase() {
        return 100.0;
    }

    /**
     * Returns the default exponential growth factor applied each level.
     *
     * <p>The server admin can override this in {@code skills.yml} under
     * {@code <skillname>.xp-multiplier}.</p>
     *
     * @return default XP multiplier (default 1.5)
     */
    default double getDefaultXpMultiplier() {
        return 1.5;
    }

    /**
     * Returns the default maximum level cap for this skill.
     *
     * <p>The server admin can override this in {@code skills.yml} under
     * {@code <skillname>.max-level}.</p>
     *
     * @return default max level (default 100)
     */
    default int getDefaultMaxLevel() {
        return 100;
    }
}
