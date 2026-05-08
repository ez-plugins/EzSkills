package com.github.ezplugins.ezskills.command;

import com.github.ezplugins.ezskills.ability.AbilityManager;
import com.github.ezplugins.ezskills.ability.AbilityType;
import com.github.ezplugins.ezskills.config.ConfigManager;
import com.github.ezplugins.ezskills.skill.SkillManager;
import com.github.ezplugins.ezskills.skill.SkillProfile;
import com.github.ezplugins.ezskills.skill.SkillProfileModel;
import com.github.ezplugins.ezskills.skill.SkillProgress;
import com.github.ezplugins.ezskills.skill.SkillType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the {@code /ezskills} admin command.
 *
 * <p>Subcommands:</p>
 * <ul>
 *   <li>{@code get <player> <skill>} — prints level and XP for one skill</li>
 *   <li>{@code info <player>} — prints a full skill overview (supports offline players)</li>
 *   <li>{@code addxp <player> <skill> <amount>} — adds XP</li>
 *   <li>{@code setlevel <player> <skill> <level>} — sets level, resets XP</li>
 *   <li>{@code reset <player> <skill>} — resets one skill to level 1</li>
 *   <li>{@code resetall <player>} — resets all skills to level 1</li>
 *   <li>{@code top <skill> [limit]} — shows the skill leaderboard (SQL only)</li>
 *   <li>{@code ability <player> <ability>} — force-activates an ability</li>
 *   <li>{@code reload} — reloads all configs</li>
 * </ul>
 */
public final class EzSkillsCommand implements CommandExecutor {

    /** Manages skill levels and experience. */
    private final SkillManager skillManager;

    /** Manages ability activation. */
    private final AbilityManager abilityManager;

    /** Provides configuration reload. */
    private final ConfigManager configManager;

    /** Plugin instance used for scheduling callbacks back onto the main thread. */
    private final JavaPlugin plugin;

