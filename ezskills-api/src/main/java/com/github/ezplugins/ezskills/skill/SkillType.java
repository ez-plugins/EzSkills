package com.github.ezplugins.ezskills.skill;

/**
 * The skills available to players in EzSkills.
 */
public enum SkillType {

    /** Skill gained by cutting trees. */
    WOODCUTTING,

    /** Skill gained by mining ore. */
    MINING,

    /** Skill gained by fishing. */
    FISHING,

    /** Skill gained by fighting mobs and players. */
    FIGHTING;

    /**
     * Returns the {@link SkillType} whose name matches the given string, case-insensitively.
     *
     * @param name the name to look up
     * @return the matching {@link SkillType}, or {@code null} if none matches
     */
    public static SkillType fromString(String name) {
        if (name == null) {
            return null;
        }
        try {
            return valueOf(name.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}
