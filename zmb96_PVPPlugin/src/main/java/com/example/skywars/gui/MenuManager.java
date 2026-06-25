package com.example.skywars.gui;

import com.example.skywars.SkyWars;
import com.example.skywars.game.GameState;
import com.example.skywars.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MenuManager {

    private final SkyWars plugin;

    public MenuManager(SkyWars plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6SkyWars 主菜单");

        ItemStack joinItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta joinMeta = joinItem.getItemMeta();
        joinMeta.setDisplayName("§a加入游戏");
        joinMeta.setLore(Arrays.asList("§7点击加入游戏"));
        joinItem.setItemMeta(joinMeta);
        menu.setItem(11, joinItem);

        ItemStack kitItem = new ItemStack(Material.CHEST);
        ItemMeta kitMeta = kitItem.getItemMeta();
        kitMeta.setDisplayName("§e选择职业");
        kitMeta.setLore(Arrays.asList("§7点击选择你的职业"));
        kitItem.setItemMeta(kitMeta);
        menu.setItem(13, kitItem);

        ItemStack statsItem = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName("§b查看统计");
        statsMeta.setLore(Arrays.asList("§7点击查看你的统计信息"));
        statsItem.setItemMeta(statsMeta);
        menu.setItem(15, statsItem);

        ItemStack leaveItem = new ItemStack(Material.BARRIER);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName("§c离开游戏");
        leaveMeta.setLore(Arrays.asList("§7点击离开游戏"));
        leaveItem.setItemMeta(leaveMeta);
        menu.setItem(22, leaveItem);

        player.openInventory(menu);
    }

    public void openKitMenu(Player player) {
        KitManager kitManager = plugin.getGameManager().getKitManager();
        int size = (int) Math.ceil(kitManager.getKits().size() / 9.0) * 9;

        if (size < 9) size = 9;
        if (size > 54) size = 54;

        Inventory menu = Bukkit.createInventory(null, size, "§6选择职业");

        int slot = 0;
        for (KitManager.Kit kit : kitManager.getKits().values()) {
            if (slot >= size) break;

            ItemStack item = kit.getDisplayItem();
            menu.setItem(slot, item);
            slot++;
        }

        player.openInventory(menu);
    }

    public void handleMenuClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        if (displayName.contains("加入游戏")) {
            plugin.getGameManager().joinGame(player);
        } else if (displayName.contains("选择职业")) {
            openKitMenu(player);
        } else if (displayName.contains("查看统计")) {
            showStats(player);
        } else if (displayName.contains("离开游戏")) {
            plugin.getGameManager().leaveGame(player);
        }
    }

    public void handleKitClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();
        KitManager kitManager = plugin.getGameManager().getKitManager();

        for (KitManager.Kit kit : kitManager.getKits().values()) {
            if (displayName.contains(kit.getName())) {
                plugin.getGameManager().getPlayerManager().setKit(player, kit.getId());
                player.sendMessage("§a你选择了职业: §e" + kit.getName());
                player.closeInventory();
                return;
            }
        }
    }

    private void showStats(Player player) {
        int kills = plugin.getGameManager().getPlayerManager().getKills(player);
        player.sendMessage("§6===== 你的统计 =====");
        player.sendMessage("§e击杀数: §a" + kills);
        player.sendMessage("§6==================");
    }
}