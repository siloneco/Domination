package net.azisaba.silo.domination.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;

import net.azisaba.silo.domination.Domination;
import net.azisaba.silo.domination.DominationUtils;
import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.game.GameHelper;

public class PlayerJoinLeftListener implements Listener {

    private final Domination plugin;

    public PlayerJoinLeftListener(Domination plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        e.setJoinMessage(null);

        if ( p.hasPermission("domination.game.exempt") ) {
            p.teleport(Bukkit.getWorld("world").getSpawnLocation());
            p.sendMessage(ChatColor.RED + "権限を持っているためゲームに参加しません。" + ChatColor.GRAY + "(/dm join でいつでも参加出来ます)");
            return;
        }

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

        game.joinPlayer(p);
    }

    @EventHandler
    public void onPlayerLeftWorldEvent(PlayerChangedWorldEvent e) {
        final Player p = e.getPlayer();
        World from = e.getFrom();

        Game game = GameHelper.getGame(from);

        if ( p.getScoreboard() != null ) {
            p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        }

        if ( game == null ) {
            return;
        }
        game.quitPlayer(p);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {

        e.setQuitMessage(null);

        Player p = e.getPlayer();
        Game game = GameHelper.getGame(p.getWorld());

        if ( game == null ) {
            return;
        }

        game.quitPlayer(p);
    }
}
