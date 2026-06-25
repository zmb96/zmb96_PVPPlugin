package com.example.skywars.utils;

import com.example.skywars.SkyWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class BorderManager {

    private final SkyWars plugin;

    public BorderManager(SkyWars plugin) {
        this.plugin = plugin;
    }

    public void setupBorder() {
        World world = plugin.getGameManager().getMapManager().getGameWorld();
        if (world == null) return;

        Location center = plugin.getGameManager().getMapManager().getCenter();
        int size = plugin.getGameManager().getMapManager().getBorderSize();

        if (center == null) {
            center = new Location(world, 0, 64, 0);
        }

        WorldBorder border = world.getWorldBorder();
        border.setCenter(center);
        border.setSize(size);
        border.setDamageAmount(2.0);
        border.setDamageBuffer(5.0);
        border.setWarningDistance(10);
        border.setWarningTime(15);
    }

    public void shrinkBorder(int amount, int minSize) {
        World world = plugin.getGameManager().getMapManager().getGameWorld();
        if (world == null) return;

        WorldBorder border = world.getWorldBorder();
        double currentSize = border.getSize();

        if (currentSize <= minSize) {
            return;
        }

        double newSize = Math.max(currentSize - amount, minSize);
        border.setSize(newSize);
    }

    public void resetBorder() {
        World world = plugin.getGameManager().getMapManager().getGameWorld();
        if (world == null) return;

        Location center = plugin.getGameManager().getMapManager().getCenter();
        int size = plugin.getGameManager().getMapManager().getBorderSize();

        if (center == null) {
            center = new Location(world, 0, 64, 0);
        }

        WorldBorder border = world.getWorldBorder();
        border.setCenter(center);
        border.setSize(size);
    }
}