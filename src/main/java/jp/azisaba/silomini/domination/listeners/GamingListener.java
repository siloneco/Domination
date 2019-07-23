package jp.azisaba.silomini.domination.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import jp.azisaba.silomini.domination.Domination;
import jp.azisaba.silomini.domination.DominationUtils;
import jp.azisaba.silomini.domination.game.Game;
import jp.azisaba.silomini.domination.game.GameHelper;
import jp.azisaba.silomini.domination.game.GameHelper.GameState;

public class GamingListener implements Listener {

    private final Domination plugin;

    public GamingListener(Domination plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent e) {
        e.setFoodLevel(25);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }

        if ( e.getCause() == DamageCause.FALL ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onVoidDamageEvent(EntityDamageEvent e) {
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }

        Player p = (Player) e.getEntity();

        Game game = GameHelper.getGame(p);

        if ( game == null ) {
            return;
        }

        if ( e.getCause() == DamageCause.VOID ) {

            if ( game.getState() == GameState.GAMING ) {
                return;
            }

            e.setCancelled(true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if ( game.getTeamManager() == null ) {
                        p.teleport(game.getData().getGeneralSpawn());
                        return;
                    }

                    game.deathPlayer(p, false);
                }
            }.runTaskLater(plugin, 0);
        }
    }

    @EventHandler
    public void arrow(ProjectileHitEvent e) {
        if ( !(e.getEntity() instanceof Arrow) ) {
            return;
        }

        Arrow arrow = (Arrow) e.getEntity();

        new BukkitRunnable() {
            @Override
            public void run() {
                if ( arrow.isOnGround() ) {
                    arrow.remove();
                }
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();

        if ( p.getGameMode() != GameMode.CREATIVE ) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if ( p.getGameMode() != GameMode.CREATIVE ) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onItemGetDamage(PlayerItemDamageEvent e) {
        ItemStack item = e.getItem();
        new BukkitRunnable() {
            @Override
            public void run() {
                item.setDurability((byte) 0);
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        if ( GameHelper.getGame(p) != null ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPhysicsEvent(BlockPhysicsEvent e) {
        World world = e.getBlock().getWorld();
        Game game = GameHelper.getGame(world);

        if ( game == null ) {
            return;
        }

        if ( !game.getMapGenerator().isFinished() ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if ( GameHelper.getGame(p) != null ) {
            return;
        }

        if ( !p.hasPermission("domination.forcejoin.exempt") ) {
            GameHelper.joinAvailableGame(p);
        }
    }

    @EventHandler
    public void changeArmorEvent(InventoryClickEvent e) {
        if ( !(e.getWhoClicked() instanceof Player) ) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        if ( GameHelper.getGame(p) == null ) {
            return;
        }

        if ( 36 <= e.getSlot() && e.getSlot() <= 39 ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent e) {
        Player p = e.getPlayer();

        if ( GameHelper.getGame(p) == null ) {
            return;
        }

        e.setCancelled(true);
        p.updateInventory();
    }

    private final HashMap<Player, Long> lastEat = new HashMap<>();

    @EventHandler
    public void onEatHeadEvent(PlayerInteractEvent e) {

        if ( !e.getAction().toString().startsWith("RIGHT_CLICK") ) {
            return;
        }

        Player p = e.getPlayer();

        ItemStack item = p.getInventory().getItemInHand().clone();

        if ( !DominationUtils.isGoldenHead(item) ) {
            return;
        }

        e.setCancelled(true);

        if ( lastEat.containsKey(p) && lastEat.get(p) + 1000 > System.currentTimeMillis() ) {
            return;
        }

        p.playSound(p.getLocation(), Sound.EAT, 1, 1);

        if ( p.hasPotionEffect(PotionEffectType.REGENERATION) ) {
            p.removePotionEffect(PotionEffectType.REGENERATION);
        }
        if ( p.hasPotionEffect(PotionEffectType.SPEED) ) {
            p.removePotionEffect(PotionEffectType.SPEED);
        }

        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 3, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 3, 0));

        ItemStack hand = p.getInventory().getItemInHand();

        if ( hand.getAmount() > 1 ) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            p.getInventory().setItemInHand(new ItemStack(Material.AIR));
        }

        lastEat.put(p, System.currentTimeMillis());
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        Block b = e.getBlock();

        if ( b.getType() == Material.TORCH ) {
            e.setCancelled(true);
            Bukkit.getLogger().info(e.getBlock().getData() + "");
        }
    }
}