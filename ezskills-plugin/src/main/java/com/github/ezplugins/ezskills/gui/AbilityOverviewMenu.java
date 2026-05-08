package com.github.ezplugins.ezskills.gui;

import com.github.ezplugins.ezskills.ability.AbilityDefinition;
import com.github.ezplugins.ezskills.ability.AbilityDefinitionRegistry;
import com.github.ezplugins.ezskills.ability.AbilityManager;
import com.github.ezplugins.ezskills.ability.AbilityType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Opens a 54-slot inventory showing all registered abilities and their current state.
 *
 * <p>Layout (6 rows × 9 cols):</p>
 * <pre>
 * Row 0: decorative border
 * Rows 1–4: [border] [ability …] [border]  — 7 ability slots per row = 28 total
 * Row 5: decorative border with close button at slot 49
 * </pre>
 *
 * <p>Each ability card is colour-coded by state:</p>
 * <ul>
 *   <li><b>Active</b> — gold name with enchant glow</li>
 *   <li><b>Preparing</b> — yellow name</li>
 *   <li><b>Ready</b> — green name</li>
 * </ul>
 */
public final class AbilityOverviewMenu {

    /** Total inventory size — 6 rows. */
    private static final int SIZE = 54;

    /** Slots where ability items are placed (rows 1–4, inner 7 per row). */
    private static final int[] ABILITY_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    /** Slot for the close button. */
    private static final int CLOSE_SLOT = 49;

    /** Registry providing all registered ability definitions. */
    private final AbilityDefinitionRegistry registry;

    /** Provides per-player ability state. */
    private final AbilityManager abilityManager;

    /**
     * Creates the ability overview menu.
     *
     * @param registry       the registry of all ability definitions
     * @param abilityManager the manager used to query per-player state
     */
    public AbilityOverviewMenu(@NotNull AbilityDefinitionRegistry registry,
                               @NotNull AbilityManager abilityManager) {
        this.registry = registry;
        this.abilityManager = abilityManager;
    }

    /**
     * Opens the ability overview inventory for the given player.
     *
     * @param player the player to open the menu for
     */
    public void open(@NotNull Player player) {
        final Inventory inv = Bukkit.createInventory(new AbilityMenuHolder(), SIZE,
                ChatColor.GOLD + "" + ChatColor.BOLD + "Abilities");

        final ItemStack border = buildBorderPane();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(45 + i, border);
        }
        for (int row = 1; row <= 4; row++) {
            inv.setItem(row * 9, border);
            inv.setItem(row * 9 + 8, border);
        }

        inv.setItem(CLOSE_SLOT, buildCloseButton());

        final List<AbilityDefinition> defs = registry.getAll();
        for (int i = 0; i < defs.size() && i < ABILITY_SLOTS.length; i++) {
            inv.setItem(ABILITY_SLOTS[i], buildAbilityItem(player, defs.get(i)));
        }

        player.openInventory(inv);
    }

    private ItemStack buildAbilityItem(@NotNull Player player,
                                       @NotNull AbilityDefinition def) {
        final boolean active = isActive(player, def);
        final boolean preparing = !active && isPreparing(player, def);

        final ItemStack item = new ItemStack(def.getIcon());
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        // Colour-code the name by state
        final String stateName;
        if (active) {
            stateName = ChatColor.GOLD + "" + ChatColor.BOLD + def.getDisplayName();
        } else if (preparing) {
            stateName = ChatColor.YELLOW + def.getDisplayName();
        } else {
            stateName = ChatColor.GREEN + def.getDisplayName();
        }
        meta.setDisplayName(stateName);
        meta.setLore(buildLore(def, active, preparing));

        // Enchant glow when active (no visible enchant label)
        if (active) {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    private List<String> buildLore(@NotNull AbilityDefinition def,
                                   boolean active, boolean preparing) {
        final List<String> lore = new ArrayList<>();
        lore.add("");

        if (active) {
            lore.add(ChatColor.GOLD + "⚡ Active!");
        } else if (preparing) {
            lore.add(ChatColor.YELLOW + "◐ Preparing...");
        } else {
            lore.add(ChatColor.GREEN + "● Ready");
        }

        lore.add("");
        lore.add(ChatColor.GRAY + def.getDescription());

        if (def.getSkillName() != null) {
            lore.add("");
            final String formatted = formatName(def.getSkillName());
            lore.add(ChatColor.DARK_GRAY + "Skill: " + ChatColor.GRAY + formatted);
        }

        return lore;
    }

    private boolean isActive(@NotNull Player player, @NotNull AbilityDefinition def) {
        final AbilityType type = AbilityType.fromString(def.getName());
        if (type != null) {
            return abilityManager.isActive(player, type);
        }
        return abilityManager.isActive(player, def.getName());
    }

    private boolean isPreparing(@NotNull Player player, @NotNull AbilityDefinition def) {
        final long windowMillis = (long) def.getPreparationWindowSeconds() * 1000L;
        return abilityManager.isPreparing(player, def.getName(), windowMillis);
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

    private static String formatName(final String name) {
        final String lower = name.toLowerCase().replace('_', ' ');
        if (lower.isEmpty()) {
            return lower;
        }
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
