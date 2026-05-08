package com.github.ezplugins.ezskills.gui;

import com.github.ezplugins.ezskills.config.ConfigManager;
import com.github.ezplugins.ezskills.skill.SkillManager;
import com.github.ezplugins.ezskills.skill.SkillProfile;
import com.github.ezplugins.ezskills.skill.SkillProgress;
import com.github.ezplugins.ezskills.skill.SkillType;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Displays a 36-slot inventory overview of the player's skill progress.
 *
 * <p>Layout (4 rows × 9 cols):</p>
 * <pre>
 * Row 0: decorative dark-gray glass border
 * Row 1: [border] [WOOD] [gap] [MINE] [gap] [FISH] [gap] [gap] [border]
 * Row 2: [border] [gap] [gap] [FGHT] [gap] [ACRO] [gap] [gap] [border]
 * Row 3: decorative border with close button at slot 31
 * </pre>
 *
 * <p>Each skill item shows level, current XP, XP needed for the next level,
 * a Unicode progress bar, and total XP earned.</p>
 */
public final class SkillProgressMenu {

    /** Inventory size -- 4 rows of 9 slots. */
    private static final int SIZE = 36;

    /**
     * Slots where skill items are placed in SkillType enum order:
     * Woodcutting(10), Mining(12), Fishing(14), Fighting(21), Acrobatics(23).
     */
    private static final int[] SKILL_SLOTS = {10, 12, 14, 21, 23};

    /** Progress-bar length in characters. */
    private static final int BAR_LENGTH = 12;

    /** Filled bar character (\u2588 = █). */
    private static final String BAR_FILLED = "\u2588";

    /** Empty bar character (\u2591 = ░). */
    private static final String BAR_EMPTY = "\u2591";

    /** Material icons -- one per SkillType enum constant (same order). */
    private static final Material[] ICONS = {
        Material.OAK_LOG,
        Material.DIAMOND_PICKAXE,
        Material.COD,
        Material.IRON_SWORD,
        Material.FEATHER
    };

    /** Provides cached skill profiles. */
    private final SkillManager skillManager;

    /** Provides access to skills and main configuration. */
    private final ConfigManager configManager;

    public SkillProgressMenu(@NotNull SkillManager skillManager,
                             @NotNull ConfigManager configManager) {
        this.skillManager = skillManager;
        this.configManager = configManager;
    }

    /**
     * Opens the skills overview inventory for the given player.
     *
     * @param player the player to open the menu for
     */
    public void open(@NotNull Player player) {
        final SkillProfile profile = skillManager.getCachedProfile(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Your profile is not loaded yet. Try again in a moment.");
            return;
        }

        final String rawTitle = configManager.getMainConfig().getString("gui.title", "&6\u2605 Skills");
        final Inventory inv = Bukkit.createInventory(new SkillMenuHolder(), SIZE,
                ChatColor.translateAlternateColorCodes('&', rawTitle));

        // Fill top and bottom border rows
        final ItemStack border = buildBorderPane();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(27 + i, border);
        }
        // Side borders on middle two rows
        inv.setItem(9, border);
        inv.setItem(17, border);
        inv.setItem(18, border);
        inv.setItem(26, border);
        // Gap panes in row 1 (between/around skills)
        inv.setItem(11, border);
        inv.setItem(13, border);
        inv.setItem(15, border);
        inv.setItem(16, border);
        // Gap panes in row 2 (around skills)
        inv.setItem(19, border);
        inv.setItem(20, border);
        inv.setItem(22, border);
        inv.setItem(24, border);
        inv.setItem(25, border);

        // Close button
        inv.setItem(31, buildCloseButton());

        // Skill items
        final SkillType[] types = SkillType.values();
        for (int i = 0; i < types.length && i < SKILL_SLOTS.length; i++) {
            final SkillType type = types[i];
            final SkillProgress progress = profile.getProgress(type);
            final double xpForNext = xpForNextLevel(type, progress.getLevel());
            inv.setItem(SKILL_SLOTS[i], buildSkillItem(type, progress, xpForNext, ICONS[i]));
        }

        player.openInventory(inv);
    }

    private ItemStack buildSkillItem(SkillType type, SkillProgress progress,
                                     double xpForNext, Material icon) {
        final ItemStack item = new ItemStack(icon);
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        final int level = progress.getLevel();
        final double xp = progress.getExperience();
        final double percent = xpForNext > 0 ? Math.min(1.0, xp / xpForNext) : 1.0;
        final int filled = (int) Math.round(percent * BAR_LENGTH);
        final double remaining = Math.max(0, xpForNext - xp);

        final String bar = ChatColor.GREEN + BAR_FILLED.repeat(filled)
                + ChatColor.DARK_GRAY + BAR_EMPTY.repeat(BAR_LENGTH - filled);

        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + formatName(type.name()));
        meta.setLore(buildLore(level, xp, xpForNext, remaining, bar, percent));
        item.setItemMeta(meta);
        return item;
    }

    private List<String> buildLore(int level, double xp, double xpForNext,
                                   double remaining, String bar, double percent) {
        return Arrays.asList(
                "",
                ChatColor.GRAY + "Level: " + ChatColor.WHITE + level,
                ChatColor.GRAY + "XP: " + ChatColor.WHITE + String.format("%,.1f", xp)
                        + ChatColor.GRAY + " / " + ChatColor.WHITE + String.format("%,.1f", xpForNext),
                "",
                " " + bar + ChatColor.YELLOW + " " + (int) (percent * 100) + ChatColor.GOLD + "%",
                "",
                ChatColor.GRAY + "To next level: " + ChatColor.WHITE + String.format("%,.1f", remaining) + " XP"
        );
    }

    private ItemStack buildBorderPane() {
        final ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        final ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack buildCloseButton() {
        final ItemStack barrier = new ItemStack(Material.BARRIER);
        final ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Close");
            barrier.setItemMeta(meta);
        }
        return barrier;
    }

    /**
     * Returns the XP required to advance from {@code currentLevel} to the next.
     *
     * @param type         the skill type (reads base/multiplier from skills.yml)
     * @param currentLevel the player's current level
     * @return XP needed to reach the next level
     */
    private double xpForNextLevel(SkillType type, int currentLevel) {
        final String key = type.name().toLowerCase();
        final double base = configManager.getSkillsConfig().getDouble(key + ".xp-base", 100.0);
        final double multiplier = configManager.getSkillsConfig().getDouble(key + ".xp-multiplier", 1.5);
        return base * Math.pow(multiplier, currentLevel - 1);
    }

    private static String formatName(String name) {
        final String lower = name.toLowerCase().replace('_', ' ');
        if (lower.isEmpty()) {
            return lower;
        }
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

