package com.flyaway.shopgui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ShopItem {
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

    public ItemStack createItemStack() {
        if (material == Material.PLAYER_HEAD && texture != null && !texture.isEmpty()) {
            return createSkullWithTexture();
        }

        return createNormalItem();
    }

    private ItemStack createNormalItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            applyCommonItemProperties(meta);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createSkullWithTexture() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            applyCommonItemProperties(meta);

            try {
                PlayerProfile profile = Bukkit.createProfile(java.util.UUID.randomUUID(), "CustomHead");
                ProfileProperty textureProperty = new ProfileProperty("textures", texture);
                profile.setProperty(textureProperty);
                meta.setPlayerProfile(profile);
            } catch (Exception e) {
                // Если не получилось установить текстуру, просто оставляем обычную голову
            }

            skull.setItemMeta(meta);
        }

        return skull;
    }

    private void applyCommonItemProperties(ItemMeta meta) {
        Component displayName = miniMessage.deserialize(name);
        meta.displayName(displayName);

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            Component loreLine = miniMessage.deserialize(line);
            loreComponents.add(loreLine);
        }
        meta.lore(loreComponents);

        if (!enchantments.isEmpty() && material != Material.PLAYER_HEAD) {
            for (Map.Entry<Enchantment, Integer> enchant : enchantments.entrySet()) {
                meta.addEnchant(enchant.getKey(), enchant.getValue(), true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
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
