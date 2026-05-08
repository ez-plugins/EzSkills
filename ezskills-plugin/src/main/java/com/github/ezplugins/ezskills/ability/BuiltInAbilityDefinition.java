package com.github.ezplugins.ezskills.ability;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default {@link AbilityDefinition} implementation for each {@link AbilityType} enum constant.
 *
 * <p>Instances are immutable singletons accessed via {@link #of(AbilityType)}.</p>
 */
final class BuiltInAbilityDefinition implements AbilityDefinition {

    /** Singleton for {@link AbilityType#TREE_FELLER}. */
    private static final BuiltInAbilityDefinition TREE_FELLER = new BuiltInAbilityDefinition(
            "TREE_FELLER", "Tree Feller", Material.IRON_AXE,
            "Instantly fells an entire tree in one swing.", "WOODCUTTING");

    /** Canonical ability name. */
    private final String name;

    /** Human-readable display name. */
    private final String displayName;

    /** GUI icon. */
    private final Material icon;

    /** Short description shown in GUI lore. */
    private final String description;

    /** Associated skill name, or {@code null}. */
    @Nullable
    private final String skillName;

    private BuiltInAbilityDefinition(final String name, final String displayName,
                                     final Material icon, final String description,
                                     @Nullable final String skillName) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.skillName = skillName;
    }

    /**
     * Returns the built-in definition for a given {@link AbilityType}.
     *
     * @param type the ability type
     * @return the corresponding definition
     */
    static BuiltInAbilityDefinition of(final AbilityType type) {
        return switch (type) {
            case TREE_FELLER -> TREE_FELLER;
        };
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName;
    }

    @Override
    public @NotNull Material getIcon() {
        return icon;
    }

    @Override
    public @NotNull String getDescription() {
        return description;
    }

    @Override
    public @Nullable String getSkillName() {
        return skillName;
    }
}
