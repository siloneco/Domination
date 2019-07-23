package jp.azisaba.silomini.domination.tabcompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import jp.azisaba.silomini.domination.Domination;
import jp.azisaba.silomini.domination.game.Game;
import jp.azisaba.silomini.domination.game.GameHelper;
import jp.azisaba.silomini.domination.game.GameHelper.GameState;
import jp.azisaba.silomini.domination.map.MapData;

public class DominationTabCompleter implements TabCompleter {

    private final Domination plugin;

    private final List<String> argsList = Arrays.asList("generate", "join", "list", "close", "maplist", "reloadmap");

    public DominationTabCompleter(Domination plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if ( !cmd.getName().equalsIgnoreCase("domination") ) {
            return null;
        }

        if ( args.length <= 1 ) {

            if ( args[0].equals("") ) {
                return argsList;
            }

            List<String> strList = new ArrayList<>();

            for ( String str : argsList ) {
                if ( str.toLowerCase().startsWith(args[0].toLowerCase()) ) {
                    strList.add(str);
                }
            }

            if ( strList.size() <= 0 ) {
                return argsList;
            }

            return strList;
        }

        if ( args[0].equalsIgnoreCase("generate") ) {
            List<String> strList = new ArrayList<>();

            for ( MapData data : plugin.mapSelector.getAllMapData() ) {

                if ( data.getMapName().toLowerCase().startsWith(args[1].toLowerCase()) ) {
                    strList.add(data.getMapName());
                }
            }

            if ( strList.size() == 0 ) {

                for ( MapData data : plugin.mapSelector.getAllMapData() ) {
                    strList.add(data.getMapName());
                }

                return strList;
            }

            return strList;
        }

        if ( args[0].equalsIgnoreCase("join") ) {

            if ( args.length < 3 ) {
                return getGameIDList(true);
            }

            return null;
        }

        if ( args[0].equalsIgnoreCase("close") ) {
            List<String> list = new ArrayList<>(getGameIDList(false));

            if ( sender instanceof Player ) {
                Game game = GameHelper.getGame((Player) sender);

                if ( game != null ) {
                    list.add(0, "-this");
                    return list;
                }
            }

            return list;
        }
        return null;
    }

    private List<String> getGameIDList(boolean onlyCanJoin) {
        List<String> strList = new ArrayList<>();

        for ( Game game : GameHelper.getAllGames() ) {

            if ( onlyCanJoin ) {
                if ( game.getPlayers().size() >= plugin.config.maxPlayers ) {
                    continue;
                }
                if ( game.getState() == GameState.GAMING || game.getState() == GameState.FINISH ) {
                    continue;
                }
            }

            strList.add(game.getId());
        }

        return strList;
    }
}
