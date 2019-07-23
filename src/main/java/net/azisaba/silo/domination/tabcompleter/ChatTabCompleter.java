package net.azisaba.silo.domination.tabcompleter;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class ChatTabCompleter implements TabCompleter {

    private final List<String> argsList = Arrays.asList("public", "private", "toggle");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if ( args.length > 1 ) {
            return Arrays.asList("");
        }

        return argsList;
    }
}
