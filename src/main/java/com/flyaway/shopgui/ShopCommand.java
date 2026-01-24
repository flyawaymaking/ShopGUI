package com.flyaway.shopgui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    private final ShopPlugin plugin;
    private final ConfigManager configManager;

    public ShopCommand(ShopPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.sendMessage(sender, configManager.getMessage("player-only"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("dshop.reload")) {
                plugin.sendMessage(sender, configManager.getMessage("no-permission"));
                return true;
            }

            plugin.getConfigManager().reloadConfig();
            plugin.getShopManager().reloadShop();
            ShopItem.setHooks();
            plugin.sendMessage(sender, configManager.getMessage("reload-success"));
            return true;
        }

        plugin.getShopManager().openShop(player);
        return true;
    }
}
