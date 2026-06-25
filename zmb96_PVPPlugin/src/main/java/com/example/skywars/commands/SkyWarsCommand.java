package com.example.skywars.commands;

import com.example.skywars.SkyWars;
import com.example.skywars.gui.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SkyWarsCommand implements CommandExecutor, TabCompleter {

    private final SkyWars plugin;
    private final MenuManager menuManager;

    public SkyWarsCommand(SkyWars plugin) {
        this.plugin = plugin;
        this.menuManager = new MenuManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            menuManager.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join":
                handleJoin(player);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "kit":
                handleKit(player);
                break;
            case "create":
                handleCreate(player, args);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "setspawn":
                handleSetSpawn(player, args);
                break;
            case "setspectator":
                handleSetSpectator(player, args);
                break;
            case "setcenter":
                handleSetCenter(player, args);
                break;
            case "setborder":
                handleSetBorder(player, args);
                break;
            case "addchest":
                handleAddChest(player, args);
                break;
            case "reload":
                handleReload(player);
                break;
            case "start":
                handleStart(player);
                break;
            case "stop":
                handleStop(player);
                break;
            default:
                player.sendMessage("§c未知命令！使用 /sw 查看帮助");
                break;
        }

        return true;
    }

    private void handleJoin(Player player) {
        plugin.getGameManager().joinGame(player);
    }

    private void handleLeave(Player player) {
        plugin.getGameManager().leaveGame(player);
    }

    private void handleKit(Player player) {
        menuManager.openKitMenu(player);
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§c用法: /sw create <地图名>");
            return;
        }

        String mapName = args[1];
        if (plugin.getGameManager().getMapManager().createMap(mapName)) {
            player.sendMessage("§a地图 §e" + mapName + " §a创建成功！");
        } else {
            player.sendMessage("§c地图创建失败！地图可能已存在。");
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§c用法: /sw delete <地图名>");
            return;
        }

        String mapName = args[1];
        if (plugin.getGameManager().getMapManager().deleteMap(mapName)) {
            player.sendMessage("§a地图 §e" + mapName + " §a删除成功！");
        } else {
            player.sendMessage("§c地图删除失败！地图可能不存在。");
        }
    }

    private void handleSetSpawn(Player player, String[] args) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§c用法: /sw setspawn <地图名>");
            return;
        }

        String mapName = args[1];
        if (plugin.getGameManager().getMapManager().setSpawn(mapName, player.getLocation())) {
            player.sendMessage("§a出生点设置成功！");
        } else {
            player.sendMessage("§c出生点设置失败！地图可能不存在。");
        }
    }

    private void handleSetSpectator(Player player, String[] args) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§c用法: /sw setspectator <地图名>");
            return;
        }

        String mapName = args[1];
        if (plugin.getGameManager().getMapManager().setSpectator(mapName, player.getLocation())) {
            player.sendMessage("§a旁观点设置成功！");
        } else {
            player.sendMessage("§c旁观点设置失败！地图可能不存在。");
        }
    }

    private void handleSetCenter(Player player, String[] args) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§c用法: /sw setcenter <地图名>");
            return;
        }

        String mapName = args[1];
        if (plugin.getGameManager().getMapManager().setCenter(mapName, player.getLocation())) {
            player.sendMessage("§a地图中心设置成功！");
        } else {
            player.sendMessage("§c地图中心设置失败！地图可能不存在。");
        }
    }

    private void handleSetBorder(Player player, String[] args) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§c用法: /sw setborder <地图名> <大小>");
            return;
        }

        String mapName = args[1];
        try {
            int size = Integer.parseInt(args[2]);
            if (plugin.getGameManager().getMapManager().setBorder(mapName, size)) {
                player.sendMessage("§a边界大小设置为 §e" + size + " §a格！");
            } else {
                player.sendMessage("§c边界设置失败！地图可能不存在。");
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§c无效的数字！");
        }
    }

    private void handleAddChest(Player player, String[] args) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§c用法: /sw addchest <地图名>");
            return;
        }

        String mapName = args[1];
        if (plugin.getGameManager().getMapManager().addChest(mapName, player.getLocation())) {
            player.sendMessage("§a箱子位置添加成功！");
        } else {
            player.sendMessage("§c箱子位置添加失败！地图可能不存在。");
        }
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        plugin.reloadConfig();
        player.sendMessage("§a配置文件已重载！");
    }

    private void handleStart(Player player) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        plugin.getGameManager().forceStart();
    }

    private void handleStop(Player player) {
        if (!player.hasPermission("skywars.admin")) {
            player.sendMessage("§c你没有权限使用此命令！");
            return;
        }

        plugin.getGameManager().forceStop();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = Arrays.asList("join", "leave", "kit", "create", "delete", "setspawn", "setspectator", "setcenter", "setborder", "addchest", "reload", "start", "stop");
            completions.addAll(commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (Arrays.asList("create", "delete", "setspawn", "setspectator", "setcenter", "setborder", "addchest").contains(subCommand)) {
                completions.addAll(plugin.getGameManager().getMapManager().getMaps().keySet().stream()
                        .filter(map -> map.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            }
        }

        return completions;
    }
}