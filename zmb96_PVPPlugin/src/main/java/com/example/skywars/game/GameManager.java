package com.example.skywars.game;

import com.example.skywars.SkyWars;
import com.example.skywars.kits.KitManager;
import com.example.skywars.maps.MapManager;
import com.example.skywars.players.PlayerManager;
import com.example.skywars.scoreboard.ScoreboardManager;
import com.example.skywars.utils.BorderManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final SkyWars plugin;
    private GameState gameState;
    private final MapManager mapManager;
    private final PlayerManager playerManager;
    private final KitManager kitManager;
    private final ScoreboardManager scoreboardManager;
    private final BorderManager borderManager;

    private BukkitTask countdownTask;
    private BukkitTask borderTask;
    private BukkitTask gameTask;
    private int countdown;
    private int gameSeconds;

    public GameManager(SkyWars plugin) {
        this.plugin = plugin;
        this.gameState = GameState.WAITING;
        this.mapManager = new MapManager(plugin);
        this.playerManager = new PlayerManager(plugin);
        this.kitManager = new KitManager(plugin);
        this.scoreboardManager = new ScoreboardManager(plugin);
        this.borderManager = new BorderManager(plugin);
    }

    public void joinGame(Player player) {
        if (gameState == GameState.PLAYING || gameState == GameState.ENDING) {
            player.sendMessage("§c游戏正在进行中，无法加入！");
            return;
        }

        if (playerManager.getPlayers().size() >= plugin.getConfig().getInt("game.max-players")) {
            player.sendMessage("§c游戏已满！");
            return;
        }

        playerManager.savePlayerState(player);
        playerManager.addPlayer(player);

        Location spawn = mapManager.getRandomSpawn();
        if (spawn != null) {
            player.teleport(spawn);
        }

        player.sendMessage("§a你已加入游戏！");
        broadcast("§e" + player.getName() + " §a加入了游戏！ (" + playerManager.getPlayers().size() + "/" + plugin.getConfig().getInt("game.max-players") + ")");

        if (gameState == GameState.WAITING && playerManager.getPlayers().size() >= plugin.getConfig().getInt("game.min-players")) {
            startCountdown();
        }

        scoreboardManager.updateScoreboard();
    }

    public void leaveGame(Player player) {
        if (!playerManager.isInGame(player)) {
            player.sendMessage("§c你不在游戏中！");
            return;
        }

        playerManager.removePlayer(player);
        playerManager.restorePlayerState(player);
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());

        player.sendMessage("§a你已离开游戏！");
        broadcast("§e" + player.getName() + " §c离开了游戏！ (" + playerManager.getPlayers().size() + "/" + plugin.getConfig().getInt("game.max-players") + ")");

        if (gameState == GameState.STARTING && playerManager.getPlayers().size() < plugin.getConfig().getInt("game.min-players")) {
            stopCountdown();
            broadcast("§c玩家不足，倒计时已取消！");
        }

        if (gameState == GameState.PLAYING && playerManager.getAlivePlayers().size() <= 1) {
            endGame();
        }

        scoreboardManager.updateScoreboard();
    }

    private void startCountdown() {
        gameState = GameState.STARTING;
        countdown = plugin.getConfig().getInt("game.countdown");

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (countdown <= 0) {
                startGame();
                return;
            }

            if (countdown <= 5 || countdown % 5 == 0) {
                broadcast("§e游戏将在 §c" + countdown + " §e秒后开始！");
            }

            countdown--;
        }, 0L, 20L);
    }

    private void stopCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        gameState = GameState.WAITING;
    }

    private void startGame() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        gameState = GameState.PLAYING;
        gameSeconds = 0;

        broadcast("§a游戏开始！祝你好运！");
        broadcast("§6击杀其他玩家，成为最后的幸存者！");

        playerManager.giveKits();

        startBorderShrink();
        startGameTimer();

        scoreboardManager.updateScoreboard();
    }

    private void startBorderShrink() {
        int interval = plugin.getConfig().getInt("game.border-shrink-interval");
        int shrinkAmount = plugin.getConfig().getInt("game.border-shrink-amount");
        int minSize = plugin.getConfig().getInt("game.min-border-size");

        borderTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (gameState != GameState.PLAYING) return;

            borderManager.shrinkBorder(shrinkAmount, minSize);
            broadcast("§c边界正在缩小！小心！");
        }, interval * 20L, interval * 20L);
    }

    private void startGameTimer() {
        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (gameState != GameState.PLAYING) return;

            gameSeconds++;
            scoreboardManager.updateScoreboard();
        }, 20L, 20L);
    }

    public void endGame() {
        gameState = GameState.ENDING;

        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (borderTask != null) {
            borderTask.cancel();
            borderTask = null;
        }
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        Player winner = null;
        if (playerManager.getAlivePlayers().size() == 1) {
            winner = playerManager.getAlivePlayers().iterator().next();
            broadcast("§6§l恭喜 §e" + winner.getName() + " §6§l获得胜利！");
        } else {
            broadcast("§c游戏结束，没有胜利者！");
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cleanup();
            resetGame();
        }, 100L);
    }

    public void forceStart() {
        if (gameState == GameState.WAITING || gameState == GameState.STARTING) {
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }
            startGame();
        } else {
            broadcast("§c游戏已经在进行中！");
        }
    }

    public void forceStop() {
        if (gameState == GameState.PLAYING || gameState == GameState.STARTING) {
            broadcast("§c游戏被管理员强制结束！");
            endGame();
        } else {
            broadcast("§c游戏未在进行中！");
        }
    }

    private void resetGame() {
        gameState = GameState.WAITING;
        countdown = 0;
        gameSeconds = 0;

        for (Player player : playerManager.getPlayers()) {
            playerManager.restorePlayerState(player);
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        playerManager.clearPlayers();
        mapManager.resetMap();
        scoreboardManager.updateScoreboard();
    }

    public void cleanup() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (borderTask != null) {
            borderTask.cancel();
            borderTask = null;
        }
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
    }

    public void broadcast(String message) {
        for (Player player : playerManager.getPlayers()) {
            player.sendMessage(message);
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public BorderManager getBorderManager() {
        return borderManager;
    }

    public int getCountdown() {
        return countdown;
    }

    public int getGameSeconds() {
        return gameSeconds;
    }
}