    public EzSkillsCommand(@NotNull SkillManager skillManager,
                           @NotNull AbilityManager abilityManager,
                           @NotNull ConfigManager configManager,
                           @NotNull JavaPlugin plugin) {
        this.skillManager = skillManager;
        this.abilityManager = abilityManager;
        this.configManager = configManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "get"      -> handleGet(sender, args);
            case "info"     -> handleInfo(sender, args);
            case "addxp"    -> handleAddXp(sender, args);
            case "setlevel" -> handleSetLevel(sender, args);
            case "reset"    -> handleReset(sender, args);
            case "resetall" -> handleResetAll(sender, args);
            case "top"      -> handleTop(sender, args);
            case "ability"  -> handleAbility(sender, args);
            case "reload"   -> handleReload(sender);
            default -> {
                sendHelp(sender);
                yield true;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Skill inspection
    // -------------------------------------------------------------------------

    private boolean handleGet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /ezskills get <player> <skill>");
            return true;
        }
        final OfflinePlayer target = resolveOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Unknown player: " + args[1]);
            return true;
        }
        final SkillType type = SkillType.fromString(args[2]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + args[2]);
            return true;
        }
        final int level = skillManager.getLevel(target.getUniqueId(), type);
        final double xp = skillManager.getExperience(target.getUniqueId(), type);
        sender.sendMessage(ChatColor.GOLD + args[1] + ChatColor.GRAY + " \u2014 "
                + ChatColor.GREEN + type.name()
                + ChatColor.GRAY + " | Level " + ChatColor.WHITE + level
                + ChatColor.GRAY + " | XP " + ChatColor.WHITE + String.format("%,.1f", xp));
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ezskills info <player>");
            return true;
        }
        final OfflinePlayer target = resolveOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Unknown player: " + args[1]);
            return true;
        }
        final String displayName = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage(ChatColor.GRAY + "Loading profile for " + displayName + "...");
        skillManager.loadProfile(target.getUniqueId()).thenAccept(profile ->
            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage(ChatColor.GOLD + "=== " + displayName + " Skills ===");
                for (final SkillType type : SkillType.values()) {
                    final SkillProgress progress = profile.getProgress(type);
                    sender.sendMessage(ChatColor.GRAY + "  " + formatSkillName(type.name())
                            + ": " + ChatColor.WHITE + "Level " + progress.getLevel()
                            + ChatColor.GRAY + " | XP " + ChatColor.WHITE
                            + String.format("%,.1f", progress.getExperience()));
                }
            })
        );
        return true;
    }

    // -------------------------------------------------------------------------
    // Skill mutation
    // -------------------------------------------------------------------------

    private boolean handleAddXp(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /ezskills addxp <player> <skill> <amount>");
            return true;
        }
        final OfflinePlayer target = resolveOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Unknown player: " + args[1]);
            return true;
        }
        final SkillType type = SkillType.fromString(args[2]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + args[2]);
            return true;
        }
        final double amount;
        try {
            amount = Double.parseDouble(args[3]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[3]);
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be positive.");
            return true;
        }
        if (skillManager.getCachedProfile(target.getUniqueId()) == null) {
            sender.sendMessage(ChatColor.RED + args[1] + " is offline — load their profile first with /ezskills info.");
            return true;
        }
        skillManager.addExperience(target.getUniqueId(), type, amount);
        sender.sendMessage(ChatColor.GREEN + "Added " + String.format("%,.1f", amount)
                + " XP to " + args[1] + "'s " + type.name() + " skill.");
        return true;
    }

    private boolean handleSetLevel(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /ezskills setlevel <player> <skill> <level>");
            return true;
        }
        final OfflinePlayer target = resolveOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Unknown player: " + args[1]);
            return true;
        }
        final SkillType type = SkillType.fromString(args[2]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + args[2]);
            return true;
        }
        final int level;
        try {
            level = Integer.parseInt(args[3]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level: " + args[3]);
            return true;
        }
        if (level < 1) {
            sender.sendMessage(ChatColor.RED + "Level must be at least 1.");
            return true;
        }
        if (skillManager.getCachedProfile(target.getUniqueId()) == null) {
            sender.sendMessage(ChatColor.RED + args[1] + " is offline — load their profile first with /ezskills info.");
            return true;
        }
        skillManager.setLevel(target.getUniqueId(), type, level);
        sender.sendMessage(ChatColor.GREEN + "Set " + args[1] + "'s "
                + type.name() + " to level " + level + ".");
        return true;
    }

    // -------------------------------------------------------------------------
    // Moderation: reset
    // -------------------------------------------------------------------------

    private boolean handleReset(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /ezskills reset <player> <skill>");
            return true;
        }
        final OfflinePlayer target = resolveOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Unknown player: " + args[1]);
            return true;
        }
        final SkillType type = SkillType.fromString(args[2]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + args[2]);
            return true;
        }
        if (skillManager.getCachedProfile(target.getUniqueId()) == null) {
            sender.sendMessage(ChatColor.RED + args[1] + " is offline — load their profile first with /ezskills info.");
            return true;
        }
        skillManager.resetSkill(target.getUniqueId(), type);
        sender.sendMessage(ChatColor.GREEN + "Reset " + args[1] + "'s "
                + type.name() + " skill to level 1.");
        return true;
    }

    private boolean handleResetAll(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ezskills resetall <player>");
            return true;
        }
        final OfflinePlayer target = resolveOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Unknown player: " + args[1]);
            return true;
        }
        if (skillManager.getCachedProfile(target.getUniqueId()) == null) {
            sender.sendMessage(ChatColor.RED + args[1] + " is offline — load their profile first with /ezskills info.");
            return true;
        }
        skillManager.resetAllSkills(target.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + "Reset all skills for " + args[1] + " to level 1.");
        return true;
    }

    // -------------------------------------------------------------------------
    // Moderation: leaderboard
    // -------------------------------------------------------------------------

    private boolean handleTop(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ezskills top <skill> [limit]");
            return true;
        }
        final SkillType type = SkillType.fromString(args[1]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + args[1]);
            return true;
        }
        final int limit;
        if (args.length >= 3) {
            final int parsed;
            try {
                parsed = Integer.parseInt(args[2]);
            }
            catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid limit: " + args[2]);
                return true;
            }
            limit = Math.max(1, Math.min(20, parsed));
        }
        else {
            limit = 10;
        }

        try {
            final List<SkillProfileModel> top =
                    skillManager.queryLeaderboard(type, limit);
            if (top.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "No data found for " + type.name() + ".");
                return true;
            }
            sender.sendMessage(ChatColor.GOLD + "=== Top " + limit
                    + " " + formatSkillName(type.name()) + " Players ===");
            int rank = 1;
            for (final SkillProfileModel model : top) {
                final String name = resolvePlayerName(model.getId());
                final int level = model.getAs(type.name().toLowerCase() + "_level", Integer.class, 1);
                sender.sendMessage(ChatColor.GRAY + "" + rank + ". "
                        + ChatColor.WHITE + name
                        + ChatColor.GRAY + " — Level " + ChatColor.GOLD + level);
                rank++;
            }
        }
        catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Leaderboard unavailable"
                    + " (MySQL storage required).");
            // Online-only fallback
            final List<Map.Entry<UUID, SkillProfile>> online =
                    skillManager.getOnlineLeaderboard(type, limit);
            if (!online.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "Showing online players only:");
                int rank = 1;
                for (final Map.Entry<UUID, SkillProfile> entry : online) {
                    final OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
                    final String name = op.getName() != null ? op.getName() : entry.getKey().toString();
                    final int level = entry.getValue().getProgress(type).getLevel();
                    sender.sendMessage(ChatColor.GRAY + "" + rank + ". "
                            + ChatColor.WHITE + name
                            + ChatColor.GRAY + " — Level " + ChatColor.GOLD + level);
                    rank++;
                }
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Ability admin
    // -------------------------------------------------------------------------

    private boolean handleAbility(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /ezskills ability <player> <ability>");
            return true;
        }
        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player " + args[1] + " is not online.");
            return true;
        }
        final AbilityType abilityType = AbilityType.fromString(args[2]);
        if (abilityType == null) {
            sender.sendMessage(ChatColor.RED + "Unknown ability: " + args[2]);
            return true;
        }
        abilityManager.activateAbility(target, abilityType);
        sender.sendMessage(ChatColor.GREEN + "Activated " + abilityType.name()
                + " for " + target.getName() + ".");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        configManager.reload();
        sender.sendMessage(ChatColor.GREEN + "EzSkills configuration reloaded.");
        return true;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves a player name to an {@link OfflinePlayer} without the blocking Mojang API call.
     *
     * <p>Checks online players first, then the server's offline-player cache (UUIDs already
     * known from previous sessions). Returns {@code null} only when the name is completely
     * unknown to this server.</p>
     *
     * @param name the player name to look up (case-insensitive)
     * @return the resolved player, or {@code null} if not found
     */
    @Nullable
    private static OfflinePlayer resolveOfflinePlayer(@NotNull String name) {
        // Fast path: currently online
        final Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        // Search the server's known offline-player cache
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(op.getName())) {
                return op;
            }
        }
        return null;
    }

    /**
     * Resolves a UUID string to a player name, falling back to the UUID itself.
     *
     * @param uuidStr the UUID string to resolve
     * @return the player name, or {@code uuidStr} if the player is unknown
     */
    private static String resolvePlayerName(String uuidStr) {
        try {
            final OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
            return op.getName() != null ? op.getName() : uuidStr;
        }
        catch (IllegalArgumentException e) {
            return uuidStr;
        }
    }

    private static String formatSkillName(String name) {
        final String lower = name.toLowerCase().replace('_', ' ');
        if (lower.isEmpty()) {
            return lower;
        }
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== EzSkills Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills get <player> <skill>");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills info <player>");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills addxp <player> <skill> <amount>");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills setlevel <player> <skill> <level>");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills reset <player> <skill>");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills resetall <player>");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills top <skill> [limit]");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills ability <player> <ability>");
        sender.sendMessage(ChatColor.YELLOW + "/ezskills reload");
    }
}
