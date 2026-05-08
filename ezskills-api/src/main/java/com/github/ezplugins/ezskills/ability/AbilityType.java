package com.github.ezplugins.ezskills.ability;

/**
 * The ability types available in EzSkills.
 */
public enum AbilityType {

    /** Ability that fells an entire tree at once. */
    TREE_FELLER;

    /**
     * Returns the {@link AbilityType} whose name matches the given string, case-insensitively.
     *
     * @param name the name to look up
     * @return the matching {@link AbilityType}, or {@code null} if none matches
     */
    public static AbilityType fromString(String name) {
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
