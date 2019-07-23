package net.azisaba.silo.domination.timers;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.game.GameHelper;
import net.azisaba.silo.domination.teams.DominationTeam;

public class TeamPointUpdater {

    private BukkitTask task;

    private final Game game;

    public TeamPointUpdater(Game game) {
        this.game = game;
    }

    public void runTask() {
        task = new BukkitRunnable() {
            @Override
            public void run() {

                game.getData().getFlags().stream().filter(flag -> flag.getCurrentTeam() != DominationTeam.NONE)
                        .forEach(flag -> game.addTeamPoint(1, flag.getCurrentTeam()));

            }
        }.runTaskTimer(GameHelper.getPlugin(), 0, 20);
    }

    public void stopTask() {
        if ( task != null ) {
            task.cancel();
        }
    }
}
