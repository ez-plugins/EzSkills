package com.github.ezplugins.ezskills.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Marker {@link InventoryHolder} used to identify the skill progress inventory.
 *
 * <p>Used by {@link SkillMenuListener} to distinguish skill GUI clicks from other
 * inventory events without relying on fragile title-string matching.</p>
 */
public final class SkillMenuHolder implements InventoryHolder {

    @Override
    @NotNull
    public Inventory getInventory() {
        throw new UnsupportedOperationException("SkillMenuHolder does not own an inventory.");
    }
}
