package com.example.skywars.loot;

import com.example.skywars.SkyWars;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LootManager {

    private final SkyWars plugin;
    private final Map<LootRarity, List<ItemStack>> lootTable;

    public LootManager(SkyWars plugin) {
        this.plugin = plugin;
        this.lootTable = new EnumMap<>(LootRarity.class);
        loadLoot();
    }

    public void loadLoot() {
        if (!plugin.getConfig().contains("loot")) {
            return;
        }

        for (LootRarity rarity : LootRarity.values()) {
            List<ItemStack> items = new ArrayList<>();
            List<String> itemStrings = plugin.getConfig().getStringList("loot." + rarity.name().toLowerCase());

            for (String itemStr : itemStrings) {
                ItemStack item = parseItem(itemStr);
                if (item != null) {
                    items.add(item);
                }
            }

            lootTable.put(rarity, items);
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

    public ItemStack getRandomLoot() {
        LootRarity rarity = getRandomRarity();
        List<ItemStack> items = lootTable.get(rarity);

        if (items == null || items.isEmpty()) {
            return new ItemStack(Material.AIR);
        }

        Random random = new Random();
        ItemStack item = items.get(random.nextInt(items.size()));
        return item.clone();
    }

    private LootRarity getRandomRarity() {
        Random random = new Random();
        int chance = random.nextInt(100);

        if (chance < 50) {
            return LootRarity.COMMON;
        } else if (chance < 75) {
            return LootRarity.RARE;
        } else if (chance < 90) {
            return LootRarity.EPIC;
        } else {
            return LootRarity.LEGENDARY;
        }
    }

    public void fillChest(org.bukkit.block.Chest chest) {
        Random random = new Random();
        int itemCount = random.nextInt(4) + 3;

        for (int i = 0; i < itemCount; i++) {
            ItemStack item = getRandomLoot();
            if (item.getType() != Material.AIR) {
                chest.getInventory().addItem(item);
            }
        }
    }

    public enum LootRarity {
        COMMON,
        RARE,
        EPIC,
        LEGENDARY
    }
}