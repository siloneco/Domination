package jp.azisaba.silomini.domination.flag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ComparisonChain;

import jp.azisaba.silomini.domination.DominationUtils;
import jp.azisaba.silomini.domination.game.Game;
import jp.azisaba.silomini.domination.game.GameHelper;
import jp.azisaba.silomini.domination.game.GameHelper.GameState;
import jp.azisaba.silomini.domination.teams.DominationTeam;

public class FlagAndActionBarUpdater {

    private BukkitTask task = null;

    private final Game game;

    private final double distance;

    private String mark;

    public FlagAndActionBarUpdater(Game game) {
        this.game = game;
        distance = GameHelper.getPlugin().config.flagRadius * 2;
    }

    public void runTask() {

        task = new BukkitRunnable() {
            @Override
            public void run() {

                mark = DominationUtils.getMark();

                Map<Flag, DominationTeam> whoHas = game.getData().getFlags().stream()
                        .collect(Collectors.toMap(s -> s, s -> s.getCurrentTeam()));

                game.getPlayers().forEach(p -> {
                    addFlagPoint(p);
                });

                if ( game.getState() == GameState.GAMING ) {
                    game.getData().getFlags().stream().forEach(flag -> {
                        flag.updateArmorStandName();

                        if ( whoHas.get(flag) != flag.getCurrentTeam() ) {

                            if ( flag.getCurrentTeam() == DominationTeam.NONE ) {
                                return;
                            }

                            String flagStr = ChatColor.GREEN + "" + ChatColor.BOLD + "[" + flag.getFlagChar() + "]";

                            String msg = flag.getCurrentTeam().toString() + "チーム" + ChatColor.YELLOW + "が旗" + flagStr
                                    + ChatColor.YELLOW + "を占領しました。";
                            msg = msg.replace("RED", ChatColor.RED + "赤").replace("BLUE", ChatColor.BLUE + "青");

                            game.broadcast(msg);

                            for ( Player p : game.getTeamManager().getPlayers(flag.getCurrentTeam()) ) {
                                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
                            }
                        }
                    });
                }

                String allFlagState = getAllFlagState();

                game.getWorld().getPlayers().stream().filter(p -> getFlagsInArea(p).size() <= 0).forEach(p -> {
                    DominationUtils.sendActionBar(p, allFlagState);
                });
            }
        }.runTaskTimer(GameHelper.getPlugin(), 0, 1);
    }

    public void stopTask() {
        if ( task != null ) {
            task.cancel();
        }
    }

    private void addFlagPoint(Player p) {

        List<Flag> flags = getFlagsInArea(p);

        if ( flags.size() <= 0 ) {
            return;
        }

        Location checkLoc = p.getLocation().clone();

        flags.forEach(flag -> {
            checkLoc.setY(flag.getLocation().getY());

            double y = flag.getLocation().getY() - p.getLocation().getY();

            if ( y < 0 ) {
                y = y * -1;
                y += 2;
            }

            if ( y > 5 ) {
                return;
            }

            DominationTeam team = game.getTeamManager().getTeam(p);

            boolean b = flag.addPointTo(1, team);

            if ( b ) {
                DominationUtils.sendActionBar(p, ChatColor.GREEN + mark + ChatColor.BOLD + "  ["
                        + flag.getFlagChar() + "]" + ChatColor.GREEN + " 占領中... " + flag.getBar());
            } else {

                if ( game.getState() != GameState.GAMING ) {
                    return;
                }

                DominationUtils.sendActionBar(p, ChatColor.GREEN + "" + ChatColor.BOLD + "["
                        + flag.getFlagChar() + "] " + ChatColor.YELLOW + "占領済み " + flag.getBar());
            }
        });
    }

    private List<Flag> getFlagsInArea(Player p) {
        return game.getData().getFlags().stream()
                .filter(flag -> p.getLocation().clone().distance(flag.getLocation()) <= distance)
                .collect(Collectors.toList());
    }

    private String getAllFlagState() {
        String allFlagState = "";

        List<Flag> flagList = new ArrayList<>(game.getData().getFlags());
        Collections.sort(flagList, (flag1, flag2) -> ComparisonChain.start()
                .compare(flag1.getFlagChar(), flag2.getFlagChar()).result());

        StringBuilder builder = new StringBuilder();
        for ( Flag flag : flagList ) {
            builder.append(DominationUtils.getColor(flag.getCurrentTeam()) + "[" + flag.getFlagChar() + "]   ");
        }
        allFlagState = builder.toString().trim();

        return allFlagState;
    }
}
