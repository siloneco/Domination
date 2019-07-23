package net.azisaba.silo.domination.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.azisaba.silo.domination.Domination;

public class ChatCommand implements CommandExecutor {

    private final Domination plugin;

    public ChatCommand(Domination plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if ( args.length == 0 ) {
            sender.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage().replace("{LABEL}", label));
            return true;
        }

        if ( args[0].equalsIgnoreCase("public") ) {

            if ( plugin.isOpeningChat() ) {
                sender.sendMessage(ChatColor.RED + "すでにチャットは開放されています。");
                return true;
            }

            plugin.openChat(true);
            sender.sendMessage(ChatColor.GREEN + "チャットを開放しました。");

        } else if ( args[0].equalsIgnoreCase("private") ) {

            if ( !plugin.isOpeningChat() ) {
                sender.sendMessage(ChatColor.RED + "すでにチャットはワールド別に設定されています。");
                return true;
            }

            plugin.openChat(false);
            sender.sendMessage(ChatColor.YELLOW + "チャットをワールド別にしました。");

        } else if ( args[0].equalsIgnoreCase("toggle") ) {

            boolean b = plugin.toggleOpenChat();

            if ( b ) {
                sender.sendMessage(ChatColor.GREEN + "チャットを開放しました。");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "チャットをワールド別にしました。");
            }
        }
        return true;
    }
}
