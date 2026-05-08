package com.github.ezplugins.ezskills.command;

import com.github.ezplugins.ezskills.ability.AbilityDefinitionRegistry;
import com.github.ezplugins.ezskills.ability.AbilityType;
import com.github.ezplugins.ezskills.skill.SkillType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tab-completer for the {@code /ezskills} command.
 */
public final class EzSkillsTabCompleter implements TabCompleter {

    /** The list of top-level subcommand names. */
    private static final List<String> SUBCOMMANDS =
            List.of("get", "info", "addxp", "setlevel", "reset", "resetall", "top", "ability", "reload");

    /** The list of all skill type names for tab-completion. */
    private static final List<String> SKILL_NAMES =
            Arrays.stream(SkillType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());

    /** Registry used to resolve dynamic ability names (includes custom abilities). */
    private final AbilityDefinitionRegistry abilityDefinitionRegistry;

    /**
     * Creates a tab completer that resolves ability names from the given registry.
     *
     * @param abilityDefinitionRegistry the ability definition registry
     */
    public EzSkillsTabCompleter(@NotNull AbilityDefinitionRegistry abilityDefinitionRegistry) {
        this.abilityDefinitionRegistry = abilityDefinitionRegistry;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "get", "info", "addxp", "setlevel", "reset", "resetall", "ability" ->
                        sender.getServer().getOnlinePlayers().stream()
                                .map(p -> p.getName())
                                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList());
                case "top" -> filter(SKILL_NAMES, args[1]);
                default -> List.of();
            };
        }
        if (args.length == 3) {
            return switch (args[0].toLowerCase()) {
                case "get", "addxp", "setlevel", "reset" -> filter(SKILL_NAMES, args[2]);
                case "ability" -> filter(abilityDefinitionRegistry.getNames(), args[2]);
                case "top" -> filter(List.of("5", "10", "15", "20"), args[2]);
                default -> List.of();
            };
        }
        return List.of();
    }

    private static List<String> filter(List<String> source, String prefix) {
        return source.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
