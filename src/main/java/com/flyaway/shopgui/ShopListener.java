package com.flyaway.shopgui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class ShopListener implements Listener {

    private ShopPlugin plugin;

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Проверяем, что кликнули в инвентаре магазина по Holder
        if (!(holder instanceof ShopInventoryHolder)) {
            return;
        }

        // Paper: отменяем все виды кликов в магазине
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        // Проверяем, что клик в пределах инвентаря магазина (не в инвентаре игрока)
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        // Paper: проверяем тип клика (только обычные клики)
        if (!isAllowedClickType(event)) {
            return;
        }

        ShopItem shopItem = plugin.getShopManager().getItemBySlot(slot);
        if (shopItem != null) {
            plugin.getShopManager().purchaseItem(player, shopItem.getId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Проверяем, что закрыли инвентарь магазина по Holder
        if (holder instanceof ShopInventoryHolder shopHolder) {
            Player player = (Player) event.getPlayer();

            plugin.getLogger().info("Игрок " + player.getName() + " закрыл магазин");
        }
    }

    /**
     * Paper: Проверяет разрешенные типы кликов для магазина
     */
    private boolean isAllowedClickType(InventoryClickEvent event) {
        switch (event.getClick()) {
            case LEFT:
            case RIGHT:
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                return true;
            case DOUBLE_CLICK:
            case DROP:
            case CONTROL_DROP:
            case SWAP_OFFHAND:
            case NUMBER_KEY:
            case MIDDLE:
            case CREATIVE:
            case WINDOW_BORDER_LEFT:
            case WINDOW_BORDER_RIGHT:
            default:
                return false;
        }
    }
}
