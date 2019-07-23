package net.azisaba.silo.domination.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import net.azisaba.silo.domination.Domination;
import net.azisaba.silo.domination.flag.Flag;

public class MapLoader {

    private final Domination plugin;

    public MapLoader(Domination plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    public List<MapData> loadAllMapData() {

        File file = new File(plugin.config.mapDataSavePath);

        if ( !file.exists() ) {
            file.mkdirs();
        }

        List<MapData> dataList = new ArrayList<>();

        int allMapCount = countMap();
        int currentMapNumber = 0;

        for ( File dataFolder : file.listFiles() ) {

            if ( !dataFolder.isDirectory() ) {
                continue;
            }

            File settingFile = new File(dataFolder, "MapData.yml");
            File blockData = new File(dataFolder, "BlockData.bin");
            File blockMetaData = new File(dataFolder, "BlockMetaData.bin");

            String mapName;
            Location generalSpawn, blueSpawn, redSpawn, worldBorderCenter;
            List<Flag> flagList = new ArrayList<>();
            double worldBorderSize;
            HashMap<String, Material> materialMap = new HashMap<>();
            HashMap<String, Byte> dataMap = new HashMap<>();
            int loweredY = 0;

            if ( !settingFile.exists() ) {
                continue;
            }
            if ( !blockData.exists() ) {
                continue;
            }
            if ( !blockMetaData.exists() ) {
                continue;
            }

            currentMapNumber++;

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(settingFile);

            if ( conf.get("MapName") == null || conf.get("GeneralSpawn") == null || conf.get("BlueSpawn") == null
                    || conf.get("RedSpawn") == null || conf.get("FlagList") == null
                    || conf.get("WorldBorderSize") == null ) {
                continue;
            }

            mapName = conf.getString("MapName");
            generalSpawn = parseLocation(conf.getString("GeneralSpawn"));
            blueSpawn = parseLocation(conf.getString("BlueSpawn"));
            redSpawn = parseLocation(conf.getString("RedSpawn"));

            if ( conf.get("WorldBorderCenter") == null ) {
                worldBorderCenter = generalSpawn.clone();

                conf.set("WorldBorderCenter", locationToString(generalSpawn));
                try {
                    conf.save(settingFile);
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            } else {
                worldBorderCenter = parseLocation(conf.getString("WorldBorderCenter"));
            }

            for ( String str : conf.getStringList("FlagList") ) {
                flagList.add(new Flag(parseLocation(str)));
            }

            worldBorderSize = conf.getDouble("WorldBorderSize");

            loweredY = conf.getInt("LoweredY");

            try {
                ObjectInputStream objInStream = new ObjectInputStream(new FileInputStream(blockData.getPath()));

                materialMap = (HashMap<String, Material>) objInStream.readObject();
                objInStream.close();
            } catch ( ClassNotFoundException | IOException e ) {
                e.printStackTrace();
            }
            try {
                ObjectInputStream objInStream = new ObjectInputStream(new FileInputStream(blockMetaData.getPath()));

                dataMap = (HashMap<String, Byte>) objInStream.readObject();
                objInStream.close();
            } catch ( ClassNotFoundException | IOException e ) {
                e.printStackTrace();
            }

            MapData data = new MapData(mapName, generalSpawn, redSpawn, blueSpawn, flagList, materialMap, dataMap,
                    worldBorderSize, worldBorderCenter, loweredY);

            dataList.add(data);

            plugin.getLogger().info("----------------------------");
            plugin.getLogger().info("MapName: " + data.getMapName());
            plugin.getLogger().info("General: " + toStringFromLocation(data.getGeneralSpawn()));
            plugin.getLogger().info("Blue: " + toStringFromLocation(data.getBlueSpawn()));
            plugin.getLogger().info("Red: " + toStringFromLocation(data.getRedSpawn()));
            plugin.getLogger().info("Flags:");
            for ( Flag flag : data.getFlags() ) {
                plugin.getLogger().info("  - " + toStringFromLocation(flag.getLocation()));
            }
            plugin.getLogger().info("----------------------------");
            plugin.getLogger().info("Loaded a map (" + currentMapNumber + "/" + allMapCount + ")");
        }

        if ( dataList.size() <= 0 ) {
            return null;
        }

        return dataList;
    }

    public int countMap() {
        File file = new File(plugin.config.mapDataSavePath);

        if ( !file.exists() ) {
            file.mkdirs();
        }

        int count = 0;

        for ( File dataFolder : file.listFiles() ) {
            if ( !dataFolder.isDirectory() ) {
                continue;
            }

            File settingFile = new File(dataFolder, "MapData.yml");
            File blockData = new File(dataFolder, "BlockData.bin");
            File blockMetaData = new File(dataFolder, "BlockMetaData.bin");

            if ( settingFile.isFile() && blockData.isFile() && blockMetaData.isFile() ) {
                count++;
            }
        }

        return count;
    }

    public boolean saveMapData(MapData data) {

        File folder = new File(plugin.config.mapDataSavePath + "/" + data.getMapName());
        File settingFile = new File(plugin.config.mapDataSavePath + "/" + data.getMapName() + "/MapData.yml");
        File blockData = new File(plugin.config.mapDataSavePath + "/" + data.getMapName() + "/BlockData.bin");
        File blockMetaData = new File(plugin.config.mapDataSavePath + "/" + data.getMapName() + "/BlockMetaData.bin");

        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(settingFile);
        conf.set("MapName", data.getMapName());
        conf.set("GeneralSpawn", locationToString(data.getGeneralSpawn()));
        conf.set("BlueSpawn", locationToString(data.getBlueSpawn()));
        conf.set("RedSpawn", locationToString(data.getRedSpawn()));

        List<String> flagLocList = new ArrayList<>();
        for ( Flag flag : data.getFlags() ) {
            flagLocList.add(locationToString(flag.getLocation()));
        }

        conf.set("FlagList", flagLocList);
        conf.set("WorldBorderSize", data.getWorldBorderSize());
        conf.set("WorldBorderCenter", locationToString(data.getWorldBorderCenter()));
        conf.set("LoweredY", data.getLoweredY());

        try {
            conf.save(settingFile);
        } catch ( IOException e1 ) {
            e1.printStackTrace();
            return false;
        }

        try {
            ObjectOutputStream blockDataStream = new ObjectOutputStream(new FileOutputStream(blockData));

            blockDataStream.writeObject(data.getMaterialMap());
            blockDataStream.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }

        try {
            ObjectOutputStream metaDataStream = new ObjectOutputStream(new FileOutputStream(blockMetaData));

            metaDataStream.writeObject(data.getDataMap());
            metaDataStream.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private String locationToString(Location loc) {
        return loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location parseLocation(String str) {
        Location loc = null;
        try {
            String[] strs = str.split(",");
            loc = new Location(null, Double.parseDouble(strs[0]), Double.parseDouble(strs[1]),
                    Double.parseDouble(strs[2]));
            loc.setYaw(Float.parseFloat(strs[3]));
            loc.setPitch(Float.parseFloat(strs[4]));
        } catch ( Exception e ) {
            // nothing
        }

        return loc;
    }

    private String toStringFromLocation(Location loc) {
        return loc.getX() + " ," + loc.getY() + " ," + loc.getZ() + " ," + loc.getPitch() + " ," + loc.getYaw();
    }
}
