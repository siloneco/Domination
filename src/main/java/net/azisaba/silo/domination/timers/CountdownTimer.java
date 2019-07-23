package net.azisaba.silo.domination.timers;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.silo.domination.Domination;
import net.azisaba.silo.domination.game.Game;

public class CountdownTimer {

    private final Domination plugin;
    private final CountdownType type;

    private final Game game;

    private BukkitTask task;

    public CountdownTimer(Domination plugin, Game game, CountdownType type) {
        this.plugin = plugin;
        this.type = type;
        this.game = game;
    }

    public void runTimer() {

        if ( type == CountdownType.START ) {
            task = startCountdownTask().runTaskTimer(plugin, 20, 20);
        } else {
            task = gamingTimer().runTaskTimer(plugin, 0, 5);
        }
    }

    public void stopTimer() {
        if ( task != null ) {
            task.cancel();
        }
    }

    public CountdownType getCountdownType() {
        return type;
    }

    private BukkitRunnable startCountdownTask() {
        game.setCountdownSeconds(plugin.config.startCountdownSeconds);

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {

                if ( game.getCountdownSeconds() <= 0 ) {
                    cancel();
                    game.startGame();
                    return;
                }

                if ( game.getCountdownSeconds() <= 5 || game.getCountdownSeconds() % 5 == 0 ) {
                    for ( Player p : game.getWorld().getPlayers() ) {
                        p.sendMessage(ChatColor.YELLOW + "開始まであと" + ChatColor.RED + game.getCountdownSeconds()
                                + ChatColor.YELLOW + "秒");
                        p.playSound(p.getLocation(), Sound.NOTE_STICKS, 1, 1);
                    }
                }

                game.setCountdownSeconds(game.getCountdownSeconds() - 1);
                game.updateScoreboard();
            }
        };

        return runnable;
    }

    private BukkitRunnable gamingTimer() {

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {

                game.updateScoreboard();

                if ( game.getStartedMiliSeconds() == -1 ) {
                    return;
                }

                if ( (System.currentTimeMillis() - game.getStartedMiliSeconds()) / 1000 > plugin.config.gameLongSecond ) {
                    cancel();
                    game.endGame();
                    return;
                }
            }
        };

        return runnable;
    }
}