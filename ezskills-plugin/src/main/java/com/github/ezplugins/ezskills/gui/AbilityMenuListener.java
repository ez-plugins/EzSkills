package com.github.ezplugins.ezskills.gui;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Cancels clicks inside the ability overview inventory to prevent item theft, and
 * closes the inventory when the player clicks the barrier (close) button.
 */
public final class AbilityMenuListener implements Listener {

    /**
     * Creates the ability menu listener.
     */
    public AbilityMenuListener() { }

    /**
     * Handles inventory clicks inside the ability overview menu.
     *
     * @param event the click event
     */
    @EventHandler
    public void onClick(@NotNull InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AbilityMenuHolder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() != null
                && event.getCurrentItem().getType() == Material.BARRIER) {
            event.getWhoClicked().closeInventory();
        }
    }
}
