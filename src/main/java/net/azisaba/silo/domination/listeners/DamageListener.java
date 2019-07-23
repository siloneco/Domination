package net.azisaba.silo.domination.listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.silo.domination.Domination;
import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.game.GameHelper;
import net.azisaba.silo.domination.game.GameHelper.GameState;

public class DamageListener implements Listener {

    private final Domination plugin;

    private final HashMap<Player, Player> damagedBy = new HashMap<>();
    private final HashMap<Player, Long> damagedByTime = new HashMap<>();

    public DamageListener(Domination plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent e) {

        e.setDeathMessage(null);
        e.setKeepInventory(true);

        Player p = e.getEntity();

        Game game = GameHelper.getGame(p);

        if ( game == null ) {
            return;
        }

        if ( damagedBy.containsKey(p) && damagedByTime.get(p) + 10000 > System.currentTimeMillis() ) {
            killed(damagedBy.get(p), p);
        } else {
            game.deathPlayer(p, false);
        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamageNotStarted(EntityDamageEvent e) {
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }

        Player p = (Player) e.getEntity();

        Game game = GameHelper.getGame(p);

        if ( game == null ) {
            return;
        }

        if ( game.getState() == GameState.WAITING || game.getState() == GameState.COUNTDOWN ) {
            e.setCancelled(true);
        }

        if ( game.getState() == GameState.GAMING && e.getCause() == DamageCause.VOID ) {

            e.setCancelled(true);

            if ( damagedBy.containsKey(p) && damagedByTime.get(p) + 10000 > System.currentTimeMillis() ) {
                killed(damagedBy.get(p), p);
            } else {
                game.deathPlayer(p, false);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }
        if ( !(e.getDamager() instanceof Player) ) {
            return;
        }

        Player p = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();

        Game game = GameHelper.getGame(p);

        if ( game != null && game.getTeamManager() != null ) {
            if ( game.getTeamManager().getTeam(p) == game.getTeamManager().getTeam(damager) ) {
                e.setCancelled(true);
                return;
            }
        }

        attacked(damager, p);
    }

    @EventHandler
    public void onDamageByArrow(EntityDamageByEntityEvent e) {
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }
        if ( !(e.getDamager() instanceof Arrow) ) {
            return;
        }

        Player p = (Player) e.getEntity();
        Arrow arrow = (Arrow) e.getDamager();

        if ( !(arrow.getShooter() instanceof Player) ) {
            return;
        }

        Player shooter = (Player) arrow.getShooter();

        if ( p == shooter ) {
            return;
        }

        attacked(shooter, p);

        displayHealth(shooter, p);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void disableSameTeamArrow(EntityDamageByEntityEvent e) {
        if ( !(e.getDamager() instanceof Arrow) ) {
            return;
        }
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }

        Arrow arrow = (Arrow) e.getDamager();
        if ( !(arrow.getShooter() instanceof Player) ) {
            return;
        }

        Player damager = (Player) arrow.getShooter();
        Player p = (Player) e.getEntity();

        Game game = GameHelper.getGame(p);

        if ( game == null || game.getTeamManager() == null ) {
            e.setCancelled(true);
        } else if ( game.getTeamManager().getTeam(p) == game.getTeamManager().getTeam(damager) ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void left(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        resetAttacks(p);
    }

    @EventHandler
    public void changedWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        resetAttacks(p);
    }

    @EventHandler
    public void onFishingRodHook(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
        Entity damager = e.getDamager();

        if ( !(damager instanceof FishHook) ) {
            return;
        }
        if ( !(ent instanceof Player) ) {
            return;
        }

        if ( !(((FishHook) damager).getShooter() instanceof Player) ) {
            return;
        }

        Player shooter = (Player) ((FishHook) damager).getShooter();

        displayHealth(shooter, (Player) ent);

        damagedBy.put((Player) ent, shooter);
        damagedByTime.put((Player) ent, System.currentTimeMillis());

        attacked(shooter, (Player) ent);
    }

    private void killed(Player killer, Player death) {

        if ( killer == death ) {
            return;
        }

        Game game = GameHelper.getGame(death);
        if ( game != GameHelper.getGame(killer) ) {
            return;
        }

        boolean success = game.killPlayer(killer, death);
        if ( !success ) {
            return;
        }

        game.deathPlayer(death, true);

        if ( damagedBy.containsKey(death) ) {
            damagedBy.remove(death);
        }
        if ( damagedByTime.containsKey(death) ) {
            damagedByTime.remove(death);
        }
    }

    private void attacked(Player attacker, Player damaged) {
        damagedBy.put(damaged, attacker);
        damagedByTime.put(damaged, System.currentTimeMillis());
    }

    private void resetAttacks(Player p) {
        if ( damagedBy.containsKey(p) ) {
            damagedBy.remove(p);
        }
        if ( damagedByTime.containsKey(p) ) {
            damagedByTime.remove(p);
        }
    }

    private void displayHealth(Player p, Player health) {

        Game game = GameHelper.getGame(p);
        ChatColor color;
        if ( game != null ) {
            color = game.getTeamManager().getColor(game.getTeamManager().getTeam(health));
        } else {
            color = ChatColor.RESET;
        }

        String msg = color + health.getName() + ChatColor.YELLOW + "の体力" + ChatColor.GREEN + ": " + ChatColor.RED
                + "{VALUE}";

        new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage(msg.replace("{VALUE}", String.format("%.1f", health.getHealth())));
            }
        }.runTaskLater(plugin, 1);
    }
}
