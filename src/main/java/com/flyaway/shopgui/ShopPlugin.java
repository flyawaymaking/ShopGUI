package com.flyaway.shopgui;

import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private static ShopPlugin instance;
    private ShopManager shopManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Создаем конфиг если его нет
        saveDefaultConfig();

        // Инициализируем менеджеры
        this.configManager = new ConfigManager(this);
        this.shopManager = new ShopManager(this);

        // Регистрируем команды
        getCommand("dshop").setExecutor(new ShopCommand(this));

        // Регистрируем слушатели
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        getLogger().info("ShopPlugin включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ShopPlugin выключен!");
    }

    public static ShopPlugin getInstance() {
        return instance;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
