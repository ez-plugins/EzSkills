package com.github.ezplugins.ezskills.command;

import com.github.ezplugins.ezskills.gui.SkillProgressMenu;
import com.github.ezplugins.ezskills.skill.SkillManager;
import com.github.ezplugins.ezskills.skill.SkillProfile;
import com.github.ezplugins.ezskills.skill.SkillProgress;
import com.github.ezplugins.ezskills.skill.SkillType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the {@code /skills} player-facing command.
 *
 * <p>Opens the skill overview menu or prints skill levels to chat.</p>
 */
public final class SkillsCommand implements CommandExecutor {

    /** Provides cached skill profiles. */
    private final SkillManager skillManager;

    /** Opens the skill progress GUI. */
    private final SkillProgressMenu skillProgressMenu;

    public SkillsCommand(@NotNull SkillManager skillManager,
                         @NotNull SkillProgressMenu skillProgressMenu) {
        this.skillManager = skillManager;
        this.skillProgressMenu = skillProgressMenu;
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

        final SkillProfile profile = skillManager.getCachedProfile(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Your profile is loading, please try again shortly.");
            return true;
        }

        // If a specific skill was requested, show details
        if (args.length >= 1) {
            final SkillType type = SkillType.fromString(args[0]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "Unknown skill: " + args[0]);
                return true;
            }
            final SkillProgress progress = profile.getProgress(type);
            player.sendMessage(ChatColor.GOLD + type.name()
                    + ChatColor.GRAY + " — Level " + ChatColor.WHITE + progress.getLevel()
                    + ChatColor.GRAY + " | XP " + ChatColor.WHITE
                    + String.format("%.1f", progress.getExperience()));
            return true;
        }

        // Default: open GUI
        skillProgressMenu.open(player);
        return true;
    }
}
