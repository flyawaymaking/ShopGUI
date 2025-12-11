package com.flyaway.shopgui;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final ShopPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public String getCurrencyName() {
        return config.getString("currency", "coins");
    }

    public String getGuiTitle() {
        return config.getString("gui-title", "Магазин за коины");
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "<red>not-found: messages." + key);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        plugin.getLogger().info("Конфигурация магазина перезагружена");
        plugin.getLogger().info("Используемая валюта: " + getCurrencyName());
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
