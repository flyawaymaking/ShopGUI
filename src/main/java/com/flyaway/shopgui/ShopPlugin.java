package com.flyaway.shopgui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private ShopManager shopManager;
    private ConfigManager configManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.shopManager = new ShopManager(this);
        ShopItem.setHooks();

        getCommand("dshop").setExecutor(new ShopCommand(this));

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        getLogger().info("ShopPlugin включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ShopPlugin выключен!");
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(miniMessage.deserialize(message));
    }
}
