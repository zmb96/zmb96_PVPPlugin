package com.example.skywars.kits;

import com.example.skywars.SkyWars;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitManager {

    private final SkyWars plugin;
    private final Map<String, Kit> kits;

    public KitManager(SkyWars plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
        loadKits();
    }

    public void loadKits() {
        if (!plugin.getConfig().contains("kits")) {
            return;
        }

        for (String kitName : plugin.getConfig().getConfigurationSection("kits").getKeys(false)) {
            Kit kit = new Kit(kitName);
            kit.setName(plugin.getConfig().getString("kits." + kitName + ".name"));
            kit.setDescription(plugin.getConfig().getString("kits." + kitName + ".description"));

            List<String> items = plugin.getConfig().getStringList("kits." + kitName + ".items");
            for (String itemStr : items) {
                ItemStack item = parseItem(itemStr);
                if (item != null) {
                    kit.addItem(item);
                }
            }

            kits.put(kitName, kit);
        }
    }

    private ItemStack parseItem(String itemStr) {
        String[] parts = itemStr.split("\\|");
        String[] mainParts = parts[0].split(":");

        Material material = Material.matchMaterial(mainParts[0]);
        if (material == null) return null;

        int amount = 1;
        if (mainParts.length > 1) {
            try {
                amount = Integer.parseInt(mainParts[1]);
            } catch (NumberFormatException e) {
                amount = 1;
            }
        }

        ItemStack item = new ItemStack(material, amount);

        if (parts.length > 1) {
            String[] enchantParts = parts[1].split(":");
            if (enchantParts.length == 2) {
                Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(enchantParts[0].toLowerCase()));
                if (enchantment != null) {
                    try {
                        int level = Integer.parseInt(enchantParts[1]);
                        item.addUnsafeEnchantment(enchantment, level);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        return item;
    }

    public void giveKit(Player player, String kitName) {
        Kit kit = kits.get(kitName);
        if (kit == null) {
            giveDefaultKit(player);
            return;
        }

        for (ItemStack item : kit.getItems()) {
            player.getInventory().addItem(item.clone());
        }

        player.updateInventory();
    }

    public void giveDefaultKit(Player player) {
        giveKit(player, "default");
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Map<String, Kit> getKits() {
        return kits;
    }

    public static class Kit {
        private final String id;
        private String name;
        private String description;
        private final java.util.List<ItemStack> items;

        public Kit(String id) {
            this.id = id;
            this.items = new java.util.ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public java.util.List<ItemStack> getItems() {
            return items;
        }

        public void addItem(ItemStack item) {
            items.add(item);
        }

        public ItemStack getDisplayItem() {
            if (items.isEmpty()) {
                return new ItemStack(Material.CHEST);
            }

            ItemStack display = items.get(0).clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + name);
                if (description != null) {
                    meta.setLore(java.util.Arrays.asList("§7" + description));
                }
                display.setItemMeta(meta);
            }
            return display;
        }
    }
}