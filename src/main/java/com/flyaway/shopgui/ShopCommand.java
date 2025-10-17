package com.flyaway.shopgui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShopCommand implements CommandExecutor {

    private ShopPlugin plugin;

    public ShopCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Paper: современный способ отправки сообщений
            sender.sendMessage(Component.text("Эта команда только для игроков!", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("dshop.reload")) {
                player.sendMessage(Component.text("У вас нет прав для перезагрузки магазина!", NamedTextColor.RED));
                return true;
            }

            plugin.getConfigManager().reloadConfig();
            plugin.getShopManager().reloadShop();
            player.sendMessage(Component.text("Конфигурация магазина перезагружена!", NamedTextColor.GREEN));
            return true;
        }

        // Открываем магазин
        plugin.getShopManager().openShop(player);
        return true;
    }
}
