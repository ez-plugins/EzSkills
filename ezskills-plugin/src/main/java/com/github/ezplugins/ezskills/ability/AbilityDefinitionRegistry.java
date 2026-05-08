package com.github.ezplugins.ezskills.ability;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;

/**
 * Holds all {@link AbilityDefinition}s registered with EzSkills, both built-in and custom.
 *
 * <p>Built-in definitions are registered automatically during plugin startup via
 * {@link #registerBuiltIns()}. External plugins may add their own definitions via
 * {@link #register(AbilityDefinition)}.</p>
 *
 * <p>This class is thread-safe.</p>
 */
public final class AbilityDefinitionRegistry {

    /** Thread-safe, ordered list of all registered definitions. */
    private final List<AbilityDefinition> definitions = new CopyOnWriteArrayList<>();

    /**
     * Registers a {@link BuiltInAbilityDefinition} for every {@link AbilityType} enum constant.
     *
     * <p>Safe to call again after a reload — existing built-in entries are replaced.</p>
     */
    public void registerBuiltIns() {
        for (final AbilityType type : AbilityType.values()) {
            register(BuiltInAbilityDefinition.of(type));
        }
    }

    /**
     * Registers a custom {@link AbilityDefinition}.
     *
     * <p>If an ability with the same name (case-insensitive) already exists it is replaced to
     * prevent duplicates in the GUI.</p>
     *
     * @param definition the definition to register
     */
    public void register(@NotNull final AbilityDefinition definition) {
        definitions.removeIf(d -> d.getName().equalsIgnoreCase(definition.getName()));
        definitions.add(definition);
    }

    /**
     * Returns an unmodifiable view of all registered definitions in registration order.
     *
     * @return unmodifiable list of definitions
     */
    @NotNull
    public List<AbilityDefinition> getAll() {
        return Collections.unmodifiableList(definitions);
    }

    /**
     * Returns all registered ability names (upper-case) for tab completion.
     *
     * @return list of ability names
     */
    @NotNull
    public List<String> getNames() {
        return definitions.stream()
                .map(AbilityDefinition::getName)
                .toList();
    }
}
