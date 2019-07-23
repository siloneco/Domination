package net.azisaba.silo.domination.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.azisaba.silo.domination.Domination;
import net.azisaba.silo.domination.DominationUtils;
import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.game.GameHelper;

public class ChatListener implements Listener {

    private final Domination plugin;

    public ChatListener(Domination plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChatAsync(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        Player p = e.getPlayer();

        Game game = GameHelper.getGame(p);

        String msg = "";

        if ( game != null && game.getTeamManager() != null ) {
            msg = game.getTeamManager().getColor(game.getTeamManager().getTeam(p)) + p.getName() + ChatColor.GREEN
                    + ": " + ChatColor.RESET + e.getMessage();
        } else {
            msg = DominationUtils.getPrefix(p) + p.getName() + ChatColor.GREEN + ": " + ChatColor.RESET
                    + e.getMessage();
        }

        if ( plugin.isOpeningChat() ) {
            for ( Player target : Bukkit.getOnlinePlayers() ) {
                target.sendMessage(msg);
            }
        } else {
            for ( Player target : p.getWorld().getPlayers() ) {
                target.sendMessage(msg);
            }
        }

        if ( game == null ) {
            Bukkit.getLogger().info("[" + p.getWorld().getName() + "(World)] " + getUnFormatString(msg));
        } else {
            Bukkit.getLogger().info("[" + game.getId() + "] " + getUnFormatString(msg));
        }
    }

    private String getUnFormatString(String msg) {
        for ( ChatColor color : ChatColor.values() ) {
            msg = msg.replace(color + "", "");
        }
        return msg;
    }
}
