package com.flyaway.shopgui;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ShopManager {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ShopPlugin plugin;
    private final ConfigManager configManager;
    private Map<String, ShopItem> shopItems;
    private Currency currency;

    public ShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.shopItems = new HashMap<>();

        loadCurrency();
        reloadShop();
    }

    private void loadCurrency() {
        String currencyName = configManager.getCurrencyName();
        this.currency = CoinsEngineAPI.getCurrency(currencyName);

        if (this.currency == null) {
            plugin.getLogger().warning("Валюта '" + currencyName + "' не найдена в CoinsEngine!");
            plugin.getLogger().warning("Проверьте название валюты в config.yml и убедитесь, что она создана в CoinsEngine");
        } else {
            plugin.getLogger().info("Успешно загружена валюта: " + currencyName);
        }
    }

    private String formatPrice(double amount) {
        if (this.currency != null) {
            return currency.format(amount);
        }
        return String.format("%.0f", amount);
    }

    public void reloadShop() {
        loadCurrency();
        this.shopItems = loadShopItems();
    }

    public Map<String, ShopItem> loadShopItems() {
        Map<String, ShopItem> items = new HashMap<>();

        if (!configManager.getConfig().contains("shop-items")) {
            plugin.getLogger().warning("В конфиге нет раздела 'shop-items'!");
            return items;
        }

        ConfigurationSection shopItemsSection = configManager.getConfig().getConfigurationSection("shop-items");
        for (String key : shopItemsSection.getKeys(false)) {

            try {
                String name = shopItemsSection.getString(key + ".name", "Предмет");
                Material material = parseMaterial(shopItemsSection.getString(key + ".material", "stone"));
                int slot = shopItemsSection.getInt(key + ".slot", 0);
                double price = shopItemsSection.getDouble(key + ".price", 0.0);
                List<String> lore = shopItemsSection.getStringList(key + ".lore");
                String command = shopItemsSection.getString(key + ".command", "");
                String texture = shopItemsSection.getString(key + ".texture", "");

                List<String> formattedLore = new ArrayList<>();
                for (String line : lore) {
                    formattedLore.add(line.replace("{price}", formatPrice(price)));
                }

                Map<Enchantment, Integer> enchantments = loadEnchantments(shopItemsSection, key);

                ShopItem shopItem = new ShopItem(key, name, material, slot, price, formattedLore, command, texture, enchantments);
                items.put(key, shopItem);

            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка загрузки предмета " + key + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Загружено " + items.size() + " товаров для магазина");
        plugin.getLogger().info("Используемая валюта: " + configManager.getCurrencyName());
        return items;
    }

    private Material parseMaterial(String materialName) {
        if (materialName == null) {
            plugin.getLogger().warning("Material name is null, using STONE");
            return Material.STONE;
        }

        Material material = Material.matchMaterial(materialName.toLowerCase());
        if (material == null) {
            plugin.getLogger().warning("Неизвестный материал: " + materialName + ", используем STONE");
            return Material.STONE;
        }

        return material;
    }

    private Map<Enchantment, Integer> loadEnchantments(ConfigurationSection shopItemsSection, String key) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();

        if (shopItemsSection.contains(key + ".enchantments")) {
            for (String enchantKey : shopItemsSection.getConfigurationSection(key + ".enchantments").getKeys(false)) {
                try {
                    Enchantment enchantment = getEnchantmentByName(enchantKey);
                    int level = shopItemsSection.getInt(key + ".enchantments." + enchantKey, 1);

                    if (enchantment != null) {
                        enchantments.put(enchantment, level);
                    } else {
                        plugin.getLogger().warning("Неизвестное зачарование: " + enchantKey + " в пути " + key);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Ошибка загрузки зачарования " + enchantKey + ": " + e.getMessage());
                }
            }
        }

        return enchantments;
    }

    private Enchantment getEnchantmentByName(String name) {
        if (name == null || name.isBlank()) return null;

        String lower = name.toLowerCase().trim();

        Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

        NamespacedKey key = lower.contains(":")
                ? NamespacedKey.fromString(lower)
                : NamespacedKey.minecraft(lower);

        if (key == null) return null;

        return registry.get(key);
    }

    public void openShop(Player player) {
        Component titleComponent = miniMessage.deserialize(configManager.getGuiTitle());

        ShopInventoryHolder holder = new ShopInventoryHolder();
        Inventory gui = Bukkit.createInventory(holder, 54, titleComponent);
        holder.setInventory(gui);

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
            plugin.sendMessage(player, configManager.getMessage("item-not-found"));
            return false;
        }

        if (currency == null) {
            plugin.sendMessage(player, configManager.getMessage("currency-not-found"));
            return false;
        }

        double balance = CoinsEngineAPI.getBalance(player, currency);
        double price = shopItem.getPrice();

        if (balance < price) {
            String message = configManager.getMessage("not-enough-money").replace(
                    "{price}", formatPrice(price));
            plugin.sendMessage(player, message);
            return false;
        }

        CoinsEngineAPI.removeBalance(player, currency, price);

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
                plugin.sendMessage(player, configManager.getMessage("item-create-fail"));
                return false;
            }
        }

        String successMessage = configManager.getMessage("purchase-success")
                .replace("{item}", shopItem.getName())
                .replace("{price}", formatPrice(price));
        plugin.sendMessage(player, successMessage);

        return true;
    }

    private ItemStack createSimpleItemFromShopItem(ShopItem shopItem) {
        try {
            Material material = shopItem.getMaterial();
            if (material == null) {
                plugin.getLogger().warning("Неизвестный материал для предмета: " + shopItem.getId());
                return null;
            }

            ItemStack item = new ItemStack(material, 1);

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
}
