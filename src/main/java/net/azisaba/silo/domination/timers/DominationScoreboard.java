package net.azisaba.silo.domination.timers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.game.GameHelper;
import net.azisaba.silo.domination.game.GameHelper.GameState;

public class DominationScoreboard {

    private final Game game;

    private final Scoreboard board;

    public DominationScoreboard(Game game) {
        this.game = game;
        board = game.getScoreboard();
    }

    public void updateScoreboard() {
        if ( game.getWorld().getPlayers().size() == 0 ) {
            return;
        }

        clear();

        for ( Player p : game.getWorld().getPlayers() ) {

            Objective obj = board.getObjective("Domination");

            if ( obj == null ) {
                obj = board.registerNewObjective("Domination", "dummy");
            }

            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName(
                    ChatColor.YELLOW + "────" + ChatColor.AQUA + "Domination" + ChatColor.YELLOW + "────");

            if ( game.getState() == GameState.WAITING ) {
            } else if ( game.getState() == GameState.COUNTDOWN ) {
                setCountdown(obj);
            } else if ( game.getState() == GameState.GAMING ) {
                setGaming(obj);
            } else if ( game.getState() == GameState.FINISH ) {
                setFinish(obj);
            }

            p.setScoreboard(board);
        }
    }

    private void setCountdown(Objective obj) {
        obj.getScore("    ").setScore(6);
        obj.getScore(
                ChatColor.YELLOW + "Players" + ChatColor.GREEN + ": " + ChatColor.LIGHT_PURPLE
                        + game.getPlayers().size() + "/" + GameHelper.getPlugin().config.maxPlayers)
                .setScore(5);
        obj.getScore(
                ChatColor.AQUA + "スタートまで" + ChatColor.GREEN + ": " + ChatColor.AQUA + (game.getCountdownSeconds() + 1)
                        + "秒")
                .setScore(4);
        obj.getScore("  ").setScore(3);
        obj.getScore(ChatColor.GRAY + "Game ID: " + game.getId()).setScore(2);
        obj.getScore(ChatColor.GRAY + "Map: " + game.getData().getMapName()).setScore(1);
        obj.getScore(" ").setScore(0);
    }

    private void setGaming(Objective obj) {
        obj.getScore("     ").setScore(8);
        obj.getScore(ChatColor.YELLOW + "残り時間" + ChatColor.GREEN + ": " + ChatColor.AQUA + getGameSeconds() + "秒")
                .setScore(7);
        obj.getScore("    ").setScore(6);
        obj.getScore(ChatColor.RED + "赤" + ChatColor.GREEN + ": " + ChatColor.LIGHT_PURPLE + game.getRedPoint()
                + ChatColor.GRAY
                + " point(s)").setScore(5);
        obj.getScore(ChatColor.BLUE + "青" + ChatColor.GREEN + ": " + ChatColor.LIGHT_PURPLE + game.getBluePoint()
                + ChatColor.GRAY + " point(s)").setScore(4);
        obj.getScore("  ").setScore(3);
        obj.getScore(ChatColor.GRAY + "Game ID: " + game.getId()).setScore(2);
        obj.getScore(ChatColor.GRAY + "Map: " + game.getData().getMapName()).setScore(1);
        obj.getScore(" ").setScore(0);
    }

    private void setFinish(Objective obj) {
        obj.getScore("    ").setScore(6);
        obj.getScore(ChatColor.RED + "赤" + ChatColor.GREEN + ": " + ChatColor.LIGHT_PURPLE + game.getRedPoint()
                + ChatColor.GRAY + " point(s) "
                + ((game.getRedPoint() >= GameHelper.getPlugin().config.gameEndPoint) + "")
                        .replace("true", ChatColor.YELLOW + "Winner").replace("false", ""))
                .setScore(5);
        obj.getScore(ChatColor.BLUE + "青" + ChatColor.GREEN + ": " + ChatColor.LIGHT_PURPLE + game.getBluePoint()
                + ChatColor.GRAY + " point(s) "
                + ((game.getBluePoint() >= GameHelper.getPlugin().config.gameEndPoint) + "")
                        .replace("true", ChatColor.YELLOW + "Winner").replace("false", ""))
                .setScore(4);
        obj.getScore("  ").setScore(3);
        obj.getScore(ChatColor.GRAY + "Game ID: " + game.getId()).setScore(2);
        obj.getScore(ChatColor.GRAY + "Map: " + game.getData().getMapName()).setScore(1);
        obj.getScore(" ").setScore(0);
    }

    private int getGameSeconds() {
        return (int) (GameHelper.getPlugin().config.gameLongSecond
                - (System.currentTimeMillis() - game.getStartedMiliSeconds()) / 1000);
    }

    private void clear() {
        for ( String str : board.getEntries() ) {
            board.resetScores(str);
        }
    }
}
