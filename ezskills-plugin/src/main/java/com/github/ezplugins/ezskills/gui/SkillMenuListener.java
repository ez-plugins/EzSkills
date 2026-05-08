package com.github.ezplugins.ezskills.gui;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Cancels clicks inside the skills menu to prevent item removal.
 */
public final class SkillMenuListener implements Listener {

    /**
     * Creates the skill menu listener.
     */
    public SkillMenuListener() { }

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SkillMenuHolder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() != null
                && event.getCurrentItem().getType() == Material.BARRIER) {
            event.getWhoClicked().closeInventory();
        }
    }
}
