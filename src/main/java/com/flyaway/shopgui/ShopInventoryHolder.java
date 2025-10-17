package com.flyaway.shopgui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopInventoryHolder implements InventoryHolder {

    private final ShopPlugin plugin;
    private Inventory inventory;

    public ShopInventoryHolder(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    public ShopPlugin getPlugin() {
        return plugin;
    }

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        // Всегда возвращаем не-null значение
        if (inventory == null) {
            // Создаем пустой инвентарь как fallback
            return plugin.getServer().createInventory(this, 9, "Магазин");
        }
        return inventory;
    }
}
