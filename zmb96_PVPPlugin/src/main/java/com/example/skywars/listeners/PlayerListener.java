package com.example.skywars.listeners;

import com.example.skywars.SkyWars;
import com.example.skywars.game.GameState;
import com.example.skywars.gui.MenuManager;
import com.example.skywars.loot.LootManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final SkyWars plugin;
    private final MenuManager menuManager;

    public PlayerListener(SkyWars plugin) {
        this.plugin = plugin;
        this.menuManager = new MenuManager(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();
        if (plugin.getGameManager().getPlayerManager().isInGame(player)) {
            plugin.getGameManager().leaveGame(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        if (!plugin.getGameManager().getPlayerManager().isInGame(player)) {
            return;
        }

        event.setDeathMessage(null);
        event.getDrops().clear();

        if (killer != null && plugin.getGameManager().getPlayerManager().isInGame(killer)) {
            plugin.getGameManager().getPlayerManager().addKill(killer);
            killer.sendMessage("§a你击杀了 §e" + player.getName() + "§a！");
            killer.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            killer.giveExp(10);
        }

        plugin.getGameManager().broadcast("§c" + player.getName() + " §e被淘汰了！");

        plugin.getGameManager().getPlayerManager().setSpectator(player);

        if (plugin.getGameManager().getPlayerManager().getAlivePlayers().size() <= 1) {
            plugin.getGameManager().endGame();
        }

        plugin.getGameManager().getScoreboardManager().updateScoreboard();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (plugin.getGameManager().getPlayerManager().isSpectator(player)) {
            event.setRespawnLocation(plugin.getGameManager().getMapManager().getSpectatorSpawn());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.contains("SkyWars 主菜单")) {
            event.setCancelled(true);
            menuManager.handleMenuClick(player, event.getCurrentItem());
        } else if (title.contains("选择职业")) {
            event.setCancelled(true);
            menuManager.handleKitClick(player, event.getCurrentItem());
        }
    }
}