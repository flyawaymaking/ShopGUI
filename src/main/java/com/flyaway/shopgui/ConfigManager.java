package com.flyaway.shopgui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ConfigManager {

    private ShopPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    /**
     * Получает название валюты из конфига
     */
    public String getCurrencyName() {
        return config.getString("currency", "coins");
    }

    public Map<String, ShopItem> loadShopItems() {
        Map<String, ShopItem> items = new HashMap<>();

        if (!config.contains("shop-items")) {
            plugin.getLogger().warning("В конфиге нет раздела 'shop-items'!");
            return items;
        }

        for (String key : config.getConfigurationSection("shop-items").getKeys(false)) {
            String path = "shop-items." + key;

            try {
                String name = config.getString(path + ".name", "Предмет");
                Material material = parseMaterial(config.getString(path + ".material", "stone"));
                int slot = config.getInt(path + ".slot", 0);
                double price = config.getDouble(path + ".price", 0.0);
                List<String> lore = config.getStringList(path + ".lore");
                String command = config.getString(path + ".command", "");
                String texture = config.getString(path + ".texture", "");

                // Заменяем плейсхолдер цены в лоре
                List<String> formattedLore = new ArrayList<>();
                for (String line : lore) {
                    formattedLore.add(line.replace("%price%", String.format("%.0f", price)));
                }

                // Загружаем зачарования
                Map<Enchantment, Integer> enchantments = loadEnchantments(path);

                ShopItem shopItem = new ShopItem(key, name, material, slot, price, formattedLore, command, texture, enchantments);
                items.put(key, shopItem);

            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка загрузки предмета " + key + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("Загружено " + items.size() + " товаров для магазина");
        plugin.getLogger().info("Используемая валюта: " + getCurrencyName());
        return items;
    }

    private Material parseMaterial(String materialName) {
        if (materialName == null) {
            plugin.getLogger().warning("Material name is null, using STONE");
            return Material.STONE;
        }

        // Paper 1.21+ способ через matchMaterial
        Material material = Material.matchMaterial(materialName.toLowerCase());
        if (material == null) {
            plugin.getLogger().warning("Неизвестный материал: " + materialName + ", используем STONE");
            return Material.STONE;
        }

        return material;
    }

    private Map<Enchantment, Integer> loadEnchantments(String path) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();

        if (config.contains(path + ".enchantments")) {
            for (String enchantKey : config.getConfigurationSection(path + ".enchantments").getKeys(false)) {
                try {
                    Enchantment enchantment = getEnchantmentByName(enchantKey);
                    int level = config.getInt(path + ".enchantments." + enchantKey, 1);

                    if (enchantment != null) {
                        enchantments.put(enchantment, level);
                    } else {
                        plugin.getLogger().warning("Неизвестное зачарование: " + enchantKey + " в пути " + path);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Ошибка загрузки зачарования " + enchantKey + ": " + e.getMessage());
                }
            }
        }

        return enchantments;
    }

    private Enchantment getEnchantmentByName(String name) {
        // Paper 1.21+ способ через NamespacedKey
        try {
            // Если указан полный ключ (например: minecraft:sharpness)
            if (name.contains(":")) {
                NamespacedKey key = NamespacedKey.fromString(name.toLowerCase());
                if (key != null) {
                    Enchantment enchant = Enchantment.getByKey(key);
                    if (enchant != null) return enchant;
                }
            }

            // Если указано только имя (например: sharpness)
            NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase());
            Enchantment enchant = Enchantment.getByKey(key);
            if (enchant != null) return enchant;

        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при поиске зачарования: " + name + " - " + e.getMessage());
        }

        return null;
    }

    public String getGuiTitle() {
        return config.getString("gui-title", "Магазин за коины");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        plugin.getLogger().info("Конфигурация магазина перезагружена");
        plugin.getLogger().info("Используемая валюта: " + getCurrencyName());
    }
}
