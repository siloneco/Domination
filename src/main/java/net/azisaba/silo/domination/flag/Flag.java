package net.azisaba.silo.domination.flag;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.silo.domination.game.GameHelper;
import net.azisaba.silo.domination.teams.DominationTeam;

public class Flag implements Cloneable {

    private Location loc;

    private DominationTeam currentTeam = DominationTeam.NONE;
    private int currentPoint = 0;

    private ArmorStand armorStand = null;
    private ArmorStand charArmorStand = null;

    private char flagChar;

    private BukkitTask timerTask;

    private boolean disable = false;

    public Flag(Location loc) {
        this.loc = loc;
    }

    public Location getLocation() {
        return loc;
    }

    public void setWorld(World world) {
        loc.setWorld(world);
    }

    public int getCurrentPoint() {
        return currentPoint;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public void addPointToRed(int num) {
        addPointTo(num, DominationTeam.RED);
    }

    public void addPointToBlue(int num) {
        addPointTo(num, DominationTeam.BLUE);
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public void setFlagChar(char flagChar) {

        this.flagChar = flagChar;

        if ( charArmorStand != null ) {
            charArmorStand.setCustomName(ChatColor.GREEN + "[" + flagChar + "]");
            return;
        }

        Location clonedLocation = loc.clone();
        clonedLocation.setY(clonedLocation.getY() + 2 + 0.3);

        charArmorStand = loc.getWorld().spawn(clonedLocation, ArmorStand.class);
        charArmorStand.setCustomNameVisible(true);

        charArmorStand.setGravity(false);
        charArmorStand.setVisible(false);
        charArmorStand.setSmall(true);
        charArmorStand.setMarker(true);
        charArmorStand.setRemoveWhenFarAway(false);
        charArmorStand.setCustomName(ChatColor.GREEN + "" + ChatColor.BOLD + "[" + flagChar + "]");
    }

    public char getFlagChar() {
        return flagChar;
    }

    public boolean addPointTo(int num, DominationTeam team) {

        if ( disable ) {
            return false;
        }

        if ( team == DominationTeam.RED ) {

            if ( currentPoint >= 100 ) {
                return false;
            }

            currentPoint += num;

            if ( currentPoint >= 100 ) {
                currentTeam = DominationTeam.RED;
            }
        } else if ( team == DominationTeam.BLUE ) {
            if ( currentPoint <= -100 ) {
                return false;
            }
            currentPoint -= num;

            if ( currentPoint <= -100 ) {
                currentTeam = DominationTeam.BLUE;
            }
        }

        if ( currentPoint == 0 ) {
            currentTeam = DominationTeam.NONE;
        }

        return true;
    }

    public DominationTeam getCurrentTeam() {
        return currentTeam;
    }

    public void spawnArmorStand() {

        new BukkitRunnable() {
            @Override
            public void run() {

                if ( armorStand != null ) {
                    return;
                }

                Location clonedLocation = loc.clone();
                clonedLocation.setY(clonedLocation.getY() + 2);
                armorStand = loc.getWorld().spawn(clonedLocation, ArmorStand.class);
                armorStand.setCustomNameVisible(true);

                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStand.setSmall(true);
                armorStand.setMarker(true);
                charArmorStand.setRemoveWhenFarAway(false);
                armorStand.setCustomName(StringUtils.repeat("┃", 100));
            }
        }.runTaskLater(GameHelper.getPlugin(), 5);
    }

    public void updateArmorStandLocation() {

        if ( !GameHelper.getPlugin().config.adjustFlagBarLocation ) {
            return;
        }

        Location loc = armorStand.getLocation().clone();
        while ( loc.getBlock() != null && loc.getBlock().getType() != Material.AIR ) {
            loc.add(0, 1, 0);
        }

        armorStand.teleport(loc);

        loc.add(0, 0.3, 0);
        charArmorStand.teleport(loc);
    }

    public void updateArmorStandName() {
        armorStand.setCustomName(getBar());
    }

    public String getBar() {

        String str = "";

        int num = currentPoint;

        if ( currentPoint < 0 ) {
            num = currentPoint * -1;
            str = ChatColor.BLUE + StringUtils.repeat("┃", num) + ChatColor.RESET
                    + StringUtils.repeat("┃", 100 - num);

        } else if ( currentPoint > 0 ) {
            str = ChatColor.RED + StringUtils.repeat("┃", num) + ChatColor.RESET
                    + StringUtils.repeat("┃", 100 - num);
        } else {
            str = ChatColor.WHITE + StringUtils.repeat("┃", 100);
        }
        return str;
    }

    public void runDisplayTimer() {

        stopDisplayTimer();

        timerTask = new BukkitRunnable() {
            Location particleLoc = loc.clone();
            double t = 0;
            double r = GameHelper.getPlugin().config.flagRadius * 2;

            @Override
            public void run() {

                for ( int num = 0; num < 8; num++ ) {
                    t = t + Math.PI / 64;
                    double x = r * Math.cos(t);
                    double z = r * Math.sin(t);

                    particleLoc.add(x, 0, z);
                    displayCollectColor(particleLoc);

                    particleLoc.add(0, 0.1, 0);
                    displayCollectColor(particleLoc);

                    particleLoc.subtract(x, 0.1, z);
                }
            }

            private void displayCollectColor(Location location) {
                if ( currentTeam == DominationTeam.RED ) {
                    displayRedDust(location);
                } else if ( currentTeam == DominationTeam.BLUE ) {
                    displayBlueDust(location);
                } else {
                    displayWhiteDust(location);
                }
            }

            private void displayRedDust(Location location) {
                location.getWorld().spigot().playEffect(location, Effect.COLOURED_DUST, 0, 1, Float.MAX_VALUE,
                        Float.MIN_VALUE, Float.MIN_VALUE, 1, 0, 64);
            }

            private void displayBlueDust(Location location) {
                location.getWorld().spigot().playEffect(location, Effect.COLOURED_DUST, 0, 1, Float.MIN_VALUE,
                        Float.MIN_VALUE, Float.MAX_VALUE, 1, 0, 64);
            }

            private void displayWhiteDust(Location location) {
                location.getWorld().spigot().playEffect(location, Effect.COLOURED_DUST, 0, 1, Float.MAX_VALUE,
                        Float.MAX_VALUE, Float.MAX_VALUE, 1, 0, 64);
            }
        }.runTaskTimer(GameHelper.getPlugin(), 0, 1);
    }

    public void stopDisplayTimer() {
        if ( timerTask != null ) {
            timerTask.cancel();
        }
    }

    @Override
    public Flag clone() {
        Flag data = null;
        try {
            data = (Flag) super.clone();

            data.loc = data.loc.clone();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return data;
    }
}
