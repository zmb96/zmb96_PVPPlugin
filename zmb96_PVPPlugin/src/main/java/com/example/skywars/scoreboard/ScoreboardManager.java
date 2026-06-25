package com.example.skywars.scoreboard;

import com.example.skywars.SkyWars;
import com.example.skywars.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    private final SkyWars plugin;

    public ScoreboardManager(SkyWars plugin) {
        this.plugin = plugin;
    }

    public void updateScoreboard() {
        for (Player player : plugin.getGameManager().getPlayerManager().getPlayers()) {
            updatePlayerScoreboard(player);
        }
    }

    public void updatePlayerScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("skywars", "dummy", "§6§lSkyWars");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        GameState state = plugin.getGameManager().getGameState();
        String stateText = "§c等待中";

        if (state == GameState.STARTING) {
            stateText = "§e倒计时: " + plugin.getGameManager().getCountdown();
        } else if (state == GameState.PLAYING) {
            stateText = "§a进行中";
        } else if (state == GameState.ENDING) {
            stateText = "§c结束";
        }

        int alive = plugin.getGameManager().getPlayerManager().getAlivePlayers().size();
        int kills = plugin.getGameManager().getPlayerManager().getKills(player);
        int seconds = plugin.getGameManager().getGameSeconds();

        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        String time = String.format("%02d:%02d", minutes, remainingSeconds);

        Score blank1 = objective.getScore(" ");
        blank1.setScore(10);

        Score stateScore = objective.getScore("§e状态: " + stateText);
        stateScore.setScore(9);

        Score blank2 = objective.getScore("  ");
        blank2.setScore(8);

        Score aliveScore = objective.getScore("§a存活: §f" + alive);
        aliveScore.setScore(7);

        Score killScore = objective.getScore("§c击杀: §f" + kills);
        killScore.setScore(6);

        Score blank3 = objective.getScore("   ");
        blank3.setScore(5);

        Score timeScore = objective.getScore("§b时间: §f" + time);
        timeScore.setScore(4);

        Score blank4 = objective.getScore("    ");
        blank4.setScore(3);

        Score serverScore = objective.getScore("§7zmb96_PVPPlugin");
        serverScore.setScore(2);

        player.setScoreboard(scoreboard);
    }

    public void clearScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}