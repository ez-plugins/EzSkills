package com.github.ezplugins.ezskills.skill;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;

/**
 * Holds all custom {@link SkillDefinition}s registered with EzSkills by external plugins.
 *
 * <p>Built-in skills are defined by the {@link SkillType} enum and are not stored here.
 * This registry is only for skills added dynamically at runtime.</p>
 *
 * <p>This class is thread-safe.</p>
 */
public final class SkillDefinitionRegistry {

    /** Thread-safe, ordered list of custom skill definitions. */
    private final List<SkillDefinition> definitions = new CopyOnWriteArrayList<>();

    /**
     * Registers a custom {@link SkillDefinition}.
     *
     * <p>If a definition with the same name (case-insensitive) is already registered it is
     * replaced to prevent duplicates.</p>
     *
     * @param definition the definition to register
     * @throws IllegalArgumentException if the name matches a built-in {@link SkillType}
     */
    public void register(@NotNull final SkillDefinition definition) {
        final String upper = definition.getName().toUpperCase();
        if (SkillType.fromString(upper) != null) {
            throw new IllegalArgumentException(
                    "Cannot register a custom skill with the same name as a built-in SkillType: " + upper);
        }
        definitions.removeIf(d -> d.getName().equalsIgnoreCase(definition.getName()));
        definitions.add(definition);
    }

    /**
     * Returns an unmodifiable view of all registered custom definitions in registration order.
     *
     * @return unmodifiable list of definitions
     */
    @NotNull
    public List<SkillDefinition> getAll() {
        return Collections.unmodifiableList(definitions);
    }

    /**
     * Returns the definition whose name matches the given string (case-insensitively),
     * or an empty {@link Optional} if no match exists.
     *
     * @param name the skill name to look up
     * @return matching definition, or empty
     */
    @NotNull
    public Optional<SkillDefinition> find(@NotNull final String name) {
        return definitions.stream()
                .filter(d -> d.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Returns {@code true} if the given name (case-insensitive) matches a registered definition.
     *
     * @param name the skill name to check
     * @return {@code true} if registered
     */
    public boolean isRegistered(@NotNull final String name) {
        return definitions.stream().anyMatch(d -> d.getName().equalsIgnoreCase(name));
    }

    /**
     * Returns all registered skill names in upper-case for tab completion.
     *
     * @return list of skill names
     */
    @NotNull
    public List<String> getNames() {
        return definitions.stream()
                .map(d -> d.getName().toUpperCase())
                .toList();
    }
}
