package net.azisaba.silo.domination.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import net.azisaba.silo.domination.game.GameHelper;

public class PlayerHideListener implements Listener {

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        World world = p.getWorld();

        if ( GameHelper.getGame(world) == null ) {
            showAllPlayers(p);
        } else {
            hideOtherWorld(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if ( e.getPlayer().hasPermission("domination.hide.exempt.onjoin") ) {
            showAllPlayers(e.getPlayer());
        } else {
            hideOtherWorld(e.getPlayer());
        }
    }

    private void hideOtherWorld(Player p) {
        for ( Player target : Bukkit.getOnlinePlayers() ) {

            if ( target.getWorld() == p.getWorld() ) {
                p.showPlayer(target);
                target.showPlayer(p);
            } else {
                p.hidePlayer(target);
            }
        }
    }

    private void showAllPlayers(Player p) {
        for ( Player target : Bukkit.getOnlinePlayers() ) {
            p.showPlayer(target);
        }
    }
}
