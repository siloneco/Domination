package jp.azisaba.silomini.domination.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import jp.azisaba.silomini.domination.game.Game;
import jp.azisaba.silomini.domination.game.GameHelper;

public class GameLogger {

    private Game game;

    private LogWriter writer;

    public GameLogger(Game game) {

        if ( !isEnableLogging() ) {
            return;
        }

        this.game = game;

        Calendar cl = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        File folder = new File(GameHelper.getPlugin().config.logFolderPath, sdf.format(cl.getTime()));

        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File file = new File(folder, game.getId() + " - " + sdf.format(cl.getTime()) + ".log");

        int num = 1;

        while ( file.exists() ) {
            file = new File(folder, game.getId() + "(" + num + ")" + " - " + sdf.format(cl.getTime()) + ".log");
            num++;
        }

        writer = new LogWriter(file);
        writer.writeLine("Log writer ready.");
    }

    public void writeLine(String log) {
        writer.writeLine(log);
    }

    public void writeLines(List<String> logList) {
        for ( String str : logList ) {
            writer.writeLine(str);
        }
    }

    public void writeError(StackTraceElement[] trace) {
        for ( StackTraceElement element : trace ) {
            writer.writeLine(element.toString());
        }
    }

    public void writeGameData() {

        if ( !isEnableLogging() ) {
            return;
        }

        writer.writeLine(StringUtils.repeat("-", 32));
        writer.writeLine("GameID: " + game.getId());
        writer.writeLine("Map: " + game.getData().getMapName());
        writer.writeLine(StringUtils.repeat("-", 32));
    }

    public void startGame() {

        if ( !isEnableLogging() ) {
            return;
        }

        writer.writeLine("Game started.");

        StringBuilder red = new StringBuilder();
        for ( Player p : game.getTeamManager().getRedPlayers() ) {
            red.append(p.getName() + ", ");
        }
        StringBuilder blue = new StringBuilder();
        for ( Player p : game.getTeamManager().getRedPlayers() ) {
            blue.append(p.getName() + ", ");
        }

        writer.writeLine(" - RedTeam: " + red.toString().substring(0, red.toString().length() - 2));
        writer.writeLine(" - BlueTeam: " + blue.toString().substring(0, blue.toString().length() - 2));
    }

    public void chat(Player p, String chat) {

        if ( !isEnableLogging() ) {
            return;
        }

        writer.writeLine("[CHAT] " + p.getName() + ": " + chat);
    }

    public void killed(Player killer, Player death) {

        if ( !isEnableLogging() ) {
            return;
        }

        writer.writeLine("" + killer.getName() + " killed " + death.getName());
    }

    public void joinedPlayer(Player p) {

        if ( !isEnableLogging() ) {
            return;
        }

        writer.writeLine(p.getName() + " joined.");
    }

    public void leftPlayer(String playerName) {

        if ( !isEnableLogging() ) {
            return;
        }

        writer.writeLine(playerName + " left.");
    }

    private boolean isEnableLogging() {
        return GameHelper.getPlugin().config.enableLogging;
    }
}
