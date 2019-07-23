package net.azisaba.silo.domination.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.google.common.collect.ComparisonChain;

import net.azisaba.silo.domination.Domination;
import net.azisaba.silo.domination.DominationUtils;
import net.azisaba.silo.domination.map.MapData;
import net.azisaba.silo.domination.map.MapGenerator;
import net.azisaba.silo.domination.teams.DominationTeam;

public class GameHelper {

    private static Domination plugin;

    private static List<Game> gameList = new ArrayList<>();

    public static void init(Domination plugin) {
        GameHelper.plugin = plugin;
    }

    public static Domination getPlugin() {
        return plugin;
    }

    public static Game getGame(World world) {

        if ( gameList.size() == 0 ) {
            return null;
        }

        for ( Game game : gameList ) {
            if ( game.getWorld().getName().equals(world.getName()) ) {
                return game;
            }
        }

        return null;
    }

    public static Game getGame(String id) {

        if ( gameList.size() == 0 ) {
            return null;
        }

        for ( Game game : gameList ) {
            if ( game.getId().equals(id) ) {
                return game;
            }
        }

        return null;
    }

    public static Game getGame(Player p) {

        if ( gameList.size() == 0 ) {
            return null;
        }

        for ( Game game : gameList ) {
            if ( game.getPlayers().contains(p) ) {
                return game;
            }
        }

        return null;
    }

    public static List<Game> getAllGames() {
        return gameList;
    }

    public static List<Game> getAllGames(MapData data) {

        List<Game> specificGameList = new ArrayList<>();

        for ( Game game : gameList ) {
            if ( game.getData().getMapName().equals(data.getMapName()) ) {
                specificGameList.add(game);
            }
        }

        return specificGameList;
    }

    public static Game getAvailableGame() {

        if ( gameList.size() == 0 ) {
            return null;
        }

        List<Game> availableGames = new ArrayList<>();

        for ( Game game : gameList ) {
            if ( Arrays.asList(GameState.WAITING, GameState.COUNTDOWN).contains(game.getState())
                    && game.getPlayers().size() < plugin.config.maxPlayers ) {
                availableGames.add(game);
            }
        }

        if ( availableGames.size() <= 0 ) {
            return null;
        }

        Collections.sort(availableGames,
                (game1, game2) -> ComparisonChain.start().compare(game1.getCreatedAt(), game2.getCreatedAt()).result());

        for ( Game game : availableGames ) {
            if ( game.getMapGenerator().isFinished() ) {
                return game;
            }
        }

        return availableGames.get(0);
    }

    public static void registerGame(Game game) {

        if ( !gameList.contains(game) ) {
            gameList.add(game);
        }
    }

    public static boolean deleteGame(String gameID) {

        Game deleteGame = null;
        for ( Game game : gameList ) {
            if ( game.getId().equals(gameID) ) {
                deleteGame = game;
                break;
            }
        }

        if ( deleteGame == null ) {
            return false;
        }

        gameList.remove(deleteGame);
        return true;
    }

    public static Game generateNewGame() {

        MapData mapData = plugin.mapSelector.chooseRandomMap();

        if ( mapData == null ) {
            plugin.getLogger().warning("使用できるマップがないようです。importしていますか？");
            return null;
        }

        return generateNewGame(mapData);
    }

    public static Game generateNewGame(String mapName) {
        MapData mapData = plugin.mapSelector.getMap(mapName);
        if ( mapData == null ) {
            return null;
        }

        return generateNewGame(mapData);
    }

    public static Game generateNewGame(MapData mapData) {

        if ( !plugin.isEnabled() ) {
            return null;
        }

        String id = RandomStringUtils.random(1, "123456789")
                + RandomStringUtils.randomNumeric(plugin.config.gameIDLength - 1);

        while ( Bukkit.getWorld(id) != null ) {
            id = RandomStringUtils.random(1, "123456789")
                    + RandomStringUtils.randomNumeric(plugin.config.gameIDLength - 1);
        }

        World world = generateWorld("DM_" + id);
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("mobGriefing", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("randomTickSpeed", "0");

        world.getWorldBorder().setCenter(mapData.getWorldBorderCenter());
        world.getWorldBorder().setSize(mapData.getWorldBorderSize());

        MapGenerator mapG = new MapGenerator(plugin, world, mapData);
        mapG.setBiome(Biome.PLAINS);
        mapG.runTimer();

        Game game = new Game(id, world, mapData, mapG);

        GameHelper.registerGame(game);

        return game;
    }

    public static void joinAvailableGame(Player p) {
        Game game = GameHelper.getAvailableGame();

        if ( game == null ) {
            game = GameHelper.generateNewGame();

            if ( game != null ) {
                GameHelper.registerGame(game);
            }
        }

        if ( game == null ) {
            plugin.getLogger().warning("使用可能なマップがありませんでした。数を増やすなどして対応してください。");

            DominationUtils.sendToLobby(p, ChatColor.RED + "エラーが発生しました。しばらく待ってから再度参加してください。");
            return;
        }

        boolean success = game.joinPlayer(p);

        if ( !success ) {
            plugin.getLogger().warning(game.getId() + " への " + p.getName() + " の参加がキャンセルされました。");
        }
    }

    public static void setUpInventory(Player p, DominationTeam team) {
        if ( team == DominationTeam.NONE ) {
            return;
        }

        p.getInventory().clear();

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
        ItemStack chestPlate = new ItemStack(Material.IRON_CHESTPLATE, 1);
        ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.IRON_BOOTS, 1);

        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();

        if ( team == DominationTeam.RED ) {
            meta.setColor(Color.fromRGB(255, 85, 85));
        } else {
            meta.setColor(Color.fromRGB(85, 85, 255));
        }

        helmet.setItemMeta(meta);

        ItemStack[] armorContents = { boots, leggings, chestPlate, helmet };

        p.getInventory().setArmorContents(armorContents);

        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.spigot().setUnbreakable(true);
        item.setItemMeta(meta);

        p.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
        p.getInventory().setItem(1, item);
        p.getInventory().setItem(8, new ItemStack(Material.ARROW, 32));
        p.getInventory().setItem(2, new ItemStack(Material.FISHING_ROD, 1));
    }

    private static World generateWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);

        creator.generator(getChunkGenerator());
        World world = creator.createWorld();
        return world;
    }

    public static enum GameState {
        WAITING,
        COUNTDOWN,
        GAMING,
        FINISH
    }

    private static ChunkGenerator getChunkGenerator() {
        return new ChunkGenerator() {
            @Override
            public byte[] generate(World world, Random random, int x, int z) {
                return new byte[32768];
            }
        };
    }
}
