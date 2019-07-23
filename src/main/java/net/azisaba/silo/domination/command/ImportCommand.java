package net.azisaba.silo.domination.command;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.silo.domination.map.MapImportHelper;

public class ImportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if ( !(sender instanceof Player) ) {
            sender.sendMessage("プレイヤーから実行してください。");
            return true;
        }

        Player p = (Player) sender;

        if ( MapImportHelper.isImportMode(p) ) {
            MapImportHelper.disableImportMode(p);
            p.sendMessage(ChatColor.RED + "Importモードを終了しました。");
            return true;
        }

        if ( args.length == 0 ) {
            p.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage().replace("{LABEL}", label));
            return true;
        }

        boolean load = false;
        for ( String str : Arrays.asList(args) ) {
            if ( str.equalsIgnoreCase("-load") ) {
                load = true;
            }
        }

        MapImportHelper.enableImportMode(p, args[0], load);
        return true;
    }
}
