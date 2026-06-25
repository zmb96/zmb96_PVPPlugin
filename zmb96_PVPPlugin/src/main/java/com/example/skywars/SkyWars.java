package com.example.skywars;

import com.example.skywars.commands.SkyWarsCommand;
import com.example.skywars.game.GameManager;
import com.example.skywars.listeners.PlayerListener;
import com.example.skywars.listeners.WorldListener;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyWars extends JavaPlugin {

    private static SkyWars instance;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        gameManager = new GameManager(this);

        getCommand("sw").setExecutor(new SkyWarsCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);

        getLogger().info("SkyWars plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.cleanup();
        }
        getLogger().info("SkyWars plugin disabled!");
    }

    public static SkyWars getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}