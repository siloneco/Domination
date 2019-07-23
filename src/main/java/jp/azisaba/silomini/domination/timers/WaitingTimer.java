package jp.azisaba.silomini.domination.timers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import jp.azisaba.silomini.domination.DominationUtils;
import jp.azisaba.silomini.domination.flag.Flag;
import jp.azisaba.silomini.domination.game.Game;
import jp.azisaba.silomini.domination.game.GameHelper;
import jp.azisaba.silomini.domination.game.GameHelper.GameState;

public class WaitingTimer {

    private final Game game;

    private BukkitTask task;

    public WaitingTimer(Game game) {
        this.game = game;
    }

    public void runTask() {
        task = new BukkitRunnable() {

            private boolean generateFinished = false;

            @Override
            public void run() {

                if ( !generateFinished && game.getMapGenerator().isFinished() ) {

                    generateFinished = true;

                    game.getLogger().writeLine("Map generate finished.");

                    for ( Flag flag : game.getData().getFlags() ) {
                        flag.updateArmorStandLocation();
                    }
                }

                if ( game.getWorld().getPlayers().size() <= 0 ) {
                    return;
                }

                String mark = DominationUtils.getMark();

                if ( game.getState() == GameState.WAITING ) {

                    if ( game.getPlayers().size() >= GameHelper.getPlugin().config.minPlayers
                            && game.getMapGenerator().isFinished() ) {
                        game.runStartCountdown();

                        game.getWorld().getPlayers().forEach(p -> DominationUtils.sendActionBar(p, ""));
                        return;
                    }

                    String wait = getWaitingPlayerActionBar(mark);
                    String generating = getMapGenerateActionBar(mark);

                    for ( Player p : game.getWorld().getPlayers() ) {

                        if ( game.getPlayers().size() < GameHelper.getPlugin().config.minPlayers ) {
                            DominationUtils.sendActionBar(p, wait);
                        } else {
                            DominationUtils.sendActionBar(p, generating);
                        }
                    }

                    return;
                }
            }
        }.runTaskTimer(GameHelper.getPlugin(), 0, 2);
    }

    public void stopTask() {
        if ( task != null ) {
            task.cancel();
            task = null;
        }
    }

    private String getWaitingPlayerActionBar(String mark) {
        return ChatColor.GREEN + mark + " " + ChatColor.YELLOW + "プレイヤー待機中...";
    }

    private String getMapGenerateActionBar(String mark) {
        return ChatColor.GREEN + mark + " " + ChatColor.LIGHT_PURPLE + "マップ生成中... "
                + ChatColor.GRAY + "(" + game.getMapGenerator().finishedPercentage()
                + "%) " + DominationUtils.generateBar(
                        game.getMapGenerator().finishedPercentage(), ChatColor.AQUA);
    }
}
