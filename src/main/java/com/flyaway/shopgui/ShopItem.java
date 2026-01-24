package com.flyaway.shopgui;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import java.util.*;

public class ShopItem {
    private static boolean hasPapi = false;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final String id;
    private final String name;
    private final Material material;
    private final int slot;
    private final double price;
    private final List<String> lore;
    private final String command;
    private final String texture;
    private final Map<Enchantment, Integer> enchantments;

    public ShopItem(String id, String name, Material material, int slot, double price,
                    List<String> lore, String command, String texture, Map<Enchantment, Integer> enchantments) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.slot = slot;
        this.price = price;
        this.lore = lore;
        this.command = command;
        this.texture = texture;
        this.enchantments = enchantments != null ? enchantments : new HashMap<>();
    }

    public static void setHooks() {
        hasPapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public ItemStack createItemStack(Player player) {
        if (material == Material.PLAYER_HEAD && texture != null && !texture.isEmpty()) {
            return createSkullWithTexture(player);
        }

        return createNormalItem(player);
    }

    private ItemStack createNormalItem(Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            applyCommonItemProperties(meta, player);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createSkullWithTexture(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            applyCommonItemProperties(meta, player);

            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "CustomHead");
                profile.setProperty(new ProfileProperty("textures", texture));
                meta.setPlayerProfile(profile);
            } catch (Exception ignored) {
            }  // Если не получилось установить текстуру, просто оставляем обычную голову

            skull.setItemMeta(meta);
        }

        return skull;
    }

    private void applyCommonItemProperties(ItemMeta meta, Player player) {
        String processedName = applyPlaceholders(name, player);
        meta.displayName(miniMessage.deserialize(processedName));

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            String processedLine = applyPlaceholders(line, player);
            loreComponents.add(miniMessage.deserialize(processedLine));
        }
        meta.lore(loreComponents);

        if (!enchantments.isEmpty() && material != Material.PLAYER_HEAD) {
            for (Map.Entry<Enchantment, Integer> enchant : enchantments.entrySet()) {
                meta.addEnchant(enchant.getKey(), enchant.getValue(), true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
    }

    private String applyPlaceholders(String input, Player player) {
        if (player == null || !hasPapi) {
            return input;
        }

        return PlaceholderAPI.setPlaceholders(player, input);
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public int getSlot() {
        return slot;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getCommand() {
        return command;
    }

    public String getTexture() {
        return texture;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }
}
