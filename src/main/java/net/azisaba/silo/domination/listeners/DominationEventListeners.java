package net.azisaba.silo.domination.listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.azisaba.silo.domination.DominationUtils;
import net.azisaba.silo.domination.events.DominationDeathEvent;
import net.azisaba.silo.domination.events.PlayerKillEvent;
import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.game.GameHelper;

public class DominationEventListeners implements Listener {

    @EventHandler
    public void onDeath(DominationDeathEvent e) {
        Player p = e.getPlayer();

        p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void giveKillReward(PlayerKillEvent e) {
        Player killer = e.getKiller();

        String apple = "Head";

        if ( GameHelper.getPlugin().config.useGoldenHead ) {
            killer.getInventory().addItem(DominationUtils.getGoldenHead(1));
        } else {
            killer.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            apple = "Apple";
        }

        killer.getInventory().addItem(new ItemStack(Material.ARROW, 10));

        killer.playSound(killer.getLocation(), Sound.ORB_PICKUP, 1, 1);
        killer.sendMessage(
                ChatColor.GOLD + "+1 Golden " + apple + ChatColor.YELLOW + ", " + ChatColor.GRAY + " +10 Arrow");

        killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,
                (int) (GameHelper.getPlugin().config.killedStrengthSeconds * 20), 0, true, false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void addTeamPoint(PlayerKillEvent e) {
        Player killer = e.getKiller();
        Game game = e.getGame();

        game.addTeamPoint(1, game.getTeamManager().getTeam(killer));
    }

    private final HashMap<Player, Long> invincible = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(DominationDeathEvent e) {
        Player p = e.getPlayer();
        invincible.put(p, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent e) {
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }
        if ( !(e.getDamager() instanceof Player) ) {
            return;
        }

        Player attacker = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();

        Game game = GameHelper.getGame(attacker);

        if ( game == null ) {
            return;
        }

        if ( !invincible.containsKey(damaged) ) {
            return;
        }

        long finishInvincible = (long) (invincible.get(damaged)
                + GameHelper.getPlugin().config.invincibleSeconds * 1000);
        if ( finishInvincible > System.currentTimeMillis() ) {

            attacker.sendMessage(
                    DominationUtils.getColor(game.getTeamManager().getTeam(damaged)) + damaged.getName()
                            + ChatColor.YELLOW + " は保護されています! " + ChatColor.GRAY + "残り時間: "
                            + String.format("%.1f", getSeconds(finishInvincible)) + "秒");
            e.setCancelled(true);
        }
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
        Player damaged = (Player) ent;

        Game game = GameHelper.getGame(shooter);

        if ( game == null ) {
            return;
        }

        if ( !invincible.containsKey(damaged) ) {
            return;
        }

        long finishInvincible = (long) (invincible.get(damaged)
                + GameHelper.getPlugin().config.invincibleSeconds * 1000);
        if ( finishInvincible > System.currentTimeMillis() ) {

            shooter.sendMessage(
                    DominationUtils.getColor(game.getTeamManager().getTeam(damaged)) + damaged.getName()
                            + ChatColor.YELLOW + " は保護されています! " + ChatColor.GRAY + "残り時間: "
                            + String.format("%.1f", getSeconds(finishInvincible)) + "秒");
            e.setCancelled(true);
        }
    }

    private double getSeconds(long l) {
        if ( l < System.currentTimeMillis() ) {
            return 0d;
        }

        double d = l - System.currentTimeMillis();
        d = d / 1000;
        return d;
    }
}
