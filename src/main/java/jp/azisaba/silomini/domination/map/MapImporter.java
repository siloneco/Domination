package jp.azisaba.silomini.domination.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.collect.ComparisonChain;

import jp.azisaba.silomini.domination.flag.Flag;

public class MapImporter {

    private final Player author;

    private final String mapName;

    private Location minimum;
    private Location maximum;

    private Location generalSpawn;
    private Location blueSpawn;
    private Location redSpawn;

    private final List<Flag> flagList = new ArrayList<>();
    private double worldBorderSize;

    public MapImporter(Player author, String mapName) {
        this.author = author;
        this.mapName = mapName;
    }

    public Player getAuthor() {
        return author;
    }

    public Location getGeneralSpawn() {
        return generalSpawn;
    }

    public void setGeneralSpawn(Location generalSpawn) {
        this.generalSpawn = generalSpawn;
    }

    public Location getBlueSpawn() {
        return blueSpawn;
    }

    public void setBlueSpawn(Location blueSpawn) {
        this.blueSpawn = blueSpawn;
    }

    public Location getRedSpawn() {
        return redSpawn;
    }

    public void setRedSpawn(Location redSpawn) {
        this.redSpawn = redSpawn;
    }

    public List<Flag> getFlagList() {
        return flagList;
    }

    public void addFlag(Flag flag) {
        flagList.add(flag);

        flag.runDisplayTimer();
    }

    public void addFlag(Location loc) {
        Flag flag = new Flag(loc);
        flagList.add(flag);
        flag.runDisplayTimer();
    }

    public void stopAllFlagTimers() {
        for ( Flag flag : flagList ) {
            flag.stopDisplayTimer();
        }
    }

    public double getWorldBorderSize() {
        return worldBorderSize;
    }

    public void setWorldBorderSize(double worldBorderSize) {
        this.worldBorderSize = worldBorderSize;
    }

    public Location getMinimum() {
        return minimum;
    }

    public void setMinimum(Location minimum) {
        this.minimum = minimum;
    }

    public Location getMaximum() {
        return maximum;
    }

    public void setMaximum(Location maximum) {
        this.maximum = maximum;
    }

    public int countAreaBlocks() {
        if ( minimum == null || maximum == null ) {
            return -1;
        }

        if ( minimum.getWorld() != maximum.getWorld() ) {
            return -1;
        }

        Location min = new Location(minimum.getWorld(), Double.min(minimum.getX(), maximum.getX()),
                Double.min(minimum.getY(), maximum.getY()), Double.min(minimum.getZ(), maximum.getZ()));
        Location max = new Location(minimum.getWorld(), Double.max(minimum.getX(), maximum.getX()),
                Double.max(minimum.getY(), maximum.getY()), Double.max(minimum.getZ(), maximum.getZ()));

        return (int) ((max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1));
    }

    public String getFailImportMessage() {
        if ( minimum == null ) {
            return "最小ポイントが指定されていません。";
        }
        if ( maximum == null ) {
            return "最大ポイントが指定されていません";
        }

        if ( generalSpawn == null ) {
            return "初期リスポーン地点が指定されていません";
        }
        if ( blueSpawn == null ) {
            return "青のスポーン地点が指定されていません";
        }
        if ( redSpawn == null ) {
            return "赤のスポーン地点が指定されていません";
        }

        if ( flagList.size() <= 0 ) {
            return "旗が1つも設定されていません";
        }
        return "";
    }

    @SuppressWarnings("deprecation")
    public MapData importData() {
        Location min = new Location(minimum.getWorld(), Double.min(minimum.getX(), maximum.getX()),
                Double.min(minimum.getY(), maximum.getY()), Double.min(minimum.getZ(), maximum.getZ()));
        Location max = new Location(minimum.getWorld(), Double.max(minimum.getX(), maximum.getX()),
                Double.max(minimum.getY(), maximum.getY()), Double.max(minimum.getZ(), maximum.getZ()));

        minimum = min.clone();
        maximum = max.clone();

        HashMap<String, Material> materialMap = new HashMap<>();
        HashMap<String, Byte> dataMap = new HashMap<>();

        int minusY = minimum.clone().getBlockY();

        generalSpawn.subtract(0, minusY, 0);
        redSpawn.subtract(0, minusY, 0);
        blueSpawn.subtract(0, minusY, 0);

        for ( Flag flag : flagList ) {
            Location loc = flag.getLocation();
            loc.subtract(0, minusY, 0);
            flag.setLocation(loc);
        }

        for ( int x = minimum.getBlockX(); x <= maximum.getBlockX(); x++ ) {
            for ( int y = minimum.getBlockY(); y <= maximum.getBlockY(); y++ ) {
                for ( int z = minimum.getBlockZ(); z <= maximum.getBlockZ(); z++ ) {

                    Location loc = new Location(minimum.getWorld(), x, y, z);

                    if ( loc.getBlock().getType() == Material.AIR ) {
                        continue;
                    }

                    String locStr = loc.getBlockX() + "," + (loc.getBlockY() - minusY) + "," + loc.getBlockZ();

                    materialMap.put(locStr, loc.getBlock().getType());
                    if ( loc.getBlock().getState().getData() != null ) {
                        dataMap.put(locStr, loc.getBlock().getData());
                    }
                }
            }
        }

        Collections.sort(flagList, (flag1, flag2) -> ComparisonChain.start()
                .compare(flag1.getLocation().getX(), flag2.getLocation().getX())
                .compare(flag1.getLocation().getZ(), flag2.getLocation().getZ())
                .compare(flag1.getLocation().getY(), flag2.getLocation().getY())
                .result());

        Location worldBorderCenter = generalSpawn.getWorld().getWorldBorder().getCenter();
        if ( worldBorderCenter == null ) {
            worldBorderCenter = generalSpawn.clone();
        }

        MapData data = new MapData(mapName, generalSpawn, redSpawn, blueSpawn, flagList, materialMap, dataMap,
                generalSpawn.getWorld().getWorldBorder().getSize(), worldBorderCenter, minusY);
        return data;
    }
}