package net.azisaba.silo.domination;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.silo.domination.command.ChatCommand;
import net.azisaba.silo.domination.command.DominationCommand;
import net.azisaba.silo.domination.command.ImportCommand;
import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.game.GameHelper;
import net.azisaba.silo.domination.listeners.ChatListener;
import net.azisaba.silo.domination.listeners.DamageListener;
import net.azisaba.silo.domination.listeners.DominationEventListeners;
import net.azisaba.silo.domination.listeners.GamingListener;
import net.azisaba.silo.domination.listeners.ImportListener;
import net.azisaba.silo.domination.listeners.PlayerHideListener;
import net.azisaba.silo.domination.listeners.PlayerJoinLeftListener;
import net.azisaba.silo.domination.map.MapLoader;
import net.azisaba.silo.domination.map.MapSelector;
import net.azisaba.silo.domination.tabcompleter.ChatTabCompleter;
import net.azisaba.silo.domination.tabcompleter.DominationTabCompleter;

public class Domination extends JavaPlugin {

    private final String PLUGIN_NAME = "Domination";

    public DominationConfig config;
    public MapSelector mapSelector;
    public MapLoader mapLoader;

    public boolean disabling = false;

    private boolean openChat = false;

    @Override
    public void onEnable() {

        DominationUtils.init(this);

        config = new DominationConfig(this);
        config.loadConfig();

        mapLoader = new MapLoader(this);

        mapSelector = new MapSelector(this);
        mapSelector.loadMaps();

        GameHelper.init(this);

        Bukkit.getPluginManager().registerEvents(new PlayerJoinLeftListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GamingListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerHideListener(), this);
        Bukkit.getPluginManager().registerEvents(new ImportListener(), this);
        Bukkit.getPluginManager().registerEvents(new DominationEventListeners(), this);

        Bukkit.getPluginCommand("domination").setExecutor(new DominationCommand());
        Bukkit.getPluginCommand("domination").setTabCompleter(new DominationTabCompleter(this));
        Bukkit.getPluginCommand("chat").setExecutor(new ChatCommand(this));
        Bukkit.getPluginCommand("chat").setTabCompleter(new ChatTabCompleter());

        Bukkit.getPluginCommand("import").setExecutor(new ImportCommand());

        Bukkit.getLogger().info(PLUGIN_NAME + " enabled.");

        List<World> worlds = Bukkit.getWorlds().stream().filter(world -> world.getName().startsWith("DM_"))
                .collect(Collectors.toList());

        if ( worlds.size() > 0 ) {
            getLogger().info(worlds.size() + "個の無駄なワールドが見つかりました。削除を実行します...");

            for ( World world : worlds ) {
                boolean success = deleteWorld(world);

                if ( success ) {
                    getLogger().info(world.getName() + " を正常に削除しました。");
                } else {
                    getLogger().warning(world.getName() + " を正常に削除できませんでした。手動で削除してください。");
                }
            }
        }

        if ( config.standByGameCount > 0 ) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    getLogger().info("Generating " + config.standByGameCount + " game...");

                    for ( int count = config.standByGameCount; count != 0; count-- ) {
                        GameHelper.generateNewGame();
                    }
                }
            }.runTaskLater(this, 1);
        }
    }

    @Override
    public void onDisable() {

        List<Game> gameList = new ArrayList<>();
        gameList.addAll(GameHelper.getAllGames());

        for ( Game game : gameList ) {
            game.closeGame();
        }

        Bukkit.getLogger().info(PLUGIN_NAME + " disabled.");
    }

    public void openChat(boolean open) {
        openChat = open;
    }

    public boolean toggleOpenChat() {
        openChat = !openChat;
        return openChat;
    }

    public boolean isOpeningChat() {
        return openChat;
    }

    private boolean deleteWorld(World world) {
        boolean success = Bukkit.unloadWorld(world, false);
        if ( success ) {

            File file = world.getWorldFolder();

            try {
                FileUtils.deleteDirectory(file);
            } catch ( IOException e ) {
                e.printStackTrace();
                return false;
            }

            file = new File("./plugins/WorldGuard/worlds/" + world.getName());

            if ( file.exists() ) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch ( IOException e ) {
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        } else {
            getLogger().warning(world.getName() + "のunloadに失敗");
            return false;
        }
    }
}
