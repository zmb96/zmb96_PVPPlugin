package com.example.skywars.players;

import com.example.skywars.SkyWars;
import com.example.skywars.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerManager {

    private final SkyWars plugin;
    private final Set<UUID> players;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> spectators;
    private final Map<UUID, PlayerState> playerStates;
    private final Map<UUID, String> playerKits;
    private final Map<UUID, Integer> playerKills;

    public PlayerManager(SkyWars plugin) {
        this.plugin = plugin;
        this.players = new HashSet<>();
        this.alivePlayers = new HashSet<>();
        this.spectators = new HashSet<>();
        this.playerStates = new HashMap<>();
        this.playerKits = new HashMap<>();
        this.playerKills = new HashMap<>();
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        alivePlayers.add(player.getUniqueId());
        playerKills.put(player.getUniqueId(), 0);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        alivePlayers.remove(player.getUniqueId());
        spectators.remove(player.getUniqueId());
        playerStates.remove(player.getUniqueId());
        playerKits.remove(player.getUniqueId());
        playerKills.remove(player.getUniqueId());
    }

    public void savePlayerState(Player player) {
        PlayerState state = new PlayerState();
        state.location = player.getLocation();
        state.inventory = player.getInventory().getContents();
        state.armor = player.getInventory().getArmorContents();
        state.health = player.getHealth();
        state.food = player.getFoodLevel();
        state.exp = player.getExp();
        state.level = player.getLevel();
        state.gameMode = player.getGameMode();
        state.effects = new HashSet<>(player.getActivePotionEffects());

        playerStates.put(player.getUniqueId(), state);
    }

    public void restorePlayerState(Player player) {
        PlayerState state = playerStates.get(player.getUniqueId());
        if (state == null) return;

        player.getInventory().clear();
        player.getInventory().setContents(state.inventory);
        player.getInventory().setArmorContents(state.armor);

        player.setHealth(state.health);
        player.setFoodLevel(state.food);
        player.setExp(state.exp);
        player.setLevel(state.level);
        player.setGameMode(state.gameMode);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        for (PotionEffect effect : state.effects) {
            player.addPotionEffect(effect);
        }

        player.setFireTicks(0);
        player.setFallDistance(0);
    }

    public void setSpectator(Player player) {
        if (!alivePlayers.contains(player.getUniqueId())) return;

        alivePlayers.remove(player.getUniqueId());
        spectators.add(player.getUniqueId());

        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();

        for (Player other : getPlayers()) {
            if (other != player && alivePlayers.contains(other.getUniqueId())) {
                other.hidePlayer(plugin, player);
            }
        }

        player.sendMessage("§a你现在是旁观者模式！");
    }

    public void addKill(Player killer) {
        playerKills.put(killer.getUniqueId(), playerKills.getOrDefault(killer.getUniqueId(), 0) + 1);
    }

    public int getKills(Player player) {
        return playerKills.getOrDefault(player.getUniqueId(), 0);
    }

    public void setKit(Player player, String kit) {
        playerKits.put(player.getUniqueId(), kit);
    }

    public String getKit(Player player) {
        return playerKits.get(player.getUniqueId());
    }

    public void giveKits() {
        KitManager kitManager = plugin.getGameManager().getKitManager();

        for (Player player : getPlayers()) {
            if (alivePlayers.contains(player.getUniqueId())) {
                String kitName = playerKits.getOrDefault(player.getUniqueId(), "default");
                kitManager.giveKit(player, kitName);
            }
        }
    }

    public void clearPlayers() {
        players.clear();
        alivePlayers.clear();
        spectators.clear();
        playerStates.clear();
        playerKits.clear();
        playerKills.clear();
    }

    public Set<Player> getPlayers() {
        Set<Player> result = new HashSet<>();
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    public Set<Player> getAlivePlayers() {
        Set<Player> result = new HashSet<>();
        for (UUID uuid : alivePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    public Set<Player> getSpectators() {
        Set<Player> result = new HashSet<>();
        for (UUID uuid : spectators) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    public boolean isInGame(Player player) {
        return players.contains(player.getUniqueId());
    }

    public boolean isAlive(Player player) {
        return alivePlayers.contains(player.getUniqueId());
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player.getUniqueId());
    }

    private static class PlayerState {
        Location location;
        ItemStack[] inventory;
        ItemStack[] armor;
        double health;
        int food;
        float exp;
        int level;
        GameMode gameMode;
        Set<PotionEffect> effects;
    }
}