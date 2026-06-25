package com.example.skywars.maps;

import com.example.skywars.SkyWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapManager {

    private final SkyWars plugin;
    private final Map<String, GameMap> maps;
    private GameMap currentMap;
    private World gameWorld;

    public MapManager(SkyWars plugin) {
        this.plugin = plugin;
        this.maps = new HashMap<>();
        loadMaps();
    }

    public void loadMaps() {
        File mapsFolder = new File(plugin.getDataFolder(), "maps");
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
            return;
        }

        File[] mapFiles = mapsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (mapFiles == null) return;

        for (File mapFile : mapFiles) {
            GameMap map = loadMap(mapFile);
            if (map != null) {
                maps.put(map.getName(), map);
            }
        }
    }

    public GameMap loadMap(File mapFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(mapFile);
        String name = config.getString("name");

        if (name == null) return null;

        GameMap map = new GameMap(name);

        List<Location> spawns = new ArrayList<>();
        ConfigurationSection spawnsSection = config.getConfigurationSection("spawns");
        if (spawnsSection != null) {
            for (String key : spawnsSection.getKeys(false)) {
                spawnsSection.getLocation(key);
            }
        }
        map.setSpawns(spawns);

        Location spectator = config.getLocation("spectator");
        if (spectator != null) {
            map.setSpectator(spectator);
        }

        Location center = config.getLocation("center");
        if (center != null) {
            map.setCenter(center);
        }

        int borderSize = config.getInt("border-size", 100);
        map.setBorderSize(borderSize);

        List<Location> chests = new ArrayList<>();
        ConfigurationSection chestsSection = config.getConfigurationSection("chests");
        if (chestsSection != null) {
            for (String key : chestsSection.getKeys(false)) {
                chests.add(chestsSection.getLocation(key));
            }
        }
        map.setChests(chests);

        return map;
    }

    public boolean createMap(String name) {
        if (maps.containsKey(name)) {
            return false;
        }

        File mapsFolder = new File(plugin.getDataFolder(), "maps");
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }

        File mapFile = new File(mapsFolder, name + ".yml");
        if (mapFile.exists()) {
            return false;
        }

        try {
            mapFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(mapFile);
            config.set("name", name);
            config.set("spawns", new ArrayList<>());
            config.set("spectator", null);
            config.set("center", null);
            config.set("border-size", 100);
            config.set("chests", new ArrayList<>());
            config.save(mapFile);

            GameMap map = new GameMap(name);
            maps.put(name, map);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMap(String name) {
        if (!maps.containsKey(name)) {
            return false;
        }

        File mapsFolder = new File(plugin.getDataFolder(), "maps");
        File mapFile = new File(mapsFolder, name + ".yml");

        if (mapFile.exists()) {
            mapFile.delete();
        }

        maps.remove(name);
        return true;
    }

    public boolean setSpawn(String mapName, Location location) {
        GameMap map = maps.get(mapName);
        if (map == null) return false;

        map.addSpawn(location);
        saveMap(map);
        return true;
    }

    public boolean setSpectator(String mapName, Location location) {
        GameMap map = maps.get(mapName);
        if (map == null) return false;

        map.setSpectator(location);
        saveMap(map);
        return true;
    }

    public boolean setCenter(String mapName, Location location) {
        GameMap map = maps.get(mapName);
        if (map == null) return false;

        map.setCenter(location);
        saveMap(map);
        return true;
    }

    public boolean setBorder(String mapName, int size) {
        GameMap map = maps.get(mapName);
        if (map == null) return false;

        map.setBorderSize(size);
        saveMap(map);
        return true;
    }

    public boolean addChest(String mapName, Location location) {
        GameMap map = maps.get(mapName);
        if (map == null) return false;

        map.addChest(location);
        saveMap(map);
        return true;
    }

    public void saveMap(GameMap map) {
        File mapsFolder = new File(plugin.getDataFolder(), "maps");
        File mapFile = new File(mapsFolder, map.getName() + ".yml");

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(mapFile);
            config.set("name", map.getName());

            List<String> spawnList = new ArrayList<>();
            for (int i = 0; i < map.getSpawns().size(); i++) {
                Location spawn = map.getSpawns().get(i);
                config.set("spawns." + i, spawn);
            }

            config.set("spectator", map.getSpectator());
            config.set("center", map.getCenter());
            config.set("border-size", map.getBorderSize());

            for (int i = 0; i < map.getChests().size(); i++) {
                Location chest = map.getChests().get(i);
                config.set("chests." + i, chest);
            }

            config.save(mapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String mapName) {
        GameMap map = maps.get(mapName);
        if (map == null) return;

        currentMap = map;

        World world = Bukkit.getWorld(mapName);
        if (world == null) {
            world = new WorldCreator(mapName).createWorld();
        }

        gameWorld = world;
    }

    public Location getRandomSpawn() {
        if (currentMap == null || currentMap.getSpawns().isEmpty()) {
            return null;
        }

        Random random = new Random();
        return currentMap.getSpawns().get(random.nextInt(currentMap.getSpawns().size()));
    }

    public Location getSpectatorSpawn() {
        if (currentMap == null) {
            return null;
        }
        return currentMap.getSpectator();
    }

    public Location getCenter() {
        if (currentMap == null) {
            return null;
        }
        return currentMap.getCenter();
    }

    public int getBorderSize() {
        if (currentMap == null) {
            return 100;
        }
        return currentMap.getBorderSize();
    }

    public List<Location> getChests() {
        if (currentMap == null) {
            return new ArrayList<>();
        }
        return currentMap.getChests();
    }

    public void resetMap() {
        if (gameWorld != null) {
            Bukkit.unloadWorld(gameWorld, false);
        }
        gameWorld = null;
        currentMap = null;
    }

    public World getGameWorld() {
        return gameWorld;
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public Map<String, GameMap> getMaps() {
        return maps;
    }

    public static class GameMap {
        private final String name;
        private List<Location> spawns;
        private Location spectator;
        private Location center;
        private int borderSize;
        private List<Location> chests;

        public GameMap(String name) {
            this.name = name;
            this.spawns = new ArrayList<>();
            this.chests = new ArrayList<>();
            this.borderSize = 100;
        }

        public String getName() {
            return name;
        }

        public List<Location> getSpawns() {
            return spawns;
        }

        public void setSpawns(List<Location> spawns) {
            this.spawns = spawns;
        }

        public void addSpawn(Location spawn) {
            spawns.add(spawn);
        }

        public Location getSpectator() {
            return spectator;
        }

        public void setSpectator(Location spectator) {
            this.spectator = spectator;
        }

        public Location getCenter() {
            return center;
        }

        public void setCenter(Location center) {
            this.center = center;
        }

        public int getBorderSize() {
            return borderSize;
        }

        public void setBorderSize(int borderSize) {
            this.borderSize = borderSize;
        }

        public List<Location> getChests() {
            return chests;
        }

        public void setChests(List<Location> chests) {
            this.chests = chests;
        }

        public void addChest(Location chest) {
            chests.add(chest);
        }
    }
}