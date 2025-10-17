package com.flyaway.shopgui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.Map;
import java.util.HashMap;

public class ShopManager {

    private ShopPlugin plugin;
    private Map<String, ShopItem> shopItems;
    private Currency currency;

    public ShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.shopItems = new HashMap<>();
        loadCurrency();

        reloadShop();
    }

    /**
     * Загружает валюту из конфига
     */
    private void loadCurrency() {
        String currencyName = plugin.getConfigManager().getCurrencyName();
        this.currency = CoinsEngineAPI.getCurrency(currencyName);

        if (this.currency == null) {
            plugin.getLogger().warning("Валюта '" + currencyName + "' не найдена в CoinsEngine!");
            plugin.getLogger().warning("Проверьте название валюты в config.yml и убедитесь, что она создана в CoinsEngine");
        } else {
            plugin.getLogger().info("Успешно загружена валюта: " + currencyName);
        }
    }

    public void reloadShop() {
        this.shopItems = plugin.getConfigManager().loadShopItems();
        // Перезагружаем валюту при релоаде
        loadCurrency();
    }

    public void openShop(Player player) {
        String guiTitle = plugin.getConfigManager().getGuiTitle().replace('&', '§');
        Component titleComponent = LegacyComponentSerializer.legacySection().deserialize(guiTitle);

        ShopInventoryHolder holder = new ShopInventoryHolder(plugin);
        Inventory gui = Bukkit.createInventory(holder, 54, titleComponent);

        for (ShopItem shopItem : shopItems.values()) {
            if (shopItem.getSlot() >= 0 && shopItem.getSlot() < 54) {
                gui.setItem(shopItem.getSlot(), shopItem.createItemStack());
            }
        }

        player.openInventory(gui);
    }

    public boolean purchaseItem(Player player, String itemId) {
        ShopItem shopItem = shopItems.get(itemId);
        if (shopItem == null) {
            player.sendMessage(Component.text("Предмет не найден!", NamedTextColor.RED));
            return false;
        }

        if (currency == null) {
            player.sendMessage(Component.text("Ошибка: валюта не найдена! Обратитесь к администратору.", NamedTextColor.RED));
            return false;
        }

        double balance = CoinsEngineAPI.getBalance(player, currency);
        double price = shopItem.getPrice();

        String formattedPrice = String.format("%.0f", price);

        if (balance < price) {
            Component message = Component.text("Недостаточно средств! Нужно: ", NamedTextColor.RED)
                .append(Component.text(formattedPrice + " " + currency.getSymbol(), NamedTextColor.GOLD));
            player.sendMessage(message);
            return false;
        }

        // Списание денег
        CoinsEngineAPI.removeBalance(player, currency, price);

        // Гибридная выдача: команда ИЛИ автоматическое создание
        String command = shopItem.getCommand();
        if (command != null && !command.trim().isEmpty()) {
            // Способ 1: Выдача через команду
            String formattedCommand = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
        } else {
            // Способ 2: Автоматическое создание предмета
            ItemStack item = createSimpleItemFromShopItem(shopItem);
            if (item != null) {
                giveItemToPlayer(player, item);
            } else {
                player.sendMessage(Component.text("Ошибка создания предмета!", NamedTextColor.RED));
                return false;
            }
        }

        // Сообщение об успешной покупке с целой ценой
        Component successMessage = Component.text("Вы успешно купили ", NamedTextColor.GREEN)
            .append(Component.text(shopItem.getName(), NamedTextColor.YELLOW))
            .append(Component.text(" за ", NamedTextColor.GREEN))
            .append(Component.text(formattedPrice + " " + currency.getSymbol(), NamedTextColor.GOLD))
            .append(Component.text("!", NamedTextColor.GREEN));

        player.sendMessage(successMessage);

        return true;
    }

    /**
     * Создает предмет без кастомного названия и лора (только материал и зачарования)
     */
    private ItemStack createSimpleItemFromShopItem(ShopItem shopItem) {
        try {
            Material material = shopItem.getMaterial();
            if (material == null) {
                plugin.getLogger().warning("Неизвестный материал для предмета: " + shopItem.getId());
                return null;
            }

            ItemStack item = new ItemStack(material, 1);

            // Добавляем зачарования если есть
            if (!shopItem.getEnchantments().isEmpty()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    for (Map.Entry<Enchantment, Integer> entry : shopItem.getEnchantments().entrySet()) {
                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }
                    item.setItemMeta(meta);
                }
            }

            return item;

        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка создания простого предмета: " + e.getMessage());
            return null;
        }
    }

    private void giveItemToPlayer(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);

        // Если не поместилось в инвентарь, выкидываем на землю
        for (ItemStack leftItem : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftItem);
        }
    }

    public ShopItem getItemBySlot(int slot) {
        return shopItems.values().stream()
            .filter(item -> item.getSlot() == slot)
            .findFirst()
            .orElse(null);
    }

    public Map<String, ShopItem> getShopItems() {
        return shopItems;
    }

    /**
     * Получает текущую валюту
     */
    public Currency getCurrency() {
        return currency;
    }
}
