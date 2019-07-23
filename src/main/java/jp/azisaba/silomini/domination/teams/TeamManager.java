package jp.azisaba.silomini.domination.teams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import jp.azisaba.silomini.domination.game.Game;

public class TeamManager {

    private final Game game;

    private final List<Player> redPlayers = new ArrayList<>();
    private final List<Player> bluePlayers = new ArrayList<>();

    private final Scoreboard board;

    private final String redTeamName = "red";
    private final String blueTeamName = "blue";

    public TeamManager(Game game) {
        this.game = game;

        board = game.getScoreboard();
    }

    public void initialize() {

        board.registerNewTeam(redTeamName);
        board.registerNewTeam(blueTeamName);

        Team red = board.getTeam(redTeamName);
        Team blue = board.getTeam(blueTeamName);

        red.setPrefix(ChatColor.RED + "");
        red.setAllowFriendlyFire(false);
        red.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);

        blue.setPrefix(ChatColor.BLUE + "");
        blue.setAllowFriendlyFire(false);
        blue.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
    }

    public List<Player> getRedPlayers() {
        return redPlayers;
    }

    public List<Player> getBluePlayers() {
        return bluePlayers;
    }

    public List<Player> getPlayers(DominationTeam team) {
        if ( team == DominationTeam.BLUE ) {
            return getBluePlayers();
        } else if ( team == DominationTeam.RED ) {
            return getRedPlayers();
        }

        return new ArrayList<>();
    }

    public DominationTeam getTeam(Player p) {
        if ( bluePlayers.contains(p) ) {
            return DominationTeam.BLUE;
        }
        if ( redPlayers.contains(p) ) {
            return DominationTeam.RED;
        }

        return DominationTeam.NONE;
    }

    public ChatColor getColor(DominationTeam team) {
        if ( team == DominationTeam.BLUE ) {
            return ChatColor.BLUE;
        } else if ( team == DominationTeam.RED ) {
            return ChatColor.RED;
        }
        return ChatColor.WHITE;
    }

    public void distribute() {
        onlyDistribute();

        for ( Player redP : redPlayers ) {
            board.getTeam(redTeamName).addEntry(redP.getName());
        }
        for ( Player blueP : bluePlayers ) {
            board.getTeam(blueTeamName).addEntry(blueP.getName());
        }
    }

    public void removeTeam() {
        try {
            board.getTeam(redTeamName).unregister();
        } catch ( Exception e ) {
            // nothing
        }
        try {
            board.getTeam(blueTeamName).unregister();
        } catch ( Exception e ) {
            // nothing
        }
    }

    public void quit(Player p) {
        if ( board.getTeam(redTeamName).hasEntry(p.getName()) ) {
            board.getTeam(redTeamName).removeEntry(p.getName());
        } else if ( board.getTeam(blueTeamName).hasEntry(p.getName()) ) {
            board.getTeam(blueTeamName).removeEntry(p.getName());
        }

        if ( redPlayers.contains(p) ) {
            redPlayers.remove(p);
        } else if ( bluePlayers.contains(p) ) {
            bluePlayers.remove(p);
        }
    }

    public void onlyDistribute() {

        List<Player> plist = new ArrayList<>(game.getPlayers());
        Collections.shuffle(plist);

        for ( Player p : plist ) {

            if ( redPlayers.size() < bluePlayers.size() ) {
                redPlayers.add(p);
            } else {
                bluePlayers.add(p);
            }
        }
    }
}
