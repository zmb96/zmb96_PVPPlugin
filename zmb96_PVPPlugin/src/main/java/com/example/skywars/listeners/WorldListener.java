package com.example.skywars.listeners;

import com.example.skywars.SkyWars;
import com.example.skywars.game.GameState;
import com.example.skywars.loot.LootManager;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class WorldListener implements Listener {

    private final SkyWars plugin;
    private final LootManager lootManager;

    public WorldListener(SkyWars plugin) {
        this.plugin = plugin;
        this.lootManager = new LootManager(plugin);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();

        if (!plugin.getGameManager().getPlayerManager().isInGame(player)) {
            return;
        }

        if (event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();

            if (plugin.getGameManager().getMapManager().getChests().contains(chest.getLocation())) {
                if (chest.getInventory().isEmpty()) {
                    lootManager.fillChest(chest);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getGameManager().getPlayerManager().isInGame(player)) {
            return;
        }

        if (plugin.getGameManager().getGameState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getGameManager().getPlayerManager().isAlive(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getGameManager().getPlayerManager().isInGame(player)) {
            return;
        }

        if (plugin.getGameManager().getGameState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getGameManager().getPlayerManager().isAlive(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if (!plugin.getGameManager().getPlayerManager().isInGame(player)) {
            return;
        }

        if (plugin.getGameManager().getGameState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getGameManager().getPlayerManager().isAlive(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getGameManager().getPlayerManager().isInGame(player)) {
            return;
        }

        if (plugin.getGameManager().getGameState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getGameManager().getPlayerManager().isAlive(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getGameManager().getPlayerManager().isInGame(player)) {
            return;
        }

        if (event.getFrom().getY() < event.getTo().getY()) {
            if (event.getTo().getY() < 0) {
                if (plugin.getGameManager().getPlayerManager().isAlive(player)) {
                    player.setHealth(0);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getGameManager().getPlayerManager().isInGame(player)) {
            return;
        }

        if (plugin.getGameManager().getGameState() != GameState.PLAYING) {
            return;
        }

        if (!plugin.getGameManager().getPlayerManager().isAlive(player)) {
            event.setCancelled(true);
        }
    }
}