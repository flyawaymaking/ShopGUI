package com.flyaway.shopgui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;

public class ShopListener implements Listener {

    private final ShopPlugin plugin;

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopInventoryHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        if (!isAllowedClickType(event)) {
            return;
        }

        ShopItem shopItem = plugin.getShopManager().getItemBySlot(slot);
        if (shopItem != null) {
            plugin.getShopManager().purchaseItem(player, shopItem.getId());
        }
    }

    private boolean isAllowedClickType(InventoryClickEvent event) {
        return switch (event.getClick()) {
            case LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT -> true;
            default -> false;
        };
    }
}
