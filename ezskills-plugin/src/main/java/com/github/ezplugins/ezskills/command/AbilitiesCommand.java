package com.github.ezplugins.ezskills.command;

import com.github.ezplugins.ezskills.gui.AbilityOverviewMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the {@code /abilities} player-facing command.
 *
 * <p>Opens the ability overview GUI showing all registered abilities and their current
 * state (ready, preparing, or active) for the executing player.</p>
 */
public final class AbilitiesCommand implements CommandExecutor {

    /** Opens the ability overview GUI. */
    private final AbilityOverviewMenu abilityOverviewMenu;

    /**
     * Creates the abilities command executor.
     *
     * @param abilityOverviewMenu the menu to open
     */
    public AbilitiesCommand(@NotNull AbilityOverviewMenu abilityOverviewMenu) {
        this.abilityOverviewMenu = abilityOverviewMenu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }
        abilityOverviewMenu.open(player);
        return true;
    }
}
