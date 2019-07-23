package net.azisaba.silo.domination.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import com.google.common.collect.ComparisonChain;

import net.azisaba.silo.domination.flag.Flag;

public class MapData implements Cloneable {

    private final String mapName;

    private final double worldBorderSize;
    private Location worldBorderCenter;

    private Location generalSpawn;
    private Location redSpawn;
    private Location blueSpawn;
    private List<Flag> flags = new ArrayList<>();

    private HashMap<String, Material> materialMap = new HashMap<>();
    private HashMap<String, Byte> dataMap = new HashMap<>();

    private List<String> sortedLocations = null;

    private final int loweredY;

    public MapData(String mapName, Location generalSpawn, Location redSpawn, Location blueSpawn, List<Flag> flags,
            HashMap<String, Material> materialMap, HashMap<String, Byte> dataMap, double worldBorderSize,
            Location worldBorderCenter, int loweredY) {
        this.mapName = mapName;
        this.generalSpawn = generalSpawn;
        this.redSpawn = redSpawn;
        this.blueSpawn = blueSpawn;
        this.flags = flags;

        this.materialMap = materialMap;
        this.dataMap = dataMap;

        this.worldBorderSize = worldBorderSize;
        this.worldBorderCenter = worldBorderCenter;

        this.loweredY = loweredY;

        sortedLocations = new ArrayList<>(materialMap.keySet());
        sort(sortedLocations);
    }

    public String getMapName() {
        return mapName;
    }

    public Location getGeneralSpawn() {
        return generalSpawn;
    }

    public Location getRedSpawn() {
        return redSpawn;
    }

    public Location getBlueSpawn() {
        return blueSpawn;
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public HashMap<String, Material> getMaterialMap() {
        return materialMap;
    }

    public HashMap<String, Byte> getDataMap() {
        return dataMap;
    }

    public double getWorldBorderSize() {
        return worldBorderSize;
    }

    public Location getWorldBorderCenter() {
        return worldBorderCenter;
    }

    public List<String> getSortedLocationList() {
        return sortedLocations;
    }

    public int getLoweredY() {
        return loweredY;
    }

    public void setWorld(World world) {
        generalSpawn.setWorld(world);
        blueSpawn.setWorld(world);
        redSpawn.setWorld(world);

        for ( Flag flag : flags ) {
            flag.setWorld(world);
        }
    }

    @Override
    public MapData clone() {
        MapData data = null;
        try {
            data = (MapData) super.clone();
            data.generalSpawn = data.generalSpawn.clone();
            data.blueSpawn = data.blueSpawn.clone();
            data.redSpawn = data.redSpawn.clone();
            data.sortedLocations = new ArrayList<>(data.sortedLocations);

            data.worldBorderCenter = data.worldBorderCenter.clone();

            List<Flag> flagList = new ArrayList<>();

            for ( Flag flag : data.flags ) {
                flagList.add(flag.clone());
            }

            data.flags = flagList;
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return data;
    }

    private void sort(List<String> locList) {
        Collections.sort(locList, (str1, str2) -> ComparisonChain.start()
                .compare(Double.parseDouble(str1.split(",")[1]), Double.parseDouble(str2.split(",")[1]))
                .compare(Double.parseDouble(str1.split(",")[0]), Double.parseDouble(str2.split(",")[0]))
                .compare(Double.parseDouble(str1.split(",")[2]), Double.parseDouble(str2.split(",")[2]))
                .result());
    }
}
