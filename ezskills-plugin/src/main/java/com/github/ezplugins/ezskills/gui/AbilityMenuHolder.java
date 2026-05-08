package com.github.ezplugins.ezskills.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Marker {@link InventoryHolder} used to identify the ability overview inventory.
 *
 * <p>Used by {@link AbilityMenuListener} to distinguish ability GUI clicks from other
 * inventory events without relying on fragile title-string matching.</p>
 */
public final class AbilityMenuHolder implements InventoryHolder {

    @Override
    @NotNull
    public Inventory getInventory() {
        throw new UnsupportedOperationException("AbilityMenuHolder does not own an inventory.");
    }
}
